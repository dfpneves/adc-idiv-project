package rest.resources;


import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;
import rest.util.*;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;


@Path("")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {

    /**
     * Logger Object
     */
    private static final Logger LOG = Logger.getLogger(LogoutResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

    private final Gson g = new Gson();

    public LogoutResource() { }

    @POST
    @Path("/Logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doLogout(Operation<InputData> op) {
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

        String targetUsername = inputData.username;
        // username check invalid input but not in the errors of this operation
        if (targetUsername == null)
            return Response.ok(g.toJson(out.getOutError(Errors.INVALID_INPUT)), MediaType.APPLICATION_JSON).build();

        // Get target user
        Key targetKey = userKeyFactory.newKey(targetUsername);
        Entity targetUser = datastore.get(targetKey);

        if (targetUser == null)
            return Response.ok(g.toJson(out.getOutError(Errors.USER_NOT_FOUND)), MediaType.APPLICATION_JSON).build();

        // get token username
        String username = tokenLog.getString("username");
        Key userKey = userKeyFactory.newKey(username);
        Entity callerUser = datastore.get(userKey);

        if (callerUser == null)
            return Response.ok(g.toJson(out.getOutError(Errors.FORBIDDEN)), MediaType.APPLICATION_JSON).build();

        // assuming logout of every session from target user
        if (!("ADMIN".equals(callerUser.getString("user_role"))))
            return Response.ok(g.toJson(out.getOutError(Errors.UNAUTHORIZED)), MediaType.APPLICATION_JSON).build();
        else if (!(targetUsername.equals(username)))
            return Response.ok(g.toJson(out.getOutError(Errors.UNAUTHORIZED)), MediaType.APPLICATION_JSON).build();

        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
        Query<Entity> allTokensQuery = Query.newEntityQueryBuilder()
                .setKind("Token")
                .setFilter(StructuredQuery.PropertyFilter.eq("username", targetUsername))
                .build();

        // Execute the query
        QueryResults<Entity> results = datastore.run(allTokensQuery);

        while (results.hasNext()) {
            Entity token = results.next();
            datastore.delete(token.getKey());
        }

        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("message", "Logout successful");

        JsonObject outJson = out.getOut(dataJson);

        return Response.ok(g.toJson(outJson), MediaType.APPLICATION_JSON).build();
    }
}
