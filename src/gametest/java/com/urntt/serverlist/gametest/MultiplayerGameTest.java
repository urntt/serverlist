package com.urntt.serverlist.gametest;

import static com.urntt.serverlist.gametest.TestSupport.LOGGER;
import static com.urntt.serverlist.gametest.TestSupport.assertEquals;
import static com.urntt.serverlist.gametest.TestSupport.assertNoOverlap;
import static com.urntt.serverlist.gametest.TestSupport.assertTrue;
import static com.urntt.serverlist.gametest.TestSupport.click;

import com.mojang.blaze3d.platform.InputConstants;
import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.ServerListKeyMappings;
import com.urntt.serverlist.gametest.mixin.ServerNameResolverInvoker;
import com.urntt.serverlist.mixin.ServerSelectionListAccessor;
import com.urntt.serverlist.pause.PauseMenuButton;
import com.urntt.serverlist.ping.PingerThreads;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.AddressCheck;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddressResolver;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.client.multiplayer.resolver.ServerRedirectHandler;
import net.minecraft.server.MinecraftServer;

/**
 * Features that need a server: pings, the pause menu button, the open key, and switching servers from in game.
 * Everything runs against one local dedicated server.
 */
@SuppressWarnings("UnstableApiUsage")
public final class MultiplayerGameTest implements FabricClientGameTest {
	/** Slow servers placed before the real server: one more than vanilla's five pinger threads. */
	private static final int SLOW_SERVERS = 6;
	/** How long the real server must stay unpinged with vanilla's pinger threads. */
	private static final int VANILLA_WAIT_TICKS = 100;

	private static final AtomicInteger DISCONNECTS = new AtomicInteger();

	@Override
	public void runTest(final ClientGameTestContext context) {
		TestSupport.resetConfig();
		ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> DISCONNECTS.incrementAndGet());

