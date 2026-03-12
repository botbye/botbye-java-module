package com.botbye.model;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import okhttp3.MediaType;

public class BotbyeConfig implements Serializable {
    private static final String MODULE_VERSION = "0.0.3";
    private static final String MODULE_NAME = "Java";

    private static final String DEFAULT_BOTBYE_ENDPOINT = "https://verify.botbye.com";
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration DEFAULT_WRITE_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration DEFAULT_CALL_TIMEOUT = Duration.ofSeconds(5);
    private static final int DEFAULT_MAX_IDLE_CONNECTIONS = 250;
    private static final Duration DEFAULT_KEEP_ALIVE_DURATION = Duration.ofSeconds(300);
    private static final int DEFAULT_MAX_REQUESTS_PER_HOST = 1500;
    private static final int DEFAULT_MAX_REQUESTS = 1500;
    private static final MediaType DEFAULT_CONTENT_TYPE = MediaType.parse("application/json");

    private String botbyeEndpoint;
    private String serverKey;
    private MediaType contentType;
    // client config
    private Duration readTimeout;
    private Duration writeTimeout;
    private Duration connectionTimeout;
    private Duration callTimeout;
    // pool config
    private int maxIdleConnections;
    private Duration keepAliveDuration;
    // dispatcher
    private int maxRequestsPerHost;
    private int maxRequests;

    private BotbyeConfig() {
    }

    public static class Builder {
        private String botbyeEndpoint = DEFAULT_BOTBYE_ENDPOINT;
        private String serverKey;
        private Duration readTimeout = DEFAULT_READ_TIMEOUT;
        private Duration writeTimeout = DEFAULT_WRITE_TIMEOUT;
        private Duration connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        private Duration callTimeout = DEFAULT_CALL_TIMEOUT;
        private int maxIdleConnections = DEFAULT_MAX_IDLE_CONNECTIONS;
        private Duration keepAliveDuration = DEFAULT_KEEP_ALIVE_DURATION;
        private int maxRequestsPerHost = DEFAULT_MAX_REQUESTS_PER_HOST;
        private int maxRequests = DEFAULT_MAX_REQUESTS;
        private MediaType contentType = DEFAULT_CONTENT_TYPE;

        public Builder botbyeEndpoint(String botbyeEndpoint) {
            this.botbyeEndpoint = botbyeEndpoint;
            return this;
        }

        public Builder serverKey(String serverKey) {
            this.serverKey = serverKey;
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

        public Builder keepAliveDuration(Duration keepAliveDuration) {
            this.keepAliveDuration = keepAliveDuration;
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
            config.botbyeEndpoint = normalizeBaseUrl(requireNonBlank(botbyeEndpoint, "botbye endpoint"));
            config.serverKey = requireNonBlank(serverKey, "server key");
            config.readTimeout = this.readTimeout;
            config.writeTimeout = this.writeTimeout;
            config.connectionTimeout = this.connectionTimeout;
            config.callTimeout = this.callTimeout;
            config.maxIdleConnections = this.maxIdleConnections;
            config.keepAliveDuration = this.keepAliveDuration;
            config.maxRequestsPerHost = this.maxRequestsPerHost;
            config.maxRequests = this.maxRequests;
            config.contentType = this.contentType;
            return config;
        }
    }

    public static String normalizeBaseUrl(String url) {
        return url.replaceAll("/+$", "");
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("[BotBye] " + fieldName + " is not specified");
        }
        return value;
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

    public Duration getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public int getMaxRequestsPerHost() {
        return maxRequestsPerHost;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotbyeConfig that = (BotbyeConfig) o;
        return maxIdleConnections == that.maxIdleConnections && maxRequestsPerHost == that.maxRequestsPerHost && maxRequests == that.maxRequests && Objects.equals(botbyeEndpoint, that.botbyeEndpoint) && Objects.equals(serverKey, that.serverKey) && Objects.equals(contentType, that.contentType) && Objects.equals(readTimeout, that.readTimeout) && Objects.equals(writeTimeout, that.writeTimeout) && Objects.equals(connectionTimeout, that.connectionTimeout) && Objects.equals(callTimeout, that.callTimeout) && Objects.equals(keepAliveDuration, that.keepAliveDuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(botbyeEndpoint, serverKey, contentType, readTimeout, writeTimeout, connectionTimeout, callTimeout, maxIdleConnections, keepAliveDuration, maxRequestsPerHost, maxRequests);
    }

    @Override
    public String toString() {
        return "BotbyeConfig{" +
                "botbyeEndpoint='" + botbyeEndpoint + '\'' +
                ", serverKey='" + serverKey + '\'' +
                ", contentType=" + contentType +
                ", readTimeout=" + readTimeout +
                ", writeTimeout=" + writeTimeout +
                ", connectionTimeout=" + connectionTimeout +
                ", callTimeout=" + callTimeout +
                ", maxIdleConnections=" + maxIdleConnections +
                ", keepAliveDuration=" + keepAliveDuration +
                ", maxRequestsPerHost=" + maxRequestsPerHost +
                ", maxRequests=" + maxRequests +
                '}';
    }
}
