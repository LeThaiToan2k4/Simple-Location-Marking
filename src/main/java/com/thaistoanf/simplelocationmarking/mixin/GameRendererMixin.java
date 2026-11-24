package com.thaistoanf.simplelocationmarking.mixin;

import com.thaistoanf.simplelocationmarking.render.ProjectionState;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Captures the exact view rotation and world projection matrices (including FOV, view bobbing,
 * hurt tilt, and portal/nausea distortion) used for the world pass so the HUD can anchor
 * waypoint markers accurately to their world positions.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private GameRenderState gameRenderState;

    @ModifyArg(
            method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
            ),
            index = 0
    )
    private Matrix4f simplelocationmarking$captureProjection(Matrix4f projectionMatrix) {
        CameraRenderState camera = this.gameRenderState.levelRenderState.cameraRenderState;
        ProjectionState.capture(camera.viewRotationMatrix, projectionMatrix, camera.pos);
        return projectionMatrix;
    }
}
