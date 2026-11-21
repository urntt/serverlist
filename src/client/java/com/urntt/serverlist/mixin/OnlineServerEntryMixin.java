package com.urntt.serverlist.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.multiplayer.PingDisplay;
import com.urntt.serverlist.multiplayer.ServerFilter;
import com.urntt.serverlist.multiplayer.ServerSelectionListExt;
import com.urntt.serverlist.ping.PingerThreads;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SelectableEntry;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Changes to saved server entries:
 *
 * <ul>
 *   <li>While the search filter is active, the entry cannot be moved: the move arrows are hidden, clicks on them
 *       and Shift+arrow keys do nothing, and clicks never reach other mods' drag-and-drop handlers, because they
 *       all treat the visible position as the saved position.</li>
 *   <li>Vanilla's move keeps the list of saved entries in sync with the visible order, so rebuilding the visible
 *       entries (for example when a LAN world is found or the filter changes) does not undo a move.</li>
 *   <li>The latency is shown next to the player count.</li>
 *   <li>The pinger thread pool is adjusted before each ping, so the "faster server pings" setting applies
 *       immediately.</li>
 * </ul>
 */
@Mixin(ServerSelectionList.OnlineServerEntry.class)
abstract class OnlineServerEntryMixin {
	/** Size of the server icon, which also holds the join and move buttons. */
	@Unique
	private static final int ICON_SIZE = 32;

	@Shadow
	@Final
	private JoinMultiplayerScreen screen;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Final
	private ServerData serverData;

	@Shadow
	public abstract void join();

	@Unique
	private boolean serverlist$isFiltered() {
		return ServerFilter.isActive(((JoinMultiplayerScreenAccessor) this.screen).serverlist$serverSelectionList());
	}

	/**
	 * Handles clicks itself while the filter is active, doing only what vanilla does apart from moving: joining
	 * from the icon or on a double click, and selecting the entry.
	 */
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void serverlist$clickWithoutMoving(final MouseButtonEvent event, final boolean doubleClick, final CallbackInfoReturnable<Boolean> cir) {
		if (!this.serverlist$isFiltered()) {
			return;
		}

		ServerSelectionList.OnlineServerEntry entry = (ServerSelectionList.OnlineServerEntry) (Object) this;
		int relX = (int) event.x() - entry.getContentX();
		int relY = (int) event.y() - entry.getContentY();
		if (((SelectableEntry) this).mouseOverRightHalf(relX, relY, ICON_SIZE) || doubleClick) {
			this.join();
		}
		cir.setReturnValue(true);
	}

	@WrapWithCondition(
			method = "keyPressed",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/multiplayer/ServerSelectionList$OnlineServerEntry;swap(II)V"))
	private boolean serverlist$noKeyboardMoveWhileFiltered(final ServerSelectionList.OnlineServerEntry entry, final int currentIndex, final int newIndex) {
		return !this.serverlist$isFiltered();
	}

	@Inject(method = "swap", at = @At("TAIL"))
	private void serverlist$keepSavedEntriesInOrder(final int currentIndex, final int newIndex, final CallbackInfo ci) {
		ServerSelectionList list = ((JoinMultiplayerScreenAccessor) this.screen).serverlist$serverSelectionList();
		Collections.swap(((ServerSelectionListExt) list).serverlist$onlineServers(), currentIndex, newIndex);
	}

	@WrapWithCondition(
			method = "extractContent",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
	private boolean serverlist$hideMoveArrowsWhileFiltered(
			final GuiGraphicsExtractor graphics, final RenderPipeline pipeline, final Identifier sprite,
			final int x, final int y, final int width, final int height) {
		return !(PingDisplay.isMoveSprite(sprite) && this.serverlist$isFiltered());
	}

	@WrapOperation(
			method = "extractContent",
			at = @At(value = "INVOKE", target = "Ljava/util/concurrent/ThreadPoolExecutor;submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;"))
	private Future<?> serverlist$configurePingerThreads(final ThreadPoolExecutor pool, final Runnable task, final Operation<Future<?>> original) {
		PingerThreads.configure(pool, ServerListClient.settings().fasterServerPings());
		return original.call(pool, task);
	}

	@Inject(method = "extractContent", at = @At("TAIL"))
	private void serverlist$drawLatency(
			final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final boolean hovered, final float a, final CallbackInfo ci) {
		if (ServerListClient.settings().showPingMs()) {
			PingDisplay.draw(graphics, this.minecraft.font, (ServerSelectionList.OnlineServerEntry) (Object) this, this.serverData);
		}
	}
}
