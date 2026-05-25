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
 * Combined Level 1+2: Bot validation + risk evaluation in a single call.
 * Use when there is no separate proxy — the middleware validates the token
 * and evaluates ATO/abuse risk in one request.
 */
@JsonAppend(attrs = {@JsonAppend.Attr(value = "server_key")})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public final class BotbyeFullEvent implements BotbyeEvent, Serializable {
    private static final BotbyeIntegrationInfo INTEGRATION = new BotbyeIntegrationInfo(
        BotbyeConfig.getModuleName(), BotbyeConfig.getModuleVersion()
    );

    private final BotbyeIntegrationInfo integration = INTEGRATION;
    private final BotbyeRequestInfo request;
    private final BotbyeEventInfo event;
    private final BotbyeUserInfo user;
    private final Map<String, String> customFields;

    public BotbyeFullEvent(
            BotbyeRequestInfo request,
            BotbyeEventInfo event,
            BotbyeUserInfo user,
            Map<String, String> customFields
    ) {
        this.request = request;
        this.event = event;
        this.user = user;
        this.customFields = customFields;
    }

    public static BotbyeFullEvent of(
            String ip,
            String token,
            Map<String, String> headers,
            BotbyeUserInfo user,
            String eventType,
            BotbyeEventStatus eventStatus
    ) {
        return of(ip, token, headers, user, eventType, eventStatus, null, null, Collections.emptyMap());
    }

    public static BotbyeFullEvent of(
            String ip,
            String token,
            Map<String, String> headers,
            BotbyeUserInfo user,
            String eventType,
            BotbyeEventStatus eventStatus,
            String requestMethod,
            String requestUri,
            Map<String, String> customFields
    ) {
        return new BotbyeFullEvent(
            new BotbyeRequestInfo(ip, token, headers, requestMethod, requestUri),
            new BotbyeEventInfo(eventType, eventStatus),
            user,
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

    public BotbyeEventInfo getEvent() {
        return event;
    }

    public BotbyeUserInfo getUser() {
        return user;
    }

    public Map<String, String> getCustomFields() {
        return customFields;
    }
}
