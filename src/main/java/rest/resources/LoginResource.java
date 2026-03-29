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


@Path("/Login")
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

	@POST@Path("/ReLogin")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doLoginV1b(Operation<InputData> op) {
        InputData data = op.input;
        TokenData tData = op.token;

        LOG.fine("Attempt to login user");

        Entity tokenLog = validateToken(tData.tokenId);

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


    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doCreateAccount(Operation<InputData> op) {

        LOG.fine("Attempt to login user");

        InputData data = op.input;

        LOG.fine("Attempt to login user: " + data.username);

        Key userKey = userKeyFactory.newKey(data.username);
        Entity user = datastore.get(userKey);

        if( user != null ) {
            String hashedPWD = user.getString("user_pwd");
            if( hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
                KeyFactory logKeyFactory = datastore.newKeyFactory()
                        .addAncestor(PathElement.of("User", data.username))
                        .setKind("UserLog");
                Key logKey = datastore.allocateId(logKeyFactory.newKey());
                Entity userLog = Entity.newBuilder(logKey)
                        .set("user_login_time", Timestamp.now())
                        .build();
                datastore.put(userLog);
                LOG.info("User '" + data.username + "' logged in successfuly.");

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
                // INVALID_CREDENTIALS 9900
                LOG.warning("Wrong password for: " + data.username);
                return Response.status(Status.FORBIDDEN).build();
            }
        }
        else {
            // USER_NOT_FOUND 9902
            LOG.warning("Failed login attempt for username: " + data.username);
            return Response.status(Status.FORBIDDEN).build();
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