
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


public class ServiceMockingLoginTest extends JerseyTest {

    public static final String HTTP_LOCALHOST_9998_AUTH_LOGIN = "http://localhost:9998/auth/login";
    static private Client client;

    public static class MockAuthServiceFactory
            implements Factory<AuthService> {
        @Override
        public AuthService provide() {
            final AuthService mockedService
                    = Mockito.mock(AuthService.class);
            Mockito.when(mockedService.login(Mockito.anyString(), Mockito.anyString()))
                    .thenAnswer(new Answer<Boolean>() {
                        @Override
                        public Boolean answer(InvocationOnMock invocation)
                                throws Throwable {
                            String email = (String) invocation.getArguments()[0];
                            String passwdHash = (String) invocation.getArguments()[1];
                            if (email.isEmpty()) {
                                throw new WebApplicationException(Response.Status.BAD_REQUEST);
                            } else if (email.equals("testuser") && passwdHash.equals("testhash")) {
                                return Boolean.TRUE;
                            }
                            return Boolean.FALSE;
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
    public void testMockedAuthService() {

        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_LOGIN)
                .queryParam("email", "testuser")
                .queryParam("passwd", "testhash")
                .request(MediaType.TEXT_PLAIN).get();
        Assert.assertEquals(200, response.getStatus());

        Boolean msg = response.readEntity(Boolean.class);
        Assert.assertEquals(Boolean.TRUE, msg);

        response.close();


    }

    @Test
    public void testMockedAuthServiceFailed() {

        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_LOGIN)
                .queryParam("email", "testuser1")
                .queryParam("passwd", "testhash1")
                .request(MediaType.TEXT_PLAIN).get();
        Assert.assertEquals(200, response.getStatus());

        Boolean msg = response.readEntity(Boolean.class);
        Assert.assertEquals(Boolean.FALSE, msg);

        response.close();

    }

    @Test
    public void testMockedAuthServiceBadRequest() {

        Response response = client.target(HTTP_LOCALHOST_9998_AUTH_LOGIN)
                .queryParam("email", "")
                .queryParam("passwd", "")
                .request(MediaType.TEXT_PLAIN).get();
        Assert.assertEquals(400, response.getStatus());

        response.close();

    }


    @AfterClass
    public static void afterClass() {
        client.close();
    }

}