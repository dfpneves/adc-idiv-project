package rest.resources;


import java.util.logging.Logger;

import com.google.cloud.datastore.*;
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
public class DeleteAccountResource {

    /**
     * Logger Object
     */
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

    private final Gson g = new Gson();

    public DeleteAccountResource() { }

    @POST
    @Path("/deleteaccount")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doDelete(Operation<InputData> op) {
        InputData inputData = op.input;
        TokenData tokenData = op.token;

        OutputData out = new OutputData();

        if (tokenData == null)
            return Response.ok(g.toJson(out.getOutError(Errors.INVALID_TOKEN)), MediaType.APPLICATION_JSON).build();

        Entity tokenLog = TokenValidation.getToken(tokenData.tokenId);

        if (tokenLog == null)
            return Response.ok(g.toJson(out.getOutError(Errors.INVALID_TOKEN)), MediaType.APPLICATION_JSON).build();

        if (TokenValidation.expiredToken(tokenLog))
            return Response.ok(g.toJson(out.getOutError(Errors.TOKEN_EXPIRED)), MediaType.APPLICATION_JSON).build();

        String targetUsername = inputData.username;

        if (targetUsername == null){
            return Response.ok(g.toJson(out.getOutError(Errors.USER_NOT_FOUND)), MediaType.APPLICATION_JSON).build();
        }

        // Get target user
        Key targetKey = userKeyFactory.newKey(targetUsername);
        Entity targetUser = datastore.get(targetKey);

        if (targetUser == null) {
            return Response.ok(g.toJson(out.getOutError(Errors.USER_NOT_FOUND)), MediaType.APPLICATION_JSON).build();
        }

        String username = tokenLog.getString("username");

        Key userKey = userKeyFactory.newKey(username);
        Entity callerUser = datastore.get(userKey);

        if (callerUser == null) {
            // session has a user that no longer exists
            return Response.ok(g.toJson(out.getOutError(Errors.FORBIDDEN)), MediaType.APPLICATION_JSON).build();
        }

        String callerRole = callerUser.getString("user_role");

        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

        if ("ADMIN".equals(callerRole)) {
            // delete the tokens
            Query<Entity> allTokensQuery = Query.newEntityQueryBuilder()
                    .setKind("Token")
                    .setFilter(StructuredQuery.PropertyFilter.eq("username", targetUsername))
                    .build();

            QueryResults<Entity> results = datastore.run(allTokensQuery);

            while (results.hasNext()) {
                Entity token = results.next();
                datastore.delete(token.getKey());
            }
            datastore.delete(targetKey);
        } else {
            return Response.ok(g.toJson(out.getOutError(Errors.UNAUTHORIZED)), MediaType.APPLICATION_JSON).build();
        }

        JsonObject outJson = new JsonObject();

        JsonObject dataJson = new JsonObject();

        outJson.addProperty("status", "success");
        dataJson.addProperty("message", "Account deleted successfully");
        outJson.add("data", dataJson);

        return Response.ok(g.toJson(outJson), MediaType.APPLICATION_JSON).build();
    }
}
