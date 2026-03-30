package rest.resources;

import java.sql.Time;
import java.util.logging.Logger;

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
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.gson.Gson;


@Path("")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	/** 
	 * Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

	private final Gson g = new Gson();
	
	public LoginResource() {} // Nothing to be done here


    @POST
    @Path("/Login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doLogin(Operation<InputData> op) {

        OutputData out = new OutputData();

        LOG.fine("Attempt to login user");

        InputData data = op.input;

        LOG.fine("Attempt to login user: " + data.username);

        Key userKey = userKeyFactory.newKey(data.username);
        Entity user = datastore.get(userKey);

        if( user != null ) {
            String hashedPWD = user.getString("user_pwd");
            if( hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
                LOG.info("User '" + data.username + "' logged in successfuly.");

                AuthToken token = new AuthToken(data.username, user.getString("user_role"));

                // added token storer
                Key tokenKey = datastore.newKeyFactory()
                        //.addAncestor(PathElement.of("User", data.username)) // add parent
                        .setKind("Token")
                        .newKey(token.tokenID);

                Entity tokenEntity = Entity.newBuilder(tokenKey)
                        .set("username", token.username)
                        .set("role", user.getString("user_role"))
                        .set("creationData", token.creationData)
                        .set("expirationData", token.expirationData)
                        .build();

                datastore.put(tokenEntity);
                // data storer
                return Response.ok(g.toJson(token)).build();
            }
            else {
                // INVALID_CREDENTIALS 9900
                LOG.warning("Wrong password for: " + data.username);
                return Response.ok(g.toJson(out.getOutError(Errors.INVALID_CREDENTIALS)), MediaType.APPLICATION_JSON).build();
            }
        }
        else {
            // USER_NOT_FOUND 9902
            LOG.warning("Failed login attempt for username: " + data.username);
            return Response.ok(g.toJson(out.getOutError(Errors.USER_NOT_FOUND)), MediaType.APPLICATION_JSON).build();
        }
    }
}
/*
NOTE
tokens, although they do have a role,
they always get the user role from the user
previously changing a user's role would not change
its permissions for an already lagged in session,
this way it always checks the user role

method 1
store role in token and use that for permissions

method 2
use the role stored in the User, heavier but
does restrict a signed-in user's actions

for this project I went with second approach.


    @POST@Path("/ReLogin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doLoginV1b(Operation<InputData> op) {
        InputData data = op.input;
        TokenData tData = op.token;

        LOG.fine("Attempt to login user");

        Entity tokenLog = getToken(tData.tokenId);

        if(tokenLog != null) {
            String username = tokenLog.getString("username");
            LOG.info("Token login successful for: " + username);

            AuthToken at = new AuthToken(username);
            return Response.ok(g.toJson(at)).build();
        }

        LOG.fine("Attempt to login user: " + data.username);

        Key userKey = userKeyFactory.newKey(data.username);
        Entity user = datastore.get(userKey);

        if( user != null ) {

            AuthToken token = new AuthToken(data.username);

            // added token storer
            Key tokenKey = datastore.newKeyFactory()
                    //.addAncestor(PathElement.of("User", data.username)) // add parent
                    .setKind("Token")
                    .newKey(token.tokenID);

            Entity tokenEntity = Entity.newBuilder(tokenKey)
                    .set("username", token.username)
                    .set("creationData", token.creationData)
                    .set("expirationData", token.expirationData)
                    .build();

            datastore.put(tokenEntity);
            // data storer

            return Response.ok(g.toJson(token)).build();
        }
        else {
            LOG.warning("Failed login attempt for username: " + data.username);
            return Response.status(Status.FORBIDDEN).build();
        }
    }

 */