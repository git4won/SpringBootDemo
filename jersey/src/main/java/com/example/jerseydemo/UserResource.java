package com.example.jerseydemo;

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
            return Response.status(404).build();
        }

        return Response.status(200).entity(user).build();
    }

    @GET
    @Produces("application/json")
    public Response getAllUsers() {
        return Response.status(200).entity(new ArrayList<>(DB.values())).build();
    }

    @POST
    // @Consumes 表明传入的 Content-Type
    @Consumes("application/json")
    @Produces("application/json")
    public Response createUser(User user) {

        if (user.getName() == null || user.getNick() == null) {
            return Response.status(400).entity("Please provide all mandatory inputs").build();
        }

        User newUser = new User();
        newUser.setId(DB.values().size()); // id 从 0 开始
        newUser.setName(user.getName());
        newUser.setNick(user.getNick());
        DB.put(newUser.getId(), newUser);

        return Response.ok().build();
    }
}