		Properties properties = new Properties();
		properties.setProperty("server-port", Integer.toString(freePort()));
		try (TestDedicatedServerContext server = context.worldBuilder().createServer(properties)) {
			String address = "127.0.0.1:" + server.computeOnServer(MinecraftServer::getPort);
			testPings(context, address);
			testReverseDns(context, address);

			TestSupport.saveServerData(context, List.of(new ServerData("Local", address, ServerData.Type.OTHER)));
			try (TestDedicatedServerConnection connection = server.connect()) {
				connection.waitForChunksRender();
				testPauseMenuButton(context);
				testOpenKey(context);
				testSwitchServer(context);
			}
		}
	}

	/**
	 * With vanilla's five pinger threads, six slow servers keep the real server unpinged. With faster pings, the
	 * real server answers right away and shows its latency.
	 */
	private static void testPings(final ClientGameTestContext context, final String address) {
		int[] originalWindow = TestSupport.useRoomyWindow(context);

		SlowServers.hold();
		try {
			List<ServerData> servers = new ArrayList<>();
			for (int i = 1; i <= SLOW_SERVERS; i++) {
				servers.add(new ServerData("Slow " + i, "server" + i + SlowServers.HOST_SUFFIX, ServerData.Type.OTHER));
			}
			servers.add(new ServerData("Local", address, ServerData.Type.OTHER));
			TestSupport.saveServerData(context, servers);

			// Pings so far ran with faster pings. Apply vanilla's configuration now, as the next ping would, and let
			// the extra idle threads stop before measuring.
			ServerListClient.config().setFasterServerPings(false);
			context.runOnClient(client -> PingerThreads.configure(ServerSelectionListAccessor.serverlist$threadPool(), false));
			int poolSize = context.waitFor(client -> ServerSelectionListAccessor.serverlist$threadPool().getPoolSize() <= vanillaThreads(), VANILLA_WAIT_TICKS);
			LOGGER.info("Extra pinger threads stopped after {} ticks", poolSize);
			JoinMultiplayerScreen vanilla = TestSupport.openServerList(context);
			context.waitTicks(VANILLA_WAIT_TICKS);
			ServerData.State vanillaState = localState(context, vanilla);
			int vanillaThreads = context.computeOnClient(client -> ServerSelectionListAccessor.serverlist$threadPool().getCorePoolSize());
			LOGGER.info("Vanilla pinger threads: {}, real server after {} ticks: {}", vanillaThreads, VANILLA_WAIT_TICKS, vanillaState);
			assertTrue(vanillaState != ServerData.State.SUCCESSFUL, "the real server was pinged although vanilla's threads were all blocked");

			ServerListClient.config().setFasterServerPings(true);
			JoinMultiplayerScreen faster = TestSupport.openServerList(context);
			int ticks = context.waitFor(client -> localStateOnClient(faster) == ServerData.State.SUCCESSFUL, VANILLA_WAIT_TICKS);
			int fasterThreads = context.computeOnClient(client -> ServerSelectionListAccessor.serverlist$threadPool().getCorePoolSize());
			LOGGER.info("Faster pinger threads: {}, real server pinged after {} ticks", fasterThreads, ticks);
			assertEquals(PingerThreads.FASTER_THREADS, fasterThreads, "pinger threads with faster pings");
			context.waitTicks(2);
			context.takeScreenshot("server_list_pings");
		} finally {
			SlowServers.release();
			TestSupport.restoreWindow(context, originalWindow);
		}
	}

	/**
	 * A literal IP address carries its literal as host name by the time the block list checks it, so the check
	 * does not trigger a reverse DNS lookup. With the setting off, vanilla's address has no host name yet.
	 */
	private static void testReverseDns(final ClientGameTestContext context, final String address) {
		String patched = context.computeOnClient(client -> addressSeenByBlockList(address));
		ServerListClient.config().setSkipReverseDnsForIpAddresses(false);
		String vanilla = context.computeOnClient(client -> addressSeenByBlockList(address));
		ServerListClient.config().setSkipReverseDnsForIpAddresses(true);
		LOGGER.info("Address seen by the block list: {} with the setting on, {} with it off", patched, vanilla);
		assertTrue(patched.startsWith("127.0.0.1/"), "the address has no host name before the block list check: " + patched);
		assertTrue(vanilla.startsWith("/"), "vanilla's address unexpectedly has a host name: " + vanilla);
	}

	/** Resolves {@code address} with the mod's hook in place and returns the address as the block list sees it. */
	private static String addressSeenByBlockList(final String address) {
		AtomicReference<String> seen = new AtomicReference<>();
		AddressCheck recordingCheck = new AddressCheck() {
			@Override
			public boolean isAllowed(final ResolvedServerAddress resolved) {
				seen.set(resolved.asInetSocketAddress().getAddress().toString());
				return true;
			}

			@Override
			public boolean isAllowed(final ServerAddress serverAddress) {
				return true;
			}
		};
		ServerNameResolver resolver = ServerNameResolverInvoker.serverlistGametest$create(
				ServerAddressResolver.SYSTEM, ServerRedirectHandler.EMPTY, recordingCheck);
		Optional<ResolvedServerAddress> resolved = resolver.resolveAddress(ServerAddress.parseString(address));
		assertTrue(resolved.isPresent(), "could not resolve " + address);
		return seen.get();
	}

	private static void testPauseMenuButton(final ClientGameTestContext context) {
		context.setScreen(() -> new PauseScreen(true));
		context.waitTick();
		SpriteIconButton button = pauseMenuButton(context);
		assertTrue(button != null, "the pause menu has no server list button");
		context.runOnClient(client -> {
			List<AbstractWidget> widgets = client.gui.screen().children().stream()
					.filter(AbstractWidget.class::isInstance).map(AbstractWidget.class::cast).toList();
			assertNoOverlap(widgets, "pause menu");
			for (AbstractWidget widget : widgets) {
				if (widget instanceof SpriteIconButton icon && icon != button && icon.getWidth() == button.getWidth()) {
					assertEquals(icon.getY(), button.getY(), "row of the server list button");
				}
			}
		});
		TestSupport.moveCursor(context, 0, 0);
		context.takeScreenshot("pause_menu");

		click(context, new double[] {button.getX() + button.getWidth() / 2.0, button.getY() + button.getHeight() / 2.0},
				InputConstants.MOUSE_BUTTON_LEFT);
		context.waitForScreen(JoinMultiplayerScreen.class);
		context.getInput().pressKey(InputConstants.getKey("key.keyboard.escape"));
		context.waitForScreen(PauseScreen.class);

		ServerListClient.config().setPauseMenuButton(false);
		context.setScreen(() -> new PauseScreen(true));
		context.waitTick();
		assertTrue(pauseMenuButton(context) == null, "the server list button is shown although disabled");
		ServerListClient.config().setPauseMenuButton(true);
		context.setScreen(() -> null);
	}

	private static SpriteIconButton pauseMenuButton(final ClientGameTestContext context) {
		return context.computeOnClient(client -> client.gui.screen().children().stream()
				.filter(child -> child instanceof SpriteIconButton icon && icon.getMessage().equals(PauseMenuButton.MESSAGE))
				.map(SpriteIconButton.class::cast)
				.findFirst()
				.orElse(null));
	}

	private static void testOpenKey(final ClientGameTestContext context) {
		KeyMapping open = ServerListKeyMappings.open();
		open.setKey(InputConstants.getKey("key.keyboard.j"));
		KeyMapping.resetMapping();
		context.setScreen(() -> null);
		context.getInput().pressKey(open);
		context.waitForScreen(JoinMultiplayerScreen.class);
		context.getInput().pressKey(InputConstants.getKey("key.keyboard.escape"));
		context.waitFor(client -> client.gui.screen() == null);
		open.setKey(open.getDefaultKey());
		KeyMapping.resetMapping();
	}

	/**
	 * Joining a server from the list opened in the pause menu leaves the current server first and then connects.
	 */
	private static void testSwitchServer(final ClientGameTestContext context) {
		ClientLevel before = context.computeOnClient(client -> client.level);
		int disconnectsBefore = DISCONNECTS.get();
		context.setScreen(() -> new PauseScreen(true));
		context.waitTick();
		SpriteIconButton button = pauseMenuButton(context);
		click(context, new double[] {button.getX() + button.getWidth() / 2.0, button.getY() + button.getHeight() / 2.0},
				InputConstants.MOUSE_BUTTON_LEFT);
		context.waitForScreen(JoinMultiplayerScreen.class);
		JoinMultiplayerScreen screen = (JoinMultiplayerScreen) TestSupport.currentScreen(context);
		click(context, TestSupport.textPoint(context, screen, "Local"), InputConstants.MOUSE_BUTTON_LEFT);
		context.clickScreenButton("selectServer.select");

		int ticks = context.waitFor(client -> client.level != null && client.level != before && client.gui.screen() == null);
		context.waitTicks(2);
		LOGGER.info("Switched servers from the pause menu in {} ticks", ticks);
		assertTrue(DISCONNECTS.get() > disconnectsBefore, "joined without leaving the previous server");
	}

	private static int vanillaThreads() {
		return ServerSelectionListAccessor.serverlist$threadPool().getCorePoolSize();
	}

	private static ServerData.State localState(final ClientGameTestContext context, final JoinMultiplayerScreen screen) {
		return context.computeOnClient(client -> localStateOnClient(screen));
	}

	private static ServerData.State localStateOnClient(final JoinMultiplayerScreen screen) {
		for (ServerSelectionList.Entry entry : TestSupport.list(screen).children()) {
			if (entry instanceof ServerSelectionList.OnlineServerEntry server && server.getServerData().name.equals("Local")) {
				return server.getServerData().state();
			}
		}
		throw new AssertionError("The real server is not shown");
	}

	private static int freePort() {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		} catch (IOException e) {
			throw new AssertionError("Could not find a free port", e);
		}
	}
}
