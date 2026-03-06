package com.botbye.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BotbyeResponse implements Serializable {
    private BotbyeChallengeResult result;
    private UUID reqId;
    private BotbyeError error;
    private BotbyeExtraData extraData;

    public BotbyeResponse() {
        this.result = new BotbyeChallengeResult();
        this.reqId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        this.error = null;
        this.extraData = null;
    }

    public BotbyeResponse(BotbyeError error) {
        this.result = new BotbyeChallengeResult();
        this.reqId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        this.error = error;
        this.extraData = null;
    }

    public BotbyeResponse(BotbyeChallengeResult result, UUID reqId, BotbyeError error, BotbyeExtraData extraData) {
        this.result = result;
        this.reqId = reqId;
        this.error = error;
        this.extraData = extraData;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public BotbyeExtraData getExtraData() {
        return extraData;
    }

    public void setExtraData(BotbyeExtraData extraData) {
        this.extraData = extraData;
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
        return Objects.equals(result, that.result) && Objects.equals(reqId, that.reqId) && Objects.equals(error, that.error) && Objects.equals(extraData, that.extraData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, reqId, error, extraData);
    }

    @Override
    public String toString() {
        return "BotbyeResponse{" +
                "result=" + result +
                ", reqId=" + reqId +
                ", error=" + error +
                ", extraData=" + extraData +
                '}';
    }
}
