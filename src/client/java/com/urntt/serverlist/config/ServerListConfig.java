package com.urntt.serverlist.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.urntt.serverlist.ServerListClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The mod's settings, persisted as JSON. Every change is written to disk immediately.
 *
 * <p>Each setting switches one feature that changes vanilla behavior. All of them are enabled by default.
 */
public final class ServerListConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerListClient.MOD_ID);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final Path path;
	private final Settings settings;

	private ServerListConfig(final Path path, final Settings settings) {
		this.path = path;
		this.settings = settings;
	}

	/**
	 * Returns the location of the config file in the Fabric config directory.
	 */
	public static Path defaultPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(ServerListClient.MOD_ID + ".json");
	}

	/**
	 * Loads the configuration from {@code path}. A missing file is created with the defaults. An unreadable
	 * file falls back to the defaults and is left untouched until the next change is saved.
	 */
	public static ServerListConfig load(final Path path) {
		if (Files.notExists(path)) {
			ServerListConfig config = new ServerListConfig(path, new Settings());
			config.save();
			return config;
		}

		try {
			Settings settings = GSON.fromJson(Files.readString(path), Settings.class);
			return new ServerListConfig(path, settings != null ? settings : new Settings());
		} catch (IOException | JsonParseException e) {
			LOGGER.warn("Failed to read config file {}, using defaults", path, e);
			return new ServerListConfig(path, new Settings());
		}
	}

	/** Whether saved servers can be reordered by dragging them in the server list. */
	public boolean dragToReorder() {
		return this.settings.dragToReorder;
	}

	public void setDragToReorder(final boolean value) {
		this.settings.dragToReorder = value;
		this.save();
	}

	/** Whether the pause menu shows a button that opens the server list. */
	public boolean pauseMenuButton() {
		return this.settings.pauseMenuButton;
	}

	public void setPauseMenuButton(final boolean value) {
		this.settings.pauseMenuButton = value;
		this.save();
	}

	/** Whether the server list shows a search box above its buttons. */
	public boolean searchBox() {
		return this.settings.searchBox;
	}

	public void setSearchBox(final boolean value) {
		this.settings.searchBox = value;
		this.save();
	}

	/** Whether each server entry shows its latency in milliseconds. */
	public boolean showPingMs() {
		return this.settings.showPingMs;
	}

	public void setShowPingMs(final boolean value) {
		this.settings.showPingMs = value;
		this.save();
	}

	/** Whether the server list pings more servers at once than vanilla does. */
	public boolean fasterServerPings() {
		return this.settings.fasterServerPings;
	}

	public void setFasterServerPings(final boolean value) {
		this.settings.fasterServerPings = value;
		this.save();
	}

	/** Whether literal IP addresses skip the reverse DNS lookup when pinging or joining a server. */
	public boolean skipReverseDnsForIpAddresses() {
		return this.settings.skipReverseDnsForIpAddresses;
	}

	public void setSkipReverseDnsForIpAddresses(final boolean value) {
		this.settings.skipReverseDnsForIpAddresses = value;
		this.save();
	}

	private void save() {
		try {
			Files.createDirectories(this.path.getParent());
			Files.writeString(this.path, GSON.toJson(this.settings));
		} catch (IOException e) {
			LOGGER.error("Failed to write config file {}", this.path, e);
		}
	}

	/**
	 * The serialized form. Field initializers are the defaults, which also apply to keys missing from the file.
	 */
	private static final class Settings {
		private boolean dragToReorder = true;
		private boolean pauseMenuButton = true;
		private boolean searchBox = true;
		private boolean showPingMs = true;
		private boolean fasterServerPings = true;
		private boolean skipReverseDnsForIpAddresses = true;
	}
}
