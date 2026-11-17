package com.urntt.serverlist.multiplayer;

import java.util.List;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;

/**
 * State that serverlist adds to {@link ServerSelectionList}. Implemented by a mixin.
 */
public interface ServerSelectionListExt {
	/** The current search query; empty when no filter is active. */
	String serverlist$query();

	/** Sets the search query and rebuilds the visible entries. */
	void serverlist$setQuery(String query);

	/** All saved server entries, including those hidden by the filter, in the saved order. */
	List<ServerSelectionList.OnlineServerEntry> serverlist$onlineServers();
}
