package com.urntt.serverlist.multiplayer;

import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.ServerListKeyMappings;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;

/**
 * Opening the server list while playing, and joining a server from it.
 */
public final class WorldSwitch {
	private WorldSwitch() {
	}

	/**
	 * Opens the server list when the open key is pressed in game. "Back" returns to the game.
	 */
	public static void registerOpenKey() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (ServerListKeyMappings.open().consumeClick()) {
				if (ServerListClient.settings().shortcuts() && client.gui.screen() == null && client.level != null) {
					client.gui.setScreen(new JoinMultiplayerScreen(null));
				}
			}
		});
	}

	/**
	 * Joins {@code server} from a server list that was opened while playing: leaves the current world the way
	 * the pause menu's Disconnect button does (saving a singleplayer world), then connects. If the connection
	 * fails, "Back" leads to a fresh server list and the title screen, as after a normal disconnect.
	 *
	 * @return whether the join was handled; {@code false} when no world is open and vanilla should join as usual
	 */
	public static boolean leaveWorldAndJoin(final Minecraft minecraft, final Screen serverList, final ServerData server) {
		if (minecraft.level == null) {
			return false;
		}

		minecraft.getReportingContext().draftReportHandled(minecraft, serverList, () -> {
			minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE);
			new JoinMultiplayerScreen(new TitleScreen()).join(server);
		}, true);
		return true;
	}
}
