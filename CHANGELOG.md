# Changelog

## [1.0.0] - Fabric 1.20.5

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `1.20.4` (Java `17`) to Minecraft Fabric `1.20.5` (Java `21`, Gradle `8.10.2`, Fabric Loom `1.7.4`, Yarn `1.20.5+build.1`, Fabric Loader `>= 0.20.1`, Fabric API `0.97.8+1.20.5`).
- **WorldRenderer Matrix Capture (`WorldRendererMixin`):** Adapted `@Inject` on `WorldRenderer#render` to Minecraft `1.20.5`'s new `(float, long, boolean, Camera, GameRenderer, LightmapTextureManager, Matrix4f, Matrix4f)` signature, capturing `positionMatrix` directly instead of `MatrixStack`.
- **HUD LayeredDrawer Pipeline (`InGameHudMixin`):** Migrated HUD injection to `@At("HEAD")` of `InGameHud#renderCrosshair(DrawContext, float)` to match Minecraft `1.20.5`'s `LayeredDrawer` architecture while rendering markers before the crosshair in both first-person and third-person perspectives.
- **Mod & Mixin Metadata:** Updated `fabric.mod.json` Java dependency to `>=21` and `simplelocationmarking.mixins.json` `compatibilityLevel` to `JAVA_21`.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `1.20.5` and Java `21`.
