package com.urntt.serverlist.config;

/**
 * Translation keys of the settings, shared by the configuration screen and the OneConfig integration. They are
 * compile-time constants so that OneConfig's option annotations can use them.
 */
public final class SettingKeys {
	private static final String PREFIX = "options.serverlist.";

	public static final String TITLE = PREFIX + "title";
	public static final String MODE = PREFIX + "mode";
	public static final String ONECONFIG_MISSING = MODE + ".oneconfig.missing";
	public static final String OPEN_ONECONFIG = PREFIX + "openOneConfig";

	public static final String SECTION_SERVER_LIST = PREFIX + "section.serverList";
	public static final String SECTION_PINGS = PREFIX + "section.pings";

	public static final String DRAG_TO_REORDER = PREFIX + "dragToReorder";
	public static final String SEARCH_BOX = PREFIX + "searchBox";
	public static final String SHOW_PING_MS = PREFIX + "showPingMs";
	public static final String PAUSE_MENU_BUTTON = PREFIX + "pauseMenuButton";
	public static final String FASTER_SERVER_PINGS = PREFIX + "fasterServerPings";
	public static final String SKIP_REVERSE_DNS = PREFIX + "skipReverseDnsForIpAddresses";

	/** Suffix of the key that describes a setting or mode, shown as its tooltip. */
	public static final String TOOLTIP = ".tooltip";

	private SettingKeys() {
	}
}
