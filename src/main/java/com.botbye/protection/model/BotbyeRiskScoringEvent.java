package com.botbye.protection.model;

import com.botbye.common.ModuleInfo;
import com.botbye.common.http.Headers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Level 2: Risk evaluation (middleware, post-authentication).
 * Evaluates ATO/abuse risk using user context and dynamic metrics.
 * Bot score comes from Level 1 result ({@code botbyeResult}).
 */
@JsonAppend(attrs = {@JsonAppend.Attr(value = "server_key")})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public final class BotbyeRiskScoringEvent implements BotbyeEvent, Serializable {
    private static final BotbyeIntegrationInfo INTEGRATION = new BotbyeIntegrationInfo(
        ModuleInfo.NAME, ModuleInfo.VERSION
    );

    private final BotbyeIntegrationInfo integration = INTEGRATION;
    private final BotbyeRequestInfo request;
    private final BotbyeEventInfo event;
    private final BotbyeUserInfo user;
    private final String botbyeResult;
    private final Map<String, String> customFields;

    public BotbyeRiskScoringEvent(
            BotbyeRequestInfo request,
            BotbyeEventInfo event,
            BotbyeUserInfo user,
            String botbyeResult,
            Map<String, String> customFields
    ) {
        this.request = request;
        this.event = event;
        this.user = user;
        this.botbyeResult = botbyeResult;
        this.customFields = customFields;
    }

    public static BotbyeRiskScoringEvent of(
            String ip,
            Headers headers,
            BotbyeUserInfo user,
            String eventType,
            BotbyeEventStatus eventStatus
    ) {
        return of(ip, headers, user, eventType, eventStatus, null, Collections.emptyMap());
    }

    public static BotbyeRiskScoringEvent of(
            String ip,
            Headers headers,
            BotbyeUserInfo user,
            String eventType,
            BotbyeEventStatus eventStatus,
            String botbyeResult,
            Map<String, String> customFields
    ) {
        boolean hasResult = botbyeResult != null && !botbyeResult.isBlank();

        return new BotbyeRiskScoringEvent(
            new BotbyeRequestInfo(ip, headers),
            new BotbyeEventInfo(eventType, eventStatus),
            user,
            hasResult ? botbyeResult : null,
            customFields != null ? customFields : Collections.emptyMap()
        );
    }

    @Override
    @JsonIgnore
    public String getUrlToken() {
        return null;
    }

    public BotbyeIntegrationInfo getIntegration() {
        return integration;
    }

    public BotbyeRequestInfo getRequest() {
        return request;
    }

    public BotbyeEventInfo getEvent() {
        return event;
    }

    public BotbyeUserInfo getUser() {
        return user;
    }

    public String getBotbyeResult() {
        return botbyeResult;
    }

    public Map<String, String> getCustomFields() {
        return customFields;
    }
}
