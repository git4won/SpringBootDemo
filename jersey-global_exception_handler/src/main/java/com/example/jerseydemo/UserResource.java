package com.example.jerseydemo;

import com.example.jerseydemo.rest.BaseResponse;
import com.example.jerseydemo.rest.ErrorCode;
import com.example.jerseydemo.rest.ListResponse;
import com.example.jerseydemo.rest.SimpleResponse;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* @Component 将 UserResource 声明为 Spring bean
 */
@Component
@Path("/users")
public class UserResource {

    private static Map<Integer, User> DB = new HashMap<>();

    static
    {
        User user1 = new User(0, "zhugeliang", "kongming");
        User user2 = new User(1, "liubei", "xuande");

        DB.put(user1.getId(), user1);
        DB.put(user2.getId(), user2);
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getUserById(@PathParam("id") int id) {
        User user = DB.get(id);
        if (user == null) {
            //return Response.status(404).build();
            return ErrorCode.NOT_FOUND.buildResponse();
        }

        //return Response.status(200).entity(user).build();
        return Response.ok(new SimpleResponse<>(user)).build();
    }

    @GET
    @Produces("application/json")
    public Response getAllUsers() {
        //return Response.status(200).entity(new ArrayList<>(DB.values())).build();
        return Response.ok(new ListResponse<>(new ArrayList<>(DB.values()))).build();
    }

    @POST
    // @Consumes 表明传入的 Content-Type
    @Consumes("application/json")
    @Produces("application/json")
    public Response createUser(User user) {

        if (user.getName() == null || user.getNick() == null) {
            //return Response.status(400).entity("Please provide all mandatory inputs").build();
            return ErrorCode.EMPTY_PARAMETER.buildResponse();
        }

        User newUser = new User();
        newUser.setId(DB.values().size()); // id 从 0 开始
        newUser.setName(user.getName());
        newUser.setNick(user.getNick());
        DB.put(newUser.getId(), newUser);

        //return Response.ok().build();
        return Response.ok(new BaseResponse(true)).build();
    }

    // 测试异常返回
    @GET
    @Path("/exception1")
    public Response testException() {
        throw ErrorCode.BAD_REQUEST.buildException();
    }

    // NullPointerException
    @GET
    @Path("/exception2")
    @Produces("application/json")
    public Response testException2() {
        User user = null;
        user.setName("test");

        return Response.ok(new BaseResponse(true)).build();
    }

    // ==================================================================================
    // 注意：即使该测试函数仅抛出异常，也需要使用注解 @Produces("application/json")，否则报错：
    //  org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException:
    //      MessageBodyWriter not found for media type=application/x-www-form-urlencoded,
    //      type=class com.example.jerseydemo.rest.ErrorResponse,
    //      genericType=class com.example.jerseydemo.rest.ErrorResponse.
    // 然后请求又会被重定向到 /error，然后因为找不到error页再次抛出 404 异常
    // ==================================================================================
    @GET
    @Path("/exception3")
    @Produces("application/json")
    public Response testException3() throws Exception {
        throw new Exception("Unhandled Exception Test");
    }
}
