# Changelog

## [1.0.0] - Fabric 1.20.2

### Changed
- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `1.20.1` to Minecraft Fabric `1.20.2` (Java `17`, Gradle `8.10.2`, Fabric Loom `1.7.4`, Yarn `1.20.2+build.4`, Fabric Loader `>= 0.20.1`, Fabric API `0.91.6+1.20.2`).
- **Screen Background Rendering & Layering:** Migrated `AddWaypointScreen`, `EditWaypointScreen`, and `LocationListScreen` to Minecraft `1.20.2`'s `Screen#renderBackground(DrawContext, int, int, float)` signature, drawing the translucent backdrop and glass panel chrome inside `renderBackground` and invoking `super.render(...)` first so widgets layer cleanly above the panels without double-darkening from `Screen#render`'s built-in background pass.
- **Scroll Input API:** Updated `LocationListScreen#mouseScrolled` to Minecraft `1.20.2`'s 4-parameter signature `(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)` using `verticalAmount`.
- **Realm Detection:** Updated `WaypointManager#computeStorageKey` from the removed `MinecraftClient#isConnectedToRealms()` method to `ServerInfo#isRealm()`.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `1.20.2`.

## [1.0.0] - Fabric 1.20.1

### Changed
- **Target Minecraft & Toolchain:** Backported from Minecraft Fabric `1.21.11` (Java `21`, Gradle `9.5.1`, Loom `1.14.10`) to Minecraft Fabric `1.20.1` (Java `17`, Gradle `8.10.2`, Fabric Loom `1.7.4`, Yarn `1.20.1+build.10`, Fabric Loader `>= 0.20.1`, Fabric API `0.92.12+1.20.1`).
- **Mixin Compatibility & Camera Matrix Capture:** Lowered Mixin compatibility level to `JAVA_17` and adapted `WorldRendererMixin` to Minecraft `1.20.1`'s `WorldRenderer#render` signature using `MatrixStack` and `Camera#getPos()`.
- **HUD Rendering Layer & Timing:** Replaced the `1.21.x` `HudElementRegistry` / `RenderTickCounter` integration with `InGameHudMixin` injecting immediately before `InGameHud#renderCrosshair` and using `MinecraftClient#getLastFrameDuration()` for frame-rate-independent hover expansion.
- **GUI, Matrix Stack & Input APIs:** Migrated `DrawContext#getMatrices()` usage from JOML `Matrix3x2fStack` to `MatrixStack`, adapted `Screen` background rendering (`renderBackground(DrawContext)`) and mouse/keyboard callbacks (`mouseClicked`, `mouseDragged`, `mouseReleased`, `mouseScrolled`, `keyPressed`) to `1.20.1` signatures, and updated `PositionedSoundInstance.ui` calls to `PositionedSoundInstance.master`.
- **Keybind, Identifier & Realm Detection:** Adapted `KeyBinding` category registration to string translation keys, `Identifier.of` to `new Identifier`, `Window#getScaleFactor()` double-to-int conversion, and `ServerInfo#isRealm()` to `MinecraftClient#isConnectedToRealms()`.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, and `README.md` for Minecraft `1.20.1` and Java `17`.
