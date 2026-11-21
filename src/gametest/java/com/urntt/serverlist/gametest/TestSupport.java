package com.urntt.serverlist.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.config.ConfigMode;
import com.urntt.serverlist.config.ServerListConfig;
import com.urntt.serverlist.mixin.JoinMultiplayerScreenAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helpers shared by the client game tests. All coordinates are GUI coordinates.
 */
final class TestSupport {
	static final Logger LOGGER = LoggerFactory.getLogger("serverlist-gametest");

	/** Servers saved by {@link #saveServers(ClientGameTestContext, List)} in the default tests. */
	static final List<String> NAMES = List.of("Alpha", "Bravo", "Charlie", "Delta", "Echo");

	/** GUI scale and window size of a 960x540 screen, which shows every server of the tests at once. */
	private static final int ROOMY_GUI_SCALE = 2;
	private static final int ROOMY_WINDOW_WIDTH = 1920;
	private static final int ROOMY_WINDOW_HEIGHT = 1080;

	/** Ticks to wait before a mouse press, so two presses never count as a double click. */
	private static final int CLICK_PAUSE_TICKS = 7;
	/** Distance the cursor moves per tick while dragging. */
	private static final double DRAG_STEP = 6.0;

	private TestSupport() {
	}

	static void resetConfig() {
		ServerListConfig config = ServerListClient.config();
		config.setMode(ConfigMode.CUSTOM);
		config.setDragToReorder(true);
		config.setPauseMenuButton(true);
		config.setSearchBox(true);
		config.setShowPingMs(true);
		config.setFasterServerPings(true);
		config.setSkipReverseDnsForIpAddresses(true);
	}

	/** Replaces the saved servers with {@code names}; each points to a closed local port. */
	static void saveServers(final ClientGameTestContext context, final List<String> names) {
		List<ServerData> servers = new ArrayList<>();
		for (int i = 0; i < names.size(); i++) {
			servers.add(new ServerData(names.get(i), "127.0.0.1:" + (i + 1), ServerData.Type.OTHER));
		}
		saveServerData(context, servers);
	}

	static void saveServerData(final ClientGameTestContext context, final List<ServerData> servers) {
		context.runOnClient(client -> {
			ServerList list = new ServerList(client);
			list.load();
			while (list.size() > 0) {
				list.remove(list.get(0));
			}
			for (ServerData server : servers) {
				list.add(server, false);
			}
			list.save();
		});
	}

	/** The saved servers' names, read back from disk. */
	static List<String> savedNames(final ClientGameTestContext context) {
		return context.computeOnClient(client -> {
			ServerList list = new ServerList(client);
			list.load();
			List<String> names = new ArrayList<>();
			for (int i = 0; i < list.size(); i++) {
				names.add(list.get(i).name);
			}
			return names;
		});
	}

	static JoinMultiplayerScreen openServerList(final ClientGameTestContext context) {
		context.setScreen(() -> new JoinMultiplayerScreen(new TitleScreen()));
		context.waitTicks(2);
		return context.computeOnClient(client -> (JoinMultiplayerScreen) client.gui.screen());
	}

	static ServerSelectionList list(final JoinMultiplayerScreen screen) {
		return ((JoinMultiplayerScreenAccessor) screen).serverlist$serverSelectionList();
	}

	/** Names of the saved servers currently shown, from top to bottom. */
	static List<String> visibleNames(final ClientGameTestContext context, final JoinMultiplayerScreen screen) {
		return context.computeOnClient(client -> list(screen).children().stream()
				.filter(ServerSelectionList.OnlineServerEntry.class::isInstance)
				.map(entry -> ((ServerSelectionList.OnlineServerEntry) entry).getServerData().name)
				.toList());
	}

	/** A point on the named server's text, away from its icon. */
	static double[] textPoint(final ClientGameTestContext context, final JoinMultiplayerScreen screen, final String name) {
		return context.computeOnClient(client -> {
			for (ServerSelectionList.Entry entry : list(screen).children()) {
				if (entry instanceof ServerSelectionList.OnlineServerEntry server && server.getServerData().name.equals(name)) {
					return new double[] {server.getContentX() + 100, server.getY() + server.getHeight() / 2.0};
				}
			}
			throw new AssertionError("Server " + name + " is not shown");
		});
	}

	/** A point on the top left (move up) or bottom left (move down) quarter of the named server's icon. */
	static double[] iconPoint(final ClientGameTestContext context, final JoinMultiplayerScreen screen, final String name, final boolean bottom) {
		return context.computeOnClient(client -> {
			for (ServerSelectionList.Entry entry : list(screen).children()) {
				if (entry instanceof ServerSelectionList.OnlineServerEntry server && server.getServerData().name.equals(name)) {
					return new double[] {server.getContentX() + 8, server.getContentY() + (bottom ? 24 : 8)};
				}
			}
			throw new AssertionError("Server " + name + " is not shown");
		});
	}

