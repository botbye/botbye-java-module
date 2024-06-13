package com.botbye.model;

import okhttp3.MediaType;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class BotbyeConfig implements Serializable {
    public static final String moduleVersion = "0.0.1";
    public static final String moduleName = "Java";
    private String botbyeEndpoint;
    private String serverKey;
    private String path;
    private long connectionTimeout;
    private TimeUnit connectionTimeoutUnit;
    private int connectionPoolSize;
    private long keepAliveDuration;
    private TimeUnit keepAliveDurationTimeUnit;
    private MediaType contentType;

    public BotbyeConfig() {
        this.botbyeEndpoint = "https://api.botbye.com";
        this.serverKey = "";
        this.path = "/validate-request/v2";
        this.connectionTimeout = 1L;
        this.connectionTimeoutUnit = TimeUnit.SECONDS;
        this.connectionPoolSize = 5;
        this.keepAliveDuration = 5L;
        this.keepAliveDurationTimeUnit = TimeUnit.MINUTES;
        this.contentType = MediaType.parse("application/json");
    }

    public static class Builder {
        private String botbyeEndpoint = "https://api.botbye.com";
        private String serverKey = "";
        private String path  = "/validate-request/v2";
        private long connectionTimeout = 5L;
        private TimeUnit connectionTimeoutUnit = TimeUnit.SECONDS;
        private int connectionPoolSize = 5;
        private long keepAliveDuration = 5L;
        private TimeUnit keepAliveDurationTimeUnit = TimeUnit.MINUTES;
        private MediaType contentType = MediaType.parse("application/json");

        public Builder botbyeEndpoint(String botbyeEndpoint) {
            this.botbyeEndpoint = botbyeEndpoint;
            return this;
        }

        public Builder serverKey(String serverKey) {
            this.serverKey = serverKey;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder connectionTimeout(long connectionTimeout, TimeUnit unit) {
            this.connectionTimeout = connectionTimeout;
            this.connectionTimeoutUnit = unit;
            return this;
        }

        public Builder connectionPoolSize(int connectionPoolSize) {
            this.connectionPoolSize = connectionPoolSize;
            return this;
        }

        public Builder keepAliveDuration(long keepAliveDuration, TimeUnit unit) {
            this.keepAliveDuration = keepAliveDuration;
            this.keepAliveDurationTimeUnit = unit;
            return this;
        }

        public Builder contentType(MediaType contentType) {
            this.contentType = contentType;
            return this;
        }

        public BotbyeConfig build() {
            BotbyeConfig config = new BotbyeConfig();
            config.botbyeEndpoint = this.botbyeEndpoint;
            config.serverKey = this.serverKey;
            config.path = this.path;
            config.connectionTimeout = this.connectionTimeout;
            config.connectionTimeoutUnit = this.connectionTimeoutUnit;
            config.connectionPoolSize = this.connectionPoolSize;
            config.keepAliveDuration = this.keepAliveDuration;
            config.keepAliveDurationTimeUnit = this.keepAliveDurationTimeUnit;
            config.contentType = this.contentType;
            return config;
        }
    }

    public String getBotbyeEndpoint() {
        return botbyeEndpoint;
    }

    public String getServerKey() {
        return serverKey;
    }

    public String getPath() {
        return path;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public TimeUnit getConnectionTimeoutUnit() {
        return connectionTimeoutUnit;
    }

    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public long getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public TimeUnit getKeepAliveDurationTimeUnit() {
        return keepAliveDurationTimeUnit;
    }

    public MediaType getContentType() {
        return contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotbyeConfig config = (BotbyeConfig) o;
        return connectionTimeout == config.connectionTimeout
                && connectionPoolSize == config.connectionPoolSize
                && keepAliveDuration == config.keepAliveDuration
                && Objects.equals(botbyeEndpoint, config.botbyeEndpoint)
                && Objects.equals(serverKey, config.serverKey)
                && Objects.equals(path, config.path)
                && connectionTimeoutUnit == config.connectionTimeoutUnit
                && keepAliveDurationTimeUnit == config.keepAliveDurationTimeUnit
                && Objects.equals(contentType, config.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                botbyeEndpoint,
                serverKey,
                path,
                connectionTimeout,
                connectionTimeoutUnit,
                connectionPoolSize,
                keepAliveDuration,
                keepAliveDurationTimeUnit,
                contentType
        );
    }

    @Override
    public String toString() {
        return "BotbyeConfig{" +
                "botbyeEndpoint='" + botbyeEndpoint + '\'' +
                ", serverKey='" + serverKey + '\'' +
                ", path='" + path + '\'' +
                ", connectionTimeout=" + connectionTimeout +
                ", connectionTimeoutUnit=" + connectionTimeoutUnit +
                ", connectionPoolSize=" + connectionPoolSize +
                ", keepAliveDuration=" + keepAliveDuration +
                ", keepAliveDurationTimeUnit=" + keepAliveDurationTimeUnit +
                ", contentType=" + contentType +
                '}';
    }
}
