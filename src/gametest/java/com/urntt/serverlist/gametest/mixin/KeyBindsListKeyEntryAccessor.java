package com.urntt.serverlist.gametest.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBindsList.KeyEntry.class)
public interface KeyBindsListKeyEntryAccessor {
	@Accessor("key")
	KeyMapping serverlistGametest$key();

	@Accessor("hasCollision")
	boolean serverlistGametest$hasCollision();
}
