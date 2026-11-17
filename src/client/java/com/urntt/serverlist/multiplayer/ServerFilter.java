package com.urntt.serverlist.multiplayer;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;

/**
 * Matching rules of the server list's search box.
 */
public final class ServerFilter {
	private ServerFilter() {
	}

	/**
	 * Whether {@code server} matches {@code query}: its name or address contains the query, ignoring case. The
	 * address is not searched while the "Hide Server Address" option hides it.
	 */
	public static boolean matches(final ServerData server, final String query) {
		String needle = normalize(query);
		if (needle.isEmpty()) {
			return true;
		}

		if (server.name != null && normalize(server.name).contains(needle)) {
			return true;
		}

		return !Minecraft.getInstance().options.hideServerAddress && server.ip != null && normalize(server.ip).contains(needle);
	}

	/** Whether a filter is active on {@code list}, which disables every way of reordering servers. */
	public static boolean isActive(final ServerSelectionList list) {
		return !normalize(((ServerSelectionListExt) list).serverlist$query()).isEmpty();
	}

	private static String normalize(final String text) {
		return text.strip().toLowerCase(Locale.ROOT);
	}
}
