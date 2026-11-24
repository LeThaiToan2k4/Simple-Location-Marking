# Changelog

## [1.0.0] - Fabric 1.21.2

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `1.21.1` to Minecraft Fabric `1.21.2` (Java `21`, Gradle `8.10.2`, Fabric Loom `1.7.4`, Yarn `1.21.2+build.1`, Fabric Loader `>= 0.20.1`, Fabric API `0.106.1+1.21.2`).
- **WorldRenderer Matrix Capture (`WorldRendererMixin`):** Updated `@Inject` on `WorldRenderer#render` for Minecraft `1.21.2`'s new leading `ObjectAllocator` (`net.minecraft.client.util.ObjectAllocator`) parameter while preserving zero-allocation `positionMatrix` and `projectionMatrix` capture.
- **Source & Mixin Verification:** Verified API and Mixin bytecode compatibility with Minecraft `1.21.2` across `InGameHudMixin`, `WaypointHudRenderer`, `SimpleLocationMarkingClient`, `Screen#renderBackground`, `mouseScrolled`, and `ServerInfo#isRealm()`.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `1.21.2`.
