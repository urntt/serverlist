package com.urntt.serverlist.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.ping.LiteralAddresses;
import java.util.Optional;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddressResolver;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Gives resolved literal IP addresses their literal as host name before vanilla checks them against the block
 * list, which would otherwise perform a reverse DNS lookup. Used for both server list pings and connections.
 */
@Mixin(ServerNameResolver.class)
abstract class ServerNameResolverMixin {
	@WrapOperation(
			method = "resolveAddress",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/multiplayer/resolver/ServerAddressResolver;resolve(Lnet/minecraft/client/multiplayer/resolver/ServerAddress;)Ljava/util/Optional;"))
	private Optional<ResolvedServerAddress> serverlist$skipReverseDns(
			final ServerAddressResolver resolver, final ServerAddress address, final Operation<Optional<ResolvedServerAddress>> original) {
		Optional<ResolvedServerAddress> resolved = original.call(resolver, address);
		if (!ServerListClient.settings().skipReverseDnsForIpAddresses()) {
			return resolved;
		}
		return resolved.map(result -> LiteralAddresses.withLiteralHostName(address.getHost(), result));
	}
}
