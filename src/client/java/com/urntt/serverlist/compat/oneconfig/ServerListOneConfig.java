package com.urntt.serverlist.compat.oneconfig;

import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.config.ServerListSettings;
import com.urntt.serverlist.config.SettingKeys;
import com.urntt.serverlist.config.SettingValues;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.MasterToggle;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;

/**
 * The settings OneConfig manages in OneConfig mode, with the same defaults as the mod's own settings. Titles use the
 * mod's translation keys; the keys double as fallback titles.
 */
public final class ServerListOneConfig extends Config implements ServerListSettings {
	/**
	 * OneConfig keeps its files in the same config directory as the mod's own {@code serverlist.json} and watches them
	 * for changes, so its file needs a name of its own.
	 */
	private static final String FILE_NAME = ServerListClient.MOD_ID + "-oneconfig.json";

	/** The mod icon, as named in {@code fabric.mod.json}, as a resource path for OneConfig's mod card. */
	private static final String ICON_PATH = "/assets/" + ServerListClient.MOD_ID + "/icon.png";

	/** The toggle on the mod's OneConfig card. While it is off, every feature is off. */
	@MasterToggle
	public boolean enabled = true;

	@Switch(
			title = SettingKeys.DRAG_TO_REORDER,
			titleKey = SettingKeys.DRAG_TO_REORDER,
			descriptionKey = SettingKeys.DRAG_TO_REORDER + SettingKeys.TOOLTIP,
			subcategory = SettingKeys.SECTION_SERVER_LIST,
			subcategoryKey = SettingKeys.SECTION_SERVER_LIST)
	public boolean dragToReorder = SettingValues.DEFAULTS.dragToReorder();

	@Switch(
			title = SettingKeys.SEARCH_BOX,
			titleKey = SettingKeys.SEARCH_BOX,
			descriptionKey = SettingKeys.SEARCH_BOX + SettingKeys.TOOLTIP,
			subcategory = SettingKeys.SECTION_SERVER_LIST,
			subcategoryKey = SettingKeys.SECTION_SERVER_LIST)
	public boolean searchBox = SettingValues.DEFAULTS.searchBox();

	@Switch(
			title = SettingKeys.SHOW_PING_MS,
			titleKey = SettingKeys.SHOW_PING_MS,
			descriptionKey = SettingKeys.SHOW_PING_MS + SettingKeys.TOOLTIP,
			subcategory = SettingKeys.SECTION_SERVER_LIST,
			subcategoryKey = SettingKeys.SECTION_SERVER_LIST)
	public boolean showPingMs = SettingValues.DEFAULTS.showPingMs();

	@Switch(
			title = SettingKeys.PAUSE_MENU_BUTTON,
			titleKey = SettingKeys.PAUSE_MENU_BUTTON,
			descriptionKey = SettingKeys.PAUSE_MENU_BUTTON + SettingKeys.TOOLTIP,
			subcategory = SettingKeys.SECTION_SERVER_LIST,
			subcategoryKey = SettingKeys.SECTION_SERVER_LIST)
	public boolean pauseMenuButton = SettingValues.DEFAULTS.pauseMenuButton();

	@Switch(
			title = SettingKeys.FASTER_SERVER_PINGS,
			titleKey = SettingKeys.FASTER_SERVER_PINGS,
			descriptionKey = SettingKeys.FASTER_SERVER_PINGS + SettingKeys.TOOLTIP,
			subcategory = SettingKeys.SECTION_PINGS,
			subcategoryKey = SettingKeys.SECTION_PINGS)
	public boolean fasterServerPings = SettingValues.DEFAULTS.fasterServerPings();

	@Switch(
			title = SettingKeys.SKIP_REVERSE_DNS,
			titleKey = SettingKeys.SKIP_REVERSE_DNS,
			descriptionKey = SettingKeys.SKIP_REVERSE_DNS + SettingKeys.TOOLTIP,
			subcategory = SettingKeys.SECTION_PINGS,
			subcategoryKey = SettingKeys.SECTION_PINGS)
	public boolean skipReverseDnsForIpAddresses = SettingValues.DEFAULTS.skipReverseDnsForIpAddresses();

	public ServerListOneConfig() {
		super(FILE_NAME, ICON_PATH, ServerListClient.MOD_ID, Category.QOL);
	}

	/** The settings that apply in OneConfig mode, which are all off while the card's toggle is off. */
	ServerListSettings applied() {
		return this.enabled ? this : SettingValues.DISABLED;
	}

	/** OneConfig has no switch for the key bindings; the card's toggle turns them off with everything else. */
	@Override
	public boolean shortcuts() {
		return SettingValues.DEFAULTS.shortcuts();
	}

	@Override
	public boolean dragToReorder() {
		return this.dragToReorder;
	}

	@Override
	public boolean pauseMenuButton() {
		return this.pauseMenuButton;
	}

	@Override
	public boolean searchBox() {
		return this.searchBox;
	}

	@Override
	public boolean showPingMs() {
		return this.showPingMs;
	}

	@Override
	public boolean fasterServerPings() {
		return this.fasterServerPings;
	}

	@Override
	public boolean skipReverseDnsForIpAddresses() {
		return this.skipReverseDnsForIpAddresses;
	}
}
