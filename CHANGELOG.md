# Changelog

## [1.0.0] - Fabric 1.20.4

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `1.20.3` to Minecraft Fabric `1.20.4` (Java `17`, Gradle `8.10.2`, Fabric Loom `1.7.4`, Yarn `1.20.4+build.3`, Fabric Loader `>= 0.20.1`, Fabric API `0.97.3+1.20.4`).
- **Source & Mixin Verification:** Verified full API and Mixin bytecode compatibility with Minecraft `1.20.4` across `InGameHudMixin`, `WorldRendererMixin`, `Screen#renderBackground` layering, `mouseScrolled`, and `ServerInfo#isRealm()`.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `1.20.4`.
