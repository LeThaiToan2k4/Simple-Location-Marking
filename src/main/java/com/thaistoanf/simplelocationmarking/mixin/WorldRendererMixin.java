package com.thaistoanf.simplelocationmarking.mixin;

import com.thaistoanf.simplelocationmarking.render.ProjectionState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
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
            method = "render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
            at = @At("HEAD")
    )
    private void simplelocationmarking$captureMatrices(ObjectAllocator allocator,
                                                       RenderTickCounter tickCounter,
                                                       boolean renderBlockOutline, Camera camera,
                                                       GameRenderer gameRenderer,
                                                       Matrix4f positionMatrix, Matrix4f projectionMatrix,
                                                       CallbackInfo ci) {
        ProjectionState.capture(positionMatrix, projectionMatrix, camera.getPos());
    }
}

