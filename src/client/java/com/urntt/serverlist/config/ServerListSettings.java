package com.urntt.serverlist.config;

/**
 * The values every feature reads. Implemented by the mod's own configuration, by the OneConfig integration and by
 * the constant {@link SettingValues} sets.
 */
public interface ServerListSettings {
	/**
	 * Whether the edit, delete and open-server-list key bindings act. They have no switch of their own, because
	 * unbinding them turns them off; only disabling the mod turns them off.
	 */
	boolean shortcuts();

	/** Whether saved servers can be reordered by dragging them in the server list. */
	boolean dragToReorder();

	/** Whether the pause menu shows a button that opens the server list. */
	boolean pauseMenuButton();

	/** Whether the server list shows a search box above its buttons. */
	boolean searchBox();

	/** Whether each server entry shows its latency in milliseconds. */
	boolean showPingMs();

	/** Whether the server list pings more servers at once than vanilla does. */
	boolean fasterServerPings();

	/** Whether literal IP addresses skip the reverse DNS lookup when pinging or joining a server. */
	boolean skipReverseDnsForIpAddresses();
}
