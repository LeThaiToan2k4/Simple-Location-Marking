package com.thaistoanf.simplelocationmarking.util;

import net.minecraft.text.Text;

public final class DimensionNames {
    private DimensionNames() {
    }

    /** Human readable, translated name of a dimension id. Unknown dimensions show their raw id. */
    public static Text of(String dimensionId) {
        return switch (dimensionId) {
            case "minecraft:overworld" -> Text.translatable("simplelocationmarking.dimension.overworld");
            case "minecraft:the_nether" -> Text.translatable("simplelocationmarking.dimension.the_nether");
            case "minecraft:the_end" -> Text.translatable("simplelocationmarking.dimension.the_end");
            default -> Text.literal(dimensionId);
        };
    }
}

