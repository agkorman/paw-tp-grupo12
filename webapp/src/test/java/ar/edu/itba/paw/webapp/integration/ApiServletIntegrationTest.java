package ar.edu.itba.paw.webapp.integration;

import ar.edu.itba.paw.webapp.api.ApiApplication;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.services.CommunityService;
import ar.edu.itba.paw.webapp.api.common.ApiMediaTypes;
import ar.edu.itba.paw.webapp.api.common.ApiAuthentication;
import ar.edu.itba.paw.services.JwtAccessTokenService;
import ar.edu.itba.paw.webapp.config.ApiSecurityConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZoneOffset;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
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
        api.setInitParameter("javax.ws.rs.Application", TestApiApplication.class.getName());
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
    void mvcCookieDoesNotAuthenticateApiRequests() throws Exception {
        // Arrange
        final HttpTester.Response login = get("/webapp/test-session", null);
        final String cookie = login.get("Set-Cookie").split(";", 2)[0];

        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api/unregistered", "Cookie: " + cookie + "\r\n");

        // Assertions
        assertError(response, 401, "AUTHENTICATION_REQUIRED");
        assertNull(response.get("Location"));
    }

    @Test
    void mutationCannotUseMvcSessionCookie() throws Exception {
        // Arrange
        final HttpTester.Response login = get("/webapp/test-session", null);
        final String cookie = login.get("Set-Cookie").split(";", 2)[0];

        // Exercise
        final HttpTester.Response response = request("POST", "/webapp/api/communities/classics", "Cookie: " + cookie + "\r\n");

        // Assertions
        assertError(response, 401, "AUTHENTICATION_REQUIRED");
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

    @Test
    void basicAuthenticatesWithUtf8PasswordAndReturnsLinkedTokensWithoutCookie() throws Exception {
        // Arrange
        final String headers = basic("nica@example.test", "contraseña:123");
        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api", headers);
        // Assertions
        assertEquals(200, response.getStatus());
        assertEquals("7", json.readTree(response.getContent()).path("name").asText());
        assertFalse(json.readTree(response.getContent()).path("admin").asBoolean());
        assertFalse(json.readTree(response.getContent()).path("session").asBoolean());
        assertEquals("rt_initial", response.get(ApiAuthentication.REFRESH_TOKEN_HEADER));
        assertNotNull(response.get(ApiAuthentication.ACCESS_TOKEN_HEADER));
        assertTrue(String.join(",", response.getValuesList("Link")).contains(
                "</webapp/api/users/7/sessions/12>; rel=\"" + ApiAuthentication.SESSION_RELATION + "\""));
        assertNull(response.get("Set-Cookie"));
        assertEquals("no-store", response.get("Cache-Control"));
    }

    @Test
    void rememberMeSelectsRememberedSession() throws Exception {
        // Arrange
        final String headers = basic("nica@example.test", "contraseña:123") + "X-Remember-Me: true\r\n";
        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api", headers);
        // Assertions
        assertEquals(200, response.getStatus());
        assertEquals("rt_remembered", response.get(ApiAuthentication.REFRESH_TOKEN_HEADER));
        assertTrue(String.join(",", response.getValuesList("Link")).contains("/sessions/13>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"yes", "TRUE", "", "false\r\nX-Remember-Me: true"})
    void invalidRememberMeIs400WithoutCredentials(final String value) throws Exception {
        // Arrange
        final String headers = basic("nica@example.test", "contraseña:123") + "X-Remember-Me: " + value + "\r\n";
        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api", headers);
        // Assertions
        assertError(response, 400, "INVALID_REQUEST");
        assertNull(response.get(ApiAuthentication.ACCESS_TOKEN_HEADER));
        assertNull(response.get(ApiAuthentication.REFRESH_TOKEN_HEADER));
    }

    @ParameterizedTest
    @CsvSource({"nica@example.test,wrong", "missing@example.test,wrong"})
    void badPasswordAndUnknownEmailHaveSameResponse(final String email, final String password) throws Exception {
        // Arrange
        final String headers = basic(email, password);
        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api", headers);
        // Assertions
        assertError(response, 401, "INVALID_CREDENTIALS");
        assertNotNull(response.get("WWW-Authenticate"));
        assertNull(response.get("Set-Cookie"));
        assertNull(response.get(ApiAuthentication.REFRESH_TOKEN_HEADER));
    }

    @Test
    void basicCannotExecuteMutations() throws Exception {
        // Arrange
        final String headers = basic("nica@example.test", "contraseña:123");
        // Exercise
        final HttpTester.Response response = request("POST", "/webapp/api", headers);
        // Assertions
        assertError(response, 401, "INVALID_CREDENTIALS");
        assertNull(response.get(ApiAuthentication.REFRESH_TOKEN_HEADER));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bearer invalid", "Basic !!!", "Digest credential", "Bearer", ""})
    void suppliedBadCredentialsFailEvenOnPublicResource(final String authorization) throws Exception {
        // Arrange
        final String headers = "Authorization: " + authorization + "\r\n";
        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api/communities/classics", headers);
        // Assertions
        assertEquals(401, response.getStatus());
        assertNotNull(response.get("WWW-Authenticate"));
        assertNull(response.get(ApiAuthentication.ACCESS_TOKEN_HEADER));
    }

    @Test
    void duplicateAuthorizationIsRejected() throws Exception {
        // Arrange
        final String headers = "Authorization: Bearer " + access(0) + "\r\nAuthorization: Bearer another\r\n";
        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api", headers);
        // Assertions
        assertError(response, 401, "INVALID_CREDENTIALS");
    }

    @Test
    void validAccessExposesIdentityWithoutReissuingTokens() throws Exception {
        // Arrange
        final String headers = "Authorization: Bearer " + access(0) + "\r\n";
        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api", headers);
        // Assertions
        assertEquals(200, response.getStatus());
        assertEquals("7", json.readTree(response.getContent()).path("name").asText());
        assertFalse(json.readTree(response.getContent()).path("session").asBoolean());
        assertNull(response.get(ApiAuthentication.ACCESS_TOKEN_HEADER));
        assertNull(response.get(ApiAuthentication.REFRESH_TOKEN_HEADER));
        assertNull(response.get("Set-Cookie"));
    }

    @Test
    void validIdentityCannotAccessUnregisteredRoutes() throws Exception {
        // Arrange
        final String headers = "Authorization: Bearer " + access(0) + "\r\n";
        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api/unregistered", headers);
        // Assertions
        assertError(response, 403, "ACCESS_DENIED");
        assertNull(response.get("WWW-Authenticate"));
        assertNull(response.get(ApiAuthentication.ACCESS_TOKEN_HEADER));
    }

    @Test
    void expiredAccessIs401EvenForPublicResource() throws Exception {
        // Arrange
        final String headers = "Authorization: Bearer " + access(-1000) + "\r\n";
        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api/communities/classics", headers);
        // Assertions
        assertError(response, 401, "TOKEN_EXPIRED");
        assertNull(response.get(ApiAuthentication.REFRESH_TOKEN_HEADER));
    }

    @Test
    void refreshLoadsCurrentRolesAndDeliversResourceAndReplacementTogether() throws Exception {
        // Arrange
        final String headers = "Authorization: Bearer rt_current\r\nX-Remember-Me: false\r\n";
        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api", headers);
        // Assertions
        assertEquals(200, response.getStatus());
        assertTrue(json.readTree(response.getContent()).path("admin").asBoolean());
        assertEquals("rt_next", response.get(ApiAuthentication.REFRESH_TOKEN_HEADER));
        assertNotNull(response.get(ApiAuthentication.ACCESS_TOKEN_HEADER));
        assertFalse(response.getContent().contains("rt_next"));
        assertNull(response.get("Set-Cookie"));
    }

    @ParameterizedTest
    @CsvSource({"rt_expired,SESSION_EXPIRED", "rt_revoked,SESSION_REVOKED", "rt_reused,REFRESH_TOKEN_REUSED", "rt_unknown,INVALID_TOKEN"})
    void refreshRejectionIsMappedToStableCode(final String refresh, final String code) throws Exception {
        // Arrange
        final String headers = "Authorization: Bearer " + refresh + "\r\n";
        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api", headers);
        // Assertions
        assertError(response, 401, code);
        assertNull(response.get(ApiAuthentication.REFRESH_TOKEN_HEADER));
    }

    @ParameterizedTest
    @CsvSource({"/webapp/api/unregistered,403", "/webapp/api/communities/missing,404", "/webapp/api/communities/broken,500"})
    void replacementSurvivesDownstreamErrors(final String path, final int status) throws Exception {
        // Arrange
        final String headers = "Authorization: Bearer rt_current\r\n";
        // Exercise
        final HttpTester.Response response = request("GET", path, headers);
        // Assertions
        assertEquals(status, response.getStatus());
        assertEquals("rt_next", response.get(ApiAuthentication.REFRESH_TOKEN_HEADER));
        assertNotNull(response.get(ApiAuthentication.ACCESS_TOKEN_HEADER));
        assertTrue(String.join(",", response.getValuesList("Link")).contains("/sessions/12>"));
        assertEquals("no-store", response.get("Cache-Control"));
    }

    @Test
    void replacementSurvivesResetAndCoexistsWithResourceLinks() throws Exception {
        // Arrange
        final String headers = "Authorization: Bearer rt_current\r\n";
        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api?reset=true", headers);
        // Assertions
        assertEquals(200, response.getStatus());
        assertEquals("rt_next", response.get(ApiAuthentication.REFRESH_TOKEN_HEADER));
        assertEquals("no-store", response.get("Cache-Control"));
        final String links = String.join(",", response.getValuesList("Link"));
        assertTrue(links.contains("/sessions/12>"));
        assertTrue(links.contains("rel=\"self\""));
    }

    @Test
    void missingCredentialsRemainAnonymousAfterAnotherRequestAuthenticated() throws Exception {
        // Arrange
        request("GET", "/webapp/api", "Authorization: Bearer " + access(0) + "\r\n");
        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api", "");
        // Assertions
        assertEquals("anonymous", json.readTree(response.getContent()).path("name").asText());
        assertNull(response.get(ApiAuthentication.ACCESS_TOKEN_HEADER));
    }

    @Test
    void unexpectedAuthenticationFailureIsSanitizedJson() throws Exception {
        // Arrange
        final String headers = basic("broken@example.test", "password");
        // Exercise
        final HttpTester.Response response = request("GET", "/webapp/api", headers);
        // Assertions
        assertError(response, 500, "INTERNAL_ERROR");
        assertFalse(response.getContent().contains("private-error-detail"));
        assertNull(response.get(ApiAuthentication.REFRESH_TOKEN_HEADER));
    }

    private String basic(final String email, final String password) {
        return "Authorization: Basic " + Base64.getEncoder().encodeToString(
                (email + ":" + password).getBytes(StandardCharsets.UTF_8)) + "\r\n";
    }

    private String access(final long offset) {
        final JwtAccessTokenService issuer = new JwtAccessTokenService(ApiTokenTestConfig.KEY,
                Clock.fixed(ApiTokenTestConfig.NOW.plusSeconds(offset), ZoneOffset.UTC));
        return issuer.issue(ApiTokenTestConfig.credentials("unused", 12, 86400), Set.of("USER"));
    }

    /** The real API index will be added with identity resources in the next point. */
    public static class TestApiApplication extends ApiApplication {
        public TestApiApplication() { register(IdentityFixture.class); }
    }

    @Path("/")
    public static class IdentityFixture {
        @GET
        @Produces(ApiMediaTypes.INDEX_V1)
        public Response identity(@Context final SecurityContext security, @Context final HttpServletRequest request,
                                 @Context final HttpServletResponse response, @QueryParam("reset") final boolean reset) {
            if (reset) {
                response.reset();
                response.setHeader("Cache-Control", "public, max-age=3600");
            }
            return Response.ok(Map.of("name", security.getUserPrincipal() == null ? "anonymous"
                    : security.getUserPrincipal().getName(), "admin", security.isUserInRole("ADMIN"),
                    "session", request.getSession(false) != null))
                    .header("Link", "</webapp/api>; rel=\"self\"").build();
        }
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
    @Import({ApiSecurityConfig.class, ApiTokenTestConfig.class})
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
