package com.supplywise.supplywise.DAO;

public class HealthCheckResponse {

    private String message;

    public HealthCheckResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
