package com.urntt.serverlist.gametest;

import static com.urntt.serverlist.gametest.TestSupport.LOGGER;
import static com.urntt.serverlist.gametest.TestSupport.assertEquals;
import static com.urntt.serverlist.gametest.TestSupport.assertNoOverlap;
import static com.urntt.serverlist.gametest.TestSupport.assertTrue;

import com.urntt.serverlist.api.ServerInfoScreenContext;
import com.urntt.serverlist.mixin.ManageServerScreenAccessor;
import com.urntt.serverlist.serverinfo.ManageServerScreenExt;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

/**
 * The option area of the Add/Edit Server screen: vanilla layout without extra widgets, placement of API rows and
 * of widgets from mods that do not use the API, save actions, scrolling on small screens, and resizing.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ServerInfoScreenGameTest implements FabricClientGameTest {
	/** GUI scale that makes the screen too small for all rows. */
	private static final int SMALL_GUI_SCALE = 4;

	@Override
	public void runTest(final ClientGameTestContext context) {
		TestSupport.resetConfig();
		List<Boolean> results = new ArrayList<>();
		int[] originalWindow = TestSupport.useRoomyWindow(context);
		int roomyScale = context.computeOnClient(client -> client.options.guiScale().get());

		TestServerInfoExtension.enabled = false;
		ForeignWidget.enabled = false;
		ManageServerScreen screen = open(context, results);
		assertVanillaLayout(context, screen);

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

		testSmallScreen(context, results, roomyScale);

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

	private static void assertVanillaLayout(final ClientGameTestContext context, final ManageServerScreen screen) {
		context.runOnClient(client -> {
			int quarter = screen.height / 4;
			ManageServerScreenExt ext = (ManageServerScreenExt) screen;
			assertEquals(quarter + 72, ext.serverlist$resourcePackButton().getY(), "resource pack button y without extra widgets");
			assertEquals(quarter + 114, ((ManageServerScreenAccessor) screen).serverlist$addButton().getY(), "Done y without extra widgets");
			assertEquals(quarter + 138, ext.serverlist$cancelButton().getY(), "Cancel y without extra widgets");
		});
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

			List<GuiEventListener> all = new ArrayList<>(List.of(vanilla.serverlist$nameEdit(), vanilla.serverlist$ipEdit()));
			all.addAll(screen.children().stream().filter(AbstractWidget.class::isInstance).toList());
			assertNoOverlap(all.stream().distinct().toList(), "extended server info screen");

			assertEquals(ServerInfoScreenContext.FULL_WIDTH, fullRow.getWidth(), "width of a full API row");
			assertEquals(ServerInfoScreenContext.HALF_WIDTH, left.getWidth(), "width of a half API row");
			assertEquals(left.getY(), right.getY(), "the two halves share a row");
			List<AbstractWidget> visualOrder = List.of(resourcePacks, fullRow, left, foreign, done, cancel);
			for (int i = 1; i < visualOrder.size(); i++) {
				assertTrue(visualOrder.get(i - 1).getY() < visualOrder.get(i).getY(),
						TestSupport.describe(visualOrder.get(i - 1)) + " is not above " + TestSupport.describe(visualOrder.get(i)));
			}
			assertTrue(cancel.getBottom() <= screen.height, "Cancel is below the screen");

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
	 * At a large GUI scale the rows do not fit, so they scroll and Done and Cancel stay on screen. Changing the
	 * GUI scale back resizes the screen, which rebuilds the screen's widgets, which must lay out again without duplicating API widgets.
	 */
	private static void testSmallScreen(final ClientGameTestContext context, final List<Boolean> results, final int roomyScale) {
		TestSupport.setGuiScale(context, SMALL_GUI_SCALE);
		ManageServerScreen screen = open(context, results);
		context.takeScreenshot("server_info_small_screen");
		context.runOnClient(client -> {
			ManageServerScreenExt ext = (ManageServerScreenExt) screen;
			AbstractWidget done = ((ManageServerScreenAccessor) screen).serverlist$addButton();
			AbstractWidget cancel = ext.serverlist$cancelButton();
			assertTrue(cancel.getBottom() <= screen.height, "Cancel is below the small screen: " + TestSupport.describe(cancel));
			assertTrue(!screen.children().contains(ext.serverlist$resourcePackButton()), "the rows do not scroll on a small screen");
			List<GuiEventListener> topLevel = new ArrayList<>(List.of(((ManageServerScreenAccessor) screen).serverlist$nameEdit(),
					((ManageServerScreenAccessor) screen).serverlist$ipEdit()));
			topLevel.addAll(screen.children().stream().filter(AbstractWidget.class::isInstance).toList());
			assertNoOverlap(topLevel.stream().distinct().toList(), "small server info screen");
			LOGGER.info("Small server info screen ({}x{}): Done {}, Cancel {}", screen.width, screen.height, done.getY(), cancel.getY());
		});

		TestSupport.setGuiScale(context, roomyScale);
		context.runOnClient(client -> {
			assertTrue(client.gui.screen() == screen, "the screen changed while resizing");
			ManageServerScreenExt ext = (ManageServerScreenExt) screen;
			assertTrue(screen.children().contains(ext.serverlist$resourcePackButton()), "the rows still scroll after enlarging the screen");
			long fullRows = screen.children().stream()
					.filter(child -> child instanceof AbstractWidget widget && widget.getMessage().equals(TestServerInfoExtension.FULL_ROW))
					.count();
			assertEquals(1L, fullRows, "API rows after resizing");
			List<GuiEventListener> all = new ArrayList<>(List.of(((ManageServerScreenAccessor) screen).serverlist$nameEdit(),
					((ManageServerScreenAccessor) screen).serverlist$ipEdit()));
			all.addAll(screen.children().stream().filter(AbstractWidget.class::isInstance).toList());
			assertNoOverlap(all.stream().distinct().toList(), "resized server info screen");
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
