package com.urntt.serverlist.config;

/**
 * An immutable set of settings.
 */
public record SettingValues(
		boolean shortcuts,
		boolean dragToReorder,
		boolean pauseMenuButton,
		boolean searchBox,
		boolean showPingMs,
		boolean fasterServerPings,
		boolean skipReverseDnsForIpAddresses) implements ServerListSettings {
	/** The defaults of the mod's own settings and of its OneConfig settings: every feature is on. */
	public static final SettingValues DEFAULTS = new SettingValues(true, true, true, true, true, true, true);

	/** Vanilla behavior, applied while the mod is disabled. */
	public static final SettingValues DISABLED = new SettingValues(false, false, false, false, false, false, false);

	/** Copies the current values of {@code settings}. */
	public static SettingValues copyOf(final ServerListSettings settings) {
		return new SettingValues(
				settings.shortcuts(),
				settings.dragToReorder(),
				settings.pauseMenuButton(),
				settings.searchBox(),
				settings.showPingMs(),
				settings.fasterServerPings(),
				settings.skipReverseDnsForIpAddresses());
	}
}
