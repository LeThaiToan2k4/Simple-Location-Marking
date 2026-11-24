# Changelog

## [1.0.0] - Fabric 26.1.1

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `26.1` to unobfuscated Minecraft Fabric `26.1.1` (Java `25`, Gradle `9.5.1`, no-remap Fabric Loom `1.17.21`, Fabric Loader `>= 0.20.1`, Fabric API `0.145.4+26.1.1`).
- **Source, Mixin & Nullability Verification:** Verified full API and Mixin bytecode compatibility with Minecraft `26.1.1` across `GameRendererMixin`, `WaypointHudRenderer`, `GlassUi`, `HexColorPalette`, `LocationListScreen`, `AddWaypointScreen`, `EditWaypointScreen`, `SimpleLocationMarkingClient`, and `WaypointManager`, with complete JSpecify `@NonNull`/`@Nullable` annotations for warning-free builds and IDE null analysis.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `26.1.1`.
