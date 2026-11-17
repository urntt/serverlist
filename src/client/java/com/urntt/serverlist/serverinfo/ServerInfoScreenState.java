package com.urntt.serverlist.serverinfo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jspecify.annotations.Nullable;

/**
 * The option area of one Add/Edit Server screen instance.
 *
 * <p>Vanilla recreates the screen's widgets when the window is resized, so the state belongs to one generation
 * of widgets, identified by the name field. When the name field changes, the extensions run again.
 */
public final class ServerInfoScreenState {
	private @Nullable Object generation;
	final List<ServerInfoRow> apiRows = new ArrayList<>();
	final Set<AbstractWidget> adopted = new LinkedHashSet<>();
	final List<Runnable> saveActions = new ArrayList<>();
	/** Widgets removed from the screen and shown inside {@link #scrollContainer} instead. */
	final List<AbstractWidget> scrolled = new ArrayList<>();
	@Nullable AbstractWidget scrollContainer;

	/**
	 * Starts a new generation if {@code generation} differs from the current one.
	 *
	 * @return whether a new generation started
	 */
	boolean startGeneration(final Object generation) {
		if (this.generation == generation) {
			return false;
		}
		this.generation = generation;
		this.apiRows.clear();
		this.adopted.clear();
		this.saveActions.clear();
		this.scrolled.clear();
		this.scrollContainer = null;
		return true;
	}

	/** Runs the save actions registered by extensions. Called when the player presses Done. */
	public void runSaveActions() {
		for (Runnable action : this.saveActions) {
			action.run();
		}
	}
}
