package com.example.jerseydemo.rest;

public class BaseResponse {

    private boolean success;

    public BaseResponse() {
        this.success = false;
    }

    public  BaseResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
