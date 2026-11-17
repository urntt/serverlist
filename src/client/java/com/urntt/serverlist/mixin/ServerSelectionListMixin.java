package com.urntt.serverlist.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.urntt.serverlist.multiplayer.ServerFilter;
import com.urntt.serverlist.multiplayer.ServerSelectionListExt;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hides saved servers that do not match the search query. The LAN section is never filtered.
 */
@Mixin(ServerSelectionList.class)
abstract class ServerSelectionListMixin implements ServerSelectionListExt {
	@Shadow
	@Final
	private List<ServerSelectionList.OnlineServerEntry> onlineServers;

	@Unique
	private String serverlist$query = "";

	@Shadow
	protected abstract void refreshEntries();

	/**
	 * Filters the copy of the saved servers that {@code refreshEntries} shows, before it adds the LAN entries and
	 * restores the selection, so a hidden server never stays selected.
	 */
	@ModifyExpressionValue(method = "refreshEntries", at = @At(value = "NEW", target = "java/util/ArrayList"))
	private ArrayList<ServerSelectionList.Entry> serverlist$filterSavedServers(final ArrayList<ServerSelectionList.Entry> entries) {
		if (!this.serverlist$query.isEmpty()) {
			entries.removeIf(entry -> entry instanceof ServerSelectionList.OnlineServerEntry server
					&& !ServerFilter.matches(server.getServerData(), this.serverlist$query));
		}
		return entries;
	}

	@Override
	public String serverlist$query() {
		return this.serverlist$query;
	}

	@Override
	public void serverlist$setQuery(final String query) {
		if (!query.equals(this.serverlist$query)) {
			this.serverlist$query = query;
			this.refreshEntries();
		}
	}

	@Override
	public List<ServerSelectionList.OnlineServerEntry> serverlist$onlineServers() {
		return this.onlineServers;
	}
}
