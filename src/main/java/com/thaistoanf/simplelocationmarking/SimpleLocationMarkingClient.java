package com.thaistoanf.simplelocationmarking;

import com.mojang.blaze3d.platform.InputConstants;
import com.thaistoanf.simplelocationmarking.gui.LocationListScreen;
import com.thaistoanf.simplelocationmarking.render.ProjectionState;
import com.thaistoanf.simplelocationmarking.render.WaypointHudRenderer;
import com.thaistoanf.simplelocationmarking.waypoint.WaypointManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entrypoint of Simple Location Marking.
 */
public final class SimpleLocationMarkingClient implements ClientModInitializer {
    public static final @NonNull String MOD_ID = "simplelocationmarking";
    public static final Logger LOGGER = LoggerFactory.getLogger("Simple Location Marking");

    /** Translation key: {@code key.category.simplelocationmarking.main}. */
    public static final KeyMapping.@NonNull Category KEY_CATEGORY = KeyMapping.Category.register(id("main"));

    public static @NonNull Identifier id(@NonNull String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitializeClient() {
        KeyMapping openScreenKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.simplelocationmarking.open",
                InputConstants.KEY_P,
                KEY_CATEGORY
        ));

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CROSSHAIR,
                id("waypoints"),
                new WaypointHudRenderer()
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreenKey.consumeClick()) {
                if (client.player != null && client.level != null && client.gui.screen() == null) {
                    WaypointManager.ensureLoaded(client);
                    client.gui.setScreen(new LocationListScreen());
                }
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                runOnClientThread(client, () -> WaypointManager.onJoin(client)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                runOnClientThread(client, () -> {
                    WaypointManager.onDisconnect();
                    ProjectionState.invalidate();
                }));

        LOGGER.info("Simple Location Marking initialized");
    }

    private static void runOnClientThread(@NonNull Minecraft client, @NonNull Runnable task) {
        if (client.isSameThread()) {
            task.run();
        } else {
            client.execute(task);
        }
    }
}
