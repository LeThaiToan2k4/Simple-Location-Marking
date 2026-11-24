# Changelog

## [1.0.0] - Fabric 26.1

### Changed

- **Target Minecraft & Toolchain:** Upgraded to unobfuscated Minecraft Fabric `26.1` with Java `25`, Gradle `9.5.1`, no-remap Fabric Loom (`net.fabricmc.fabric-loom` `1.17.21`), Fabric Loader `>= 0.20.1`, and Fabric API `0.145.1+26.1`.
- **Unobfuscated Mojang Mappings Migration:** Migrated all client, GUI, rendering, and waypoint management classes from Yarn mappings to official unobfuscated Minecraft `26.1` APIs (`Minecraft`, `Component`, `Font`, `Mth`, `BlockPos`, `Vec3`, `KeyMapping`, `KeyMappingHelper`, `ServerData`, `IntegratedServer`, `LevelResource`, and `ResourceKey<Level>`).
- **26.1 Rendering, HUD & GUI Extraction Architecture:**
  - Replaced `InGameHudMixin` with Fabric API `HudElementRegistry.attachElementBefore(VanillaHudElements.CROSSHAIR, ...)` and migrated `WaypointHudRenderer` to `HudElement#extractRenderState(GuiGraphicsExtractor, DeltaTracker)`.
  - Replaced `WorldRendererMixin` with `GameRendererMixin` (`JAVA_25` compatibility), capturing `CameraRenderState#viewRotationMatrix`, camera position, and the bobbed/effect-adjusted world projection matrix from `GameRenderer#renderLevel`.
  - Updated `GlassUi`, `HexColorPalette`, `LocationListScreen`, `AddWaypointScreen`, and `EditWaypointScreen` to `GuiGraphicsExtractor`, `Screen#extractBackground`/`extractRenderState`, `MouseButtonEvent`/`KeyEvent`, `Button`, `EditBox`, and custom integer input filtering for coordinate fields.
- **CI Workflows & Documentation:** Updated `.github/workflows/build.yml`, `.github/workflows/release.yml`, `README.md`, and `CHANGELOG.md` for Minecraft `26.1` and Java `25`.
