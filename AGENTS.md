# AGENTS.md

Instructions for AI coding agents working in this repository. `CLAUDE.md` imports this file, so keep all agent guidance here.

## Project

`serverlist` is a client-side [Fabric](https://fabricmc.net/) mod for Minecraft: Java Edition that makes the multiplayer server list more usable: drag-and-drop reordering, rebindable shortcuts for editing and deleting servers, a search box, faster and more informative server pings, quick access from the pause menu and from in game, and a shared area on the Add/Edit Server screen where other mods can add their widgets without overlapping each other.

## Project decisions

These decisions are settled. Do not deviate from them without the user's explicit approval.

### Minecraft and toolchain

- Development started on Minecraft 26.3 with Java 25. `gradle.properties` is the single source of truth for the mod, Minecraft, Fabric Loader, Loom, Fabric API, Mod Menu, and Java versions. `build.gradle`, `fabric.mod.json`, the mixin config, and the CI workflows read them from there; do not restate them elsewhere.
- Follow the latest official Fabric template ([FabricMC/fabric-example-mod](https://github.com/FabricMC/fabric-example-mod), also available from the [template generator](https://fabricmc.net/develop/template/)): the `net.fabricmc.fabric-loom` Gradle plugin, Mojang's official names with no `mappings` dependency, and `implementation` (not `modImplementation`) for dependencies. Do not use Yarn.
- Pin `loom_version` to a release version instead of the template's `-SNAPSHOT`, so builds are reproducible.
- Target only the latest stable (release) Minecraft version. Updates, fixes, and new features are always developed against it. Snapshots, pre-releases, and release candidates are not supported targets.
- Do not maintain older Minecraft versions and do not set up multi-version builds (no per-version branches, no Stonecutter or other preprocessors). When a new stable version is released, port the mod to it and drop the previous one.

### Identity

| Item | Value |
| --- | --- |
| Mod ID | `serverlist` |
| Display name | `serverlist` |
| Maven group | `com.urntt` |
| Base package | `com.urntt.serverlist` |
| Version format | `<SemVer>+<Minecraft version>`, for example `1.0.0+26.3` |
| License | MIT |

The mod version itself follows [Semantic Versioning](https://semver.org/); the `+<Minecraft version>` suffix is build metadata naming the Minecraft version the build targets. Version numbers start at `1.0.0`. The public API in `com.urntt.serverlist.api` is part of the versioned surface: breaking it requires a major version bump.

### Distribution

- Releases are published only as GitHub Releases. Do not publish to Modrinth, CurseForge, Maven repositories, or any other platform, and do not add publishing tooling for them.

### Scope and behavior

- Client-only: `fabric.mod.json` declares `"environment": "client"`. There is no server-side component and no networking of its own. The mod only changes the client's menus and how the client pings servers in the server list; it does not change gameplay.
- Features:
  - Drag-and-drop reordering of saved servers in the server list, saved immediately.
  - Rebindable "edit server" (default: right mouse button on a server) and "delete server" (default: Delete with a server selected) actions. They are exactly equivalent to selecting the server and pressing the vanilla Edit or Delete button, including the confirmation dialog. They are listed in the vanilla Controls screen and never conflict with in-game key bindings, because they only act inside the server list.
  - A search box above the footer buttons that filters saved servers by name or address. While a filter is active, every way of reordering (drag-and-drop, vanilla move arrows, Shift+arrow keys, other mods' drag-and-drop) is disabled, because vanilla and other mods use the visible position as the saved position.
  - A server list button in the pause menu's icon row, and a key binding (unbound by default) that opens the server list in game. The current world stays open while the list is shown; "Back" returns to where the player came from. Joining a server from that list first leaves the current world like the vanilla Disconnect button (saving a singleplayer world), then connects.
  - Server pings: more concurrent pings than vanilla's five (bounded, idle threads time out), no reverse DNS lookups for literal IP addresses, and the latency in milliseconds shown on each server entry.
  - A shared area on the Add/Edit Server screen (`ManageServerScreen`) for other mods' widgets: a public entrypoint API, plus automatic placement of widgets that other mods add without the API (for example Essential's "Show to Friends" button). Without extra widgets the screen looks exactly like vanilla.
- Every feature that changes vanilla behavior has a switch in the configuration file, enabled by default.
- Possible future features (not planned for now, but not excluded): server groups or folders; periodic automatic refresh with the last known status shown while re-pinging (stale-while-revalidate cache) and deduplicated pings.
- Out of scope: anything that changes gameplay or talks to servers beyond vanilla's status ping.

### Compatibility

- Essential (mod id `essential`) has its own drag-and-drop for the server list, but its handler is created in `init()` and destroyed in `removed()`. Since 26.x screens only run `init()` the first time they are shown, Essential's handler is gone after returning from the Edit, Delete, Add, or Direct Connection screens. Before starting a drag, check whether Essential's handler exists on the screen (`essential$getDragHandlerOrNull()`, via reflection): if it does, let Essential handle the drag; if it does not, use this mod's. Never run both.
- Essential adds a "Show to Friends" button to `ManageServerScreen` at hard-coded positions and makes the server list header 32 pixels taller for its tabs. The extension area must place Essential's button like any other foreign widget, and the server list's header must be left to Essential.
- Server Pinger Fixer, FastServerPings, and Fast IP Ping overlap with the ping features. Each overlapping feature has a configuration switch, so players can turn ours off.
- Read other mods' source code only for compatibility research. Never copy it.

### Localization

- All user-facing text, including key binding names, the key binding category, the search box hint, and the configuration screen, uses translation keys. Never hard-code display strings. Reuse vanilla translation keys where vanilla already has the exact text (for example `menu.multiplayer` and `multiplayer.status.ping`).
- Provide translations for `en_us` and `zh_cn`, and keep both complete whenever a translation key is added or changed.

### Dependencies

- Required: Fabric Loader and Fabric API.
- Optional: Mod Menu, declared under `suggests` in `fabric.mod.json`. The mod must load and work normally without it, so Mod Menu classes may only be referenced from the Mod Menu entrypoint.
- Configuration is hand-written without a config library: a JSON file in the Fabric config directory, serialized with Gson (bundled with Minecraft). The configuration screen uses vanilla widgets.
- Do not add other dependencies without the user's explicit approval.

### Implementation

- Language: Java only.
- Source sets: `src/main` holds only `fabric.mod.json` and the icon. All code and client resources live in `src/client`, and the client game tests live in `src/gametest`.
- Mixins: prefer the MixinExtras injectors bundled with Fabric Loader (for example `@ModifyExpressionValue` and `@WrapOperation`) over `@Redirect` and `@Overwrite`, to stay compatible with other mods and keep porting work small. Prefix mixin handler methods and added members with `serverlist$`.
- Since 26.x, `Screen.init(int, int)` runs `init()` only the first time a screen instance is shown and only `repositionElements()` afterwards. Never tie per-screen state to `init()` alone; attach it in Fabric's `ScreenEvents.AFTER_INIT`, which fires on every show and resize.

### Testing

- The client game tests in `src/gametest` start Minecraft and check every feature: drag-and-drop (including after returning from the Edit, Delete, and Add screens), the edit and delete bindings and their rebinding, the search filter and its reorder lock, the pause menu button, the open key, switching servers from in game, the ping changes, the extension area and its API, and the configuration. Keep them passing and extend them when behavior changes.
- The tests use doubles from the test mod instead of real mods and networks: `TestServerInfoExtension` (a mod using the API), `ForeignServerInfoWidgetMixin` (a mod placing a widget by hand, like Essential), `FakeEssentialServerListMixin` (Essential's drag handler, switchable), and `SlowServerPingMixin` (servers whose pings hold a pinger thread, like a slow DNS lookup). Server-dependent tests run against one local dedicated server.
- Test-writing notes: the test input API sends key events without modifiers, so send shortcuts such as Ctrl+F to `KeyboardHandler.keyPress` directly; opening a screen re-centers the cursor, so move it away after opening a screen and before a screenshot; wait about 7 ticks between mouse presses so they do not count as a double click; and use `TestSupport.useRoomyWindow` when every server or widget must be visible at once.
- To check that the tests can fail, break one feature at a time (for example disable a mixin's effect or skip an event registration), run the tests, confirm the expected assertion fails, and restore the code.
- After porting to a new Minecraft version, run the client game tests. A successful build does not prove that the mixins still have the intended effect.
- `README.md` describes how to run them, including on a headless machine.

### CI, releases, and changelog

- GitHub Actions (`.github/workflows/build.yml`) builds the project and runs the client game tests on every push and pull request.
- Maintain `CHANGELOG.md` following [Keep a Changelog](https://keepachangelog.com/). Record every user-visible change under `Unreleased` in the same change that introduces it.
- `.github/workflows/release.yml` builds the mod and publishes a GitHub Release for the project version, with the jar attached and the matching `CHANGELOG.md` section as release notes. It runs when a tag `v<version>` (for example `v1.0.0+26.3`) is pushed, or when started manually on a branch, in which case it creates that tag on the branch's latest commit. It fails if a pushed tag does not match the project version, if the changelog has no section for the version, or if the release already exists.
- Release only when the user asks. To release, set `mod_version` in `gradle.properties`, rename `Unreleased` in `CHANGELOG.md` to `[<version>] - <YYYY-MM-DD>` above a new empty `Unreleased` section, commit, and push. Then start the release workflow on `main`. Claude Code cloud sessions cannot push tags, so start the workflow through the GitHub Actions API instead.

## Engineering principles

- Fix root causes, not symptoms. Diagnose the underlying cause before implementing a permanent fix. If an immediate mitigation is necessary, treat it as temporary and follow through with a root-cause fix.
- Prefer configuration-driven design for values that are expected to vary by environment, deployment, or product requirements. Avoid unexplained or duplicated magic values, but do not introduce configuration where a well-named constant is the clearer source of truth.
- Preserve a single source of truth and clear ownership for data, state, configuration, business logic, and authoritative documentation. Avoid duplicating canonical information across multiple locations.
- Do not maintain parallel legacy and replacement implementations without an explicit migration and removal plan.

## Documentation

- Keep documentation aligned with the code. When a code change affects documented behavior, APIs, architecture, configuration, workflows, or usage, update the relevant documentation in the same change.
- Keep each document's responsibility clear. For example, use `README.md` for project overview and usage, and `VISION.md` for product direction, architectural principles, or long-term decisions.
- Always specify a language identifier for fenced code blocks in Markdown.

## Language

- Communicate with the user in Chinese, including explanations, progress updates, and user-facing planning.
- Use English for development artifacts, including source code, comments, docstrings, documentation, READMEs, Git branch names, commit messages, and other deliverables intended to live in the repository.

## Git

- Do not change or override the Git author or committer identity. When an identity must be configured for commits created during the task, use:
  - Name: `urntt`
  - Email: `urntts@gmail.com`
- Do all actions on the user's behalf. Do not rewrite existing commit authorship unless explicitly requested. Do not add `Co-Authored-By` trailers or session links to commit messages or pull request descriptions.
- Develop on `main` and push directly to it. Branches and pull requests are not required.
- Because changes land on `main` without review, make sure `./gradlew build` and the client game tests pass locally before pushing.
- If a branch is used, give it a category-based prefix that reflects the purpose of the change, such as `feat/`, `fix/`, `refactor/`, `docs/`, `test/`, or `chore/`.
- Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification for commit messages.
