package com.urntt.serverlist.api;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;

/**
 * What an {@link ServerInfoScreenExtension} can do on the Add Server or Edit Server Info screen.
 *
 * <p>Rows are placed below the vanilla "Server Resource Packs" button, in the order they are added, and above
 * the Done and Cancel buttons, which sit side by side at the bottom of the screen. serverlist sets their position
 * and width; widgets should be 20 pixels high like vanilla buttons. When the rows do not fit on the screen, the
 * area scrolls.
 */
public interface ServerInfoScreenContext {
	/** Width of a widget that fills a row on its own. */
	int FULL_WIDTH = 200;

	/** Width of each of two widgets that share a row. */
	int HALF_WIDTH = 98;

	/**
	 * The screen being set up.
	 */
	Screen screen();

	/**
	 * The server being edited. On the Edit Server Info screen this is vanilla's working copy, which is copied
	 * into the saved server list when the player presses Done and discarded on Cancel. Widgets may change it
	 * directly.
	 */
	ServerData serverData();

	/**
	 * Adds a row holding one widget, resized to {@link #FULL_WIDTH}.
	 *
	 * @return {@code widget}
	 */
	<T extends AbstractWidget> T addRow(T widget);

	/**
	 * Adds a row holding two widgets side by side, each resized to {@link #HALF_WIDTH}.
	 */
	void addRow(AbstractWidget left, AbstractWidget right);

	/**
	 * Registers an action that runs when the player presses Done, after vanilla has copied the name and address
	 * fields into {@link #serverData()} and before the server list saves it. Use it to commit state kept
	 * outside {@link net.minecraft.client.multiplayer.ServerData}. Nothing runs when the player cancels.
	 */
	void onSave(Runnable action);
}
