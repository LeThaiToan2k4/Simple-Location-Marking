package com.thaistoanf.simplelocationmarking.waypoint;

import java.util.Locale;

/**
 * Distance label formatting: {@code 145m} below 10000 meters and {@code 10.5km} from 10000 meters.
 */
public final class DistanceFormat {
    public static final double KM_THRESHOLD = 10000.0;

    private DistanceFormat() {
    }

    /** A numeric key that changes exactly when the formatted text changes. */
    public static long key(double distance) {
        if (distance < KM_THRESHOLD) {
            return (long) distance;
        }
        // Offset so that kilometer keys never collide with meter keys.
        return -1L - Math.round(distance / 100.0);
    }

    public static String format(double distance) {
        if (distance < KM_THRESHOLD) {
            return ((long) distance) + "m";
        }
        return String.format(Locale.ROOT, "%.1fkm", Math.round(distance / 100.0) / 10.0);
    }
}

