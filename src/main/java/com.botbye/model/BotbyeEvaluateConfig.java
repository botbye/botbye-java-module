package com.botbye.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BotbyeEvaluateConfig implements Serializable {
    private final boolean bypassBotValidation;

    public BotbyeEvaluateConfig() {
        this.bypassBotValidation = false;
    }

    public BotbyeEvaluateConfig(boolean bypassBotValidation) {
        this.bypassBotValidation = bypassBotValidation;
    }

    public boolean isBypassBotValidation() {
        return bypassBotValidation;
    }
}
