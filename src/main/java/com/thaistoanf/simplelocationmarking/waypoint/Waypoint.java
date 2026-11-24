package com.thaistoanf.simplelocationmarking.waypoint;

import com.thaistoanf.simplelocationmarking.util.TextUtil;
import net.minecraft.client.gui.Font;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A single marked location.
 * <p>
 * Persistent fields are serialized with Gson. Fields marked {@code transient} hold per-frame
 * render caches so that the HUD renderer never needs to allocate strings while drawing.
 */
public final class Waypoint {
    public @Nullable String id;
    public @Nullable String name;
    public int x;
    public int y;
    public int z;
    /** RGB color without alpha (0xRRGGBB). */
    public int color;
    /** Dimension identifier, for example {@code minecraft:overworld}. */
    public @Nullable String dimension;
    public boolean hidden;

    // ---- Transient render caches (not saved) ----
    /** Smooth hover expansion progress in the range [0, 1]. */
    public transient float hoverProgress;

    private transient @Nullable String cachedNameSource;
    private transient @NonNull String cachedDisplayName = "";
    private transient @NonNull String cachedInitial = "?";
    private transient int cachedDisplayNameWidth = -1;
    private transient int cachedInitialWidth = -1;

    private transient long cachedDistanceKey = Long.MIN_VALUE;
    private transient @NonNull String cachedDistanceText = "";
    private transient int cachedDistanceWidth = -1;

    /** Required by Gson. */
    public Waypoint() {
    }

    public Waypoint(@NonNull String name, int x, int y, int z, int color, @NonNull String dimension) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color & 0xFFFFFF;
        this.dimension = dimension;
        this.hidden = false;
    }

    /** Ensures that data loaded from disk is usable even if the JSON was edited by hand. */
    public void sanitize() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        if (name == null) {
            name = "";
        }
        if (dimension == null || dimension.isBlank()) {
            dimension = "minecraft:overworld";
        }
        color &= 0xFFFFFF;
    }

    private void refreshNameCache() {
        if (cachedNameSource != name) {
            cachedNameSource = name;
            cachedDisplayName = TextUtil.capitalizeFirst(name);
            cachedInitial = TextUtil.initialUpper(name);
            cachedDisplayNameWidth = -1;
            cachedInitialWidth = -1;
        }
    }

    /** Name with the first letter capitalized. Cached until the name changes. */
    public @NonNull String displayName() {
        refreshNameCache();
        return cachedDisplayName;
    }

    /** Uppercase first letter of the name. Cached until the name changes. */
    public @NonNull String initial() {
        refreshNameCache();
        return cachedInitial;
    }

    public int displayNameWidth(@NonNull Font font) {
        refreshNameCache();
        if (cachedDisplayNameWidth < 0) {
            cachedDisplayNameWidth = font.width(cachedDisplayName);
        }
        return cachedDisplayNameWidth;
    }

    public int initialWidth(@NonNull Font font) {
        refreshNameCache();
        if (cachedInitialWidth < 0) {
            cachedInitialWidth = font.width(cachedInitial);
        }
        return cachedInitialWidth;
    }

    /**
     * Returns the formatted distance label. The string is only rebuilt when the visible value
     * (whole meters or tenths of kilometers) changes.
     */
    public @NonNull String distanceText(double distance) {
        long key = DistanceFormat.key(distance);
        if (key != cachedDistanceKey) {
            cachedDistanceKey = key;
            cachedDistanceText = DistanceFormat.format(distance);
            cachedDistanceWidth = -1;
        }
        return cachedDistanceText;
    }

    /** Width of the last value returned by {@link #distanceText(double)}. */
    public int distanceWidth(@NonNull Font font) {
        if (cachedDistanceWidth < 0) {
            cachedDistanceWidth = font.width(cachedDistanceText);
        }
        return cachedDistanceWidth;
    }

    /** Euclidean distance from a position to the marked block (bottom center of the block). */
    public double distanceTo(double px, double py, double pz) {
        double dx = (x + 0.5) - px;
        double dy = y - py;
        double dz = (z + 0.5) - pz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
