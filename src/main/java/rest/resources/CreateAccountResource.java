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
    public Response doCreateAccount(Operation<InputData> op) {
        InputData data = op.input;

        LOG.fine("Attempt to register user: " + data.username);

        OutputData out = new OutputData();
        try {
            if (!data.validCreateAccount()) {
                return Response.ok(g.toJson(out.getOutError(Errors.INVALID_INPUT)), MediaType.APPLICATION_JSON).build();
            }
        } catch (Exception e) {
            return Response.ok(g.toJson(out.getOutError(Errors.INVALID_INPUT)), MediaType.APPLICATION_JSON).build();
        }

        try {
            Transaction txn = datastore.newTransaction();
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity user = txn.get(userKey);

            if(user != null) {
                // USER_ALREADY_EXISTS 9901
                txn.rollback();
                return Response.ok(g.toJson(out.getOutError(Errors.USER_ALREADY_EXISTS)), MediaType.APPLICATION_JSON).build();
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




                JsonObject dataJson = new JsonObject();
                dataJson.addProperty("username", data.username);
                dataJson.addProperty("role", data.role);

                JsonObject outJson = out.getOut(dataJson);

                return Response.ok(g.toJson(outJson)).build();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.severe("Error creating user account: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error creating user account.").build();
        }
    }
}