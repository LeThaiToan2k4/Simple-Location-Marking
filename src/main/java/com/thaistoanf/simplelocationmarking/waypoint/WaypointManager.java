package com.thaistoanf.simplelocationmarking.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.thaistoanf.simplelocationmarking.SimpleLocationMarkingClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Loads and saves waypoints in {@code .minecraft/config/simplelocationmarking/}.
 * <p>
 * One JSON file is used per singleplayer world folder or per multiplayer server address, so
 * waypoints never leak between worlds or servers. All methods must be called on the client thread.
 */
public final class WaypointManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int FILE_FORMAT_VERSION = 1;

    private static final List<Waypoint> WAYPOINTS = new ArrayList<>();
    private static final List<Waypoint> WAYPOINTS_VIEW = Collections.unmodifiableList(WAYPOINTS);

    private static String currentKey;
    private static Path currentFile;

    // Cached dimension id so that per-frame callers do not allocate a new string.
    private static RegistryKey<World> cachedDimensionKey;
    private static String cachedDimensionId = "minecraft:overworld";

    private WaypointManager() {
    }

    /** Persisted file layout. */
    private static final class StorageFile {
        int version = FILE_FORMAT_VERSION;
        List<Waypoint> waypoints = new ArrayList<>();
    }

    // ------------------------------------------------------------------------------------------
    // Session handling
    // ------------------------------------------------------------------------------------------

    /** Called when the client joins a world or server. */
    public static void onJoin(MinecraftClient client) {
        String key = computeStorageKey(client);
        if (!key.equals(currentKey)) {
            load(key);
        }
    }

    /** Called when the client leaves a world or server. */
    public static void onDisconnect() {
        if (currentKey != null) {
            save();
        }
        WAYPOINTS.clear();
        currentKey = null;
        currentFile = null;
        cachedDimensionKey = null;
    }

    /** Lazily loads the storage file if a world is active but no join event was observed yet. */
    public static void ensureLoaded(MinecraftClient client) {
        if (currentKey == null && client.world != null) {
            load(computeStorageKey(client));
        }
    }

    private static String computeStorageKey(MinecraftClient client) {
        IntegratedServer server = client.getServer();
        if (client.isInSingleplayer() && server != null) {
            Path root = server.getSavePath(WorldSavePath.ROOT).toAbsolutePath().normalize();
            Path folder = root.getFileName();
            String name = folder != null ? folder.toString() : server.getSaveProperties().getLevelName();
            return "singleplayer_" + sanitizeFileName(name);
        }
        ServerInfo entry = client.getCurrentServerEntry();
        if (entry != null && entry.address != null && !entry.address.isBlank()) {
            String prefix = entry.isRealm() ? "realms_" : "multiplayer_";
            return prefix + sanitizeFileName(entry.address.trim().toLowerCase(Locale.ROOT));
        }
        return "unknown_session";
    }

    static String sanitizeFileName(String raw) {
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                    || c == '.' || c == '-' || c == '_') {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        if (sb.isEmpty()) {
            sb.append("default");
        }
        return sb.toString();
    }

    private static Path storageDirectory() {
        return FabricLoader.getInstance().getConfigDir().resolve(SimpleLocationMarkingClient.MOD_ID);
    }

    // ------------------------------------------------------------------------------------------
    // Persistence
    // ------------------------------------------------------------------------------------------

    private static void load(String key) {
        WAYPOINTS.clear();
        currentKey = key;
        currentFile = storageDirectory().resolve(key + ".json");

        if (!Files.isRegularFile(currentFile)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(currentFile, StandardCharsets.UTF_8)) {
            StorageFile data = GSON.fromJson(reader, StorageFile.class);
            if (data != null && data.waypoints != null) {
                for (Waypoint waypoint : data.waypoints) {
                    if (waypoint != null) {
                        waypoint.sanitize();
                        WAYPOINTS.add(waypoint);
                    }
                }
            }
            SimpleLocationMarkingClient.LOGGER.info("Loaded {} waypoints from {}", WAYPOINTS.size(), currentFile.getFileName());
        } catch (IOException | JsonParseException e) {
            SimpleLocationMarkingClient.LOGGER.error("Failed to read waypoint file {}", currentFile, e);
        }
    }

    public static void save() {
        if (currentFile == null) {
            return;
        }
        StorageFile data = new StorageFile();
        data.waypoints.addAll(WAYPOINTS);
        try {
            Files.createDirectories(currentFile.getParent());
            Path temp = currentFile.resolveSibling(currentFile.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
            try {
                Files.move(temp, currentFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, currentFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            SimpleLocationMarkingClient.LOGGER.error("Failed to save waypoint file {}", currentFile, e);
        }
    }

    // ------------------------------------------------------------------------------------------
    // Queries and mutations
    // ------------------------------------------------------------------------------------------

    /** All waypoints of the active world or server (every dimension). Read-only view. */
    public static List<Waypoint> all() {
        return WAYPOINTS_VIEW;
    }

    /** Fills {@code out} with the waypoints of the given dimension, without allocating. */
    public static void collectForDimension(String dimensionId, List<Waypoint> out) {
        out.clear();
        for (int i = 0, n = WAYPOINTS.size(); i < n; i++) {
            Waypoint waypoint = WAYPOINTS.get(i);
            if (dimensionId.equals(waypoint.dimension)) {
                out.add(waypoint);
            }
        }
    }

    public static void add(Waypoint waypoint) {
        WAYPOINTS.add(waypoint);
        save();
    }

    public static void remove(Waypoint waypoint) {
        if (WAYPOINTS.remove(waypoint)) {
            save();
        }
    }

    public static void toggleHidden(Waypoint waypoint) {
        waypoint.hidden = !waypoint.hidden;
        save();
    }

    /** Call after editing a waypoint in place. */
    public static void markChanged() {
        save();
    }

    /** Returns the identifier string of the world's dimension, cached per dimension key. */
    public static String dimensionId(World world) {
        RegistryKey<World> key = world.getRegistryKey();
        if (key != cachedDimensionKey) {
            cachedDimensionKey = key;
            cachedDimensionId = key.getValue().toString();
        }
        return cachedDimensionId;
    }
}

