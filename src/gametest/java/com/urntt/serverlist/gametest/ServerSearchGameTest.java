package com.urntt.serverlist.gametest;

import static com.urntt.serverlist.gametest.TestSupport.NAMES;
import static com.urntt.serverlist.gametest.TestSupport.assertEquals;
import static com.urntt.serverlist.gametest.TestSupport.assertTrue;
import static com.urntt.serverlist.gametest.TestSupport.click;
import static com.urntt.serverlist.gametest.TestSupport.dragServer;
import static com.urntt.serverlist.gametest.TestSupport.iconPoint;
import static com.urntt.serverlist.gametest.TestSupport.savedNames;
import static com.urntt.serverlist.gametest.TestSupport.textPoint;
import static com.urntt.serverlist.gametest.TestSupport.visibleNames;

import com.mojang.blaze3d.platform.InputConstants;
import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.mixin.ManageServerScreenAccessor;
import com.urntt.serverlist.multiplayer.ServerListScreenExt;
import com.urntt.serverlist.multiplayer.ServerSearchBox;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.sdl.SDLKeyboard;

/**
 * The search box, its filter, and the reorder lock while filtering.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ServerSearchGameTest implements FabricClientGameTest {
	/** Vanilla's footer height on the server list. */
	private static final int VANILLA_FOOTER_HEIGHT = 60;
	/** Saved order after the first drag, which every later step must keep. */
	private static final List<String> REORDERED = List.of("Bravo", "Alpha", "Charlie", "Delta", "Echo");

	@Override
	public void runTest(final ClientGameTestContext context) {
		TestSupport.resetConfig();
		FakeEssential.dragHandler = null;
		int[] originalWindow = TestSupport.useRoomyWindow(context);
		TestSupport.saveServers(context, NAMES);
		JoinMultiplayerScreen screen = TestSupport.openServerList(context);
		EditBox searchBox = context.computeOnClient(client -> ((ServerListScreenExt) screen).serverlist$searchBox());
		assertTrue(searchBox != null, "the server list has no search box");
		assertFooterHeight(context, screen, VANILLA_FOOTER_HEIGHT + ServerSearchBox.ROW_HEIGHT);
		context.takeScreenshot("server_list_search_empty");

		// Reorder before searching: the search rebuilds the visible entries, which must keep the new order.
		dragServer(context, screen, "Alpha", "Bravo");
		assertEquals(REORDERED, savedNames(context), "saved order after dragging Alpha onto Bravo");

		pressWithModifiers(context, InputConstants.getKey("key.keyboard.f"), InputQuirks.EDIT_SHORTCUT_KEY_MODIFIER);
		assertTrue(context.computeOnClient(client -> screen.getFocused() == searchBox), "Ctrl+F does not focus the search box");

		context.getInput().typeChars("e");
		context.waitTick();
		assertEquals(List.of("Charlie", "Delta", "Echo"), visibleNames(context, screen), "servers matching 'e'");
		assertTrue(context.computeOnClient(client -> TestSupport.list(screen).children().stream()
				.anyMatch(ServerSelectionList.LANHeader.class::isInstance)), "the LAN section is hidden by the filter");
		context.takeScreenshot("server_list_search_filtered");

		testReorderLock(context, screen);
		testActionsWhileFiltered(context, screen);

		context.runOnClient(client -> searchBox.setValue(""));
		assertEquals(REORDERED, visibleNames(context, screen), "shown order after clearing the search");

		dragServer(context, screen, "Alpha", "Bravo");
		assertEquals(NAMES, savedNames(context), "dragging works again after clearing the search");

		ServerListClient.config().setSearchBox(false);
		JoinMultiplayerScreen plain = TestSupport.openServerList(context);
		assertTrue(context.computeOnClient(client -> ((ServerListScreenExt) plain).serverlist$searchBox() == null), "search box shown although disabled");
		assertFooterHeight(context, plain, VANILLA_FOOTER_HEIGHT);
		ServerListClient.config().setSearchBox(true);
		context.setScreen(TitleScreen::new);
		TestSupport.restoreWindow(context, originalWindow);
	}

	/** While filtering, no way of moving a server changes the saved order. */
	private static void testReorderLock(final ClientGameTestContext context, final JoinMultiplayerScreen screen) {
		dragServer(context, screen, "Charlie", "Echo");
		assertEquals(REORDERED, savedNames(context), "saved order after dragging while filtered");

		// One move at a time: a move up followed by a move down would cancel out and hide a move.
		click(context, iconPoint(context, screen, "Delta", false), InputConstants.MOUSE_BUTTON_LEFT);
		assertEquals(REORDERED, savedNames(context), "saved order after clicking the move up arrow while filtered");

		click(context, textPoint(context, screen, "Delta"), InputConstants.MOUSE_BUTTON_LEFT);
		pressWithModifiers(context, InputConstants.getKey("key.keyboard.down"), InputConstants.MOD_SHIFT);
		assertEquals(REORDERED, savedNames(context), "saved order after Shift+Down while filtered");
		assertEquals(List.of("Charlie", "Delta", "Echo"), visibleNames(context, screen), "shown order while filtered");
	}

	private static void testActionsWhileFiltered(final ClientGameTestContext context, final JoinMultiplayerScreen screen) {
		click(context, textPoint(context, screen, "Echo"), InputConstants.MOUSE_BUTTON_RIGHT);
		context.waitForScreen(ManageServerScreen.class);
		String name = context.computeOnClient(client ->
				((ManageServerScreenAccessor) client.gui.screen()).serverlist$nameEdit().getValue());
		assertEquals("Echo", name, "server opened by right-click while filtered");
		context.clickScreenButton("gui.cancel");
		context.waitForScreen(JoinMultiplayerScreen.class);
		assertEquals(List.of("Charlie", "Delta", "Echo"), visibleNames(context, screen), "filter after returning from Edit");
	}

	/**
	 * Presses {@code key} with modifier keys held. The test input API sends key events without modifiers, so the
	 * event is sent to the keyboard handler directly.
	 */
	private static void pressWithModifiers(final ClientGameTestContext context, final InputConstants.Key key, final int modifiers) {
		context.runOnClient(client -> {
			KeyEvent event = new KeyEvent(key.getValue(), SDLKeyboard.SDL_GetKeyFromScancode(key.getValue(), (short) 0, false), modifiers);
			client.keyboardHandler.keyPress(client.getWindow().handle(), InputConstants.PRESS, event);
			client.keyboardHandler.keyPress(client.getWindow().handle(), InputConstants.RELEASE, event);
		});
		context.waitTick();
	}

	private static void assertFooterHeight(final ClientGameTestContext context, final JoinMultiplayerScreen screen, final int expected) {
		int footer = context.computeOnClient(client -> screen.height - TestSupport.list(screen).getBottom());
		assertEquals(expected, footer, "footer height");
	}
}
