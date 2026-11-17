package com.urntt.serverlist.gametest;

import static com.urntt.serverlist.gametest.TestSupport.assertEquals;
import static com.urntt.serverlist.gametest.TestSupport.assertTrue;

import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.config.ServerListConfig;
import com.urntt.serverlist.config.ServerListConfigScreen;
import java.nio.file.Files;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

/**
 * The configuration file and screen, and screenshots of every screen the mod changes in Chinese.
 *
 * <p>Runs first, so it sees the defaults of a fresh installation.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ConfigGameTest implements FabricClientGameTest {
	@Override
	public void runTest(final ClientGameTestContext context) {
		ServerListConfig config = ServerListClient.config();
		assertTrue(Files.exists(ServerListConfig.defaultPath()), "the config file was not created");
		assertAllEnabled(config, "defaults");
		assertAllEnabled(ServerListConfig.load(ServerListConfig.defaultPath()), "defaults read back from the file");

		config.setShowPingMs(false);
		config.setSkipReverseDnsForIpAddresses(false);
		ServerListConfig reloaded = ServerListConfig.load(ServerListConfig.defaultPath());
		assertTrue(!reloaded.showPingMs() && !reloaded.skipReverseDnsForIpAddresses(), "changed settings were not saved");
		assertTrue(reloaded.dragToReorder() && reloaded.fasterServerPings(), "unchanged settings were not kept");
		TestSupport.resetConfig();

		screenshotScreens(context, "en_us");
		TestSupport.setLanguage(context, "zh_cn");
		screenshotScreens(context, "zh_cn");
		TestSupport.setLanguage(context, "en_us");
		context.setScreen(TitleScreen::new);
	}

	private static void screenshotScreens(final ClientGameTestContext context, final String language) {
		context.setScreen(() -> new ServerListConfigScreen(new TitleScreen()));
		context.waitTick();
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

	private static void assertAllEnabled(final ServerListConfig config, final String what) {
		assertEquals(List.of(true, true, true, true, true, true), List.of(
				config.dragToReorder(), config.pauseMenuButton(), config.searchBox(),
				config.showPingMs(), config.fasterServerPings(), config.skipReverseDnsForIpAddresses()), what);
	}
}
