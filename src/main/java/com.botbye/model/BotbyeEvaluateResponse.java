package com.botbye.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BotbyeEvaluateResponse implements Serializable {
    private UUID requestId;
    private BotbyeDecision decision = BotbyeDecision.ALLOW;
    private Double riskScore;
    private List<String> signals;
    private Map<String, Double> scores;
    private BotbyeEvaluateConfig config = new BotbyeEvaluateConfig();
    private BotbyeChallenge challenge;
    private BotbyeExtraData extraData;
    private BotbyeError error;

    public BotbyeEvaluateResponse() {
    }

    /** Creates a fallback/bypass response with the given config. */
    public BotbyeEvaluateResponse(BotbyeEvaluateConfig config) {
        this.config = config;
    }

    /** Creates a fallback/bypass response with the given config and error. */
    public BotbyeEvaluateResponse(BotbyeEvaluateConfig config, BotbyeError error) {
        this.config = config;
        this.error = error;
    }

    @JsonIgnore
    public boolean isBlocked() {
        return BotbyeDecision.BLOCK == decision;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public BotbyeDecision getDecision() {
        return decision;
    }

    public void setDecision(BotbyeDecision decision) {
        this.decision = decision;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public List<String> getSignals() {
        return signals;
    }

    public void setSignals(List<String> signals) {
        this.signals = signals;
    }

    public Map<String, Double> getScores() {
        return scores;
    }

    public void setScores(Map<String, Double> scores) {
        this.scores = scores;
    }

    public BotbyeEvaluateConfig getConfig() {
        return config;
    }

    public void setConfig(BotbyeEvaluateConfig config) {
        this.config = config;
    }

    public BotbyeChallenge getChallenge() {
        return challenge;
    }

    public void setChallenge(BotbyeChallenge challenge) {
        this.challenge = challenge;
    }

    public BotbyeExtraData getExtraData() {
        return extraData;
    }

    public void setExtraData(BotbyeExtraData extraData) {
        this.extraData = extraData;
    }

    public BotbyeError getError() {
        return error;
    }

    public void setError(BotbyeError error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "BotbyeEvaluateResponse{" +
                "requestId=" + requestId +
                ", decision=" + decision +
                ", riskScore=" + riskScore +
                ", signals=" + signals +
                ", scores=" + scores +
                ", config=" + config +
                ", challenge=" + challenge +
                ", extraData=" + extraData +
                ", error=" + error +
                '}';
    }
}
