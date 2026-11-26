# serverlist

A client-side Fabric mod for Minecraft: Java Edition that makes the multiplayer server list more usable.

## Features

- **Drag to reorder.** Drag a saved server up or down in the server list. The new order is saved right away.
- **Edit and delete shortcuts.** Right-click a server to edit it, or select a server and press <kbd>Delete</kbd> to delete it. Both do exactly what the Edit and Delete buttons do, including the confirmation before deleting.
- **Search.** A search box above the buttons filters saved servers by name or address. Press <kbd>Ctrl</kbd>+<kbd>F</kbd> (<kbd>Cmd</kbd>+<kbd>F</kbd> on macOS) to jump to it. While a search is active, servers cannot be reordered, because the visible order is incomplete.
- **Server list in game.** The pause menu has a server list button at the end of its row of icon buttons, and a key (unbound by default) opens the server list while playing. "Back" returns to where you were. Joining a server from there first leaves your current world or server, the same way the Disconnect button does (a singleplayer world is saved), and then connects.
- **Faster, more informative pings.**
  - Vanilla pings at most five servers at a time, so a few slow or unreachable servers can keep the rest at "Pinging...". The mod pings up to 32 at a time; idle pinger threads stop after 30 seconds.
  - Servers entered as IP addresses no longer trigger a reverse DNS lookup when pinged or joined, which can take several seconds on some networks.
  - Each server shows its latency in milliseconds next to its player count, colored like the signal bars.
- **Room for other mods on the Add/Edit Server screen.** Done and Cancel sit side by side at the bottom of the Add Server and Edit Server Info screens, which leaves room for more options above them. Mods can add their own buttons there through the mod's API, and the mod also arranges buttons that other mods place there by hand, so they no longer overlap.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) and [Fabric API](https://modrinth.com/mod/fabric-api).
2. Download the jar from this repository's [Releases](https://github.com/urntt/serverlist/releases) page. Each release supports a single Minecraft version, shown after the `+` in its version number. For example, `1.0.0+26.3` is for Minecraft 26.3.
3. Put the jar into your `.minecraft/mods` folder.

Optional:

