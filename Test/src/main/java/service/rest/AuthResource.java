package service.rest;

import entities.UserAccount;
import service.auth.AuthService;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Class containing Auth resources
 */
@Path("/auth")
public class AuthResource {

    @Inject
    private AuthService authService;

    @GET
    @Path("login")
    @Produces(MediaType.TEXT_PLAIN)
    public Boolean login(@QueryParam("email") String email, @QueryParam("passwd") String passwdHash) {
        return authService.login(email, passwdHash);
    }

    @GET
    @Path(value = "passwdreset/{emailAddress:.+@.+\\.[a-z]+}")
    @Produces(MediaType.TEXT_PLAIN)
    public String passwordReset(@PathParam(value = "emailAddress") String emailAddress) {
        return authService.passwdReset(emailAddress);
    }


    /**
     * For partial update in this case we should use PATCH. But it doesn't implemented in Jersey. Using Post.
     * Based on RFC 7231. However, we could make notes in documentation and use Put(reset is idempotent operation).
     */
    @POST
    @Path(value = "reset")
    @Consumes(MediaType.APPLICATION_XML)
    public Response passwordReset(@NotNull UserAccount userAccount) {
        UserAccount userAccountReseted = authService.resetPasswd(userAccount.getUserEmail(), userAccount.getResetClause(), userAccount.getPasswd());
        return Response.status(200).entity(userAccountReseted).build();
    }


    /**
     * For partial update in this case we should use PATCH(Based on RFC 7231.). But it doesn't implemented in Jersey. Using Post.
     */
    @POST
    @Path(value = "changepasswd")
    @Consumes(MediaType.APPLICATION_XML)
    public Response passwordChange(@NotNull UserAccount userAccount) {
        UserAccount newUserAccount = authService.changePasswd(userAccount.getUserEmail(), userAccount.getOldPasswd(), userAccount.getPasswd());
        return Response.status(200).entity(newUserAccount.toString()).build();
    }
}
