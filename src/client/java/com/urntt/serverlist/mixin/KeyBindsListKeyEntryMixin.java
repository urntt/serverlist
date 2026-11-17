package com.urntt.serverlist.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.urntt.serverlist.ServerListKeyMappings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps the Controls screen from marking serverlist's server list bindings as conflicting with in-game bindings
 * on the same key. They never act in the same place: the server list bindings only work inside the server list.
 * Conflicts among the server list bindings themselves are still shown.
 */
@Mixin(KeyBindsList.KeyEntry.class)
abstract class KeyBindsListKeyEntryMixin {
	@WrapOperation(method = "refreshEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;same(Lnet/minecraft/client/KeyMapping;)Z"))
	private boolean serverlist$ignoreScreenOnlyConflicts(final KeyMapping key, final KeyMapping other, final Operation<Boolean> original) {
		if (ServerListKeyMappings.isScreenOnly(key) != ServerListKeyMappings.isScreenOnly(other)) {
			return false;
		}
		return original.call(key, other);
	}
}
