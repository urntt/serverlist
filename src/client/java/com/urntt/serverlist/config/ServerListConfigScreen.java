package com.urntt.serverlist.config;

import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.compat.OneConfigBridge;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * The configuration screen, built from vanilla widgets. Opened from Mod Menu or with its key binding.
 *
 * <p>The mode button comes first. The settings below it can only be edited in custom mode; otherwise they are greyed
 * out and show the values that currently apply.
 */
public final class ServerListConfigScreen extends OptionsSubScreen {
	public static final Component TITLE = Component.translatable(SettingKeys.TITLE);

	private final ServerListConfig config = ServerListClient.config();

	public ServerListConfigScreen(final @Nullable Screen lastScreen) {
		super(lastScreen, Minecraft.getInstance().options, TITLE);
	}

	/** Shows the screen on top of whatever screen is open. */
	public static void open(final Minecraft minecraft) {
		minecraft.gui.setScreen(new ServerListConfigScreen(minecraft.gui.screen()));
	}

	@Override
	protected void addOptions() {
		ConfigMode mode = this.config.effectiveMode();
		boolean editable = mode == ConfigMode.CUSTOM;
		ServerListSettings shown = this.config.effective();

		this.list.addBig(this.createModeButton(mode));
		if (mode == ConfigMode.ONECONFIG) {
			this.list.addBig(Button.builder(Component.translatable(SettingKeys.OPEN_ONECONFIG), button -> OneConfigBridge.openOneConfig()).build());
		}

		this.list.addHeader(Component.translatable(SettingKeys.SECTION_SERVER_LIST));
		this.list.addBig(toggle(SettingKeys.DRAG_TO_REORDER, shown.dragToReorder(), editable, this.config::setDragToReorder));
		this.list.addBig(toggle(SettingKeys.SEARCH_BOX, shown.searchBox(), editable, this.config::setSearchBox));
		this.list.addBig(toggle(SettingKeys.SHOW_PING_MS, shown.showPingMs(), editable, this.config::setShowPingMs));
		this.list.addBig(toggle(SettingKeys.PAUSE_MENU_BUTTON, shown.pauseMenuButton(), editable, this.config::setPauseMenuButton));
		this.list.addBig(Button.builder(Component.translatable("controls.keybinds"),
				button -> this.minecraft.gui.setScreen(new KeyBindsScreen(this, this.options))).build());

		this.list.addHeader(Component.translatable(SettingKeys.SECTION_PINGS));
		this.list.addBig(toggle(SettingKeys.FASTER_SERVER_PINGS, shown.fasterServerPings(), editable, this.config::setFasterServerPings));
		this.list.addBig(toggle(SettingKeys.SKIP_REVERSE_DNS, shown.skipReverseDnsForIpAddresses(), editable, this.config::setSkipReverseDnsForIpAddresses));
	}

	private CycleButton<ConfigMode> createModeButton(final ConfigMode mode) {
		List<ConfigMode> modes = OneConfigBridge.isAvailable()
				? List.of(ConfigMode.values())
				: List.of(ConfigMode.DISABLED, ConfigMode.CUSTOM);
		boolean oneConfigMissing = this.config.storedMode() == ConfigMode.ONECONFIG && !OneConfigBridge.isAvailable();
		return CycleButton.builder(ConfigMode::label, mode)
				.withValues(modes)
				.withTooltip(value -> Tooltip.create(oneConfigMissing && value == mode
						? Component.translatable(SettingKeys.ONECONFIG_MISSING)
						: value.description()))
				.create(Component.translatable(SettingKeys.MODE), (button, value) -> {
					this.config.setMode(value);
					// The settings below change their values and whether they can be edited, so start over.
					this.minecraft.gui.setScreen(new ServerListConfigScreen(this.lastScreen));
				});
	}

	/** An on/off button whose caption is {@code key} and whose tooltip is {@code key} with {@code .tooltip} appended. */
	private static CycleButton<Boolean> toggle(final String key, final boolean value, final boolean editable, final Consumer<Boolean> setter) {
		Tooltip tooltip = Tooltip.create(Component.translatable(key + SettingKeys.TOOLTIP));
		CycleButton<Boolean> button = CycleButton.onOffBuilder(value)
				.withTooltip(ignored -> tooltip)
				.create(Component.translatable(key), (cycleButton, newValue) -> setter.accept(newValue));
		button.active = editable;
		return button;
	}
}
