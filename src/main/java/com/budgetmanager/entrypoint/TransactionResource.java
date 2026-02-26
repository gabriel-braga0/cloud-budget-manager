package com.budgetmanager.entrypoint;

import jakarta.transaction.Transaction;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("transaction")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {

    @GET
    public Response ping() {
        return Response.ok(Map.of("ping", "Lambda ok!")).build();
    }

    @POST
    public Response createTransaction(String text) {

        System.out.println("Creating transaction: " + text);
        return Response.status(Response.Status.CREATED).entity(Map.of("text", text)).build();
    }
}
