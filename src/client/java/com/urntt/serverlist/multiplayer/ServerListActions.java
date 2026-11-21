package com.urntt.serverlist.multiplayer;

import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.ServerListKeyMappings;
import com.urntt.serverlist.mixin.JoinMultiplayerScreenAccessor;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.Nullable;

/**
 * The server list's edit and delete bindings, and the search shortcut.
 *
 * <p>Each binding does exactly what pressing the vanilla button would do, including its confirmation screen. A
 * mouse binding acts on the server under the cursor, which it selects first; a keyboard binding acts on the
 * selected server.
 */
public final class ServerListActions {
	/** Key code of the letter F, which focuses the search box together with Ctrl (Cmd on macOS). */
	private static final int SEARCH_SHORTCUT_KEY = 'f';

	private enum Action {
		EDIT(ServerListKeyMappings::edit, JoinMultiplayerScreenAccessor::serverlist$editButton),
		DELETE(ServerListKeyMappings::delete, JoinMultiplayerScreenAccessor::serverlist$deleteButton);

		private final Supplier<KeyMapping> mapping;
		private final Function<JoinMultiplayerScreenAccessor, Button> button;

		Action(final Supplier<KeyMapping> mapping, final Function<JoinMultiplayerScreenAccessor, Button> button) {
			this.mapping = mapping;
			this.button = button;
		}
	}

	private ServerListActions() {
	}

	/**
	 * Handles a mouse press on the server list.
	 *
	 * @return whether the press was consumed
	 */
	public static boolean mousePressed(final JoinMultiplayerScreen screen, final MouseButtonEvent event) {
		if (!ServerListClient.settings().shortcuts()) {
			return false;
		}
		ServerSelectionList list = list(screen);
		for (Action action : Action.values()) {
			if (action.mapping.get().matchesMouse(event)) {
				ServerSelectionList.OnlineServerEntry entry = savedServerAt(list, event.x(), event.y());
				if (entry != null) {
					list.setSelected(entry);
					return press(screen, action, event);
				}
			}
		}
		return false;
	}

	/**
	 * Handles a key press on the server list.
	 *
	 * @return whether the press was consumed
	 */
	public static boolean keyPressed(final JoinMultiplayerScreen screen, final KeyEvent event) {
		EditBox searchBox = ((ServerListScreenExt) screen).serverlist$searchBox();
		if (searchBox != null && event.shortcutKey() == SEARCH_SHORTCUT_KEY && event.hasControlDownWithQuirk()) {
			screen.setFocused(searchBox);
			return true;
		}

		if (!ServerListClient.settings().shortcuts() || screen.getFocused() instanceof EditBox) {
			return false;
		}

		for (Action action : Action.values()) {
			if (action.mapping.get().matches(event) && list(screen).getSelected() instanceof ServerSelectionList.OnlineServerEntry) {
				return press(screen, action, event);
			}
		}
		return false;
	}

	/** The saved server under the given point of the list, if any. */
	public static ServerSelectionList.@Nullable OnlineServerEntry savedServerAt(final ServerSelectionList list, final double x, final double y) {
		if (!list.isMouseOver(x, y)) {
			return null;
		}
		return list.getChildAt(x, y).orElse(null) instanceof ServerSelectionList.OnlineServerEntry entry ? entry : null;
	}

	static ServerSelectionList list(final JoinMultiplayerScreen screen) {
		return ((JoinMultiplayerScreenAccessor) screen).serverlist$serverSelectionList();
	}

	private static boolean press(final JoinMultiplayerScreen screen, final Action action, final InputWithModifiers input) {
		Button button = action.button.apply((JoinMultiplayerScreenAccessor) screen);
		if (!button.active || !button.visible) {
			return false;
		}
		button.playDownSound(Minecraft.getInstance().getSoundManager());
		button.onPress(input);
		return true;
	}
}
