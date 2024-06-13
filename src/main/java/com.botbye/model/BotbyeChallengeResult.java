package com.botbye.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

public class BotbyeChallengeResult implements Serializable {
    private boolean isAllowed;

    public BotbyeChallengeResult() {
        this.isAllowed = true;
    }

    public BotbyeChallengeResult(boolean isAllowed) {
        this.isAllowed = isAllowed;
    }

    @JsonProperty("isAllowed")
    public boolean isAllowed() {
        return isAllowed;
    }

    public void setAllowed(boolean isAllowed) {
        this.isAllowed = isAllowed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BotbyeChallengeResult that)) return false;
        return isAllowed == that.isAllowed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isAllowed);
    }

    @Override
    public String toString() {
        return "BotbyeChallengeResult{" +
                "isAllowed=" + isAllowed +
                '}';
    }
}
