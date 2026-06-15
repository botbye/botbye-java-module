package com.botbye.common.http;

import java.util.Collections;
import java.util.Map;

/**
 * Transport-neutral HTTP response. {@code body} is fully buffered so the underlying connection is
 * released before the response is handed back.
 */
public final class BotbyeHttpResponse {
    private final int status;
    private final Map<String, String> headers;
    private final byte[] body;

    public BotbyeHttpResponse(int status, Map<String, String> headers, byte[] body) {
        this.status = status;
        this.headers = headers == null ? Collections.emptyMap() : headers;
        this.body = body == null ? new byte[0] : body;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }
}
