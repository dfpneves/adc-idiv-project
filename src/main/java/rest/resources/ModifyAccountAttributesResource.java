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
public class ModifyAccountAttributesResource {

    /**
     * Logger Object
     */
    private static final Logger LOG = Logger.getLogger(ModifyAccountAttributesResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

    private final Gson g = new Gson();

    public ModifyAccountAttributesResource() { }

    @POST
    @Path("/modifyaccountattributes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doModifyAttributes(Operation<InputData> op) {
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

        if (!(targetUsername.equals(username)))
            return Response.ok(g.toJson(out.getOutError(Errors.UNAUTHORIZED)), MediaType.APPLICATION_JSON).build();

        Entity updatedUser = Entity.newBuilder(callerUser).build();

        // does not update the c
        if (inputData.phone != null) {
            updatedUser = Entity.newBuilder(callerUser)
                    .set("user_phone", inputData.phone)
                    .build();
        }

        if (inputData.address != null) {
            updatedUser = Entity.newBuilder(callerUser)
                    .set("user_address", inputData.address)
                    .build();
        }

        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

        datastore.put(updatedUser);

        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("message", "Updated successfully");
        JsonObject outJson = out.getOut(dataJson);

        return Response.ok(g.toJson(outJson), MediaType.APPLICATION_JSON).build();
    }
}
