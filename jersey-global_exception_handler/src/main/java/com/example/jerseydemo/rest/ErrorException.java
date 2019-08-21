package com.example.jerseydemo.rest;

import javax.ws.rs.core.Response;

public class ErrorException extends RuntimeException {

    private static final long serialVersionUID = 3281072262669739458L;

    private String error;
    private int status;

    public ErrorException(String error, int status) {
        super(status + ": " + error);
        this.error = error;
        this.status = status;
    }

    public ErrorException(String error, Response.Status status) {
        this(error, status.getStatusCode());
    }

    public ErrorException(ErrorCode errorCode) {
        this(errorCode.getError(), errorCode.getCode());
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
