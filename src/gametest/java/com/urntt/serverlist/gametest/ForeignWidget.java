package com.urntt.serverlist.gametest;

import net.minecraft.network.chat.Component;

/** Switch and label of the widget added by {@code ForeignServerInfoWidgetMixin}. */
public final class ForeignWidget {
	public static final Component MESSAGE = Component.literal("Foreign widget");
	public static volatile boolean enabled;

	private ForeignWidget() {
	}
}
