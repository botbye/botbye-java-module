package com.botbye.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class BotbyeRequest implements Serializable {
    @JsonProperty("server_key")
    private String serverKey;
    private Headers headers;
    @JsonProperty("request_info")
    private ConnectionDetails requestInfo;
    @JsonProperty("custom_fields")
    private Map<String, String> customFields;

    public BotbyeRequest() {
    }

    public BotbyeRequest(String serverKey, Headers headers, ConnectionDetails requestInfo, Map<String, String> customFields) {
        this.serverKey = serverKey;
        this.headers = headers;
        this.requestInfo = requestInfo;
        this.customFields = customFields;
    }

    public String getServerKey() {
        return serverKey;
    }

    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }

    public Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
    }

    public ConnectionDetails getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(ConnectionDetails requestInfo) {
        this.requestInfo = requestInfo;
    }

    public Map<String, String> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(Map<String, String> customFields) {
        this.customFields = customFields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotbyeRequest that = (BotbyeRequest) o;
        return Objects.equals(serverKey, that.serverKey)
                && Objects.equals(headers, that.headers)
                && Objects.equals(requestInfo, that.requestInfo)
                && Objects.equals(customFields, that.customFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverKey, headers, requestInfo, customFields);
    }

    @Override
    public String toString() {
        return "BotbyeRequest{" +
                "serverKey='" + serverKey + '\'' +
                ", headers=" + headers +
                ", requestInfo=" + requestInfo +
                ", customFields=" + customFields +
                '}';
    }
}

