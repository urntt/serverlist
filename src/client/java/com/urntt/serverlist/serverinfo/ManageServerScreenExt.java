package com.urntt.serverlist.serverinfo;

import net.minecraft.client.gui.components.AbstractWidget;
import org.jspecify.annotations.Nullable;

/**
 * State that serverlist adds to {@link net.minecraft.client.gui.screens.ManageServerScreen}. Implemented by a
 * mixin.
 */
public interface ManageServerScreenExt {
	/** Vanilla's "Server Resource Packs" button of the current widgets. */
	@Nullable AbstractWidget serverlist$resourcePackButton();

	/** Vanilla's Cancel button of the current widgets. */
	@Nullable AbstractWidget serverlist$cancelButton();

	ServerInfoScreenState serverlist$state();
}
