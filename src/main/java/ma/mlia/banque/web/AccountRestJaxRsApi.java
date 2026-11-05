package ma.mlia.banque.web;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import ma.mlia.banque.entities.Account;
import ma.mlia.banque.repositories.AccountRepository;
import org.springframework.stereotype.Component;

/**
 * JAX-RS resource exposing the CRUD operations of the bank accounts.
 * Every method can produce and consume both JSON and XML.
 */
@Component
@Path("/banque")
public class AccountRestJaxRsApi {

    private final AccountRepository accountRepository;

    public AccountRestJaxRsApi(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // READ: all accounts
    @GET
    @Path("/comptes")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

    // READ: a single account by its identifier
    @GET
    @Path("/comptes/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAccount(@PathParam("id") Long id) {
        return accountRepository.findById(id)
                .map(account -> Response.ok(account).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    // CREATE: a new account
    @POST
    @Path("/comptes")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addAccount(Account account) {
        Account saved = accountRepository.save(account);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    // UPDATE: an existing account
    @PUT
    @Path("/comptes/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateAccount(@PathParam("id") Long id, Account account) {
        return accountRepository.findById(id)
                .map(existing -> {
                    existing.setBalance(account.getBalance());
                    existing.setCreationDate(account.getCreationDate());
                    existing.setType(account.getType());
                    return Response.ok(accountRepository.save(existing)).build();
                })
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    // DELETE: an account
    @DELETE
    @Path("/comptes/{id}")
    public Response deleteAccount(@PathParam("id") Long id) {
        if (!accountRepository.existsById(id)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        accountRepository.deleteById(id);
        return Response.noContent().build();
    }

    // READ: aggregated balance per account type, handy to check a JSON/XML response quickly
    @GET
    @Path("/comptes/stats")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response stats() {
        long total = accountRepository.count();
        double sum = accountRepository.findAll().stream().mapToDouble(Account::getBalance).sum();
        return Response.ok(new Stats(total, sum)).build();
    }
}
