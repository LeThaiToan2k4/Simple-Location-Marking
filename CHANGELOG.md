# Changelog

## [1.0.0] - Fabric 1.21.9

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `1.21.8` to Minecraft Fabric `1.21.9` (Java `21`, Gradle `9.5.1`, Fabric Loom `1.14.10`, Yarn `1.21.9+build.1`, Fabric Loader `>= 0.20.1`, Fabric API `0.134.1+1.21.9`).
- **WorldRenderer Matrix Capture (`WorldRendererMixin`):** Updated `@Inject` on `WorldRenderer#render` for Minecraft `1.21.9`'s `(ObjectAllocator, RenderTickCounter, boolean, Camera, Matrix4f, Matrix4f, Matrix4f, GpuBufferSlice, Vector4f, boolean)` signature (adding `cullProjectionMatrix`) while preserving zero-allocation `positionMatrix` and `projectionMatrix` capture.
- **Input Record & KeyBinding.Category Migration:**
  - Updated `SimpleLocationMarkingClient` to use `KeyBinding.Category.create(id("main"))`.
  - Migrated `AddWaypointScreen`, `EditWaypointScreen`, and `LocationListScreen` to Minecraft `1.21.9`'s `Click` (`mouseClicked`, `mouseDragged`, `mouseReleased`) and `KeyInput` (`keyPressed`) input records and `GuiRenderState` panel layering.
  - Updated `HexColorPalette` for integer `Window#getScaleFactor()`.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `1.21.9`.
