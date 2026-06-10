package com.botbye.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

public final class ErrorClassifier {
    private ErrorClassifier() {
    }

    public static String classify(Exception e) {
        if (e instanceof SocketTimeoutException) return "timeout";
        if (e instanceof ConnectException) return "connection error";
        if (e instanceof JsonProcessingException) return "invalid json response";
        if (e instanceof java.io.IOException) return "connection error";
        if (e.getMessage() != null && e.getMessage().startsWith("connection error")) return "connection error";
        return e.getMessage() != null ? e.getMessage() : "unknown error";
    }
}
