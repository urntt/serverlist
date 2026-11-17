package com.urntt.serverlist.multiplayer;

import com.urntt.serverlist.ServerListClient;
import java.lang.reflect.Method;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects Essential's own drag-and-drop on the server list.
 *
 * <p>Essential creates its drag handler when the screen is first initialized and destroys it when the screen is
 * closed. Screens that come back (from Edit, Delete, Add, or Direct Connection) are not initialized again, so
 * the handler is then missing and Essential's drag-and-drop stops working. serverlist uses its own drag-and-drop
 * exactly when Essential's handler is missing.
 */
public final class EssentialCompat {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerListClient.MOD_ID);
	private static final String ESSENTIAL_MOD_ID = "essential";
	/** Public method that Essential adds to the server list screen. */
	private static final String DRAG_HANDLER_GETTER = "essential$getDragHandlerOrNull";

	private static final ClassValue<Optional<Method>> GETTERS = new ClassValue<>() {
		@Override
		protected Optional<Method> computeValue(final Class<?> type) {
			try {
				return Optional.of(type.getMethod(DRAG_HANDLER_GETTER));
			} catch (NoSuchMethodException e) {
				return Optional.empty();
			}
		}
	};

	private static boolean warned;

	private EssentialCompat() {
	}

	/** Whether Essential's drag-and-drop currently handles {@code screen}. */
	public static boolean handlesDrag(final Screen screen) {
		Optional<Method> getter = GETTERS.get(screen.getClass());
		if (getter.isEmpty()) {
			if (FabricLoader.getInstance().isModLoaded(ESSENTIAL_MOD_ID)) {
				warnOnce("Essential is installed but its drag handler could not be found; leaving drag-and-drop to Essential", null);
				return true;
			}
			return false;
		}

		try {
			return getter.get().invoke(screen) != null;
		} catch (ReflectiveOperationException | RuntimeException e) {
			warnOnce("Could not query Essential's drag handler; leaving drag-and-drop to Essential", e);
			return true;
		}
	}

	private static void warnOnce(final String message, final Throwable cause) {
		if (!warned) {
			warned = true;
			LOGGER.warn(message, cause);
		}
	}
}
