package com.botbye.model;

import static com.botbye.model.BotbyeConfig.normalizeBaseUrl;
import java.io.Serializable;
import java.util.Objects;

public class BotbyePhishingConfig implements Serializable {
    private String endpoint;
    private String clientKey;

    private BotbyePhishingConfig() {
    }

    public static class Builder {
        private String endpoint = "https://verify.botbye.com";
        private String clientKey = "";

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder clientKey(String clientKey) {
            this.clientKey = clientKey;
            return this;
        }

        public BotbyePhishingConfig build() {
            BotbyePhishingConfig config = new BotbyePhishingConfig();
            config.endpoint = normalizeBaseUrl(requireNonBlank(endpoint, "endpoint"));
            config.clientKey = requireNonBlank(clientKey, "clientKey");
            return config;
        }
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("[BotBye] phishing " + fieldName + " is not specified");
        }
        return value;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getClientKey() {
        return clientKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotbyePhishingConfig that = (BotbyePhishingConfig) o;
        return Objects.equals(endpoint, that.endpoint)
                && Objects.equals(clientKey, that.clientKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint, clientKey);
    }

    @Override
    public String toString() {
        return "BotbyePhishingConfig{" +
                "endpoint='" + endpoint + '\'' +
                ", clientKey='" + clientKey + '\'' +
                '}';
    }
}
