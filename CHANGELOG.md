# Changelog

## [1.0.0] - Fabric 1.20.6

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `1.20.5` to Minecraft Fabric `1.20.6` (Java `21`, Gradle `8.10.2`, Fabric Loom `1.7.4`, Yarn `1.20.6+build.3`, Fabric Loader `>= 0.20.1`, Fabric API `0.100.8+1.20.6`).
- **Source & Mixin Verification:** Verified full API and Mixin bytecode compatibility with Minecraft `1.20.6` across `WorldRendererMixin` (`positionMatrix` and `projectionMatrix` capture), `InGameHudMixin` (`LayeredDrawer` `renderCrosshair(DrawContext, float)` injection), `Screen#renderBackground` layering, `mouseScrolled`, and `ServerInfo#isRealm()`.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `1.20.6`.
