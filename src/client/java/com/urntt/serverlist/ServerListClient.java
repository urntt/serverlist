package com.urntt.serverlist;

import com.urntt.serverlist.config.ServerListConfig;
import com.urntt.serverlist.config.ServerListConfigScreen;
import com.urntt.serverlist.config.ServerListSettings;
import com.urntt.serverlist.multiplayer.ServerListScreenHooks;
import com.urntt.serverlist.multiplayer.WorldSwitch;
import com.urntt.serverlist.serverinfo.ServerInfoScreenController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.resources.Identifier;

public final class ServerListClient implements ClientModInitializer {
	public static final String MOD_ID = "serverlist";

	private static ServerListConfig config;

	@Override
	public void onInitializeClient() {
		config = ServerListConfig.load(ServerListConfig.defaultPath());
		ServerListKeyMappings.register();
		ServerListScreenHooks.register();
		ServerInfoScreenController.register();
		WorldSwitch.registerOpenKey();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (ServerListKeyMappings.openConfig().consumeClick()) {
				ServerListConfigScreen.open(client);
			}
		});
	}

	/** The mod's own settings and the configuration mode. */
	public static ServerListConfig config() {
		return config;
	}

	/** The settings that currently apply, according to the configuration mode. */
	public static ServerListSettings settings() {
		return config.effective();
	}

	public static Identifier id(final String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
