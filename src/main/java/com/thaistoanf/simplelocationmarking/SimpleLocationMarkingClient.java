package com.thaistoanf.simplelocationmarking;

import com.thaistoanf.simplelocationmarking.gui.LocationListScreen;
import com.thaistoanf.simplelocationmarking.render.ProjectionState;
import com.thaistoanf.simplelocationmarking.render.WaypointHudRenderer;
import com.thaistoanf.simplelocationmarking.waypoint.WaypointManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entrypoint of Simple Location Marking.
 */
public final class SimpleLocationMarkingClient implements ClientModInitializer {
    public static final String MOD_ID = "simplelocationmarking";
    public static final Logger LOGGER = LoggerFactory.getLogger("Simple Location Marking");

    /** Translation key: {@code key.category.simplelocationmarking.main}. */
    public static final KeyBinding.Category KEY_CATEGORY = KeyBinding.Category.create(id("main"));

    private static KeyBinding openScreenKey;

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitializeClient() {
        openScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.simplelocationmarking.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreenKey.wasPressed()) {
                if (client.player != null && client.world != null && client.currentScreen == null) {
                    WaypointManager.ensureLoaded(client);
                    client.setScreen(new LocationListScreen());
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

        HudElementRegistry.attachElementBefore(VanillaHudElements.CROSSHAIR, id("waypoints"), new WaypointHudRenderer());

        LOGGER.info("Simple Location Marking initialized");
    }

    private static void runOnClientThread(MinecraftClient client, Runnable task) {
        if (client.isOnThread()) {
            task.run();
        } else {
            client.execute(task);
        }
    }
}