- [Mod Menu](https://modrinth.com/mod/modmenu) opens the settings screen from the mod list.
- [OneConfig](https://modrinth.com/mod/oneconfig) can manage the settings instead (see [Settings](#settings)).

## Usage

### Key bindings

The bindings are listed in **Options → Controls → Key Binds** under **Server List**:

| Binding | Default | What it does |
| --- | --- | --- |
| Edit Server | Right mouse button | Opens Edit Server Info for the server under the cursor (mouse) or the selected server (keyboard). |
| Delete Server | <kbd>Delete</kbd> | Asks to delete the selected server (keyboard) or the server under the cursor (mouse). |
| Open Server List | Unbound | Opens the server list while playing. |
| Open Server List Settings | Unbound | Opens the settings screen while playing. |

Edit Server and Delete Server only work inside the server list, so they never conflict with in-game bindings on the same key, such as the right mouse button for using items.

### Settings

Open the settings screen from Mod Menu, or bind **Open Server List Settings** in the Controls screen. The button at the top of the screen selects the mode:

| Mode | Effect |
| --- | --- |
| Disabled | Every feature is off: the server list, the pause menu and server pings work like vanilla, and the Edit Server, Delete Server and Open Server List bindings do nothing. Widgets that other mods add to the Add Server and Edit Server Info screens are still arranged, because those mods rely on it. |
| Custom | The settings on this screen apply. |
| OneConfig (default) | The settings registered with OneConfig apply, and are changed in OneConfig. This screen shows them greyed out and links to the mod's OneConfig page. Turning the mod off on its OneConfig card works like Disabled. Only offered when OneConfig is installed. |

OneConfig mode is the default, but it needs OneConfig: without OneConfig, the mode button shows Custom and the custom settings apply. Once OneConfig is installed, OneConfig mode applies, unless you have picked another mode.

In custom mode, every feature that changes vanilla behavior can be turned off. All of them are on by default. Settings are saved to `config/serverlist.json` as soon as you change them.

| Setting | Key in `serverlist.json` |
| --- | --- |
| Drag to Reorder | `dragToReorder` |
| Search Box | `searchBox` |
| Show Latency | `showPingMs` |
| Pause Menu Button | `pauseMenuButton` |
| Faster Pings | `fasterServerPings` |
| Skip Reverse DNS for IPs | `skipReverseDnsForIpAddresses` |

## Compatibility

- **Essential** has its own drag-and-drop for the server list, but it stops working after you return from the Edit, Delete, Add, or Direct Connection screen. While Essential's drag-and-drop works, it handles dragging; once it stops, this mod takes over. Essential's "Show to Friends" button on the Edit Server Info screen is placed in the shared area like any other mod's button.
- **Server Pinger Fixer**, **FastServerPings**, and **Fast IP Ping** overlap with the ping features. If you use one of them, you can turn off the matching setting.

## For mod developers

Mods can add widgets to the Add Server and Edit Server Info screens without positioning them. serverlist places them below vanilla's "Server Resource Packs" button and above Done and Cancel, which sit side by side at the bottom of the screen, and makes the area scroll when the screen is too small.

Implement `com.urntt.serverlist.api.ServerInfoScreenExtension`:

```java
public final class MyServerInfoExtension implements ServerInfoScreenExtension {
	@Override
	public void init(ServerInfoScreenContext context) {
		ServerData server = context.serverData();
		context.addRow(CycleButton.onOffBuilder(isEnabled(server))
				.create(Component.translatable("mymod.option"), (button, value) -> setEnabled(server, value)));
		context.onSave(() -> commit(server));
	}
}
```

Declare it under the `serverlist` entrypoint and suggest serverlist, so your mod also works without it:

```json
{
	"entrypoints": {
		"serverlist": ["com.example.MyServerInfoExtension"]
	},
	"suggests": {
		"serverlist": "*"
	}
}
```

- `init` runs every time the screen is shown or resized. Create new widgets on each call.
- `addRow(widget)` adds a full-width row; `addRow(left, right)` adds a row with two half-width widgets. Rows appear in the order of the providing mods' ids.
- `serverData()` is the server being edited. On the Edit Server Info screen it is vanilla's working copy: vanilla saves it when the player presses Done and discards it on Cancel.
- `onSave` actions run when the player presses Done, before the server list is saved.

The API lives in `com.urntt.serverlist.api` and follows the mod's Semantic Versioning. serverlist is only published on GitHub Releases, so download the jar and compile against it, for example with `compileOnly files("libs/serverlist-<version>.jar")`.

Widgets that a mod adds to the screen without the API are also arranged, as long as they sit in the screen's central column below the address field.

## Development

Building requires the JDK version set by `java_version` in `gradle.properties`.

Build the mod:

```bash
./gradlew build
```

The jar is written to `build/libs/`.

Run the client game tests, which start Minecraft and a local dedicated server and check every feature: dragging (also after returning from other screens), the key bindings and their rebinding, the search and its reorder lock, the pause menu button, switching servers from in game, the ping changes, the Add/Edit Server area and its API, and the settings and their modes:

```bash
./gradlew runClientGameTest
```

The game tests need a display. On a headless Linux machine, run them under Xvfb. Xvfb offers no sRGB-capable OpenGL visuals, so install Mesa's Vulkan driver (`mesa-vulkan-drivers` on Ubuntu) for the game to fall back to:

```bash
xvfb-run -a -s "-screen 0 1920x1080x24" ./gradlew runClientGameTest
```

Screenshots taken by the tests are saved to `build/run/clientGameTest/screenshots/`.

By default, the development client and the tests run without OneConfig. Add `-PoneConfigRuntime=true` to run them with OneConfig installed, which offers the OneConfig mode:

```bash
./gradlew runClientGameTest -PoneConfigRuntime=true
```

The mod icon is rendered from `art/icon.svg`.

## License

[MIT](LICENSE)
