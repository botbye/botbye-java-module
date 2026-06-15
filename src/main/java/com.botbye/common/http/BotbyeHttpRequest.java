package com.botbye.common.http;

import java.util.Collections;
import java.util.Map;

/**
 * Transport-neutral HTTP request. Carries everything the SDK needs to issue a call without exposing
 * a concrete HTTP library to the rest of the codebase.
 */
public final class BotbyeHttpRequest {
    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private final byte[] body;
    private final String contentType;

    public BotbyeHttpRequest(String url, String method, Map<String, String> headers, byte[] body, String contentType) {
        this.url = url;
        this.method = method;
        this.headers = headers == null ? Collections.emptyMap() : headers;
        this.body = body;
        this.contentType = contentType;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /** Request body, or {@code null} for bodyless methods (e.g. GET). */
    public byte[] getBody() {
        return body;
    }

    /** Content-Type for the body, or {@code null}. */
    public String getContentType() {
        return contentType;
    }
}
