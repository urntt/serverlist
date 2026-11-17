package com.urntt.serverlist.config;

import com.urntt.serverlist.ServerListClient;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;

/**
 * Configuration screen built from vanilla widgets. Opened from Mod Menu.
 */
public final class ServerListConfigScreen extends OptionsSubScreen {
	public static final Component TITLE = Component.translatable("options.serverlist.title");
	private static final String KEY_PREFIX = "options.serverlist.";

	public ServerListConfigScreen(final Screen lastScreen) {
		super(lastScreen, Minecraft.getInstance().options, TITLE);
	}

	@Override
	protected void addOptions() {
		ServerListConfig config = ServerListClient.config();
		this.list.addHeader(Component.translatable(KEY_PREFIX + "section.serverList"));
		this.list.addBig(toggle("dragToReorder", config.dragToReorder(), config::setDragToReorder));
		this.list.addBig(toggle("searchBox", config.searchBox(), config::setSearchBox));
		this.list.addBig(toggle("showPingMs", config.showPingMs(), config::setShowPingMs));
		this.list.addBig(toggle("pauseMenuButton", config.pauseMenuButton(), config::setPauseMenuButton));
		this.list.addBig(Button.builder(Component.translatable("controls.keybinds"),
				button -> this.minecraft.gui.setScreen(new KeyBindsScreen(this, this.options))).build());
		this.list.addHeader(Component.translatable(KEY_PREFIX + "section.pings"));
		this.list.addBig(toggle("fasterServerPings", config.fasterServerPings(), config::setFasterServerPings));
		this.list.addBig(toggle("skipReverseDnsForIpAddresses", config.skipReverseDnsForIpAddresses(), config::setSkipReverseDnsForIpAddresses));
	}

	/**
	 * An on/off option whose caption is {@code options.serverlist.<name>} and whose tooltip is the same key with
	 * {@code .tooltip} appended.
	 */
	private static OptionInstance<Boolean> toggle(final String name, final boolean value, final Consumer<Boolean> setter) {
		String key = KEY_PREFIX + name;
		return OptionInstance.createBoolean(
				key, OptionInstance.cachedConstantTooltip(Component.translatable(key + ".tooltip")), value, setter::accept);
	}
}
