package com.botbye.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

/** Maps a thrown exception to one of the normalized {@link BotbyeErrors} messages. */
public final class ErrorClassifier {
    private ErrorClassifier() {
    }

    public static String classify(Exception e) {
        if (e instanceof SocketTimeoutException) return BotbyeErrors.TIMEOUT_ERROR;
        if (e instanceof ConnectException) return BotbyeErrors.CONNECTION_ERROR;
        if (e instanceof JsonProcessingException) return BotbyeErrors.JSON_ERROR;
        if (e instanceof java.io.IOException) return BotbyeErrors.CONNECTION_ERROR;
        if (e.getMessage() != null && e.getMessage().startsWith(BotbyeErrors.CONNECTION_ERROR)) return BotbyeErrors.CONNECTION_ERROR;
        return BotbyeErrors.UNKNOWN_ERROR;
    }
}
