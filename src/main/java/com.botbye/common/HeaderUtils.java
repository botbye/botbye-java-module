package com.botbye.common;

import java.util.Map;

/**
 * Header helpers, mirroring BotBye node-core's {@code getIpFromHeaders}.
 */
public final class HeaderUtils {
    private HeaderUtils() {
    }

    /**
     * Best-effort extraction of the client IP from request headers. Prefers the first hop of
     * {@code x-forwarded-for}, then falls back to {@code x-real-ip}. Lookup is case-insensitive.
     * Returns {@code null} when neither header is present.
     */
    public static String getIpFromHeaders(Map<String, String> headers) {
        if (headers == null) {
            return null;
        }

        String forwardedFor = lookup(headers, "x-forwarded-for");
        if (forwardedFor != null) {
            String firstHop = forwardedFor.split(",", 2)[0].trim();
            if (!firstHop.isEmpty()) {
                return firstHop;
            }
        }

        String realIp = lookup(headers, "x-real-ip");
        if (realIp != null) {
            String trimmed = realIp.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }

        return null;
    }

    private static String lookup(Map<String, String> headers, String lowerName) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(lowerName)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
