package com.botbye.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;

public class ConnectionDetails implements Serializable {
    @JsonProperty("remote_addr")
    private String remoteAddr;
    @JsonProperty("request_method")
    private String requestMethod;
    @JsonProperty("request_uri")
    private String requestUri;

    public ConnectionDetails() {
    }

    public ConnectionDetails(String remoteAddr, String requestMethod, String requestUri) {
        this.remoteAddr = remoteAddr;
        this.requestMethod = requestMethod;
        this.requestUri = requestUri;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConnectionDetails that)) return false;
        return Objects.equals(remoteAddr, that.remoteAddr) && Objects.equals(requestMethod, that.requestMethod) && Objects.equals(requestUri, that.requestUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remoteAddr, requestMethod, requestUri);
    }

    @Override
    public String toString() {
        return "ConnectionDetails{" +
                "remoteAddr='" + remoteAddr + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                ", requestUri='" + requestUri + '\'' +
                '}';
    }
}
