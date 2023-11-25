package com.botbye.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

public class BotbyeChallengeResult implements Serializable {
    @JsonProperty("isBot")
    private boolean isBot;
    private boolean banRequired;

    public BotbyeChallengeResult() {
        this.isBot = false;
        this.banRequired = false;
    }

    public BotbyeChallengeResult(boolean isBot, boolean banRequired) {
        this.isBot = isBot;
        this.banRequired = banRequired;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setBot(boolean isBot) {
        this.isBot = isBot;
    }

    public boolean isBanRequired() {
        return banRequired;
    }

    public void setBanRequired(boolean banRequired) {
        this.banRequired = banRequired;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotbyeChallengeResult that = (BotbyeChallengeResult) o;
        return isBot == that.isBot && banRequired == that.banRequired;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isBot, banRequired);
    }

    @Override
    public String toString() {
        return "BotbyeChallangeResult{" +
                "isBot=" + isBot +
                ", banRequired=" + banRequired +
                '}';
    }
}
