package com.urntt.serverlist.serverinfo;

import com.urntt.serverlist.api.ServerInfoScreenContext;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;

/**
 * The context handed to one extension; records its rows and save actions in the screen's state.
 */
final class ServerInfoContextImpl implements ServerInfoScreenContext {
	private final Screen screen;
	private final ServerData serverData;
	private final ServerInfoScreenState state;

	ServerInfoContextImpl(final Screen screen, final ServerData serverData, final ServerInfoScreenState state) {
		this.screen = screen;
		this.serverData = serverData;
		this.state = state;
	}

	@Override
	public Screen screen() {
		return this.screen;
	}

	@Override
	public ServerData serverData() {
		return this.serverData;
	}

	@Override
	public <T extends AbstractWidget> T addRow(final T widget) {
		widget.setWidth(FULL_WIDTH);
		this.state.apiRows.add(new ServerInfoRow(List.of(widget), true));
		return widget;
	}

	@Override
	public void addRow(final AbstractWidget left, final AbstractWidget right) {
		left.setWidth(HALF_WIDTH);
		right.setWidth(HALF_WIDTH);
		this.state.apiRows.add(new ServerInfoRow(List.of(left, right), true));
	}

	@Override
	public void onSave(final Runnable action) {
		this.state.saveActions.add(action);
	}
}
