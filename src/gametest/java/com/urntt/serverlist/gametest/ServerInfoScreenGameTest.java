package com.urntt.serverlist.gametest;

import static com.urntt.serverlist.gametest.TestSupport.LOGGER;
import static com.urntt.serverlist.gametest.TestSupport.assertEquals;
import static com.urntt.serverlist.gametest.TestSupport.assertNoOverlap;
import static com.urntt.serverlist.gametest.TestSupport.assertTrue;

import com.urntt.serverlist.api.ServerInfoScreenContext;
import com.urntt.serverlist.mixin.ManageServerScreenAccessor;
import com.urntt.serverlist.serverinfo.ManageServerScreenExt;
import com.urntt.serverlist.serverinfo.ServerInfoLayout;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

/**
 * The Add/Edit Server screen: Done and Cancel side by side at the bottom, placement of API rows and of widgets
 * from mods that do not use the API, save actions, scrolling on small screens, and resizing.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ServerInfoScreenGameTest implements FabricClientGameTest {
	/** Window size and GUI scale of a 427x240 screen, too small for all rows of the tests. */
	private static final int SMALL_WINDOW_WIDTH = 854;
	private static final int SMALL_WINDOW_HEIGHT = 480;
	private static final int SMALL_GUI_SCALE = 2;

	@Override
	public void runTest(final ClientGameTestContext context) {
		TestSupport.resetConfig();
		List<Boolean> results = new ArrayList<>();
		int[] originalWindow = TestSupport.useRoomyWindow(context);

		TestServerInfoExtension.enabled = false;
		ForeignWidget.enabled = false;
		ManageServerScreen screen = open(context, results);
		assertDefaultLayout(context, screen);
		context.takeScreenshot("server_info_default");

		TestServerInfoExtension.enabled = true;
		ForeignWidget.enabled = true;
		screen = open(context, results);
		assertExtendedLayout(context, screen);
		context.takeScreenshot("server_info_extended");

		TestServerInfoExtension.saves = 0;
		context.clickScreenButton("gui.done");
		assertEquals(1, TestServerInfoExtension.saves, "save actions after Done");
		open(context, results);
		context.clickScreenButton("gui.cancel");
		assertEquals(1, TestServerInfoExtension.saves, "save actions after Cancel");
		assertEquals(List.of(true, false), results, "vanilla results of Done and Cancel");

		testSmallScreen(context, results);

		TestServerInfoExtension.enabled = false;
		ForeignWidget.enabled = false;
		context.setScreen(TitleScreen::new);
		TestSupport.restoreWindow(context, originalWindow);
	}

	private static ManageServerScreen open(final ClientGameTestContext context, final List<Boolean> results) {
		context.setScreen(() -> new ManageServerScreen(
				new TitleScreen(),
				Component.translatable("manageServer.edit.title"),
				results::add,
				new ServerData("Test Server", "127.0.0.1", ServerData.Type.OTHER)));
		context.waitTick();
		return context.computeOnClient(client -> (ManageServerScreen) client.gui.screen());
	}

	/**
	 * Without extra widgets, the resource pack button stays at its vanilla position and Done and Cancel sit side by
	 * side at the bottom.
	 */
	private static void assertDefaultLayout(final ClientGameTestContext context, final ManageServerScreen screen) {
		context.runOnClient(client -> {
			ManageServerScreenExt ext = (ManageServerScreenExt) screen;
			ManageServerScreenAccessor vanilla = (ManageServerScreenAccessor) screen;
			assertEquals(screen.height / 4 + 72, ext.serverlist$resourcePackButton().getY(), "resource pack button y without extra widgets");
			assertFooter(screen, vanilla.serverlist$addButton(), ext.serverlist$cancelButton());
			assertNoOverlap(allWidgets(screen), "default server info screen");
		});
	}

	/**
	 * Done and Cancel share one row in the footer at the bottom of the screen, in vanilla's footer style: Done on the
	 * left, both as wide as vanilla's default button, 8 pixels apart, centered.
	 */
	private static void assertFooter(final ManageServerScreen screen, final AbstractWidget done, final AbstractWidget cancel) {
		assertEquals(done.getY(), cancel.getY(), "Done and Cancel share a row");
		assertTrue(done.getY() >= screen.height - HeaderAndFooterLayout.DEFAULT_HEADER_AND_FOOTER_HEIGHT && cancel.getBottom() <= screen.height,
				"Done and Cancel are not in the footer at the bottom of the screen: " + TestSupport.describe(done));
		assertEquals(Button.DEFAULT_WIDTH, done.getWidth(), "width of Done");
		assertEquals(Button.DEFAULT_WIDTH, cancel.getWidth(), "width of Cancel");
		assertEquals(done.getRight() + ServerInfoLayout.FOOTER_BUTTON_SPACING, cancel.getX(), "x of Cancel next to Done");
		assertTrue(Math.abs(done.getX() - (screen.width - cancel.getRight())) <= 1,
				"Done and Cancel are not centered: " + TestSupport.describe(done) + ", " + TestSupport.describe(cancel));
	}

	/** The name and address fields and every widget on the screen. */
	private static List<GuiEventListener> allWidgets(final ManageServerScreen screen) {
		ManageServerScreenAccessor vanilla = (ManageServerScreenAccessor) screen;
		List<GuiEventListener> all = new ArrayList<>(List.of(vanilla.serverlist$nameEdit(), vanilla.serverlist$ipEdit()));
		all.addAll(screen.children().stream().filter(AbstractWidget.class::isInstance).toList());
		return all.stream().distinct().toList();
	}

	private static void assertExtendedLayout(final ClientGameTestContext context, final ManageServerScreen screen) {
		context.runOnClient(client -> {
			ManageServerScreenExt ext = (ManageServerScreenExt) screen;
			ManageServerScreenAccessor vanilla = (ManageServerScreenAccessor) screen;
			AbstractWidget resourcePacks = ext.serverlist$resourcePackButton();
			AbstractWidget fullRow = widget(screen, TestServerInfoExtension.FULL_ROW);
			AbstractWidget left = widget(screen, TestServerInfoExtension.LEFT);
			AbstractWidget right = widget(screen, TestServerInfoExtension.RIGHT);
			AbstractWidget foreign = widget(screen, ForeignWidget.MESSAGE);
			AbstractWidget done = vanilla.serverlist$addButton();
			AbstractWidget cancel = ext.serverlist$cancelButton();

			assertNoOverlap(allWidgets(screen), "extended server info screen");

			assertEquals(ServerInfoScreenContext.FULL_WIDTH, fullRow.getWidth(), "width of a full API row");
			assertEquals(ServerInfoScreenContext.HALF_WIDTH, left.getWidth(), "width of a half API row");
			assertEquals(left.getY(), right.getY(), "the two halves share a row");
			List<AbstractWidget> visualOrder = List.of(resourcePacks, fullRow, left, foreign, done);
			for (int i = 1; i < visualOrder.size(); i++) {
				assertTrue(visualOrder.get(i - 1).getY() < visualOrder.get(i).getY(),
						TestSupport.describe(visualOrder.get(i - 1)) + " is not above " + TestSupport.describe(visualOrder.get(i)));
			}
			assertFooter(screen, done, cancel);

			List<? extends GuiEventListener> children = screen.children();
			List<AbstractWidget> tabOrder = List.of(resourcePacks, fullRow, left, right, foreign, done, cancel);
			for (int i = 1; i < tabOrder.size(); i++) {
				assertTrue(children.indexOf(tabOrder.get(i - 1)) < children.indexOf(tabOrder.get(i)),
						"keyboard navigation reaches " + TestSupport.describe(tabOrder.get(i)) + " too early");
			}
			LOGGER.info("Extended server info layout: resource packs {}, API {} / {}, foreign {}, Done {}, Cancel {}",
					resourcePacks.getY(), fullRow.getY(), left.getY(), foreign.getY(), done.getY(), cancel.getY());
		});
	}

	/**
	 * On a small screen the rows do not fit, so they scroll and Done and Cancel stay at the bottom. Enlarging the
	 * window again resizes the screen, which rebuilds its widgets; they must lay out again without duplicating API
	 * widgets.
	 */
	private static void testSmallScreen(final ClientGameTestContext context, final List<Boolean> results) {
		int[] roomyWindow = TestSupport.useWindow(context, SMALL_WINDOW_WIDTH, SMALL_WINDOW_HEIGHT, SMALL_GUI_SCALE);
		ManageServerScreen screen = open(context, results);
		context.takeScreenshot("server_info_small_screen");
		context.runOnClient(client -> {
			ManageServerScreenExt ext = (ManageServerScreenExt) screen;
			AbstractWidget done = ((ManageServerScreenAccessor) screen).serverlist$addButton();
			AbstractWidget cancel = ext.serverlist$cancelButton();
			assertTrue(!screen.children().contains(ext.serverlist$resourcePackButton()), "the rows do not scroll on a small screen");
			assertFooter(screen, done, cancel);
			assertNoOverlap(allWidgets(screen), "small server info screen");
			LOGGER.info("Small server info screen ({}x{}): Done and Cancel at {}", screen.width, screen.height, done.getY());
		});

		TestSupport.restoreWindow(context, roomyWindow);
		context.runOnClient(client -> {
			assertTrue(client.gui.screen() == screen, "the screen changed while resizing");
			ManageServerScreenExt ext = (ManageServerScreenExt) screen;
			assertTrue(screen.children().contains(ext.serverlist$resourcePackButton()), "the rows still scroll after enlarging the screen");
			long fullRows = screen.children().stream()
					.filter(child -> child instanceof AbstractWidget widget && widget.getMessage().equals(TestServerInfoExtension.FULL_ROW))
					.count();
			assertEquals(1L, fullRows, "API rows after resizing");
			assertFooter(screen, ((ManageServerScreenAccessor) screen).serverlist$addButton(), ext.serverlist$cancelButton());
			assertNoOverlap(allWidgets(screen), "resized server info screen");
		});
		context.takeScreenshot("server_info_resized");
	}

	private static AbstractWidget widget(final ManageServerScreen screen, final Component message) {
		return screen.children().stream()
				.filter(child -> child instanceof AbstractWidget widget && widget.getMessage().equals(message))
				.map(AbstractWidget.class::cast)
				.findFirst()
				.orElseThrow(() -> new AssertionError("No widget '" + message.getString() + "' on the screen"));
	}
}
