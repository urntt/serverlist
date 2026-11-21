package com.urntt.serverlist.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.multiplayer.ServerDragHandler;
import com.urntt.serverlist.multiplayer.ServerListScreenExt;
import com.urntt.serverlist.multiplayer.ServerSearchBox;
import com.urntt.serverlist.multiplayer.ServerSelectionListExt;
import com.urntt.serverlist.multiplayer.WorldSwitch;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the search box above the footer buttons, leaves the current world before joining a server from a list
 * that was opened in game, and stores the screen's drag-and-drop state.
 */
@Mixin(JoinMultiplayerScreen.class)
abstract class JoinMultiplayerScreenMixin extends Screen implements ServerListScreenExt {
	@Shadow
	@Final
	private HeaderAndFooterLayout layout;

	@Shadow
	protected ServerSelectionList serverSelectionList;

	@Unique
	private @Nullable EditBox serverlist$searchBox;

	@Unique
	private final ServerDragHandler serverlist$dragHandler = new ServerDragHandler((JoinMultiplayerScreen) (Object) this);

	private JoinMultiplayerScreenMixin(final Component title) {
		super(title);
	}

	/**
	 * Puts the search box at the top of the footer, before vanilla adds its two rows of buttons.
	 */
	@ModifyExpressionValue(
			method = "init",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;addToFooter(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
	private LayoutElement serverlist$addSearchBox(final LayoutElement footer) {
		if (ServerListClient.settings().searchBox() && footer instanceof LinearLayout column) {
			ServerSelectionListExt list = (ServerSelectionListExt) this.serverSelectionList;
			EditBox searchBox = ServerSearchBox.create(this.font, list::serverlist$setQuery);
			column.addChild(searchBox);
			this.layout.setFooterHeight(this.layout.getFooterHeight() + ServerSearchBox.ROW_HEIGHT);
			this.serverlist$searchBox = searchBox;
		}
		return footer;
	}

	@Inject(method = "join", at = @At("HEAD"), cancellable = true)
	private void serverlist$leaveWorldBeforeJoining(final ServerData data, final CallbackInfo ci) {
		if (WorldSwitch.leaveWorldAndJoin(this.minecraft, this, data)) {
			ci.cancel();
		}
	}

	@Override
	public @Nullable EditBox serverlist$searchBox() {
		return this.serverlist$searchBox;
	}

	@Override
	public ServerDragHandler serverlist$dragHandler() {
		return this.serverlist$dragHandler;
	}
}
