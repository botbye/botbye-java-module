package com.botbye.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class ConnectionDetails implements Serializable {
    @JsonProperty("created_at")
    private Date createdAt;
    @JsonProperty("server_port")
    private int serverPort;
    @JsonProperty("remote_addr")
    private String remoteAddr;
    @JsonProperty("server_name")
    private String serverName;
    @JsonProperty("request_method")
    private String requestMethod;
    @JsonProperty("request_uri")
    private String requestUri;

    public ConnectionDetails() {
    }

    public ConnectionDetails(int serverPort, String remoteAddr, String serverName, String requestMethod, String requestUri) {
        this.createdAt = new Date();
        this.serverPort = serverPort;
        this.remoteAddr = remoteAddr;
        this.serverName = serverName;
        this.requestMethod = requestMethod;
        this.requestUri = requestUri;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
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
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionDetails that = (ConnectionDetails) o;
        return serverPort == that.serverPort
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(remoteAddr, that.remoteAddr)
                && Objects.equals(serverName, that.serverName)
                && Objects.equals(requestMethod, that.requestMethod)
                && Objects.equals(requestUri, that.requestUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt, serverPort, remoteAddr, serverName, requestMethod, requestUri);
    }

    @Override
    public String toString() {
        return "ConnectionDetails{" +
                "createdAt=" + createdAt +
                ", serverPort=" + serverPort +
                ", remoteAddr='" + remoteAddr + '\'' +
                ", serverName='" + serverName + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                ", requestUri='" + requestUri + '\'' +
                '}';
    }
}
