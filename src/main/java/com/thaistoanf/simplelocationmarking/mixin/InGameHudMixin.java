package com.thaistoanf.simplelocationmarking.mixin;

import com.thaistoanf.simplelocationmarking.SimpleLocationMarkingClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
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
            method = "renderCrosshair(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
            at = @At("HEAD")
    )
    private void simplelocationmarking$renderWaypointsBeforeCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        SimpleLocationMarkingClient.HUD_RENDERER.render(context, tickCounter);
    }
}
