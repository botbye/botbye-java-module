package com.botbye.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

public class BotbyeChallangeResult implements Serializable {
    @JsonProperty("isBot")
    private boolean isBot;

    public BotbyeChallangeResult() {
        this.isBot = false;
    }

    public BotbyeChallangeResult(boolean isBot) {
        this.isBot = isBot;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setBot(boolean bot) {
        isBot = bot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotbyeChallangeResult that = (BotbyeChallangeResult) o;
        return isBot == that.isBot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isBot);
    }

    @Override
    public String toString() {
        return "BotbyeChallangeResult{" +
                "isBot=" + isBot +
                '}';
    }
}
