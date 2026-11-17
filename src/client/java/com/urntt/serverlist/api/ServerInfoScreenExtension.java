package com.urntt.serverlist.api;

/**
 * Adds widgets to the Add Server and Edit Server Info screens.
 *
 * <p>Declare implementations under the {@value #ENTRYPOINT} entrypoint in {@code fabric.mod.json}:
 *
 * <pre>{@code
 * "entrypoints": {
 *   "serverlist": ["com.example.MyServerInfoExtension"]
 * }
 * }</pre>
 *
 * <p>{@link #init(ServerInfoScreenContext)} runs every time the screen is shown or resized, after vanilla and
 * other mods have set up the screen. Create new widgets on every call and read their initial state from
 * {@link ServerInfoScreenContext#serverData()}. Extensions run in the order of their mod ids.
 */
@FunctionalInterface
public interface ServerInfoScreenExtension {
	/** The entrypoint key under which implementations are declared. */
	String ENTRYPOINT = "serverlist";

	/**
	 * Adds this extension's widgets to the screen through {@code context}.
	 */
	void init(ServerInfoScreenContext context);
}
