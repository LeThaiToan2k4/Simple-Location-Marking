# Changelog

## [1.0.0] - Fabric 1.21.3

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `1.21.2` to Minecraft Fabric `1.21.3` (Java `21`, Gradle `8.10.2`, Fabric Loom `1.7.4`, Yarn `1.21.3+build.2`, Fabric Loader `>= 0.20.1`, Fabric API `0.114.1+1.21.3`).
- **Source & Mixin Verification:** Verified full API and Mixin bytecode compatibility with Minecraft `1.21.3` across `WorldRendererMixin`, `InGameHudMixin`, `WaypointHudRenderer`, `SimpleLocationMarkingClient`, `Screen#renderBackground`, `mouseScrolled`, and `ServerInfo#isRealm()`.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `1.21.3`.
