package com.botbye.model;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;

public class BotbyeConfig implements Serializable {
    private static final String MODULE_VERSION = "0.0.1";
    private static final String MODULE_NAME = "Java";
    private String botbyeEndpoint;
    private String serverKey;
    private String path;
    private MediaType contentType;
    // client config
    private Duration readTimeout;
    private Duration writeTimeout;
    private Duration connectionTimeout;
    private Duration callTimeout;
    // pool config
    private int maxIdleConnections;
    private long keepAliveDuration;
    private TimeUnit keepAliveDurationTimeUnit;
    // dispatcher
    private int maxRequestsPerHost;
    private int maxRequests;


    public BotbyeConfig() {
        this.botbyeEndpoint = "https://verify.botbye.com";
        this.serverKey = "";
        this.path = "/validate-request/v2";
        this.readTimeout = Duration.ofSeconds(2);
        this.writeTimeout = Duration.ofSeconds(2);
        this.connectionTimeout = Duration.ofSeconds(2);
        this.callTimeout = Duration.ofSeconds(5);
        this.maxIdleConnections = 250;
        this.keepAliveDuration = 5L;
        this.keepAliveDurationTimeUnit = TimeUnit.MINUTES;
        this.maxRequestsPerHost = 1500;
        this.maxRequests = 1500;
        this.contentType = MediaType.parse("application/json");
    }

    public static class Builder {
        private String botbyeEndpoint = "https://verify.botbye.com";
        private String serverKey = "";
        private String path = "/validate-request/v2";
        private Duration readTimeout = Duration.ofSeconds(2);
        private Duration writeTimeout = Duration.ofSeconds(2);
        private Duration connectionTimeout = Duration.ofSeconds(2);
        private Duration callTimeout = Duration.ofSeconds(5);
        private int maxIdleConnections = 250;
        private long keepAliveDuration = 5L;
        private TimeUnit keepAliveDurationTimeUnit = TimeUnit.MINUTES;
        private int maxRequestsPerHost = 1500;
        private int maxRequests = 1500;
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

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder writeTimeout(Duration writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        public Builder connectionTimeout(Duration connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder callTimeout(Duration callTimeout) {
            this.callTimeout = callTimeout;
            return this;
        }

        public Builder maxIdleConnections(int maxIdleConnections) {
            this.maxIdleConnections = maxIdleConnections;
            return this;
        }

        public Builder keepAliveDuration(long keepAliveDuration, TimeUnit unit) {
            this.keepAliveDuration = keepAliveDuration;
            this.keepAliveDurationTimeUnit = unit;
            return this;
        }

        public Builder maxRequestsPerHost(int maxRequestsPerHost) {
            this.maxRequestsPerHost = maxRequestsPerHost;
            return this;
        }

        public Builder maxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
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
            config.readTimeout = this.readTimeout;
            config.writeTimeout = this.writeTimeout;
            config.connectionTimeout = this.connectionTimeout;
            config.callTimeout = this.callTimeout;
            config.maxIdleConnections = this.maxIdleConnections;
            config.keepAliveDuration = this.keepAliveDuration;
            config.keepAliveDurationTimeUnit = this.keepAliveDurationTimeUnit;
            config.maxRequestsPerHost = this.maxRequestsPerHost;
            config.maxRequests = this.maxRequests;
            config.contentType = this.contentType;
            return config;
        }
    }

    public static String getModuleName() {
        return MODULE_NAME;
    }

    public static String getModuleVersion() {
        return MODULE_VERSION;
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

    public MediaType getContentType() {
        return contentType;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    public Duration getCallTimeout() {
        return callTimeout;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public long getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public TimeUnit getKeepAliveDurationTimeUnit() {
        return keepAliveDurationTimeUnit;
    }

    public int getMaxRequestsPerHost() {
        return maxRequestsPerHost;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BotbyeConfig that)) return false;
        return maxIdleConnections == that.maxIdleConnections && keepAliveDuration == that.keepAliveDuration && maxRequestsPerHost == that.maxRequestsPerHost && maxRequests == that.maxRequests && Objects.equals(botbyeEndpoint, that.botbyeEndpoint) && Objects.equals(serverKey, that.serverKey) && Objects.equals(path, that.path) && Objects.equals(contentType, that.contentType) && Objects.equals(readTimeout, that.readTimeout) && Objects.equals(writeTimeout, that.writeTimeout) && Objects.equals(connectionTimeout, that.connectionTimeout) && Objects.equals(callTimeout, that.callTimeout) && keepAliveDurationTimeUnit == that.keepAliveDurationTimeUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(botbyeEndpoint, serverKey, path, contentType, readTimeout, writeTimeout, connectionTimeout, callTimeout, maxIdleConnections, keepAliveDuration, keepAliveDurationTimeUnit, maxRequestsPerHost, maxRequests);
    }

    @Override
    public String toString() {
        return "BotbyeConfig{" +
                "botbyeEndpoint='" + botbyeEndpoint + '\'' +
                ", serverKey='" + serverKey + '\'' +
                ", path='" + path + '\'' +
                ", contentType=" + contentType +
                ", readTimeout=" + readTimeout +
                ", writeTimeout=" + writeTimeout +
                ", connectionTimeout=" + connectionTimeout +
                ", callTimeout=" + callTimeout +
                ", maxIdleConnections=" + maxIdleConnections +
                ", keepAliveDuration=" + keepAliveDuration +
                ", keepAliveDurationTimeUnit=" + keepAliveDurationTimeUnit +
                ", maxRequestsPerHost=" + maxRequestsPerHost +
                ", maxRequests=" + maxRequests +
                '}';
    }
}
