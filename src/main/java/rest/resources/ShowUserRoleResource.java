package rest.resources;


import java.io.IOException;
import java.net.URI;
import java.sql.Time;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.cloud.datastore.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.codec.digest.DigestUtils;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import rest.util.*;

import com.google.cloud.Timestamp;
import com.google.gson.Gson;


@Path("")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowUserRoleResource  {

    /**
     * Logger Object
     */
    private static final Logger LOG = Logger.getLogger(ShowUserRoleResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

    private final Gson g = new Gson();

    public ShowUserRoleResource() { }

    @POST
    @Path("/ShowUserRole")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doCreateAccount(Operation<InputData> op) {
        InputData inputData = op.input;
        TokenData tokenData = op.token;

        if (tokenData == null){
            return Response.ok("token data null",MediaType.TEXT_PLAIN).build();
        }

        Entity tokenLog = validateToken(tokenData.tokenId);

        if (tokenLog == null){
            return Response.ok("validate token null",MediaType.TEXT_PLAIN).build();
        }

        String targetUsername = inputData.username;

        if (targetUsername == null){
            return Response.ok("not valid username",MediaType.TEXT_PLAIN).build();
        }

        // Get target user
        Key targetKey = userKeyFactory.newKey(targetUsername);
        Entity targetUser = datastore.get(targetKey);

        if (targetUser == null) {
            return Response.ok("user not found",MediaType.TEXT_PLAIN).build();
        }

        // get token username
        String username = tokenLog.getString("username");
        Key userKey = userKeyFactory.newKey(username);
        Entity callerUser = datastore.get(userKey);

        if (callerUser == null) {
            return Response.ok("User does not exist or no type",MediaType.TEXT_PLAIN).build();
        }


        String callerUserRole = callerUser.getString("user_role");
        String targetUserRole = targetUser.getString("user_role");

        if ("USER".equals(callerUserRole)){
            return Response.ok("Role error",MediaType.TEXT_PLAIN).build();
        } else if ("BOFFICER".equals(callerUserRole) && ("BOFFICER".equals(targetUserRole) || "ADMIN".equals(targetUserRole))){
            return Response.ok("Role error",MediaType.TEXT_PLAIN).build();
        }

        JsonObject outJson = new JsonObject();

        JsonObject dataJson = new JsonObject();

        outJson.addProperty("status", "success");
        dataJson.addProperty("username", targetUsername);
        dataJson.addProperty("role", targetUserRole);

        outJson.add("data", dataJson);

        return Response.ok(g.toJson(outJson), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/helloSUR")
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello() {
        try {
            throw new IOException("UPS");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception on Method /hello", e);
            return Response.temporaryRedirect(URI.create("/error/500.html")).build();
        }
    }

    private Entity validateToken(String tokenID) {

        if(tokenID == null)
            return null;

        Key tokenKey = datastore.newKeyFactory()
                .setKind("Token")
                .newKey(tokenID);

        Entity token = datastore.get(tokenKey);

        if(token == null)
            return null;

        long expiration = token.getLong("expirationData");

        if(System.currentTimeMillis() > expiration)
            return null;

        return token;
    }

}
