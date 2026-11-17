package com.urntt.serverlist.gametest;

import static com.urntt.serverlist.gametest.TestSupport.LOGGER;
import static com.urntt.serverlist.gametest.TestSupport.NAMES;
import static com.urntt.serverlist.gametest.TestSupport.assertEquals;
import static com.urntt.serverlist.gametest.TestSupport.assertTrue;
import static com.urntt.serverlist.gametest.TestSupport.click;
import static com.urntt.serverlist.gametest.TestSupport.currentScreen;
import static com.urntt.serverlist.gametest.TestSupport.dragServer;
import static com.urntt.serverlist.gametest.TestSupport.savedNames;
import static com.urntt.serverlist.gametest.TestSupport.textPoint;
import static com.urntt.serverlist.gametest.TestSupport.visibleNames;

import com.mojang.blaze3d.platform.InputConstants;
import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.ServerListKeyMappings;
import com.urntt.serverlist.gametest.mixin.KeyBindsListKeyEntryAccessor;
import com.urntt.serverlist.gametest.mixin.KeyBindsScreenAccessor;
import com.urntt.serverlist.mixin.ManageServerScreenAccessor;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;

/**
 * Drag-and-drop, the edit and delete bindings, their rebinding, and the Controls screen.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ServerListScreenGameTest implements FabricClientGameTest {
	@Override
	public void runTest(final ClientGameTestContext context) {
		TestSupport.resetConfig();
		FakeEssential.dragHandler = null;
		int[] originalWindow = TestSupport.useRoomyWindow(context);
		TestSupport.saveServers(context, NAMES);
		JoinMultiplayerScreen screen = TestSupport.openServerList(context);
		assertEquals(NAMES, visibleNames(context, screen), "servers shown");

		testDragAndDrop(context, screen);
		testEditBinding(context, screen);
		testDeleteBinding(context, screen);
		testDragAfterAddScreen(context, screen);
		testEssentialHandler(context, screen);
		testDragSetting(context, screen);
		testRebinding(context, screen);
		testControlsScreen(context);
		TestSupport.restoreWindow(context, originalWindow);
	}

	private static void testDragAndDrop(final ClientGameTestContext context, final JoinMultiplayerScreen screen) {
		dragServer(context, screen, "Alpha", "Delta");
		List<String> expected = List.of("Bravo", "Charlie", "Delta", "Alpha", "Echo");
		assertEquals(expected, savedNames(context), "saved order after dragging Alpha onto Delta");
		assertEquals(expected, visibleNames(context, screen), "shown order after dragging Alpha onto Delta");
		context.takeScreenshot("server_list_after_drag");
		LOGGER.info("Drag and drop: {}", savedNames(context));
	}

	/**
	 * Right-clicking a server opens Edit Server Info for it. Dragging still works after returning, which is where
	 * Essential's drag-and-drop stops working.
	 */
	private static void testEditBinding(final ClientGameTestContext context, final JoinMultiplayerScreen screen) {
		click(context, textPoint(context, screen, "Charlie"), InputConstants.MOUSE_BUTTON_RIGHT);
		context.waitForScreen(ManageServerScreen.class);
		String name = context.computeOnClient(client ->
				((ManageServerScreenAccessor) client.gui.screen()).serverlist$nameEdit().getValue());
		assertEquals("Charlie", name, "server opened by right-click");
		context.takeScreenshot("edit_server_from_right_click");

		context.clickScreenButton("gui.cancel");
		context.waitForScreen(JoinMultiplayerScreen.class);
		assertTrue(currentScreen(context) == screen, "returned to the same server list");

		dragServer(context, screen, "Echo", "Bravo");
		assertEquals(List.of("Echo", "Bravo", "Charlie", "Delta", "Alpha"), savedNames(context), "saved order after dragging Echo to the top following Edit");
	}

	/** Delete with a selected server asks for confirmation, like the Delete button. */
	private static void testDeleteBinding(final ClientGameTestContext context, final JoinMultiplayerScreen screen) {
		click(context, textPoint(context, screen, "Delta"), InputConstants.MOUSE_BUTTON_LEFT);
		context.getInput().pressKey(ServerListKeyMappings.delete());
		context.waitForScreen(ConfirmScreen.class);
		context.takeScreenshot("delete_server_from_key");

		context.clickScreenButton("gui.cancel");
		context.waitForScreen(JoinMultiplayerScreen.class);
		assertEquals(5, savedNames(context).size(), "servers after cancelling the deletion");

		dragServer(context, screen, "Alpha", "Bravo");
		assertEquals(List.of("Echo", "Alpha", "Bravo", "Charlie", "Delta"), savedNames(context), "saved order after dragging Alpha up following Delete");
	}

	private static void testDragAfterAddScreen(final ClientGameTestContext context, final JoinMultiplayerScreen screen) {
		context.clickScreenButton("selectServer.add");
		context.waitForScreen(ManageServerScreen.class);
		context.clickScreenButton("gui.cancel");
		context.waitForScreen(JoinMultiplayerScreen.class);

		dragServer(context, screen, "Echo", "Delta");
		assertEquals(List.of("Alpha", "Bravo", "Charlie", "Delta", "Echo"), savedNames(context), "saved order after dragging Echo down following Add");
	}

	/** While Essential's own drag handler exists, Essential handles dragging and this mod stays out. */
	private static void testEssentialHandler(final ClientGameTestContext context, final JoinMultiplayerScreen screen) {
		FakeEssential.dragHandler = new Object();
		dragServer(context, screen, "Alpha", "Charlie");
		assertEquals(NAMES, savedNames(context), "saved order while Essential's drag handler exists");

		FakeEssential.dragHandler = null;
		dragServer(context, screen, "Alpha", "Charlie");
		assertEquals(List.of("Bravo", "Charlie", "Alpha", "Delta", "Echo"), savedNames(context), "saved order once Essential's drag handler is gone");
	}

	private static void testDragSetting(final ClientGameTestContext context, final JoinMultiplayerScreen screen) {
		ServerListClient.config().setDragToReorder(false);
		dragServer(context, screen, "Bravo", "Echo");
		assertEquals(List.of("Bravo", "Charlie", "Alpha", "Delta", "Echo"), savedNames(context), "saved order with dragging disabled");
		ServerListClient.config().setDragToReorder(true);
	}

	/** Both bindings work with keyboard and mouse keys. */
	private static void testRebinding(final ClientGameTestContext context, final JoinMultiplayerScreen screen) {
		KeyMapping edit = ServerListKeyMappings.edit();
		KeyMapping delete = ServerListKeyMappings.delete();
		edit.setKey(InputConstants.getKey("key.keyboard.e"));
		delete.setKey(InputConstants.getKey("key.mouse.middle"));
		KeyMapping.resetMapping();

		click(context, textPoint(context, screen, "Alpha"), InputConstants.MOUSE_BUTTON_RIGHT);
		assertTrue(currentScreen(context) == screen, "right-click does nothing once Edit is bound to E");

		click(context, textPoint(context, screen, "Delta"), InputConstants.MOUSE_BUTTON_LEFT);
		context.getInput().pressKey(edit);
		context.waitForScreen(ManageServerScreen.class);
		String name = context.computeOnClient(client ->
				((ManageServerScreenAccessor) client.gui.screen()).serverlist$nameEdit().getValue());
		assertEquals("Delta", name, "server opened by E");
		context.clickScreenButton("gui.cancel");
		context.waitForScreen(JoinMultiplayerScreen.class);

		click(context, textPoint(context, screen, "Charlie"), InputConstants.MOUSE_BUTTON_MIDDLE);
		context.waitForScreen(ConfirmScreen.class);
		context.clickScreenButton("selectServer.deleteButton");
		context.waitForScreen(JoinMultiplayerScreen.class);
		assertEquals(List.of("Bravo", "Alpha", "Delta", "Echo"), savedNames(context), "servers after deleting Charlie with the middle button");

		edit.setKey(edit.getDefaultKey());
		delete.setKey(delete.getDefaultKey());
		KeyMapping.resetMapping();
	}

	/**
	 * The Controls screen does not report the server list bindings as conflicting with in-game bindings on the same
	 * key, but still reports conflicts between the server list bindings themselves.
	 */
	private static void testControlsScreen(final ClientGameTestContext context) {
		KeyMapping edit = ServerListKeyMappings.edit();
		KeyMapping delete = ServerListKeyMappings.delete();
		edit.setKey(InputConstants.getKey("key.keyboard.e"));
		KeyMapping.resetMapping();

		context.setScreen(() -> new KeyBindsScreen(new TitleScreen(), context.computeOnClient(client -> client.options)));
		context.waitTick();
		assertTrue(!hasCollision(context, edit), "Edit Server bound to E conflicts with the inventory key");
		KeyMapping inventory = context.computeOnClient(client -> client.options.keyInventory);
		assertTrue(!hasCollision(context, inventory), "the inventory key conflicts with Edit Server");

		delete.setKey(InputConstants.getKey("key.keyboard.e"));
		KeyMapping.resetMapping();
		context.setScreen(() -> new KeyBindsScreen(new TitleScreen(), context.computeOnClient(client -> client.options)));
		context.waitTick();
		assertTrue(hasCollision(context, edit), "Edit Server and Delete Server on the same key are not reported as conflicting");

		edit.setKey(edit.getDefaultKey());
		delete.setKey(delete.getDefaultKey());
		KeyMapping.resetMapping();
		context.setScreen(TitleScreen::new);
	}

	private static boolean hasCollision(final ClientGameTestContext context, final KeyMapping mapping) {
		return context.computeOnClient(client -> {
			KeyBindsList list = ((KeyBindsScreenAccessor) client.gui.screen()).serverlistGametest$keyBindsList();
			for (KeyBindsList.Entry entry : list.children()) {
				if (entry instanceof KeyBindsListKeyEntryAccessor keyEntry && keyEntry.serverlistGametest$key() == mapping) {
					return keyEntry.serverlistGametest$hasCollision();
				}
			}
			throw new AssertionError("No Controls entry for " + mapping.getName());
		});
	}
}
