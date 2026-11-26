package com.urntt.serverlist.gametest;

import static com.urntt.serverlist.gametest.TestSupport.LOGGER;
import static com.urntt.serverlist.gametest.TestSupport.assertEquals;
import static com.urntt.serverlist.gametest.TestSupport.assertTrue;

import com.mojang.blaze3d.platform.InputConstants;
import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.compat.OneConfigBridge;
import com.urntt.serverlist.config.ConfigMode;
import com.urntt.serverlist.config.ServerListConfig;
import com.urntt.serverlist.config.ServerListConfigScreen;
import com.urntt.serverlist.config.SettingKeys;
import com.urntt.serverlist.config.SettingValues;
import com.urntt.serverlist.multiplayer.ServerListScreenExt;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

/**
 * The configuration file, the configuration modes and screen, what Disabled mode turns off in the server list, and
 * screenshots of every screen the mod changes in Chinese.
 *
 * <p>Runs first, so it sees the defaults of a fresh installation. The test client runs without OneConfig by default
 * and with it when Gradle is given {@code -PoneConfigRuntime=true}; the checks follow whichever modes are offered.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ConfigGameTest implements FabricClientGameTest {
	/** The six on/off settings below the mode button. */
	private static final int SETTING_TOGGLES = 6;

	@Override
	public void runTest(final ClientGameTestContext context) {
		ServerListConfig config = ServerListClient.config();
		assertTrue(Files.exists(ServerListConfig.defaultPath()), "the config file was not created");
		assertDefaults(config, "defaults");
		assertDefaults(ServerListConfig.load(ServerListConfig.defaultPath()), "defaults read back from the file");
		boolean oneConfigInstalled = FabricLoader.getInstance().isModLoaded(OneConfigBridge.MOD_ID);
		assertEquals(oneConfigInstalled, OneConfigBridge.isAvailable(), "OneConfig mode is available exactly when OneConfig is installed");
		// OneConfig mode is the default, and the custom settings apply while OneConfig is not installed.
		assertEquals(oneConfigInstalled ? ConfigMode.ONECONFIG : ConfigMode.CUSTOM, config.effectiveMode(), "default effective mode");

		testModeCycle(context, config, oneConfigInstalled);

		config.setShowPingMs(false);
		config.setSkipReverseDnsForIpAddresses(false);
		ServerListConfig reloaded = ServerListConfig.load(ServerListConfig.defaultPath());
		assertTrue(!reloaded.showPingMs() && !reloaded.skipReverseDnsForIpAddresses(), "changed settings were not saved");
		assertTrue(reloaded.dragToReorder() && reloaded.fasterServerPings(), "unchanged settings were not kept");
		TestSupport.resetConfig();

		testDisabledServerList(context, config);

		screenshotScreens(context, "en_us");
		TestSupport.setLanguage(context, "zh_cn");
		screenshotScreens(context, "zh_cn");
		TestSupport.setLanguage(context, "en_us");
		context.setScreen(TitleScreen::new);
	}

	private static void assertDefaults(final ServerListConfig config, final String what) {
		assertEquals(ConfigMode.ONECONFIG, config.storedMode(), "mode of the " + what);
		assertEquals(SettingValues.DEFAULTS, SettingValues.copyOf(config), what);
	}

	/**
	 * Clicks the mode button from the default mode through every offered mode and back. OneConfig mode is only
	 * offered while OneConfig is installed; otherwise the default OneConfig mode shows as custom.
	 */
	private static void testModeCycle(final ClientGameTestContext context, final ServerListConfig config, final boolean oneConfigInstalled) {
		List<ConfigMode> modes = oneConfigInstalled
				? List.of(ConfigMode.ONECONFIG, ConfigMode.DISABLED, ConfigMode.CUSTOM)
				: List.of(ConfigMode.CUSTOM, ConfigMode.DISABLED);
		// A custom value that differs from OneConfig's default shows which settings apply in OneConfig mode.
		config.setShowPingMs(false);

		openConfigScreen(context);
		assertEquals(!oneConfigInstalled, settingsActive(context), "settings editable in the default mode");
		hoverWidget(context, widget -> widget instanceof CycleButton<?> cycle && cycle.getValue() instanceof ConfigMode);
		context.takeScreenshot("config_default_mode");
		for (int i = 1; i <= modes.size(); i++) {
			ConfigMode expected = modes.get(i % modes.size());
			clickWidget(context, widget -> widget instanceof CycleButton<?> cycle && cycle.getValue() instanceof ConfigMode);
			context.waitForScreen(ServerListConfigScreen.class);
			context.waitTicks(2);
			assertEquals(expected, config.storedMode(), "mode after clicking the mode button");
			assertEquals(expected, ServerListConfig.load(ServerListConfig.defaultPath()).storedMode(), "saved mode");
			if (expected == ConfigMode.CUSTOM) {
				assertTrue(settingsActive(context), "the settings are not editable again in custom mode");
			} else {
				assertTrue(settingsInactive(context), "the settings are not greyed out in " + expected + " mode");
			}
			TestSupport.moveCursor(context, 0, 0);
			context.waitTick();
			context.takeScreenshot("config_" + expected.name().toLowerCase(Locale.ROOT));

			switch (expected) {
				case DISABLED -> {
					assertEquals(SettingValues.DISABLED, SettingValues.copyOf(ServerListClient.settings()), "settings in disabled mode");
					assertEquals(List.of(false), shownToggleValues(context).stream().distinct().toList(), "toggles shown in disabled mode");
				}
				case ONECONFIG -> testOneConfigMode(context);
				case CUSTOM -> assertTrue(!ServerListClient.settings().showPingMs(), "custom mode does not apply the custom settings");
			}
		}
		config.setShowPingMs(true);
		context.setScreen(TitleScreen::new);
	}

	/** In OneConfig mode, OneConfig's settings apply and the screen links to the mod's OneConfig page. */
	private static void testOneConfigMode(final ClientGameTestContext context) {
		assertTrue(ServerListClient.settings().showPingMs(), "OneConfig mode applies the custom settings instead of OneConfig's");

		Component openLabel = Component.translatable(SettingKeys.OPEN_ONECONFIG);
		clickWidget(context, widget -> widget instanceof Button && widget.getMessage().equals(openLabel));
		context.waitFor(client -> client.gui.screen() != null && !(client.gui.screen() instanceof ServerListConfigScreen));
		context.waitTicks(20);
		context.takeScreenshot("oneconfig_page");
		String shown = context.computeOnClient(client -> client.gui.screen().getClass().getName());
		LOGGER.info("Open in OneConfig shows {}", shown);
		openConfigScreen(context);
	}

	/**
	 * In disabled mode the server list works like vanilla: no search box, right-click does not edit, and dragging
	 * does not reorder.
	 */
	private static void testDisabledServerList(final ClientGameTestContext context, final ServerListConfig config) {
		config.setMode(ConfigMode.DISABLED);
		int[] originalWindow = TestSupport.useRoomyWindow(context);
		TestSupport.saveServers(context, TestSupport.NAMES);
		JoinMultiplayerScreen screen = TestSupport.openServerList(context);
		assertTrue(context.computeOnClient(client -> ((ServerListScreenExt) screen).serverlist$searchBox()) == null,
				"the server list has a search box in disabled mode");

		TestSupport.click(context, TestSupport.textPoint(context, screen, "Charlie"), InputConstants.MOUSE_BUTTON_RIGHT);
		context.waitTicks(2);
		assertTrue(!(TestSupport.currentScreen(context) instanceof ManageServerScreen), "right-click edits a server in disabled mode");

		TestSupport.dragServer(context, screen, "Alpha", "Delta");
		assertEquals(TestSupport.NAMES, TestSupport.savedNames(context), "saved order after dragging in disabled mode");

		config.setMode(ConfigMode.CUSTOM);
		TestSupport.restoreWindow(context, originalWindow);
		context.setScreen(TitleScreen::new);
	}

	private static void screenshotScreens(final ClientGameTestContext context, final String language) {
		openConfigScreen(context);
		TestSupport.moveCursor(context, 0, 0);
		context.waitTick();
		context.takeScreenshot("config_" + language);

		TestSupport.saveServers(context, List.of("Alpha", "Bravo"));
		TestSupport.openServerList(context);
		TestSupport.moveCursor(context, 0, 0);
		context.waitTick();
		context.takeScreenshot("server_list_" + language);

		TestServerInfoExtension.enabled = true;
		ForeignWidget.enabled = true;
		context.setScreen(() -> new ManageServerScreen(new TitleScreen(), Component.translatable("manageServer.add.title"),
				result -> { }, new ServerData("", "", ServerData.Type.OTHER)));
		context.waitTick();
		TestSupport.moveCursor(context, 0, 0);
		context.waitTick();
		context.takeScreenshot("server_info_" + language);
		TestServerInfoExtension.enabled = false;
		ForeignWidget.enabled = false;
	}

	private static void openConfigScreen(final ClientGameTestContext context) {
		context.setScreen(() -> new ServerListConfigScreen(new TitleScreen()));
		// The option list positions its widgets while rendering, so let it render before clicking.
		context.waitTicks(2);
	}

	/** Clicks the first widget of the open screen that matches, with the real mouse. */
	private static void clickWidget(final ClientGameTestContext context, final Predicate<AbstractWidget> matches) {
		TestSupport.click(context, widgetCenter(context, matches), InputConstants.MOUSE_BUTTON_LEFT);
	}

	/** Moves the cursor over the first widget of the open screen that matches, so its tooltip shows. */
	private static void hoverWidget(final ClientGameTestContext context, final Predicate<AbstractWidget> matches) {
		double[] center = widgetCenter(context, matches);
		TestSupport.moveCursor(context, center[0], center[1]);
		context.waitTicks(2);
	}

	/** The center, in GUI coordinates, of the first widget of the open screen that matches. */
	private static double[] widgetCenter(final ClientGameTestContext context, final Predicate<AbstractWidget> matches) {
		return context.computeOnClient(client -> {
			AbstractWidget widget = widgets(client).stream()
					.filter(matches)
					.findFirst()
					.orElseThrow(() -> new AssertionError("no matching widget on the screen"));
			return new double[] {widget.getX() + widget.getWidth() / 2.0, widget.getY() + widget.getHeight() / 2.0};
		});
	}

	private static boolean settingsActive(final ClientGameTestContext context) {
		return context.computeOnClient(client -> settingToggles(client).stream().allMatch(toggle -> toggle.active));
	}

	private static boolean settingsInactive(final ClientGameTestContext context) {
		return context.computeOnClient(client -> settingToggles(client).stream().noneMatch(toggle -> toggle.active));
	}

	private static List<Boolean> shownToggleValues(final ClientGameTestContext context) {
		return context.computeOnClient(client -> settingToggles(client).stream()
				.map(toggle -> (Boolean) toggle.getValue())
				.toList());
	}

	/** The on/off buttons of the settings, without the mode button. */
	private static List<CycleButton<?>> settingToggles(final Minecraft client) {
		List<CycleButton<?>> toggles = widgets(client).stream()
				.filter(widget -> widget instanceof CycleButton<?> cycle && cycle.getValue() instanceof Boolean)
				.<CycleButton<?>>map(widget -> (CycleButton<?>) widget)
				.toList();
		assertEquals(SETTING_TOGGLES, toggles.size(), "on/off settings on the configuration screen");
		return toggles;
	}

	private static List<AbstractWidget> widgets(final Minecraft client) {
		Screen screen = Objects.requireNonNull(client.gui.screen(), "no screen is open");
		List<AbstractWidget> widgets = new ArrayList<>();
		collect(screen.children(), widgets);
		return widgets;
	}

	private static void collect(final List<? extends GuiEventListener> listeners, final List<AbstractWidget> widgets) {
		for (GuiEventListener listener : listeners) {
			if (listener instanceof AbstractWidget widget) {
				widgets.add(widget);
			}
			if (listener instanceof ContainerEventHandler container) {
				collect(container.children(), widgets);
			}
		}
	}
}
