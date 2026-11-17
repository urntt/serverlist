package com.urntt.serverlist.gametest.mixin;

import net.minecraft.client.multiplayer.resolver.AddressCheck;
import net.minecraft.client.multiplayer.resolver.ServerAddressResolver;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.client.multiplayer.resolver.ServerRedirectHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerNameResolver.class)
public interface ServerNameResolverInvoker {
	@Invoker("<init>")
	static ServerNameResolver serverlistGametest$create(
			final ServerAddressResolver resolver, final ServerRedirectHandler redirectHandler, final AddressCheck addressCheck) {
		throw new AssertionError();
	}
}
