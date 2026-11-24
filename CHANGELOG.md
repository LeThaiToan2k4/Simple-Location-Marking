# Changelog

## [1.0.0] - Fabric 26.1.2

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `26.1.1` to unobfuscated Minecraft Fabric `26.1.2` (Java `25`, Gradle `9.5.1`, no-remap Fabric Loom `1.17.21`, Fabric Loader `>= 0.20.1`, Fabric API `0.155.3+26.1.2`).
- **Source, Mixin & Nullability Verification:** Verified full API and Mixin bytecode compatibility with Minecraft `26.1.2` across `GameRendererMixin`, `WaypointHudRenderer`, `GlassUi`, `HexColorPalette`, `LocationListScreen`, `AddWaypointScreen`, `EditWaypointScreen`, `SimpleLocationMarkingClient`, and `WaypointManager`, with JSpecify `@NonNull`/`@Nullable` annotations and clean lambda callbacks.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `26.1.2`.
