# Changelog

## [1.0.0] - Fabric 1.21.5

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `1.21.4` to Minecraft Fabric `1.21.5` (Java `21`, Gradle `8.12`, Fabric Loom `1.10-SNAPSHOT`, Yarn `1.21.5+build.1`, Fabric Loader `>= 0.20.1`, Fabric API `0.128.2+1.21.5`).
- **HUD & RenderTickCounter Migration:**
  - Removed obsolete `RenderSystem.enableBlend()` call in `InGameHudMixin` following Minecraft `1.21.5`'s Blaze3D `RenderPipeline` refactor (`DrawContext` manages blend state via `RenderLayer`).
  - Updated `WaypointHudRenderer` from `RenderTickCounter#getLastDuration()` to `RenderTickCounter#getDynamicDeltaTicks()`.
- **Source & Mixin Verification:** Verified full API and Mixin bytecode compatibility with Minecraft `1.21.5` across `WorldRendererMixin`, `InGameHudMixin`, `WaypointHudRenderer`, `SimpleLocationMarkingClient`, `Screen#renderBackground`, `mouseScrolled`, and `ServerInfo#isRealm()`.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `1.21.5`.
