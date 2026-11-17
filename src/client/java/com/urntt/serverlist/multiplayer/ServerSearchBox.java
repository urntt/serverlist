package com.urntt.serverlist.multiplayer;

import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * The search box shown above the server list's footer buttons.
 */
public final class ServerSearchBox {
	/** Width of the footer's top row of buttons: three 100-pixel buttons with 4 pixels between them. */
	public static final int WIDTH = 308;
	public static final int HEIGHT = 20;
	/** Spacing between the rows of vanilla's footer. */
	private static final int FOOTER_SPACING = 4;
	/** Extra footer height taken by the search box row. */
	public static final int ROW_HEIGHT = HEIGHT + FOOTER_SPACING;

	public static final Component NARRATION = Component.translatable("serverlist.search");
	public static final Component HINT = Component.translatable("serverlist.search.hint").withStyle(EditBox.SEARCH_HINT_STYLE);

	private ServerSearchBox() {
	}

	public static EditBox create(final Font font, final Consumer<String> onQueryChanged) {
		EditBox box = new EditBox(font, WIDTH, HEIGHT, NARRATION);
		box.setHint(HINT);
		box.setResponder(onQueryChanged);
		return box;
	}
}
