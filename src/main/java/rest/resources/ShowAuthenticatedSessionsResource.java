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
public class ShowAuthenticatedSessionsResource {

    /**
     * Logger Object
     */
    private static final Logger LOG = Logger.getLogger(ShowAuthenticatedSessionsResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

    private final Gson g = new Gson();

    public ShowAuthenticatedSessionsResource() {
    }


    @POST
    @Path("/ShowAuthenticatedSessions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response showAuthenticatedSessionsResource(Operation<InputData> op) {
        InputData inputData = op.input;
        TokenData tokenData = op.token;

        if (tokenData == null){
            return Response.ok("token data null",MediaType.TEXT_PLAIN).build();
        }

        Entity tokenLog = validateToken(tokenData.tokenId);

        if (tokenLog == null){
            return Response.ok("validate token null",MediaType.TEXT_PLAIN).build();
        }

        String username = tokenLog.getString("username");
        LOG.info("Token login successful for: " + username);

        LOG.fine("Attempt to login user: " + username);

        Key userKey = userKeyFactory.newKey(username);
        Entity callerUser = datastore.get(userKey);

        if (callerUser == null) {
            return Response.ok("User does not exist or no type",MediaType.TEXT_PLAIN).build();
        }

        String callerRole = callerUser.getString("user_role");

        if (!("ADMIN".equals(callerRole))) {
            // add error here
            return Response.ok("role error",MediaType.TEXT_PLAIN).build();
        }

        // Prepare the query for users
        Query<Entity> query;

        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
        Query<Entity> allTokensQuery = Query.newEntityQueryBuilder()
                .setKind("Token")
                .build();

        // Execute the query
        QueryResults<Entity> results = datastore.run(allTokensQuery);


        // Build JSON array
        JsonArray usersArray = new JsonArray();

        try {
            while (results.hasNext()) {
                Entity token = results.next();

                String curUsername = token.getString("username");
                long expiration = token.getLong("expirationData");

                Key curUserKey = userKeyFactory.newKey(curUsername);
                Entity user = datastore.get(curUserKey);

                if (user != null) {

                    String role = user.getString("user_role");

                    JsonObject tokenJson = new JsonObject();
                    tokenJson.addProperty("tokenId", token.getKey().getName());
                    tokenJson.addProperty("username", curUsername);  // or user.getString("user_username") if stored
                    tokenJson.addProperty("role", role);
                    tokenJson.addProperty("expiresAt:", expiration);
                    usersArray.add(tokenJson);
                }
            }
            JsonObject response = new JsonObject();
            response.addProperty("status", "success");

            JsonObject dataJson = new JsonObject();
            dataJson.add("sessions", usersArray);
            response.add("data", dataJson);

            return Response.ok(g.toJson(response), MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("ERROR: " + e.getMessage(), MediaType.TEXT_PLAIN).build();
        }
        /*
        format
        input
        {
        "input": {},
        "token": { ... }

        output:
        {
            "status": "success",
            "data": {
                "sessions": [
                            {
                                "tokenId": "string",
                                "username": "string",
                                "role": "string",
                                "expiresAt": "timestamp"
                            }
                        ]
                    }
            }
        }
         */
    }

    @GET
    @Path("/helloSAS")
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello() {
        try {
            throw new IOException("UPS");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception on Method /hello", e);
            return Response.ok("all good, hello",MediaType.TEXT_PLAIN).build();
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
