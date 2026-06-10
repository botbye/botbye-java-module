package com.botbye.common;

public final class UrlUtils {
    private UrlUtils() {
    }

    public static String normalizeBaseUrl(String url) {
        return url.replaceAll("/+$", "");
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("[BotBye] " + fieldName + " is not specified");
        }
        return value;
    }
}
