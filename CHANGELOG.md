# Changelog

## [1.0.0] - Fabric 26.2

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `26.1.2` to unobfuscated Minecraft Fabric `26.2` (Java `25`, Gradle `9.5.1`, no-remap Fabric Loom `1.17.21`, Fabric Loader `>= 0.20.1`, Fabric API `0.161.0+26.2`).
- **26.2 GUI & HUD API Migration:** Migrated active screen checks, screen transitions, and HUD visibility queries to Minecraft `26.2`'s restructured `Gui` and `Hud` classes (`Minecraft#gui#screen()`, `Minecraft#gui#setScreen(Screen)`, and `Minecraft#gui#hud#isHidden()`) across `SimpleLocationMarkingClient`, `WaypointHudRenderer`, `LocationListScreen`, `AddWaypointScreen`, and `EditWaypointScreen`.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `26.2`.
