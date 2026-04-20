package com.botbye.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BotbyeEventInfo implements Serializable {
    private final String type;
    private final BotbyeEventStatus status;

    public BotbyeEventInfo(String type, BotbyeEventStatus status) {
        this.type = type;
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public BotbyeEventStatus getStatus() {
        return status;
    }
}
