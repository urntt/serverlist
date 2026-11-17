package com.urntt.serverlist.gametest.mixin;

import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Simulates a mod that adds a widget to the server info screen without serverlist's API, positioning it by
 * hand the way such mods do: it moves the resource pack button up and puts its own button below it, on top of
 * vanilla's Done button. Enabled only by the tests that need it.
 */
@Mixin(ManageServerScreen.class)
abstract class ForeignServerInfoWidgetMixin extends Screen {
	@Unique
	private static final int SHIFT = 16;

	private ForeignServerInfoWidgetMixin(final Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void serverlistGametest$addForeignWidget(final CallbackInfo ci) {
		if (!com.urntt.serverlist.gametest.ForeignWidget.enabled) {
			return;
		}

		List<? extends AbstractWidget> cycleButtons = this.children().stream()
				.filter(CycleButton.class::isInstance).map(AbstractWidget.class::cast).toList();
		AbstractWidget resourcePacks = cycleButtons.getFirst();
		resourcePacks.setY(resourcePacks.getY() - SHIFT);
		this.addRenderableWidget(Button.builder(com.urntt.serverlist.gametest.ForeignWidget.MESSAGE, button -> { })
				.bounds(this.width / 2 - 100, resourcePacks.getY() + 24, 200, 20)
				.build());
	}
}
