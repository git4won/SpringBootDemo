package com.example.jerseydemo.rest;

import javax.ws.rs.core.Response;

public class ErrorCode {

    // HTTP status compatible error codes
    public static ErrorCode BAD_REQUEST             = new ErrorCode(400, "Bad Request");
    public static ErrorCode UNAUTHORIZED            = new ErrorCode(401, "Unauthorized"); // not logged in
    public static ErrorCode PERMISSION_DENIED       = new ErrorCode(403, "Forbidden"); // permission denied
    public static ErrorCode NOT_FOUND               = new ErrorCode(404, "Not Found");
    public static ErrorCode INTERNAL_SERVER_ERROR   = new ErrorCode(500, "Internal Server Error");

    // Customize error codes beyond HTTP status
    public static ErrorCode EMPTY_PARAMETER         = new ErrorCode(1000, "Empty Parameter");
    public static ErrorCode BAD_PARAMETER           = new ErrorCode(1001, "Bad Parameters");

    private int code;
    private String error;

    public ErrorCode(int code, String error) {
        this.code = code;
        this.error = error;
    }

    public int getCode() {
        return code;
    }

    public String getError() {
        return error;
    }

    public Response buildResponse() {
        return Response.status(Response.Status.OK).entity(new ErrorResponse(error, code)).build();
    }

    public ErrorException buildException() {
        return new ErrorException(error, code);
    }
}
