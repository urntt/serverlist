package com.urntt.serverlist.serverinfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes where the rows of the Add/Edit Server screen's option area and its Done and Cancel buttons go.
 *
 * <p>The option area starts with vanilla's "Server Resource Packs" button, followed by the rows added by other
 * mods. The rules, in order:
 *
 * <ol>
 *   <li>Use vanilla's positions and spacing: rows from {@code height / 4 + 72}, 4 pixels apart, Done 22 pixels
 *       below the last row but no higher than vanilla puts it, Cancel 24 pixels below Done.</li>
 *   <li>If Cancel would leave the screen, start right below the address field and leave only a small gap
 *       before Done.</li>
 *   <li>If it still does not fit, pin Done and Cancel to the bottom of the screen and make the option area
 *       scroll.</li>
 * </ol>
 */
public final class ServerInfoLayout {
	/** Vanilla's offset of the resource pack button from a quarter of the screen height. */
	static final int VANILLA_OPTIONS_OFFSET = 72;
	/** Vanilla's offset of the Done button from a quarter of the screen height. */
	static final int VANILLA_DONE_OFFSET = 114;
	/** Vertical gap between rows. */
	static final int ROW_SPACING = 4;
	/** Vanilla's gap between the resource pack button and Done. */
	static final int VANILLA_GAP_BEFORE_DONE = 22;
	/** Gap between the option area and Done when space is short. */
	static final int COMPACT_GAP_BEFORE_DONE = 8;
	/** Distance from Done to Cancel. */
	static final int BUTTON_PITCH = 24;
	static final int BUTTON_HEIGHT = 20;
	/** Space kept free below Cancel. */
	static final int SCREEN_MARGIN = 4;

	private ServerInfoLayout() {
	}

	/**
	 * @param screenHeight height of the screen
	 * @param addressBottom bottom edge of the server address field
	 * @param rowHeights heights of the option rows, starting with the resource pack button
	 */
	public static Placement compute(final int screenHeight, final int addressBottom, final List<Integer> rowHeights) {
		int contentHeight = contentHeight(rowHeights);
		int vanillaTop = screenHeight / 4 + VANILLA_OPTIONS_OFFSET;
		int minTop = addressBottom + ROW_SPACING;
		int bottomLimit = screenHeight - SCREEN_MARGIN;

		int top = Math.max(vanillaTop, minTop);
		int done = Math.max(screenHeight / 4 + VANILLA_DONE_OFFSET, top + contentHeight + VANILLA_GAP_BEFORE_DONE);
		if (done + BUTTON_PITCH + BUTTON_HEIGHT <= bottomLimit) {
			return Placement.stacked(top, rowHeights, done);
		}

		top = minTop;
		done = top + contentHeight + COMPACT_GAP_BEFORE_DONE;
		if (done + BUTTON_PITCH + BUTTON_HEIGHT <= bottomLimit) {
			return Placement.stacked(top, rowHeights, done);
		}

		done = bottomLimit - BUTTON_HEIGHT - BUTTON_PITCH;
		int scrollHeight = Math.max(rowHeights.getFirst(), done - COMPACT_GAP_BEFORE_DONE - top);
		return new Placement(true, top, List.of(), scrollHeight, done, done + BUTTON_PITCH);
	}

	private static int contentHeight(final List<Integer> rowHeights) {
		int height = 0;
		for (int rowHeight : rowHeights) {
			height += rowHeight;
		}
		return height + ROW_SPACING * (rowHeights.size() - 1);
	}

	/**
	 * The computed positions.
	 *
	 * @param scrolling whether the option area scrolls
	 * @param top top edge of the option area
	 * @param rowYs top edge of each row; empty when scrolling
	 * @param scrollHeight height of the scrolling option area; 0 when not scrolling
	 * @param doneY top edge of Done
	 * @param cancelY top edge of Cancel
	 */
	public record Placement(boolean scrolling, int top, List<Integer> rowYs, int scrollHeight, int doneY, int cancelY) {
		static Placement stacked(final int top, final List<Integer> rowHeights, final int doneY) {
			List<Integer> rowYs = new ArrayList<>(rowHeights.size());
			int y = top;
			for (int rowHeight : rowHeights) {
				rowYs.add(y);
				y += rowHeight + ROW_SPACING;
			}
			return new Placement(false, top, List.copyOf(rowYs), 0, doneY, doneY + BUTTON_PITCH);
		}
	}
}
