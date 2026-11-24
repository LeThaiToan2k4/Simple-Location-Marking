# Changelog

## [1.0.0] - Fabric 1.21

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `1.20.6` to Minecraft Fabric `1.21` (Java `21`, Gradle `8.10.2`, Fabric Loom `1.7.4`, Yarn `1.21+build.9`, Fabric Loader `>= 0.20.1`, Fabric API `0.102.0+1.21`).
- **RenderTickCounter & Identifier Migration:**
  - Updated `SimpleLocationMarkingClient#id` from `new Identifier(...)` to `Identifier.of(...)`.
  - Migrated `WorldRendererMixin` to `WorldRenderer#render(RenderTickCounter, boolean, Camera, GameRenderer, LightmapTextureManager, Matrix4f, Matrix4f)`.
  - Migrated `InGameHudMixin` and `WaypointHudRenderer` to `InGameHud#renderCrosshair(DrawContext, RenderTickCounter)` and `RenderTickCounter#getLastDuration()`.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `1.21`.
