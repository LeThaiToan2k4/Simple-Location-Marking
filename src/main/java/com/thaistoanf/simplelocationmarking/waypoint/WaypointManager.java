package com.thaistoanf.simplelocationmarking.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.thaistoanf.simplelocationmarking.SimpleLocationMarkingClient;
import com.thaistoanf.simplelocationmarking.util.TextUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
 * Loads and saves waypoints in
 * {@code .minecraft/config/simplelocationmarking/}.
 * <p>
 * One JSON file is used per singleplayer world folder or per multiplayer server
 * address, so waypoints never leak between worlds or servers. All methods must
 * be called on the client thread.
 */
public final class WaypointManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int FILE_FORMAT_VERSION = 1;

    private static final List<Waypoint> WAYPOINTS = new ArrayList<>();
    private static final List<Waypoint> WAYPOINTS_VIEW = Collections.unmodifiableList(WAYPOINTS);

    private static @Nullable String currentKey;
    private static @Nullable Path currentFile;

    // Cached dimension id so that per-frame callers do not allocate a new string.
    private static @Nullable ResourceKey<Level> cachedDimensionKey;
    private static @NonNull String cachedDimensionId = "minecraft:overworld";

    private WaypointManager() {
    }

    /** Persisted file layout. */
    private static final class StorageFile {
        @SuppressWarnings("unused")
        int version = FILE_FORMAT_VERSION;
        @Nullable List<Waypoint> waypoints = new ArrayList<>();
    }

    // ------------------------------------------------------------------------------------------
    // Session handling
    // ------------------------------------------------------------------------------------------

    /** Called when the client joins a world or server. */
    public static void onJoin(@NonNull Minecraft client) {
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

    /**
     * Lazily loads the storage file if a world is active but no join event was
     * observed yet.
     */
    public static void ensureLoaded(@NonNull Minecraft client) {
        if (currentKey == null && client.level != null) {
            load(computeStorageKey(client));
        }
    }

    private static @NonNull String computeStorageKey(@NonNull Minecraft client) {
        IntegratedServer server = client.getSingleplayerServer();
        if (client.isLocalServer() && server != null) {
            Path root = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            Path folder = root.getFileName();
            String name = folder != null ? TextUtil.nonNull(folder.toString()) : server.getWorldData().getLevelName();
            return "singleplayer_" + sanitizeFileName(name);
        }
        ServerData entry = client.getCurrentServer();
        if (entry != null && !entry.ip.isBlank()) {
            String prefix = entry.isRealm() ? "realms_" : "multiplayer_";
            return prefix + sanitizeFileName(TextUtil.nonNull(entry.ip.trim().toLowerCase(Locale.ROOT)));
        }
        return "unknown_session";
    }

    static @NonNull String sanitizeFileName(@NonNull String raw) {
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
        return TextUtil.nonNull(sb.toString());
    }

    private static Path storageDirectory() {
        return FabricLoader.getInstance().getConfigDir().resolve(SimpleLocationMarkingClient.MOD_ID);
    }

    // ------------------------------------------------------------------------------------------
    // Persistence
    // ------------------------------------------------------------------------------------------

    private static void load(@NonNull String key) {
        WAYPOINTS.clear();
        currentKey = key;
        Path file = storageDirectory().resolve(key + ".json");
        currentFile = file;

        if (!Files.isRegularFile(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            StorageFile data = GSON.fromJson(reader, StorageFile.class);
            if (data != null) {
                List<Waypoint> loaded = data.waypoints;
                if (loaded != null) {
                    for (Waypoint waypoint : loaded) {
                        if (waypoint != null) {
                            waypoint.sanitize();
                            WAYPOINTS.add(waypoint);
                        }
                    }
                }
            }
            SimpleLocationMarkingClient.LOGGER.info("Loaded {} waypoints from {}", WAYPOINTS.size(),
                    file.getFileName());
        } catch (IOException | JsonParseException e) {
            SimpleLocationMarkingClient.LOGGER.error("Failed to read waypoint file {}", file, e);
        }
    }

    public static void save() {
        Path file = currentFile;
        if (file == null) {
            return;
        }
        StorageFile data = new StorageFile();
        List<Waypoint> out = data.waypoints;
        if (out != null) {
            out.addAll(WAYPOINTS);
        }
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            SimpleLocationMarkingClient.LOGGER.error("Failed to save waypoint file {}", file, e);
        }
    }

    // ------------------------------------------------------------------------------------------
    // Queries and mutations
    // ------------------------------------------------------------------------------------------

    /**
     * All waypoints of the active world or server (every dimension). Read-only
     * view.
     */
    public static List<Waypoint> all() {
        return WAYPOINTS_VIEW;
    }

    /**
     * Fills {@code out} with the waypoints of the given dimension, without
     * allocating.
     */
    public static void collectForDimension(@NonNull String dimensionId, @NonNull List<Waypoint> out) {
        out.clear();
        for (int i = 0, n = WAYPOINTS.size(); i < n; i++) {
            Waypoint waypoint = WAYPOINTS.get(i);
            if (dimensionId.equals(waypoint.dimension)) {
                out.add(waypoint);
            }
        }
    }

    public static void add(@NonNull Waypoint waypoint) {
        WAYPOINTS.add(waypoint);
        save();
    }

    public static void remove(@NonNull Waypoint waypoint) {
        if (WAYPOINTS.remove(waypoint)) {
            save();
        }
    }

    public static void toggleHidden(@NonNull Waypoint waypoint) {
        waypoint.hidden = !waypoint.hidden;
        save();
    }

    /** Call after editing a waypoint in place. */
    public static void markChanged() {
        save();
    }

    /**
     * Returns the identifier string of the level's dimension, cached per dimension
     * key.
     */
    public static @NonNull String dimensionId(@NonNull Level level) {
        ResourceKey<Level> key = level.dimension();
        if (key != cachedDimensionKey) {
            cachedDimensionKey = key;
            cachedDimensionId = TextUtil.nonNull(key.identifier().toString());
        }
        return cachedDimensionId;
    }
}
