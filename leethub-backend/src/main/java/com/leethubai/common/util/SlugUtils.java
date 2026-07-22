package com.leethubai.common.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utility for generating URL-safe slugs from problem titles.
 * e.g., "Two Sum" → "two-sum", "LRU Cache" → "lru-cache"
 */
public final class SlugUtils {

    private static final Pattern NON_ALPHA_NUMERIC = Pattern.compile("[^a-z0-9\\s-]");
    private static final Pattern WHITESPACE_OR_HYPHEN = Pattern.compile("[\\s-]+");

    private SlugUtils() {}

    /**
     * Converts a string to a URL-safe slug.
     * "Two Sum" → "two-sum"
     */
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return NON_ALPHA_NUMERIC.matcher(normalized.toLowerCase())
                .replaceAll("")
                .transform(s -> WHITESPACE_OR_HYPHEN.matcher(s).replaceAll("-"))
                .replaceAll("^-|-$", "");
    }

    /**
     * Converts a string to a file-system safe folder name.
     * "Two Sum" → "Two_Sum"
     */
    public static String toFolderName(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        return input.trim().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "_");
    }
}
