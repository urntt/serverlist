package com.urntt.serverlist.multiplayer;

import com.mojang.blaze3d.platform.InputConstants;
import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.mixin.OnlineServerEntryAccessor;
import java.util.List;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import org.jspecify.annotations.Nullable;

/**
 * Drag-and-drop reordering of saved servers on one server list screen.
 *
 * <p>Pressing the left mouse button on a saved server (outside its icon, which holds vanilla's join and move
 * buttons) and moving the cursor up or down starts a drag. While dragging, the server moves to the row under the
 * cursor using vanilla's move, which saves the new order right away. Near the top or bottom edge of the list, the
 * list scrolls.
 *
 * <p>Dragging is off while the search filter is active, and while Essential's own drag-and-drop handler is active
 * on the screen, so two handlers never move the same server.
 */
public final class ServerDragHandler {
	/** Vertical distance the cursor must move before a press becomes a drag. */
	private static final double START_DISTANCE = 4.0;
	/** Width of the server icon, which holds vanilla's join and move buttons. */
	private static final int ICON_SIZE = 32;
	/** Height of the zone at the top and bottom of the list that scrolls it while dragging. */
	private static final int SCROLL_ZONE = 16;
	/** Distance the list scrolls per tick while the cursor is in a scroll zone. */
	private static final double SCROLL_STEP = 8.0;

	private final JoinMultiplayerScreen screen;
	private @Nullable ServerData pressed;
	private double pressY;
	private double cursorY;
	private boolean dragging;

	public ServerDragHandler(final JoinMultiplayerScreen screen) {
		this.screen = screen;
	}

	/** Forgets any press or drag, for example when the screen is shown again. */
	public void reset() {
		this.pressed = null;
		this.dragging = false;
	}

	public void mousePressed(final MouseButtonEvent event) {
		this.reset();
		if (event.button() != InputConstants.MOUSE_BUTTON_LEFT || !this.enabled()) {
			return;
		}

		ServerSelectionList.OnlineServerEntry entry = ServerListActions.savedServerAt(this.list(), event.x(), event.y());
		if (entry != null && event.x() - entry.getContentX() >= ICON_SIZE) {
			this.pressed = entry.getServerData();
			this.pressY = event.y();
		}
	}

	/**
	 * Handles a mouse drag.
	 *
	 * @return whether the drag was consumed
	 */
	public boolean mouseDragged(final MouseButtonEvent event) {
		if (this.pressed == null) {
			return false;
		}

		if (!this.dragging) {
			if (Math.abs(event.y() - this.pressY) < START_DISTANCE) {
				return false;
			}
			this.dragging = true;
		}

		this.cursorY = event.y();
		this.moveTo(this.cursorY);
		return true;
	}

	public void mouseReleased() {
		this.reset();
	}

	/** Scrolls the list while the cursor rests near its top or bottom edge during a drag. */
	public void tick() {
		if (!this.dragging) {
			return;
		}

		ServerSelectionList list = this.list();
		if (this.cursorY < list.getY() + SCROLL_ZONE) {
			list.setScrollAmount(list.scrollAmount() - SCROLL_STEP);
		} else if (this.cursorY > list.getBottom() - SCROLL_ZONE) {
			list.setScrollAmount(list.scrollAmount() + SCROLL_STEP);
		}
		this.moveTo(this.cursorY);
	}

	private boolean enabled() {
		return ServerListClient.settings().dragToReorder()
				&& !ServerFilter.isActive(this.list())
				&& !EssentialCompat.handlesDrag(this.screen);
	}

	private ServerSelectionList list() {
		return ServerListActions.list(this.screen);
	}

	/** Moves the dragged server to the saved-server row under {@code y}, one row at a time. */
	private void moveTo(final double y) {
		ServerSelectionList list = this.list();
		List<ServerSelectionList.Entry> children = list.children();
		int savedCount = 0;
		int from = -1;
		for (ServerSelectionList.Entry child : children) {
			if (!(child instanceof ServerSelectionList.OnlineServerEntry server)) {
				break;
			}
			if (server.getServerData() == this.pressed) {
				from = savedCount;
			}
			savedCount++;
		}

		if (from < 0) {
			this.reset();
			return;
		}

		int target = savedCount - 1;
		for (int row = 0; row < savedCount; row++) {
			if (y < list.getRowBottom(row)) {
				target = row;
				break;
			}
		}

		OnlineServerEntryAccessor dragged = (OnlineServerEntryAccessor) children.get(from);
		while (from != target) {
			int next = from + Integer.signum(target - from);
			dragged.serverlist$swap(from, next);
			from = next;
		}
	}
}
