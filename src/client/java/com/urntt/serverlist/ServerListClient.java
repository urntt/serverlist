package com.urntt.serverlist;

import com.urntt.serverlist.config.ServerListConfig;
import com.urntt.serverlist.multiplayer.ServerListScreenHooks;
import com.urntt.serverlist.multiplayer.WorldSwitch;
import com.urntt.serverlist.serverinfo.ServerInfoScreenController;
import net.fabricmc.api.ClientModInitializer;
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
	}

	public static ServerListConfig config() {
		return config;
	}

	public static Identifier id(final String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
