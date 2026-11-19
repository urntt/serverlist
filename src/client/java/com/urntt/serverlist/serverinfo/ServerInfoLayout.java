package com.urntt.serverlist.serverinfo;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;

/**
 * Computes where the rows of the Add/Edit Server screen's option area and its Done and Cancel buttons go.
 *
 * <p>Done and Cancel sit side by side at the bottom of the screen, centered in a footer of vanilla's usual
 * height, like the footers of vanilla's other screens. The option area between the address field and that footer
 * starts with vanilla's "Server Resource Packs" button, followed by the rows added by other mods. The rules for
 * the option area, in order:
 *
 * <ol>
 *   <li>Start at vanilla's position of the resource pack button, {@code height / 4 + 72}, with rows 4 pixels
 *       apart.</li>
 *   <li>If the rows would reach the footer, start right below the address field instead.</li>
 *   <li>If they still do not fit, make the option area scroll between the address field and the footer.</li>
 * </ol>
 */
public final class ServerInfoLayout {
	/** Vanilla's offset of the resource pack button from a quarter of the screen height. */
	static final int VANILLA_OPTIONS_OFFSET = 72;
	/** Vertical gap between rows, and between the address field and the first row. */
	static final int ROW_SPACING = 4;
	/** Gap kept between the last row and the footer buttons. */
	static final int GAP_BEFORE_FOOTER = 8;
	/** Height of the footer that holds Done and Cancel, as in vanilla's screens with a footer. */
	static final int FOOTER_HEIGHT = HeaderAndFooterLayout.DEFAULT_HEADER_AND_FOOTER_HEIGHT;
	/** Width of each footer button, as in vanilla's footers with two buttons. */
	public static final int FOOTER_BUTTON_WIDTH = Button.DEFAULT_WIDTH;
	/** Gap between the two footer buttons; vanilla's footers use {@code LinearLayout.horizontal().spacing(8)}. */
	public static final int FOOTER_BUTTON_SPACING = 8;

	private ServerInfoLayout() {
	}

	/**
	 * @param screenHeight height of the screen
	 * @param addressBottom bottom edge of the server address field
	 * @param rowHeights heights of the option rows, starting with the resource pack button
	 */
	public static Placement compute(final int screenHeight, final int addressBottom, final List<Integer> rowHeights) {
		int footerY = footerY(screenHeight);
		int bottomLimit = footerY - GAP_BEFORE_FOOTER;
		int contentHeight = contentHeight(rowHeights);
		int minTop = addressBottom + ROW_SPACING;

		int top = Math.max(screenHeight / 4 + VANILLA_OPTIONS_OFFSET, minTop);
		if (top + contentHeight <= bottomLimit) {
			return Placement.stacked(top, rowHeights, footerY);
		}

		if (minTop + contentHeight <= bottomLimit) {
			return Placement.stacked(minTop, rowHeights, footerY);
		}

		int scrollHeight = Math.max(rowHeights.getFirst(), bottomLimit - minTop);
		return new Placement(true, minTop, List.of(), scrollHeight, footerY);
	}

	/** Top edge of Done and Cancel: vertically centered in the footer at the bottom of the screen. */
	public static int footerY(final int screenHeight) {
		return screenHeight - FOOTER_HEIGHT + (FOOTER_HEIGHT - Button.DEFAULT_HEIGHT) / 2;
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
	 * @param footerY top edge of Done and Cancel
	 */
	public record Placement(boolean scrolling, int top, List<Integer> rowYs, int scrollHeight, int footerY) {
		static Placement stacked(final int top, final List<Integer> rowHeights, final int footerY) {
			List<Integer> rowYs = new ArrayList<>(rowHeights.size());
			int y = top;
			for (int rowHeight : rowHeights) {
				rowYs.add(y);
				y += rowHeight + ROW_SPACING;
			}
			return new Placement(false, top, List.copyOf(rowYs), 0, footerY);
		}
	}
}
