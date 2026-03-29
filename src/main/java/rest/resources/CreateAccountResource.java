package rest.resources;


import java.util.logging.Logger;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import com.google.gson.Gson;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;
import rest.util.*;


@Path("")
public class CreateAccountResource {

    private static final Logger LOG = Logger.getLogger(CreateAccountResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    private final Gson g = new Gson();

    public CreateAccountResource() {}


    @POST
    @Path("/CreateAccount")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUserV3(Operation<CreateAccountData> op) {
        CreateAccountData data = op.input;

        LOG.fine("Attempt to register user: " + data.username);

        if(!data.validCreateAccount()) {
            // INVALID_INPUT 9906
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
        }

        try {
            Transaction txn = datastore.newTransaction();
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity user = txn.get(userKey);

            if(user != null) {
                // USER_ALREADY_EXISTS 9901
                txn.rollback();
                return Response.status(Response.Status.CONFLICT).entity("User already exists.").build();
            }
            else {
                user = Entity.newBuilder(userKey)
                        .set("user_username", data.username)
                        .set("user_pwd", DigestUtils.sha512Hex(data.password))
                        .set("user_phone", data.phone)
                        .set("user_address", data.address)
                        .set("user_role", data.role)
                        .set("user_creation_time", Timestamp.now())
                        .build();
                txn.put(user);
                txn.commit();
                LOG.info("User account created " + data.username);


                OutputJ<OutputData> out = new OutputJ<>(new OutputData(data.username, data.role), "success");
                return Response.ok(g.toJson(out)).build();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.severe("Error creating user account: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error creating user account.").build();
        }
    }
}

/*
alternative output jason, using json objects also valid

JsonObject out = new JsonObject();
out.addProperty("status", "success");

JsonObject dataJson = new JsonObject();
dataJson.addProperty("username", data.username);
dataJson.addProperty("role", data.role);

out.add("data", dataJson);
*/