	static void moveCursor(final ClientGameTestContext context, final double x, final double y) {
		double[] raw = context.computeOnClient(client -> {
			Window window = client.getWindow();
			return new double[] {
				x * window.getScreenWidth() / window.getGuiScaledWidth(),
				y * window.getScreenHeight() / window.getGuiScaledHeight()
			};
		});
		context.getInput().setCursorPos(raw[0], raw[1]);
	}

	static void click(final ClientGameTestContext context, final double[] point, final int button) {
		context.waitTicks(CLICK_PAUSE_TICKS);
		moveCursor(context, point[0], point[1]);
		context.getInput().pressMouse(button);
		context.waitTick();
	}

	/** Drags with the left mouse button from {@code from} straight up or down to the height of {@code to}. */
	static void drag(final ClientGameTestContext context, final double[] from, final double[] to) {
		context.waitTicks(CLICK_PAUSE_TICKS);
		moveCursor(context, from[0], from[1]);
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		double y = from[1];
		while (Math.abs(to[1] - y) > DRAG_STEP) {
			y += Math.signum(to[1] - y) * DRAG_STEP;
			moveCursor(context, from[0], y);
			context.waitTick();
		}
		moveCursor(context, from[0], to[1]);
		context.waitTick();
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
	}

	static void dragServer(final ClientGameTestContext context, final JoinMultiplayerScreen screen, final String name, final String onto) {
		drag(context, textPoint(context, screen, name), textPoint(context, screen, onto));
	}

	static Screen currentScreen(final ClientGameTestContext context) {
		return context.computeOnClient(client -> client.gui.screen());
	}

	static void assertEquals(final Object expected, final Object actual, final String what) {
		if (!expected.equals(actual)) {
			throw new AssertionError(what + ": expected " + expected + ", got " + actual);
		}
	}

	static void assertTrue(final boolean condition, final String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	/** Fails if any two of {@code widgets} overlap. */
	static void assertNoOverlap(final List<? extends GuiEventListener> widgets, final String where) {
		for (int i = 0; i < widgets.size(); i++) {
			for (int j = i + 1; j < widgets.size(); j++) {
				if (widgets.get(i).getRectangle().intersects(widgets.get(j).getRectangle())) {
					throw new AssertionError(where + ": " + describe(widgets.get(i)) + " overlaps " + describe(widgets.get(j)));
				}
			}
		}
	}

	static String describe(final GuiEventListener widget) {
		String label = widget instanceof AbstractWidget abstractWidget ? abstractWidget.getMessage().getString() : "";
		return widget.getClass().getSimpleName() + "('" + label + "' at " + widget.getRectangle() + ")";
	}

	/** Switches the game language and waits until the resources are reloaded. */
	static void setLanguage(final ClientGameTestContext context, final String code) {
		CompletableFuture<Void> reload = context.computeOnClient(client -> {
			client.getLanguageManager().setSelected(code);
			client.options.languageCode = code;
			return client.reloadResourcePacks();
		});
		context.waitFor(client -> reload.isDone() && client.gui.overlay() == null);
		context.waitTicks(2);
	}

	/**
	 * Enlarges the window so that every server and widget of the tests fits on screen.
	 *
	 * @return the previous window state, for {@link #restoreWindow}
	 */
	static int[] useRoomyWindow(final ClientGameTestContext context) {
		return useWindow(context, ROOMY_WINDOW_WIDTH, ROOMY_WINDOW_HEIGHT, ROOMY_GUI_SCALE);
	}

	/**
	 * Resizes the window and sets the GUI scale.
	 *
	 * @return the previous window state, for {@link #restoreWindow}
	 */
	static int[] useWindow(final ClientGameTestContext context, final int width, final int height, final int guiScale) {
		int[] previous = context.computeOnClient(client -> new int[] {
			client.getWindow().getScreenWidth(), client.getWindow().getScreenHeight(), client.options.guiScale().get()});
		context.getInput().resizeWindow(width, height);
		setGuiScale(context, guiScale);
		return previous;
	}

	static void restoreWindow(final ClientGameTestContext context, final int[] previous) {
		setGuiScale(context, previous[2]);
		context.getInput().resizeWindow(previous[0], previous[1]);
	}

	static void setGuiScale(final ClientGameTestContext context, final int scale) {
		context.runOnClient(client -> {
			client.options.guiScale().set(scale);
			client.resizeGui();
		});
		context.waitTick();
	}
}
