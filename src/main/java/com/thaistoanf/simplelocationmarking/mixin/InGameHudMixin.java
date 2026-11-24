package com.thaistoanf.simplelocationmarking.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thaistoanf.simplelocationmarking.SimpleLocationMarkingClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders waypoint HUD markers immediately before the vanilla crosshair so the crosshair,
 * boss bar, chat, scoreboard, and player list remain cleanly layered on top.
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(
            method = "renderCrosshair(Lnet/minecraft/client/gui/DrawContext;F)V",
            at = @At("HEAD")
    )
    private void simplelocationmarking$renderWaypointsBeforeCrosshair(DrawContext context, float tickDelta, CallbackInfo ci) {
        SimpleLocationMarkingClient.HUD_RENDERER.render(context, tickDelta);
        context.draw();
        RenderSystem.enableBlend();
    }
}
