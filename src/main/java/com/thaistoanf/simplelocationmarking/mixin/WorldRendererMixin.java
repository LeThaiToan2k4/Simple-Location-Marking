package com.thaistoanf.simplelocationmarking.mixin;

import com.thaistoanf.simplelocationmarking.render.ProjectionState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures the exact view rotation and projection matrices used for the world pass so the HUD
 * can anchor waypoint markers to their world positions.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Inject(
            method = "render(FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
            at = @At("HEAD")
    )
    private void simplelocationmarking$captureMatrices(float tickDelta, long limitTime,
                                                       boolean renderBlockOutline, Camera camera,
                                                       GameRenderer gameRenderer,
                                                       LightmapTextureManager lightmapTextureManager,
                                                       Matrix4f positionMatrix, Matrix4f projectionMatrix,
                                                       CallbackInfo ci) {
        ProjectionState.capture(positionMatrix, projectionMatrix, camera.getPos());
    }
}

