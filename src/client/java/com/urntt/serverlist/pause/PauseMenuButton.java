package com.urntt.serverlist.pause;

import com.urntt.serverlist.ServerListClient;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The pause menu button that opens the server list. It sits at the end of the pause menu's row of icon buttons
 * and uses vanilla's "Multiplayer" text as its tooltip.
 */
public final class PauseMenuButton {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerListClient.MOD_ID);
	public static final Component MESSAGE = Component.translatable("menu.multiplayer");
	public static final Identifier SPRITE = ServerListClient.id("pause_menu/multiplayer");
	/** Size of the vanilla pause menu icon buttons and of their sprites. */
	private static final int BUTTON_SIZE = 20;
	private static final int SPRITE_SIZE = 15;

	private PauseMenuButton() {
	}

	/**
	 * Adds the button to the first row of icon buttons in the pause menu's grid.
	 */
	public static void addTo(final GridLayout pauseMenu, final PauseScreen pauseScreen) {
		AtomicReference<LinearLayout> iconRow = new AtomicReference<>();
		pauseMenu.visitChildren(child -> {
			if (iconRow.get() == null && child instanceof LinearLayout row) {
				iconRow.set(row);
			}
		});

		if (iconRow.get() == null) {
			LOGGER.warn("The pause menu has no row of icon buttons; not adding the server list button");
			return;
		}

		iconRow.get().addChild(create(pauseScreen));
	}

	private static SpriteIconButton create(final PauseScreen pauseScreen) {
		return SpriteIconButton.builder(
						MESSAGE,
						button -> Minecraft.getInstance().gui.setScreen(new JoinMultiplayerScreen(pauseScreen)),
						true)
				.width(BUTTON_SIZE)
				.sprite(SPRITE, SPRITE_SIZE, SPRITE_SIZE)
				.withTootip()
				.build();
	}
}
