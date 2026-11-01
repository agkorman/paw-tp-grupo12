package ar.edu.itba.paw.webapp.integration;

import ar.edu.itba.paw.webapp.api.ApiApplication;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.services.CommunityService;
import ar.edu.itba.paw.webapp.api.common.ApiMediaTypes;
import ar.edu.itba.paw.webapp.config.ApiSecurityConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Real servlet, Jersey providers, Spring injection and Security filters, with no TCP sockets or database. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiServletIntegrationTest {

    private final ObjectMapper json = new ObjectMapper();
    private Server server;
    private LocalConnector connector;

    @BeforeAll
    void startServlets() throws Exception {
        final AnnotationConfigWebApplicationContext spring = new AnnotationConfigWebApplicationContext();
        spring.register(TestConfig.class);
        server = new Server();
        connector = new LocalConnector(server);
        server.addConnector(connector);
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/webapp");
        context.addEventListener(new ContextLoaderListener(spring));
        context.addFilter(new FilterHolder(new DelegatingFilterProxy("springSecurityFilterChain")),
                "/*", java.util.EnumSet.of(DispatcherType.REQUEST));
        final ServletHolder api = new ServletHolder(ServletContainer.class);
        api.setInitParameter("javax.ws.rs.Application", ApiApplication.class.getName());
        api.setInitOrder(2);
        context.addServlet(api, "/api/*");
        context.addServlet(new ServletHolder(new LegacyServlet()), "/");
        server.setHandler(context);
        server.start();
    }

    @AfterAll
    void stopServlets() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void communityUsesSpringServiceAndSerializesOnlyPublicDto() throws Exception {
        // Arrange
        final String path = "/webapp/api/communities/classics";

        // Exercise
        final HttpTester.Response response = get(path, ApiMediaTypes.COMMUNITY_V1);

        // Assertions
        assertEquals(200, response.getStatus());
        assertEquals(ApiMediaTypes.COMMUNITY_V1, response.get("Content-Type"));
        final JsonNode body = json.readTree(response.getContent());
        assertTrue(body.get("id").isTextual());
        assertEquals("9007199254740993", body.get("id").asText());
        assertEquals("Classics", body.get("name").asText());
        assertEquals("/webapp/api/communities/classics", body.at("/_links/self/href").asText());
        assertEquals(5, body.size());
        assertFalse(body.has("createdBy"));
        assertFalse(body.has("createdAt"));
        assertNull(response.get("Set-Cookie"));
    }

    @Test
    void missingCommunityReturnsJsonInsteadOfLegacyFallback() throws Exception {
        // Arrange
        final String path = "/webapp/api/communities/missing";

        // Exercise
        final HttpTester.Response response = get(path, ApiMediaTypes.COMMUNITY_V1);

        // Assertions
        assertError(response, 404, "RESOURCE_NOT_FOUND");
    }

    @Test
    void exceptionDoesNotExposeInternalDetails() throws Exception {
        // Arrange
        final String path = "/webapp/api/communities/broken";

        // Exercise
        final HttpTester.Response response = get(path, ApiMediaTypes.COMMUNITY_V1);

        // Assertions
        assertError(response, 500, "INTERNAL_ERROR");
        assertFalse(response.getContent().contains("private-database-detail"));
        assertFalse(response.getContent().contains("IllegalStateException"));
    }

    @Test
    void unsupportedAcceptReturnsStructured406() throws Exception {
        // Arrange
        final String path = "/webapp/api/communities/classics";

        // Exercise
        final HttpTester.Response response = get(path, "application/xml");

        // Assertions
        assertError(response, 406, "NOT_ACCEPTABLE");
    }

    @Test
    void missingAcceptSelectsV1() throws Exception {
        // Arrange
        final String path = "/webapp/api/communities/classics";

        // Exercise
        final HttpTester.Response response = get(path, null);

        // Assertions
        assertEquals(200, response.getStatus());
        assertEquals(ApiMediaTypes.COMMUNITY_V1, response.get("Content-Type"));
        assertEquals("no-store", response.get("Cache-Control"));
        assertEquals("Accept", response.get("Vary"));
    }

    @Test
    void anonymousUnregisteredApiRequestReturns401WithoutLoginRedirect() throws Exception {
        // Arrange
        final String path = "/webapp/api/unregistered";

        // Exercise
        final HttpTester.Response response = get(path, "text/html");

        // Assertions
        assertError(response, 401, "AUTHENTICATION_REQUIRED");
        assertNull(response.get("Location"));
        assertNull(response.get("Set-Cookie"));
    }

    @Test
    void authenticatedUnregisteredApiRequestIsDeniedByDefault() throws Exception {
        // Arrange
        final HttpTester.Response login = get("/webapp/test-session", null);
        final String cookie = login.get("Set-Cookie").split(";", 2)[0];

        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api/unregistered", "Cookie: " + cookie + "\r\n");

        // Assertions
        assertError(response, 403, "ACCESS_DENIED");
        assertNull(response.get("Location"));
    }

    @Test
    void csrfStillRejectsSessionBasedMutation() throws Exception {
        // Arrange
        final HttpTester.Response login = get("/webapp/test-session", null);
        final String cookie = login.get("Set-Cookie").split(";", 2)[0];

        // Exercise
        final HttpTester.Response response = request("POST", "/webapp/api/communities/classics", "Cookie: " + cookie + "\r\n");

        // Assertions
        assertError(response, 403, "ACCESS_DENIED");
    }

    @Test
    void legacyPathsRemainOutsideJersey() throws Exception {
        // Arrange
        final String path = "/webapp/communities/classics";

        // Exercise
        final HttpTester.Response response = get(path, null);

        // Assertions
        assertEquals(200, response.getStatus());
        assertEquals("legacy", response.getContent());
    }

    private HttpTester.Response get(final String path, final String accept) throws Exception {
        return request("GET", path, accept == null ? "" : "Accept: " + accept + "\r\n");
    }

    private HttpTester.Response request(final String method, final String path, final String headers) throws Exception {
        return HttpTester.parseResponse(connector.getResponse(method + " " + path
                + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n" + headers + "\r\n"));
    }

    private void assertError(final HttpTester.Response response, final int status, final String code) throws IOException {
        assertEquals(status, response.getStatus());
        assertEquals(ApiMediaTypes.ERROR_V1, response.get("Content-Type"));
        assertEquals(code, json.readTree(response.getContent()).get("code").asText());
        assertEquals("no-store", response.get("Cache-Control"));
        assertEquals("Accept", response.get("Vary"));
    }

    @Configuration
    @EnableWebSecurity
    @Import(ApiSecurityConfig.class)
    @ComponentScan("ar.edu.itba.paw.webapp.api")
    static class TestConfig {

        @Bean
        CommunityService communityService() {
            final CommunityService service = mock(CommunityService.class);
            final Community community = new Community("classics", "Classics", "Classic cars");
            community.setId(9007199254740993L);
            when(service.getCommunityBySlug(anyString())).thenReturn(Optional.empty());
            when(service.getCommunityBySlug("classics")).thenReturn(Optional.of(community));
            when(service.getCommunityBySlug("broken")).thenThrow(new IllegalStateException("private-database-detail"));
            return service;
        }

        @Bean
        @Order(2)
        SecurityFilterChain legacySecurityChain(final HttpSecurity http) throws Exception {
            return http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll()).build();
        }
    }

    /** Test-only legacy servlet and session fixture; neither is registered in production. */
    static class LegacyServlet extends HttpServlet {

        @Override
        protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
            if ("/test-session".equals(request.getServletPath())) {
                request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        new SecurityContextImpl(new UsernamePasswordAuthenticationToken("test-user", null, List.of())));
            }
            response.setContentType("text/plain");
            response.getWriter().write("legacy");
        }
    }
}
