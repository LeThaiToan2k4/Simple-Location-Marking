# Changelog

## [1.0.0] - Fabric 1.21.8

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `1.21.7` to Minecraft Fabric `1.21.8` (Java `21`, Gradle `8.12`, Fabric Loom `1.10-SNAPSHOT`, Yarn `1.21.8+build.1`, Fabric Loader `>= 0.20.1`, Fabric API `0.136.1+1.21.8`).
- **Source & Mixin Verification:** Verified full API and Mixin bytecode compatibility with Minecraft `1.21.8` across `WorldRendererMixin`, `InGameHudMixin`, `WaypointHudRenderer`, `GlassUi`, `HexColorPalette`, `SimpleLocationMarkingClient`, `Screen#renderBackground`, `mouseScrolled`, and `ServerInfo#isRealm()`.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `1.21.8`.
