package com.urntt.serverlist.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.pause.PauseMenuButton;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Adds the server list button to the pause menu's icon row before vanilla lays out the menu.
 */
@Mixin(PauseScreen.class)
abstract class PauseScreenMixin {
	@WrapOperation(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout;arrangeElements()V"))
	private void serverlist$addServerListButton(final GridLayout pauseMenu, final Operation<Void> original) {
		if (ServerListClient.settings().pauseMenuButton()) {
			PauseMenuButton.addTo(pauseMenu, (PauseScreen) (Object) this);
		}
		original.call(pauseMenu);
	}
}
