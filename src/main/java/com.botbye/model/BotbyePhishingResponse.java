package com.botbye.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class BotbyePhishingResponse implements Serializable {
    private final int status;
    private final Map<String, String> headers;
    private final byte[] body;
    private final BotbyeError error;

    public BotbyePhishingResponse() {
        this.status = 0;
        this.headers = Collections.emptyMap();
        this.body = new byte[0];
        this.error = null;
    }

    public BotbyePhishingResponse(int status, Map<String, String> headers, byte[] body) {
        this.status = status;
        this.headers = headers == null ? Collections.emptyMap() : headers;
        this.body = body == null ? new byte[0] : body;
        this.error = null;
    }

    public BotbyePhishingResponse(int status, Map<String, String> headers, byte[] body, BotbyeError error) {
        this.status = status;
        this.headers = headers == null ? Collections.emptyMap() : headers;
        this.body = body == null ? new byte[0] : body;
        this.error = error;
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

    public BotbyeError getError() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotbyePhishingResponse that = (BotbyePhishingResponse) o;
        return status == that.status && Objects.equals(headers, that.headers) && Arrays.equals(body, that.body) && Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(status, headers, error);
        result = 31 * result + Arrays.hashCode(body);
        return result;
    }

    @Override
    public String toString() {
        return "BotbyePhishingResponse{" +
                "status=" + status +
                ", headers=" + headers +
                ", bodyLength=" + (body == null ? 0 : body.length) +
                ", error=" + error +
                '}';
    }
}
