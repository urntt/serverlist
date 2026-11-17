package com.urntt.serverlist.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.urntt.serverlist.serverinfo.ManageServerScreenExt;
import com.urntt.serverlist.serverinfo.ServerInfoScreenState;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ManageServerScreen;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Remembers vanilla's resource pack and Cancel buttons, which vanilla keeps only in local variables, and runs the
 * extensions' save actions when the player presses Done. The layout itself happens in
 * {@link com.urntt.serverlist.serverinfo.ServerInfoScreenController}.
 */
@Mixin(ManageServerScreen.class)
abstract class ManageServerScreenMixin implements ManageServerScreenExt {
	@Unique
	private @Nullable AbstractWidget serverlist$resourcePackButton;

	@Unique
	private @Nullable AbstractWidget serverlist$cancelButton;

	@Unique
	private final ServerInfoScreenState serverlist$state = new ServerInfoScreenState();

	@ModifyExpressionValue(
			method = "init",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/ManageServerScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;",
					ordinal = 0))
	private GuiEventListener serverlist$rememberResourcePackButton(final GuiEventListener widget) {
		this.serverlist$resourcePackButton = (AbstractWidget) widget;
		return widget;
	}

	@ModifyExpressionValue(
			method = "init",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/ManageServerScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;",
					ordinal = 2))
	private GuiEventListener serverlist$rememberCancelButton(final GuiEventListener widget) {
		this.serverlist$cancelButton = (AbstractWidget) widget;
		return widget;
	}

	@Inject(method = "onAdd", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/booleans/BooleanConsumer;accept(Z)V"))
	private void serverlist$runSaveActions(final CallbackInfo ci) {
		this.serverlist$state.runSaveActions();
	}

	@Override
	public @Nullable AbstractWidget serverlist$resourcePackButton() {
		return this.serverlist$resourcePackButton;
	}

	@Override
	public @Nullable AbstractWidget serverlist$cancelButton() {
		return this.serverlist$cancelButton;
	}

	@Override
	public ServerInfoScreenState serverlist$state() {
		return this.serverlist$state;
	}
}
