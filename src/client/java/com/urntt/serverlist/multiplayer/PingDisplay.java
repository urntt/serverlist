package com.urntt.serverlist.multiplayer;

import com.urntt.serverlist.mixin.OnlineServerEntryAccessor;
import com.urntt.serverlist.mixin.ServerSelectionListAccessor;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Draws a saved server's latency in milliseconds to the left of its player count.
 *
 * <p>The color follows the signal bars vanilla has already chosen for the server, so the thresholds stay
 * vanilla's.
 */
public final class PingDisplay {
	/** Horizontal gap vanilla leaves between the status icon, the player count, and other text. */
	private static final int SPACING = 5;
	/** Width of vanilla's status icon. */
	private static final int STATUS_ICON_WIDTH = 10;
	/** Vertical offset of the first text line inside an entry, as vanilla draws the name and player count. */
	private static final int TEXT_Y_OFFSET = 1;
	/** Vanilla's color for the secondary text of an entry, used when the latency has no color of its own. */
	private static final int TEXT_COLOR = 0xFF808080;

	private static @Nullable Map<Identifier, ChatFormatting> colors;
	private static @Nullable Set<Identifier> moveSprites;

	private PingDisplay() {
	}

	public static void draw(
			final GuiGraphicsExtractor graphics, final Font font, final ServerSelectionList.OnlineServerEntry entry, final ServerData server) {
		if (server.state() != ServerData.State.SUCCESSFUL) {
			return;
		}

		Identifier statusIcon = ((OnlineServerEntryAccessor) entry).serverlist$statusIcon();
		ChatFormatting color = colors().getOrDefault(statusIcon, ChatFormatting.GRAY);
		Component latency = Component.translatable("multiplayer.status.ping", server.ping).withStyle(color);

		int statusIconX = entry.getContentRight() - STATUS_ICON_WIDTH - SPACING;
		int statusWidth = font.width(server.status);
		int statusX = statusIconX - statusWidth - SPACING;
		int right = statusWidth > 0 ? statusX - SPACING : statusX;
		graphics.text(font, latency, right - font.width(latency), entry.getContentY() + TEXT_Y_OFFSET, TEXT_COLOR);
	}

	/** Whether {@code sprite} is one of vanilla's move arrows on a server icon. */
	public static boolean isMoveSprite(final Identifier sprite) {
		if (moveSprites == null) {
			moveSprites = Set.of(
					ServerSelectionListAccessor.serverlist$moveUpSprite(),
					ServerSelectionListAccessor.serverlist$moveUpHighlightedSprite(),
					ServerSelectionListAccessor.serverlist$moveDownSprite(),
					ServerSelectionListAccessor.serverlist$moveDownHighlightedSprite());
		}
		return moveSprites.contains(sprite);
	}

	private static Map<Identifier, ChatFormatting> colors() {
		if (colors == null) {
			colors = Map.of(
					ServerSelectionListAccessor.serverlist$ping5Sprite(), ChatFormatting.GREEN,
					ServerSelectionListAccessor.serverlist$ping4Sprite(), ChatFormatting.YELLOW,
					ServerSelectionListAccessor.serverlist$ping3Sprite(), ChatFormatting.GOLD,
					ServerSelectionListAccessor.serverlist$ping2Sprite(), ChatFormatting.RED,
					ServerSelectionListAccessor.serverlist$ping1Sprite(), ChatFormatting.DARK_RED);
		}
		return colors;
	}
}
