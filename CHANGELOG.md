# Changelog

## [1.0.0] - Fabric 1.21.4

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `1.21.3` to Minecraft Fabric `1.21.4` (Java `21`, Gradle `8.10.2`, Fabric Loom `1.7.4`, Yarn `1.21.4+build.8`, Fabric Loader `>= 0.20.1`, Fabric API `0.119.4+1.21.4`).
- **WorldRenderer Matrix Capture (`WorldRendererMixin`):** Updated `@Inject` on `WorldRenderer#render` for Minecraft `1.21.4`'s updated signature `(ObjectAllocator, RenderTickCounter, boolean, Camera, GameRenderer, Matrix4f, Matrix4f)` (removing `LightmapTextureManager`) while preserving zero-allocation `positionMatrix` and `projectionMatrix` capture.
- **Source & Mixin Verification:** Verified API and Mixin bytecode compatibility with Minecraft `1.21.4` across `InGameHudMixin`, `WaypointHudRenderer`, `SimpleLocationMarkingClient`, `Screen#renderBackground`, `mouseScrolled`, and `ServerInfo#isRealm()`.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `1.21.4`.
