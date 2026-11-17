package com.urntt.serverlist.serverinfo;

import com.urntt.serverlist.ServerListClient;
import com.urntt.serverlist.api.ServerInfoScreenContext;
import com.urntt.serverlist.api.ServerInfoScreenExtension;
import com.urntt.serverlist.mixin.ManageServerScreenAccessor;
import com.urntt.serverlist.mixin.ScreenChildrenAccessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Arranges the option area of the Add Server and Edit Server Info screens ({@link ManageServerScreen}).
 *
 * <p>Runs after the screen is set up, in a phase after Fabric's default phase, so that vanilla, mods that inject
 * into the screen's {@code init()} (for example Essential's "Show to Friends" button), and mods that add widgets
 * in the default phase of {@link ScreenEvents#AFTER_INIT} are all done. It then:
 *
 * <ol>
 *   <li>runs the {@link ServerInfoScreenExtension} entrypoints, ordered by mod id, for new widgets;</li>
 *   <li>adopts widgets that other mods placed in the option area themselves: widgets that overlap the central
 *       column below the address field; widgets elsewhere are left alone;</li>
 *   <li>lays out the resource pack button, the extension rows, the adopted widgets, Done, and Cancel with
 *       {@link ServerInfoLayout}, and puts them in that order for keyboard navigation.</li>
 * </ol>
 *
 * <p>Without extension rows or adopted widgets, the screen is left exactly as vanilla made it.
 */
public final class ServerInfoScreenController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerListClient.MOD_ID);
	/** Runs after mods that add widgets in Fabric's default phase. */
	public static final Identifier LAYOUT_PHASE = ServerListClient.id("server_info_layout");
	/** Half the width of vanilla's widgets on the screen, which are centered. */
	private static final int COLUMN_HALF_WIDTH = ServerInfoScreenContext.FULL_WIDTH / 2;
	/** Gap between the two widgets of a row that holds two. */
	private static final int PAIR_SPACING = ServerInfoScreenContext.FULL_WIDTH - 2 * ServerInfoScreenContext.HALF_WIDTH;

	private static @Nullable List<EntrypointContainer<ServerInfoScreenExtension>> extensions;

	private ServerInfoScreenController() {
	}

	public static void register() {
		ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, LAYOUT_PHASE);
		ScreenEvents.AFTER_INIT.register(LAYOUT_PHASE, (client, screen, width, height) -> {
			if (screen instanceof ManageServerScreen manageServerScreen) {
				arrange(client, manageServerScreen);
			}
		});
	}

	private static void arrange(final Minecraft minecraft, final ManageServerScreen screen) {
		ManageServerScreenAccessor vanilla = (ManageServerScreenAccessor) screen;
		ManageServerScreenExt ext = (ManageServerScreenExt) screen;
		AbstractWidget resourcePacks = ext.serverlist$resourcePackButton();
		AbstractWidget cancel = ext.serverlist$cancelButton();
		AbstractWidget done = vanilla.serverlist$addButton();
		EditBox address = vanilla.serverlist$ipEdit();
		if (resourcePacks == null || cancel == null || done == null || address == null) {
			return;
		}

		ServerInfoScreenState state = ext.serverlist$state();
		List<AbstractWidget> widgets = Screens.getWidgets(screen);
		restoreScrolledWidgets(widgets, state);

		if (state.startGeneration(vanilla.serverlist$nameEdit())) {
			runExtensions(screen, vanilla, state);
			for (ServerInfoRow row : state.apiRows) {
				widgets.addAll(row.widgets());
			}
		}

		adoptForeignWidgets(screen, widgets, state, address, resourcePacks, done, cancel);

		List<ServerInfoRow> rows = new ArrayList<>();
		rows.add(ServerInfoRow.ownSize(resourcePacks));
		rows.addAll(state.apiRows);
		for (AbstractWidget widget : state.adopted) {
			rows.add(ServerInfoRow.ownSize(widget));
		}
		if (rows.size() == 1) {
			return;
		}

		List<Integer> rowHeights = rows.stream().map(ServerInfoRow::height).toList();
		ServerInfoLayout.Placement placement = ServerInfoLayout.compute(screen.height, address.getBottom(), rowHeights);
		int centerX = screen.width / 2;
		done.setY(placement.doneY());
		cancel.setY(placement.cancelY());

		List<AbstractWidget> navigationOrder = new ArrayList<>();
		if (placement.scrolling()) {
			navigationOrder.add(scroll(minecraft, widgets, state, rows, placement, centerX));
		} else {
			for (int i = 0; i < rows.size(); i++) {
				placeRow(rows.get(i), centerX, placement.rowYs().get(i));
				navigationOrder.addAll(rows.get(i).widgets());
			}
		}
		navigationOrder.add(done);
		navigationOrder.add(cancel);
		putAfter(((ScreenChildrenAccessor) screen).serverlist$children(), address, navigationOrder);
	}

	private static void runExtensions(final ManageServerScreen screen, final ManageServerScreenAccessor vanilla, final ServerInfoScreenState state) {
		for (EntrypointContainer<ServerInfoScreenExtension> container : extensions()) {
			ServerInfoContextImpl context = new ServerInfoContextImpl(screen, vanilla.serverlist$serverData(), state);
			try {
				container.getEntrypoint().init(context);
			} catch (RuntimeException e) {
				LOGGER.error("Server info screen extension of mod {} failed", container.getProvider().getMetadata().getId(), e);
			}
		}
	}

	private static List<EntrypointContainer<ServerInfoScreenExtension>> extensions() {
		if (extensions == null) {
			extensions = FabricLoader.getInstance()
					.getEntrypointContainers(ServerInfoScreenExtension.ENTRYPOINT, ServerInfoScreenExtension.class)
					.stream()
					.sorted(Comparator.comparing(container -> container.getProvider().getMetadata().getId()))
					.toList();
		}
		return extensions;
	}

	/**
	 * Adds widgets that other mods put into the option area without the API to {@code state}, and forgets adopted
	 * widgets that their mods removed.
	 */
	private static void adoptForeignWidgets(
			final ManageServerScreen screen, final List<AbstractWidget> widgets, final ServerInfoScreenState state,
			final EditBox address, final AbstractWidget resourcePacks, final AbstractWidget done, final AbstractWidget cancel) {
		Set<AbstractWidget> present = Collections.newSetFromMap(new IdentityHashMap<>());
		present.addAll(widgets);
		state.adopted.retainAll(present);

		Set<AbstractWidget> known = Collections.newSetFromMap(new IdentityHashMap<>());
		known.add(resourcePacks);
		known.add(done);
		known.add(cancel);
		for (ServerInfoRow row : state.apiRows) {
			known.addAll(row.widgets());
		}

		int centerX = screen.width / 2;
		for (AbstractWidget widget : widgets) {
			if (!known.contains(widget) && widget.visible
					&& widget.getRight() > centerX - COLUMN_HALF_WIDTH && widget.getX() < centerX + COLUMN_HALF_WIDTH
					&& widget.getY() >= address.getBottom()) {
				state.adopted.add(widget);
			}
		}
	}

	private static void placeRow(final ServerInfoRow row, final int centerX, final int y) {
		List<AbstractWidget> rowWidgets = row.widgets();
		if (rowWidgets.size() == 2) {
			rowWidgets.get(0).setPosition(centerX - COLUMN_HALF_WIDTH, y);
			rowWidgets.get(1).setPosition(centerX - COLUMN_HALF_WIDTH + ServerInfoScreenContext.HALF_WIDTH + PAIR_SPACING, y);
		} else if (row.sized()) {
			rowWidgets.getFirst().setPosition(centerX - COLUMN_HALF_WIDTH, y);
		} else {
			AbstractWidget widget = rowWidgets.getFirst();
			widget.setPosition(centerX - widget.getWidth() / 2, y);
		}
	}

	/**
	 * Moves the rows into a scrolling area and returns the area's widget.
	 */
	private static AbstractWidget scroll(
			final Minecraft minecraft, final List<AbstractWidget> widgets, final ServerInfoScreenState state,
			final List<ServerInfoRow> rows, final ServerInfoLayout.Placement placement, final int centerX) {
		LinearLayout content = LinearLayout.vertical().spacing(ServerInfoLayout.ROW_SPACING);
		content.defaultCellSetting().alignHorizontallyCenter();
		for (ServerInfoRow row : rows) {
			if (row.widgets().size() == 2) {
				LinearLayout pair = LinearLayout.horizontal().spacing(PAIR_SPACING);
				row.widgets().forEach(pair::addChild);
				content.addChild(pair);
			} else {
				content.addChild(row.widgets().getFirst());
			}
			widgets.removeAll(row.widgets());
			state.scrolled.addAll(row.widgets());
		}

		ScrollableLayout area = new ScrollableLayout(minecraft, content, placement.scrollHeight());
		area.arrangeElements();
		area.setPosition(centerX - area.getWidth() / 2, placement.top());
		area.visitWidgets(widget -> {
			widgets.add(widget);
			state.scrollContainer = widget;
		});
		return state.scrollContainer;
	}

	/** Undoes {@link #scroll} from an earlier pass over the same widgets. */
	private static void restoreScrolledWidgets(final List<AbstractWidget> widgets, final ServerInfoScreenState state) {
		if (state.scrollContainer != null) {
			widgets.remove(state.scrollContainer);
			state.scrollContainer = null;
		}
		widgets.addAll(state.scrolled);
		state.scrolled.clear();
	}

	/**
	 * Moves {@code ordered} to directly after {@code anchor} in {@code children}, keeping their order.
	 */
	private static void putAfter(final List<GuiEventListener> children, final GuiEventListener anchor, final List<? extends GuiEventListener> ordered) {
		Set<GuiEventListener> moving = Collections.newSetFromMap(new IdentityHashMap<>());
		moving.addAll(ordered);
		children.removeIf(moving::contains);
		int anchorIndex = children.indexOf(anchor);
		children.addAll(anchorIndex < 0 ? children.size() : anchorIndex + 1, ordered);
	}
}
