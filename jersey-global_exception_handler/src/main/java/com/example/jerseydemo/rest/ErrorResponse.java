package com.example.jerseydemo.rest;

public class ErrorResponse extends BaseResponse {

    private String error;
    private Integer errorCode;

    public ErrorResponse() {
        super(false);
        this.error = null;
        this.errorCode = null;
    }

    public ErrorResponse(String error, Integer errorCode) {
        super(false);
        this.error = error;
        this.errorCode = errorCode;
    }

    public ErrorResponse(ErrorCode errorCode) {
        super(false);
        this.error = errorCode.getError();
        this.errorCode = errorCode.getCode();
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }
}
