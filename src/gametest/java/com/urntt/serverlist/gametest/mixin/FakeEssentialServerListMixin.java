package com.urntt.serverlist.gametest.mixin;

import com.urntt.serverlist.gametest.FakeEssential;
import com.urntt.serverlist.gametest.FakeEssentialDragScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds the public method that Essential adds to the server list screen, so the tests can switch "Essential's
 * drag handler" on and off.
 */
@Mixin(JoinMultiplayerScreen.class)
abstract class FakeEssentialServerListMixin implements FakeEssentialDragScreen {
	@Override
	public Object essential$getDragHandlerOrNull() {
		return FakeEssential.dragHandler;
	}
}
