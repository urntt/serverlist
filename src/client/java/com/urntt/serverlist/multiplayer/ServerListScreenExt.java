package com.urntt.serverlist.multiplayer;

import net.minecraft.client.gui.components.EditBox;
import org.jspecify.annotations.Nullable;

/**
 * State that serverlist adds to {@link net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen}.
 * Implemented by a mixin.
 */
public interface ServerListScreenExt {
	/** The search box, or {@code null} when the search box is disabled. */
	@Nullable EditBox serverlist$searchBox();

	/** The drag-and-drop state of this screen instance. */
	ServerDragHandler serverlist$dragHandler();
}
