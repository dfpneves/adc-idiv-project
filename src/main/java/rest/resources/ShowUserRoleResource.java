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
    @Path("/showuserrole")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doShowUserRole(Operation<InputData> op) {
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
        // input checks
        String targetUsername = inputData.username;

        if (targetUsername == null)
            return Response.ok(g.toJson(out.getOutError(Errors.USER_NOT_FOUND)), MediaType.APPLICATION_JSON).build();

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

        String callerUserRole = callerUser.getString("user_role");
        String targetUserRole = targetUser.getString("user_role");

        if ("USER".equals(callerUserRole)){
            return Response.ok(g.toJson(out.getOutError(Errors.UNAUTHORIZED)), MediaType.APPLICATION_JSON).build();
        } else if ("BOFFICER".equals(callerUserRole) && ("BOFFICER".equals(targetUserRole) || "ADMIN".equals(targetUserRole))){
            return Response.ok(g.toJson(out.getOutError(Errors.UNAUTHORIZED)), MediaType.APPLICATION_JSON).build();
        }

        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("username", targetUsername);
        dataJson.addProperty("role", targetUserRole);

        JsonObject outJson = out.getOut(dataJson);

        return Response.ok(g.toJson(outJson), MediaType.APPLICATION_JSON).build();
    }
}
