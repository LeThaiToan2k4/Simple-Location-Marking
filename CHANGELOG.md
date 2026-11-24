# Changelog

## [1.0.0] - Fabric 26.3

### Changed

- **Target Minecraft & Toolchain:** Upgraded from Minecraft Fabric `26.2` to unobfuscated Minecraft Fabric `26.3` (Java `25`, Gradle `9.5.1`, no-remap Fabric Loom `1.17.21`, Fabric Loader `>= 0.20.1`, Fabric API `0.167.0+26.3`).
- **26.3 Input & Keybinding Migration:** Replaced removed `org.lwjgl.glfw.GLFW` imports and `InputConstants.Type.KEYSYM` references with `com.mojang.blaze3d.platform.InputConstants` key constants (`KEY_P`, `KEY_RETURN`, `KEY_NUMPADENTER`, `KEY_DELETE`, `KEY_UP`, `KEY_DOWN`) and `KeyMapping(String, int, Category)` across `SimpleLocationMarkingClient`, `LocationListScreen`, `AddWaypointScreen`, and `EditWaypointScreen`.
- **26.3 GameRenderer & RenderPearl Mixin Migration:** Updated `GameRendererMixin` injection target from `renderLevel(Lnet/minecraft/client/DeltaTracker;)V` and `com.mojang.blaze3d.buffers.GpuBufferSlice` to `renderLevel()V` and `com.mojang.renderpearl.api.buffers.GpuBufferSlice`.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `26.3`.
