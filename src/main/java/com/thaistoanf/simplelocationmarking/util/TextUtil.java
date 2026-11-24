package com.thaistoanf.simplelocationmarking.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public final class TextUtil {
    private TextUtil() {
    }

    /** Returns {@code text} when non-null, or an empty string otherwise. */
    public static @NonNull String nonNull(@Nullable String text) {
        return text != null ? text : "";
    }

    /** Returns the text with its first letter in upper case, for example {@code "home" -> "Home"}. */
    public static @NonNull String capitalizeFirst(@Nullable String text) {
        if (text == null) {
            return "";
        }
        String trimmed = nonNull(text.strip());
        if (trimmed.isEmpty()) {
            return "";
        }
        int first = trimmed.codePointAt(0);
        int upper = Character.toUpperCase(first);
        if (first == upper) {
            return trimmed;
        }
        return nonNull(new StringBuilder(trimmed.length())
                .appendCodePoint(upper)
                .append(trimmed, Character.charCount(first), trimmed.length())
                .toString());
    }

    /** Returns the uppercase first letter of the text, or {@code "?"} when the text is empty. */
    public static @NonNull String initialUpper(@Nullable String text) {
        if (text == null) {
            return "?";
        }
        String trimmed = nonNull(text.strip());
        if (trimmed.isEmpty()) {
            return "?";
        }
        String first = new String(Character.toChars(trimmed.codePointAt(0)));
        return nonNull(first.toUpperCase(Locale.ROOT));
    }
}
