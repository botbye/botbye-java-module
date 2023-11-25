package com.botbye.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class BotbyeResponse implements Serializable {
    private BotbyeChallengeResult result;
    private UUID reqId;
    private BotbyeError error;

    public BotbyeResponse() {
        this.result = new BotbyeChallengeResult();
        this.reqId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        this.error = null;
    }

    public BotbyeResponse(BotbyeChallengeResult result, UUID reqId, BotbyeError error) {
        this.result = result;
        this.reqId = reqId;
        this.error = error;
    }

    public BotbyeChallengeResult getResult() {
        return result;
    }

    public void setResult(BotbyeChallengeResult result) {
        this.result = result;
    }

    public UUID getReqId() {
        return reqId;
    }

    public void setReqId(UUID reqId) {
        this.reqId = reqId;
    }

    public BotbyeError getError() {
        return error;
    }

    public void setError(BotbyeError error) {
        this.error = error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotbyeResponse that = (BotbyeResponse) o;
        return Objects.equals(result, that.result) && Objects.equals(reqId, that.reqId) && Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, reqId, error);
    }

    @Override
    public String toString() {
        return "BotbyeResponse{" +
                "result=" + result +
                ", reqId=" + reqId +
                ", error=" + error +
                '}';
    }
}
