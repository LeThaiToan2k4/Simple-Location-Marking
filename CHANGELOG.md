# Changelog

## [1.0.0] - Fabric 1.21.6

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `1.21.5` to Minecraft Fabric `1.21.6` (Java `21`, Gradle `8.12`, Fabric Loom `1.10-SNAPSHOT`, Yarn `1.21.6+build.1`, Fabric Loader `>= 0.20.1`, Fabric API `0.128.2+1.21.6`).
- **WorldRenderer Matrix Capture (`WorldRendererMixin`):** Updated `@Inject` on `WorldRenderer#render` for Minecraft `1.21.6`'s `(ObjectAllocator, RenderTickCounter, boolean, Camera, Matrix4f, Matrix4f, GpuBufferSlice, Vector4f, boolean)` signature while preserving zero-allocation `positionMatrix` and `projectionMatrix` capture.
- **2D GuiRenderState & Matrix3x2fStack Migration:**
  - Updated `GlassUi`, `HexColorPalette`, and `WaypointHudRenderer` from `MatrixStack` (`push`/`pop`/`translate(x,y,z)`/`scale(x,y,z)`) to JOML `Matrix3x2fStack` (`pushMatrix`/`popMatrix`/`translate(x,y)`/`scale(x,y)`) returned by `DrawContext#getMatrices()`.
  - Removed obsolete `DrawContext#draw()` call in `InGameHudMixin` for Minecraft `1.21.6`'s `GuiRenderState` pipeline.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `1.21.6`.
