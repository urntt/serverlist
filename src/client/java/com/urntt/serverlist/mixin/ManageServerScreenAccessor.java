package com.urntt.serverlist.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ManageServerScreen.class)
public interface ManageServerScreenAccessor {
	@Accessor("serverData")
	ServerData serverlist$serverData();

	@Accessor("nameEdit")
	EditBox serverlist$nameEdit();

	@Accessor("ipEdit")
	EditBox serverlist$ipEdit();

	@Accessor("addButton")
	Button serverlist$addButton();
}
