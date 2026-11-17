/**
 * The public API of serverlist. Everything in this package follows the mod's Semantic Versioning: breaking
 * changes only happen in a new major version. Classes outside this package are internal and may change at any
 * time.
 *
 * <p>The API lets other mods add widgets to the Add Server and Edit Server Info screens ({@code
 * ManageServerScreen}) without positioning them by hand. Implement {@link
 * com.urntt.serverlist.api.ServerInfoScreenExtension} and declare it under the {@code "serverlist"}
 * entrypoint in {@code fabric.mod.json}. The entrypoint is only loaded when serverlist is installed, so a mod
 * can use it as an optional dependency (declare {@code "suggests": {"serverlist": "*"}}).
 */
package com.urntt.serverlist.api;
