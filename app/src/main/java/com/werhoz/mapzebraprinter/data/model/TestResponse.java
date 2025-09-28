package com.werhoz.mapzebraprinter.data.model;

public class TestResponse {
    private boolean status;
    private String message;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public TestResponse(boolean status, String message) {
        this.status = status;
        this.message = message;
    }
}