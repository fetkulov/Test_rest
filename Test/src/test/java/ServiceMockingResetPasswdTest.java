import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import entities.UserAccount;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.*;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import service.auth.AuthService;
import service.rest.AuthResource;


public class ServiceMockingResetPasswdTest extends JerseyTest {

    public static final String HTTP_LOCALHOST_9998_AUTH_RESET = "http://localhost:9998/auth/reset";
    static private Client client;

    public static class MockAuthServiceFactory
            implements Factory<AuthService> {
        @Override
        public AuthService provide() {
            final AuthService mockedService
                    = Mockito.mock(AuthService.class);
            Mockito.when(mockedService.resetPasswd(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                    .thenAnswer(new Answer<UserAccount>() {
                        @Override
                        public UserAccount answer(InvocationOnMock invocation)
                                throws Throwable {
                            String email = (String) invocation.getArguments()[0];
                            String resetClause = (String) invocation.getArguments()[1];
                            String newPasswd = (String) invocation.getArguments()[2];

                            if (email == null || newPasswd == null) {
                                throw new WebApplicationException(Response.Status.BAD_REQUEST);
                            } else if (email.isEmpty() || newPasswd.isEmpty()) {
                                throw new WebApplicationException(Response.Status.BAD_REQUEST);
                            } else if (!"test@email.com".equals(email)) {
                                throw new WebApplicationException(Response.Status.NO_CONTENT);
                            }
                            // could/should contain other validation

                            UserAccount newUserAccount = new UserAccount();
                            newUserAccount.setUserEmail(email);
                            newUserAccount.setPasswd(newPasswd);
                            newUserAccount.setResetClause(resetClause);
                            return newUserAccount;
                        }

                    });


            return mockedService;
        }

        @Override
        public void dispose(AuthService t) {
        }
    }

    @Override
    public Application configure() {
        AbstractBinder binder = new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(MockAuthServiceFactory.class)
                        .to(AuthService.class);
            }
        };
        ResourceConfig config = new ResourceConfig(AuthResource.class);
        config.register(binder);
        return config;
    }

    @BeforeClass
    public static void initialize() {
        client = ClientBuilder.newClient();
    }

    @Test
    public void testMockedAuthServiceResetTest() {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserEmail("test@email.com");
        userAccount.setPasswd("newPasswd");
        userAccount.setResetClause("testClause");

        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_RESET)
                .request(MediaType.APPLICATION_XML).post(Entity.entity(userAccount, MediaType.WILDCARD_TYPE));

        UserAccount resetedUserAccount = response.readEntity(UserAccount.class);
        Assert.assertEquals(200, response.getStatus());

        Assert.assertEquals(userAccount.getUserEmail(), resetedUserAccount.getUserEmail());
        Assert.assertEquals(userAccount.getResetClause(), resetedUserAccount.getResetClause());
        Assert.assertEquals(userAccount.getPasswd(), resetedUserAccount.getPasswd());

        response.close();

    }


    @Test
    public void testMockedAuthServiceResetNullFailed() {

        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_RESET)
                .request(MediaType.APPLICATION_XML).post(Entity.entity(null, MediaType.WILDCARD_TYPE));

        Assert.assertEquals(400, response.getStatus());

        response.close();

    }

    @Test
    public void testMockedAuthServiceResetEmptyFailed() {
        UserAccount userAccount = new UserAccount();
        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_RESET)
                .request(MediaType.APPLICATION_XML).post(Entity.entity(userAccount, MediaType.WILDCARD_TYPE));

        Assert.assertEquals(400, response.getStatus());

        response.close();

    }

    @Test
    public void testMockedAuthServiceResetWrongEmailFailed() {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserEmail("wrong@email.com");
        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_RESET)
                .request(MediaType.APPLICATION_XML).post(Entity.entity(userAccount, MediaType.WILDCARD_TYPE));

        Assert.assertEquals(400, response.getStatus());

        response.close();

    }

    @Test
    public void testMockedAuthServiceResetEmptyNewPasswdFailed() {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserEmail("test@email.com");
        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_RESET)
                .request(MediaType.APPLICATION_XML).post(Entity.entity(userAccount, MediaType.WILDCARD_TYPE));

        Assert.assertEquals(400, response.getStatus());

        response.close();

    }

    @AfterClass
    public static void afterClass() {
        client.close();
    }

}