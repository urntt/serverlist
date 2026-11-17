package com.urntt.serverlist.serverinfo;

import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;

/**
 * One row of the Add/Edit Server screen's option area.
 *
 * @param widgets the widgets from left to right: one or two
 * @param sized whether serverlist sets the widths (rows added through the API) or keeps the widgets' own size
 *     (vanilla's button and widgets adopted from mods that do not use the API)
 */
record ServerInfoRow(List<AbstractWidget> widgets, boolean sized) {
	static ServerInfoRow ownSize(final AbstractWidget widget) {
		return new ServerInfoRow(List.of(widget), false);
	}

	int height() {
		int height = 0;
		for (AbstractWidget widget : this.widgets) {
			height = Math.max(height, widget.getHeight());
		}
		return height;
	}
}
