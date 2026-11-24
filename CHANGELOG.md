# Changelog

## [1.0.0] - Fabric 1.21.10

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `1.21.9` to Minecraft Fabric `1.21.10` (Java `21`, Gradle `9.5.1`, Fabric Loom `1.14.10`, Yarn `1.21.10+build.3`, Fabric Loader `>= 0.20.1`, Fabric API `0.138.4+1.21.10`).
- **Source & Mixin Verification:** Verified full API and Mixin bytecode compatibility with Minecraft `1.21.10` across `WorldRendererMixin`, `InGameHudMixin`, `WaypointHudRenderer`, `GlassUi`, `HexColorPalette`, `AddWaypointScreen`, `EditWaypointScreen`, `LocationListScreen`, `SimpleLocationMarkingClient`, and `ServerInfo#isRealm()`.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `1.21.10`.
