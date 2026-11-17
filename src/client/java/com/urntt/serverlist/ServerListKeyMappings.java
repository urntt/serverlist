package com.urntt.serverlist;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Set;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * The mod's key mappings. They are listed in the vanilla Controls screen under the "Server List" category.
 */
public final class ServerListKeyMappings {
	public static final String EDIT_NAME = "key.serverlist.edit";
	public static final String DELETE_NAME = "key.serverlist.delete";
	public static final String OPEN_NAME = "key.serverlist.open";

	private static KeyMapping edit;
	private static KeyMapping delete;
	private static KeyMapping open;
	private static Set<KeyMapping> screenOnly = Set.of();

	private ServerListKeyMappings() {
	}

	static void register() {
		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath(ServerListClient.MOD_ID, "general"));
		edit = KeyMappingHelper.registerKeyMapping(create(EDIT_NAME, "key.mouse.right", category));
		delete = KeyMappingHelper.registerKeyMapping(create(DELETE_NAME, "key.keyboard.delete", category));
		open = KeyMappingHelper.registerKeyMapping(new KeyMapping(OPEN_NAME, InputConstants.UNKNOWN.getValue(), category));
		screenOnly = Set.of(edit, delete);
	}

	private static KeyMapping create(final String name, final String defaultKey, final KeyMapping.Category category) {
		InputConstants.Key key = InputConstants.getKey(defaultKey);
		return new KeyMapping(name, key.getType(), key.getValue(), category);
	}

	/** Opens the Edit Server Info screen for the hovered (mouse) or selected (keyboard) server. */
	public static KeyMapping edit() {
		return edit;
	}

	/** Asks to delete the hovered (mouse) or selected (keyboard) server. */
	public static KeyMapping delete() {
		return delete;
	}

	/** Opens the server list while playing. */
	public static KeyMapping open() {
		return open;
	}

	/**
	 * Whether {@code mapping} only acts inside the server list. Such mappings never fire in game, so they do not
	 * conflict with in-game mappings bound to the same key.
	 */
	public static boolean isScreenOnly(final KeyMapping mapping) {
		return screenOnly.contains(mapping);
	}
}
