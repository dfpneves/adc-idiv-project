package rest.resources;


import java.util.logging.Logger;

import com.google.cloud.datastore.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import rest.util.*;

import com.google.gson.Gson;


@Path("")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowUsersResource {

    /**
     * Logger Object
     */
    private static final Logger LOG = Logger.getLogger(ShowUsersResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

    private final Gson g = new Gson();

    public ShowUsersResource() {
    }


    @POST
    @Path("/showusers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doUsers(Operation<InputData> op) {
        InputData inputData = op.input;
        TokenData tokenData = op.token;

        OutputData out = new OutputData();
        // token checks
        if (tokenData == null)
            return Response.ok(g.toJson(out.getOutError(Errors.INVALID_TOKEN)), MediaType.APPLICATION_JSON).build();

        Entity tokenLog = TokenValidation.getToken(tokenData.tokenId);

        if (tokenLog == null)
            return Response.ok(g.toJson(out.getOutError(Errors.INVALID_TOKEN)), MediaType.APPLICATION_JSON).build();

        if (TokenValidation.expiredToken(tokenLog))
            return Response.ok(g.toJson(out.getOutError(Errors.TOKEN_EXPIRED)), MediaType.APPLICATION_JSON).build();

        String username = tokenLog.getString("username");
        LOG.info("Token login successful for: " + username);

        LOG.fine("Attempt to login user: " + username);

        Key userKey = userKeyFactory.newKey(username);
        Entity callerUser = datastore.get(userKey);

        if (callerUser == null) {
            return Response.ok("User does not exist or no type",MediaType.TEXT_PLAIN).build();
        }

        String callerRole = callerUser.getString("user_role");

        // Prepare the query for users
        Query<Entity> query;

        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
        Query<Entity> allUsersQuery;

        // ADMIN sees all users
        if ("ADMIN".equals(callerRole)) {
            allUsersQuery = Query.newEntityQueryBuilder()
                    .setKind("User")
                    .build();
        }
        // BOFFICER sees only USER accounts
        else if ("BOFFICER".equals(callerRole)) {
            allUsersQuery = Query.newEntityQueryBuilder()
                    .setKind("User")
                    .setFilter(StructuredQuery.PropertyFilter.eq("user_role", "USER"))
                    .build();
        }
        // Other roles are unauthorized
        else {
            // add error here
            return Response.ok(g.toJson(out.getOutError(Errors.UNAUTHORIZED)), MediaType.APPLICATION_JSON).build();
        }

        // Execute the query
        QueryResults<Entity> results = datastore.run(allUsersQuery);


        // Build JSON array
        JsonArray usersArray = new JsonArray();

        try {
            while (results.hasNext()) {
                Entity user = results.next();
                JsonObject userJson = new JsonObject();
                userJson.addProperty("userId", user.getKey().getName());
                userJson.addProperty("username", user.getKey().getName());  // or user.getString("user_username") if stored
                userJson.addProperty("email", user.getKey().getName());
                userJson.addProperty("role", user.getString("user_role"));
                usersArray.add(userJson);
            }

            JsonObject dataJson = new JsonObject();
            dataJson.add("users", usersArray);
            JsonObject outJson = out.getOut(dataJson);

            return Response.ok(g.toJson(outJson), MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            // unexpected error
            return Response.ok("ERROR: " + e.getMessage(), MediaType.TEXT_PLAIN).build();
        }
    }
}
