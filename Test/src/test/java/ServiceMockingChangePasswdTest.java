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


public class ServiceMockingChangePasswdTest extends JerseyTest {

    public static final String HTTP_LOCALHOST_9998_AUTH_CHANGE = "http://localhost:9998/auth/changepasswd";
    static private Client client;

    public static class MockAuthServiceFactory
            implements Factory<AuthService> {
        @Override
        public AuthService provide() {
            final AuthService mockedService
                    = Mockito.mock(AuthService.class);
            Mockito.when(mockedService.changePasswd(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                    .thenAnswer(new Answer<UserAccount>() {
                        @Override
                        public UserAccount answer(InvocationOnMock invocation)
                                throws Throwable {
                            String email = (String) invocation.getArguments()[0];
                            String oldPasswd = (String) invocation.getArguments()[1];
                            String newPasswd = (String) invocation.getArguments()[2];

                            if (email == null || oldPasswd == null || newPasswd == null) {
                                throw new WebApplicationException(Response.Status.BAD_REQUEST);
                            } else if (email.isEmpty() || newPasswd.isEmpty()) {
                                throw new WebApplicationException(Response.Status.BAD_REQUEST);
                            } else if (!"test@email.com".equals(email)) {
                                throw new WebApplicationException(Response.Status.NO_CONTENT);
                            } else if (!"oldPasswd".equals(oldPasswd)) {
                                throw new WebApplicationException(Response.Status.BAD_REQUEST);
                            }
                            // could/should contain other validation

                            UserAccount newUserAccount = new UserAccount();
                            newUserAccount.setUserEmail(email);
                            newUserAccount.setPasswd(newPasswd);
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
    public void testMockedAuthServiceChangePasswd() {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserEmail("test@email.com");
        userAccount.setPasswd("newPasswd");
        userAccount.setOldPasswd("oldPasswd");

        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_CHANGE)
                .request(MediaType.APPLICATION_XML).post(Entity.entity(userAccount, MediaType.WILDCARD_TYPE));

        Assert.assertEquals(200, response.getStatus());
        String newUserAccountStr = response.readEntity(String.class);
        Assert.assertEquals("UserAccount{resetClause='null', passwd='newPasswd', userEmail='test@email.com'}", newUserAccountStr);
        response.close();

    }

    @Test
    public void testMockedAuthServiceChangePasswdwrongOld() {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserEmail("test@email.com");
        userAccount.setPasswd("newPasswd");
        userAccount.setOldPasswd("notoldPasswd");

        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_CHANGE)
                .request(MediaType.APPLICATION_XML).post(Entity.entity(userAccount, MediaType.WILDCARD_TYPE));

        Assert.assertEquals(400, response.getStatus());

        response.close();

    }

    @Test
    public void testMockedAuthServiceChangePasswdEmptyNew() {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserEmail("test@email.com");
        userAccount.setPasswd("");
        userAccount.setOldPasswd("oldPasswd");

        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_CHANGE)
                .request(MediaType.APPLICATION_XML).post(Entity.entity(userAccount, MediaType.WILDCARD_TYPE));

        Assert.assertEquals(400, response.getStatus());

        response.close();

    }

    @Test
    public void testMockedAuthServiceChangePasswdWrongEmail() {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserEmail("test1@email.com");
        userAccount.setPasswd("newPasswd");
        userAccount.setOldPasswd("oldPasswd");

        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_CHANGE)
                .request(MediaType.APPLICATION_XML).post(Entity.entity(userAccount, MediaType.WILDCARD_TYPE));

        Assert.assertEquals(204, response.getStatus());

        response.close();

    }

    @Test
    public void testMockedAuthServiceChangePasswdEmpty() {
        UserAccount userAccount = new UserAccount();
        userAccount.setOldPasswd("oldPasswd");

        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_CHANGE)
                .request(MediaType.APPLICATION_XML).post(Entity.entity(userAccount, MediaType.WILDCARD_TYPE));

        Assert.assertEquals(400, response.getStatus());

        response.close();

    }

    @AfterClass
    public static void afterClass() {
        client.close();
    }

}