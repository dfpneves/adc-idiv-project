package rest.resources;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.google.gson.Gson;


@Path("/utils")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8") 
public class ComputationResource {

    private final Gson g = new Gson();

    public ComputationResource() {
    } //nothing to be done here @GET

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello() {
            return Response.ok("all good, hello",MediaType.TEXT_PLAIN).build();
    }
}