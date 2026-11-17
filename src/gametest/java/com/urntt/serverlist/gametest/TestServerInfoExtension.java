package com.urntt.serverlist.gametest;

import com.urntt.serverlist.api.ServerInfoScreenContext;
import com.urntt.serverlist.api.ServerInfoScreenExtension;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * A mod that uses serverlist's API: one full-width row and one row with two buttons. Enabled only by the tests
 * that need it.
 */
public final class TestServerInfoExtension implements ServerInfoScreenExtension {
	public static final Component FULL_ROW = Component.literal("API full row");
	public static final Component LEFT = Component.literal("API left");
	public static final Component RIGHT = Component.literal("API right");

	public static volatile boolean enabled;
	public static volatile int saves;

	@Override
	public void init(final ServerInfoScreenContext context) {
		if (!enabled) {
			return;
		}
		context.addRow(Button.builder(FULL_ROW, button -> { }).build());
		context.addRow(Button.builder(LEFT, button -> { }).build(), Button.builder(RIGHT, button -> { }).build());
		context.onSave(() -> saves++);
	}
}
