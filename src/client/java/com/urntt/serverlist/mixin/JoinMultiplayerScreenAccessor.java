package com.urntt.serverlist.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(JoinMultiplayerScreen.class)
public interface JoinMultiplayerScreenAccessor {
	@Accessor("serverSelectionList")
	ServerSelectionList serverlist$serverSelectionList();

	@Accessor("editButton")
	Button serverlist$editButton();

	@Accessor("deleteButton")
	Button serverlist$deleteButton();
}
