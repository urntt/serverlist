package com.urntt.serverlist.multiplayer;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;

/**
 * Attaches serverlist's input handling to every server list screen.
 *
 * <p>Fabric runs {@link ScreenEvents#AFTER_INIT} and recreates the per-screen events every time a screen is shown
 * or resized, including when the player returns to an existing server list from the Edit, Delete, or Add screen
 * (which does not run the screen's {@code init()} again). Attaching here keeps the handlers working in all these
 * cases.
 */
public final class ServerListScreenHooks {
	private ServerListScreenHooks() {
	}

	public static void register() {
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (screen instanceof JoinMultiplayerScreen serverList) {
				attach(serverList);
			}
		});
	}

	private static void attach(final JoinMultiplayerScreen screen) {
		ServerDragHandler drag = ((ServerListScreenExt) screen).serverlist$dragHandler();
		drag.reset();

		ScreenMouseEvents.allowMouseClick(screen).register((s, event) -> {
			if (ServerListActions.mousePressed(screen, event)) {
				return false;
			}
			drag.mousePressed(event);
			return true;
		});
		ScreenMouseEvents.allowMouseDrag(screen).register((s, event, dx, dy) -> !drag.mouseDragged(event));
		ScreenMouseEvents.allowMouseRelease(screen).register((s, event) -> {
			drag.mouseReleased();
			return true;
		});
		ScreenEvents.afterTick(screen).register(s -> drag.tick());
		ScreenKeyboardEvents.allowKeyPress(screen).register((s, event) -> !ServerListActions.keyPressed(screen, event));
	}
}
