package com.botbye.common;

/**
 * Normalized error messages surfaced in {@link BotbyeError#getMessage()} when an evaluation falls
 * back open. Mirror the BotBye node-core error codes so messages are consistent across SDKs.
 */
public final class BotbyeErrors {
    public static final String SDK_ERROR = "SDK error";
    public static final String UNKNOWN_ERROR = "unknown error";
    public static final String TIMEOUT_ERROR = "timeout";
    public static final String CONNECTION_ERROR = "connection error";
    public static final String JSON_ERROR = "invalid json response";

    private BotbyeErrors() {
    }
}
