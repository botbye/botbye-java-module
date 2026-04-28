package com.botbye.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = BotbyeValidationEvent.class, name = "validate"),
    @JsonSubTypes.Type(value = BotbyeRiskScoringEvent.class, name = "risk"),
    @JsonSubTypes.Type(value = BotbyeFullEvent.class, name = "full"),
})
public interface BotbyeEvent {
    @JsonIgnore
    String getUrlToken();
}
