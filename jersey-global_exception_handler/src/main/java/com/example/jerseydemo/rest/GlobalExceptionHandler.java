package com.example.jerseydemo.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// 使用 @Provider 注释告诉 JAX-RS 这是一个组件
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {

        System.out.println("Handling exception: " + exception);

        if (exception instanceof ErrorException) {
            ErrorException e = (ErrorException) exception;
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(
                    new ErrorResponse(e.getError(), e.getStatus())).build();
        }

        if (exception instanceof WebApplicationException) {
            WebApplicationException e = (WebApplicationException)exception;

            Response.StatusType status = e.getResponse().getStatusInfo();

            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(
                    new ErrorResponse(status.getReasonPhrase(), status.getStatusCode())).build();
        }

        // 其他异常作为服务内部错误返回，如 NullPointerException
        return ErrorCode.INTERNAL_SERVER_ERROR.buildResponse();
    }

}
