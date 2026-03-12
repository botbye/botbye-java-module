package com.botbye.model;

import static com.botbye.model.BotbyeConfig.normalizeBaseUrl;
import java.io.Serializable;
import java.util.Objects;

public class BotbyePhishingConfig implements Serializable {
    private String endpoint;
    private String accountId;
    private String projectId;
    private String apiKey;

    private BotbyePhishingConfig() {
    }

    public static class Builder {
        private String endpoint = "https://verify.botbye.com";
        private String accountId = "";
        private String projectId = "";
        private String apiKey = "";

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public BotbyePhishingConfig build() {
            BotbyePhishingConfig config = new BotbyePhishingConfig();
            config.endpoint = normalizeBaseUrl(requireNonBlank(endpoint, "endpoint"));
            config.accountId = requireNonBlank(accountId, "accountId");
            config.projectId = requireNonBlank(projectId, "projectId");
            config.apiKey = requireNonBlank(apiKey, "apiKey");
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

    public String getAccountId() {
        return accountId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getApiKey() {
        return apiKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotbyePhishingConfig that = (BotbyePhishingConfig) o;
        return Objects.equals(endpoint, that.endpoint)
                && Objects.equals(accountId, that.accountId)
                && Objects.equals(projectId, that.projectId)
                && Objects.equals(apiKey, that.apiKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint, accountId, projectId, apiKey);
    }

    @Override
    public String toString() {
        return "BotbyePhishingConfig{" +
                "endpoint='" + endpoint + '\'' +
                ", accountId='" + accountId + '\'' +
                ", projectId='" + projectId + '\'' +
                ", apiKey='" + apiKey + '\'' +
                '}';
    }
}
