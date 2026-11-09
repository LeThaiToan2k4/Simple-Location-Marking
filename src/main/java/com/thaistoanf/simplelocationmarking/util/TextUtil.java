package com.thaistoanf.simplelocationmarking.util;

import java.util.Locale;

public final class TextUtil {
    private TextUtil() {
    }

    /** Returns the text with its first letter in upper case, for example {@code "home" -> "Home"}. */
    public static String capitalizeFirst(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.strip();
        if (trimmed.isEmpty()) {
            return "";
        }
        int first = trimmed.codePointAt(0);
        int upper = Character.toUpperCase(first);
        if (first == upper) {
            return trimmed;
        }
        return new StringBuilder(trimmed.length())
                .appendCodePoint(upper)
                .append(trimmed, Character.charCount(first), trimmed.length())
                .toString();
    }

    /** Returns the uppercase first letter of the text, or {@code "?"} when the text is empty. */
    public static String initialUpper(String text) {
        if (text == null) {
            return "?";
        }
        String trimmed = text.strip();
        if (trimmed.isEmpty()) {
            return "?";
        }
        String first = new String(Character.toChars(trimmed.codePointAt(0)));
        return first.toUpperCase(Locale.ROOT);
    }
}

