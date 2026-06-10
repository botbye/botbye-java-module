package com.botbye.protection.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BotbyeRequestInfo implements Serializable {
    private final String ip;
    private final String token;
    private final Map<String, String> headers;
    private final String requestMethod;
    private final String requestUri;

    /** For risk-scoring events — no token or URI context. */
    public BotbyeRequestInfo(String ip, Map<String, String> headers) {
        this(ip, null, headers, null, null);
    }

    public BotbyeRequestInfo(String ip, String token, Map<String, String> headers, String requestMethod, String requestUri) {
        this.ip = ip;
        this.token = token;
        this.headers = headers;
        this.requestMethod = requestMethod;
        this.requestUri = requestUri;
    }

    public String getIp() {
        return ip;
    }

    public String getToken() {
        return token;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }
}
