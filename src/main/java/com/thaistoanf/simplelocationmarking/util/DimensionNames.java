package com.thaistoanf.simplelocationmarking.util;

import net.minecraft.network.chat.Component;

public final class DimensionNames {
    private DimensionNames() {
    }

    /** Human readable, translated name of a dimension id. Unknown dimensions show their raw id. */
    public static Component of(String dimensionId) {
        return switch (dimensionId) {
            case "minecraft:overworld" -> Component.translatable("simplelocationmarking.dimension.overworld");
            case "minecraft:the_nether" -> Component.translatable("simplelocationmarking.dimension.the_nether");
            case "minecraft:the_end" -> Component.translatable("simplelocationmarking.dimension.the_end");
            default -> Component.literal(dimensionId);
        };
    }
}
