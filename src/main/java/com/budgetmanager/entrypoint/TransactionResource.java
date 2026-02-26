package com.budgetmanager.entrypoint;

import com.budgetmanager.application.CreateTransactionCommand;
import com.budgetmanager.application.CreateTransactionUseCase;
import com.budgetmanager.application.GetTransactionUseCase;
import com.budgetmanager.domain.Transaction;
import com.budgetmanager.domain.TransactionType;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Path("transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {


    @Inject
    CreateTransactionUseCase createTransactionUseCase;

    @Inject
    GetTransactionUseCase getTransactionUseCase;

    @Inject
    JsonWebToken jwt;

    public record TransactionRequest(String userId, BigDecimal amount, TransactionType type, String categoryId,
                                     String categoryName) {
    }

//    @GET
//    public Response ping() {
//        return Response.ok(Map.of("ping", "Lambda ok!")).build();
//    }

    @POST
    public Response createTransaction(TransactionRequest transactionRequest) {

        CreateTransactionCommand command = new CreateTransactionCommand(transactionRequest.userId, transactionRequest.amount, transactionRequest.type, transactionRequest.categoryId, transactionRequest.categoryName);

        Transaction savedTransaction = createTransactionUseCase.execute(command);


        System.out.println("Creating transaction: " + savedTransaction);
        return Response.status(Response.Status.CREATED).entity(savedTransaction).build();
    }

    @GET
    public Response getTransactionsByUser(@QueryParam("userId") String userId) {
        if (userId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"O parâmetro userId é obrigatório\"}").build();
        }

        List<Transaction> transactions = getTransactionUseCase.execute(userId);
        return Response.ok(transactions).build();
    }
}
