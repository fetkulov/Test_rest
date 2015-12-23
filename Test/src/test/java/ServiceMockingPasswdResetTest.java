import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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


public class ServiceMockingPasswdResetTest extends JerseyTest {

    public static final String HTTP_LOCALHOST_9998_AUTH_PASSWDRESET = "http://localhost:9998/auth/passwdreset";
    static private Client client;

    public static class MockAuthServiceFactory
            implements Factory<AuthService> {
        @Override
        public AuthService provide() {
            final AuthService mockedService
                    = Mockito.mock(AuthService.class);
            Mockito.when(mockedService.passwdReset(Mockito.anyString()))
                    .thenAnswer(new Answer<String>() {
                        @Override
                        public String answer(InvocationOnMock invocation)
                                throws Throwable {
                            String email = (String) invocation.getArguments()[0];

                            if (email.isEmpty()) {
                                throw new WebApplicationException(Response.Status.BAD_REQUEST);
                            } else if (!email.equals("email@email.com")) {
                                throw new WebApplicationException(Response.Status.NO_CONTENT);
                            }
                            return "newpasswd";
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
    public void testMockedPasswdResetService() {

        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_PASSWDRESET)
                .path("email@email.com")
                .request(MediaType.TEXT_PLAIN).get();
        Assert.assertEquals(200, response.getStatus());

        String msg = response.readEntity(String.class);
        Assert.assertEquals("newpasswd", msg);

        response.close();


    }

    @Test
    public void testMockedPasswdResetServiceFailed() {

        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_PASSWDRESET)
                .path("email1@email.com")
                .request(MediaType.TEXT_PLAIN).get();
        Assert.assertEquals(204, response.getStatus());


        response.close();

    }

    @Test
    public void testMockedPasswdResetServiceBadRequest() {

        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_PASSWDRESET)
                .path("email.com")
                .request(MediaType.TEXT_PLAIN).get();
        Assert.assertEquals(404, response.getStatus()); //URI doesn't match

        response.close();

    }

    @Test
    public void testMockedAuthServiceEmptyBadRequest() {

        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_PASSWDRESET)
                .request(MediaType.TEXT_PLAIN).get();

        Assert.assertEquals(404, response.getStatus()); //URI doesn't match

        response.close();

    }

    @AfterClass
    public static void afterClass() {
        client.close();
    }

}