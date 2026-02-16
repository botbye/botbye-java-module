package com.botbye.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitErrorResponse implements Serializable {
    private String error;
    private String status;

    public InitErrorResponse() {
    }

    public InitErrorResponse(String error, String status) {
        this.error = error;
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "InitErrorResponse{" +
                "error='" + error + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
