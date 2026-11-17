package com.urntt.serverlist.gametest.mixin;

import com.urntt.serverlist.gametest.SlowServers;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.server.network.EventLoopGroupHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes pings to {@link SlowServers} hold their pinger thread until the test releases them, then drops them.
 */
@Mixin(ServerStatusPinger.class)
abstract class SlowServerPingMixin {
	@Unique
	private static final long MAX_WAIT_SECONDS = 60;

	@Inject(method = "pingServer", at = @At("HEAD"), cancellable = true)
	private void serverlistGametest$holdSlowServer(
			final ServerData data, final Runnable onPersistentDataChange, final Runnable onPongResponse,
			final EventLoopGroupHolder eventLoopGroupHolder, final CallbackInfo ci) throws InterruptedException {
		if (data.ip.endsWith(SlowServers.HOST_SUFFIX)) {
			SlowServers.gate().await(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
			ci.cancel();
		}
	}
}
