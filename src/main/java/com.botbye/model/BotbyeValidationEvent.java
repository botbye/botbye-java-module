package com.botbye.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Level 1: Bot validation (proxy, pre-authentication).
 * Validates device token and returns bot score. No user context — only bot detection.
 */
@JsonAppend(attrs = {@JsonAppend.Attr(value = "server_key")})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public final class BotbyeValidationEvent implements BotbyeEvent, Serializable {
    private static final BotbyeIntegrationInfo INTEGRATION = new BotbyeIntegrationInfo(
        BotbyeConfig.getModuleName(), BotbyeConfig.getModuleVersion()
    );

    private final BotbyeIntegrationInfo integration = INTEGRATION;
    private final BotbyeRequestInfo request;
    private final BotbyeEvaluateConfig config;
    private final Map<String, String> customFields;

    public BotbyeValidationEvent(BotbyeRequestInfo request, BotbyeEvaluateConfig config, Map<String, String> customFields) {
        this.request = request;
        this.config = config;
        this.customFields = customFields;
    }

    public static BotbyeValidationEvent of(String ip, String token, Map<String, String> headers, String requestUri) {
        return of(ip, token, headers, null, requestUri, Collections.emptyMap());
    }

    public static BotbyeValidationEvent of(
            String ip,
            String token,
            Map<String, String> headers,
            String requestMethod,
            String requestUri,
            Map<String, String> customFields
    ) {
        return new BotbyeValidationEvent(
            new BotbyeRequestInfo(ip, token, headers, requestMethod, requestUri),
            new BotbyeEvaluateConfig(),
            customFields != null ? customFields : Collections.emptyMap()
        );
    }

    @Override
    @JsonIgnore
    public String getUrlToken() {
        return request.getToken();
    }

    public BotbyeIntegrationInfo getIntegration() {
        return integration;
    }

    public BotbyeRequestInfo getRequest() {
        return request;
    }

    public BotbyeEvaluateConfig getConfig() {
        return config;
    }

    public Map<String, String> getCustomFields() {
        return customFields;
    }
}
