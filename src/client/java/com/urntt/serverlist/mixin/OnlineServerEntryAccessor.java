package com.urntt.serverlist.mixin;

import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public interface OnlineServerEntryAccessor {
	/** Vanilla's move: swaps the server with another one in the saved list and in the visible list. */
	@Invoker("swap")
	void serverlist$swap(int currentIndex, int newIndex);

	@Accessor("statusIcon")
	@Nullable Identifier serverlist$statusIcon();
}
