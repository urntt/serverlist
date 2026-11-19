# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0+26.3] - 2026-11-19

### Added

- Drag saved servers up or down in the server list to reorder them.
- Right-click a server to edit it, and press Delete to delete the selected server. Both bindings can be changed in the Controls screen and do not conflict with in-game bindings on the same key.
- A search box above the server list's buttons that filters saved servers by name or address (Ctrl+F to focus it). Reordering is disabled while a search is active.
- A server list button in the pause menu, and an unbound key that opens the server list while playing. Joining a server from there leaves the current world or server first.
- Faster server list pings: up to 32 servers are pinged at once instead of 5.
- Servers entered as IP addresses skip the reverse DNS lookup when pinged or joined.
- Each server shows its latency in milliseconds.
- An API for other mods to add widgets to the Add Server and Edit Server Info screens, and automatic placement of widgets that mods add there by hand, so they no longer overlap. Done and Cancel sit side by side at the bottom of these screens to leave room for the widgets.
- A settings screen (through Mod Menu) and `config/serverlist.json` to turn each feature off.

### Fixed

- Moving a server with the vanilla arrows or Shift+arrow keys no longer reverts visually when the list is refreshed, for example when a LAN world is found.
