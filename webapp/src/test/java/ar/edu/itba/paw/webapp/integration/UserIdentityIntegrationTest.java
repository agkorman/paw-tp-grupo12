package ar.edu.itba.paw.webapp.integration;

import ar.edu.itba.paw.persistence.UserJpaDao;
import ar.edu.itba.paw.services.CarRequestService;
import ar.edu.itba.paw.services.CommunityService;
import ar.edu.itba.paw.services.JwtAccessTokenService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.TokenAuthenticationServiceImpl;
import ar.edu.itba.paw.services.UserRegistrationServiceImpl;
import ar.edu.itba.paw.services.UserServiceImpl;
import ar.edu.itba.paw.webapp.api.ApiApplication;
import ar.edu.itba.paw.webapp.api.common.ApiAuthentication;
import ar.edu.itba.paw.webapp.api.common.ApiMediaTypes;
import ar.edu.itba.paw.webapp.config.ApiSecurityConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/** Real Jersey/Security/services/JPA over a private database; Jetty uses an in-memory connector. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserIdentityIntegrationTest {

    private final ObjectMapper json = new ObjectMapper();
    private Server server;
    private LocalConnector connector;
    private JdbcTemplate jdbc;

    @BeforeAll
    void start() throws Exception {
        final AnnotationConfigWebApplicationContext spring = new AnnotationConfigWebApplicationContext();
        spring.register(Config.class);
        server = new Server();
        connector = new LocalConnector(server);
        server.addConnector(connector);
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/webapp");
        context.addEventListener(new ContextLoaderListener(spring));
        context.addFilter(new FilterHolder(new DelegatingFilterProxy("springSecurityFilterChain")), "/*",
                java.util.EnumSet.of(DispatcherType.REQUEST));
        final ServletHolder api = new ServletHolder(ServletContainer.class);
        api.setInitParameter("javax.ws.rs.Application", ApiApplication.class.getName());
        api.setInitOrder(1);
        context.addServlet(api, "/api/*");
        server.setHandler(context);
        server.start();
        jdbc = spring.getBean(JdbcTemplate.class);
    }

    @AfterEach
    void cleanIsolatedDatabase() {
        jdbc.update("DELETE FROM users");
    }

    @AfterAll
    void stop() throws Exception {
        if (server != null) { server.stop(); }
    }

    @Test
    void createsUserThenAuthenticatesThroughIndexAndFollowsCanonicalLinks() throws Exception {
        // Arrange
        final String body = registration("nica");
        // Exercise
        final HttpTester.Response created = request("POST", "/webapp/api/users", null, body);
        // Assertions
        assertEquals(201, created.getStatus(), created.getContent());
        final String userPath = json.readTree(created.getContent()).at("/_links/self/href").asText();
        assertEquals(userPath, java.net.URI.create(created.get("Location")).getPath());
        assertEquals(3, json.readTree(created.getContent()).size());
        assertNull(created.get(ApiAuthentication.ACCESS_TOKEN_HEADER));
        assertEquals(0, count("auth_sessions"));
        assertNotEquals("password123", jdbc.queryForObject("SELECT password FROM users", String.class));

        final HttpTester.Response login = login("nica");
        assertEquals(200, login.getStatus(), login.getContent());
        assertEquals(1, json.readTree(login.getContent()).size());
        assertEquals("/webapp/api/users", json.readTree(login.getContent()).at("/_links/users/href").asText());
        final String access = login.get(ApiAuthentication.ACCESS_TOKEN_HEADER);
        final String sessionPath = sessionLink(login);
        assertTrue(sessionPath.startsWith(userPath + "/sessions/"));
        final HttpTester.Response owner = request("GET", userPath, access, null);
        final JsonNode profile = json.readTree(owner.getContent());
        assertEquals("nica@example.test", profile.path("email").asText());
        assertEquals("es", profile.path("preferredLocale").asText());
        assertEquals("USER", profile.path("roles").get(0).asText());
        assertFalse(profile.has("password"));
        final HttpTester.Response session = request("GET", sessionPath, access, null);
        assertEquals(200, session.getStatus(), session.getContent());
        assertEquals(userPath, json.readTree(session.getContent()).at("/_links/user/href").asText());
        assertEquals(ApiMediaTypes.SESSION_V1, session.get("Content-Type"));
        assertEquals(2, json.readTree(session.getContent()).path("_links").size());
        assertFalse(session.getContent().contains(login.get(ApiAuthentication.REFRESH_TOKEN_HEADER)));
        assertNull(login.get("Set-Cookie"));
        assertEquals("no-store", owner.get("Cache-Control"));
    }

    @Test
    void publicAndOtherUserViewsCannotSeePrivateProfileFields() throws Exception {
        // Arrange
        final String target = create("nica");
        create("other");
        final String otherAccess = login("other").get(ApiAuthentication.ACCESS_TOKEN_HEADER);
        // Exercise
        final HttpTester.Response response = request("GET", target, otherAccess, null);
        // Assertions
        assertEquals(200, response.getStatus());
        final JsonNode profile = json.readTree(response.getContent());
        assertEquals(3, profile.size());
        assertFalse(profile.has("email"));
        assertFalse(profile.has("preferredLocale"));
        assertFalse(profile.has("roles"));
        assertEquals(profile, json.readTree(request("GET", target, null, null).getContent()));
    }

    @Test
    void localeOnlyPatchPreservesNameAndPersistsPreference() throws Exception {
        // Arrange
        final String target = create("nica");
        final String access = login("nica").get(ApiAuthentication.ACCESS_TOKEN_HEADER);
        // Exercise
        final HttpTester.Response response = request("PATCH", target, access, "{\"preferredLocale\":\"en\"}");
        // Assertions
        assertEquals(200, response.getStatus(), response.getContent());
        assertEquals("nica", json.readTree(response.getContent()).path("displayName").asText());
        assertEquals("en", json.readTree(response.getContent()).path("preferredLocale").asText());
        assertEquals("en", jdbc.queryForObject("SELECT preferred_locale FROM users", String.class));
        assertEquals("nica", jdbc.queryForObject("SELECT username FROM users", String.class));
    }

    @Test
    void updatesBothFieldsAtomically() throws Exception {
        // Arrange
        final String target = create("nica");
        final String access = login("nica").get(ApiAuthentication.ACCESS_TOKEN_HEADER);
        // Exercise
        final HttpTester.Response response = request("PATCH", target, access,
                "{\"displayName\":\"  nica.new  \",\"preferredLocale\":\"en\"}");
        // Assertions
        assertEquals(200, response.getStatus(), response.getContent());
        assertEquals("nica.new", json.readTree(response.getContent()).path("displayName").asText());
        assertEquals("nica.new", jdbc.queryForObject("SELECT username FROM users", String.class));
        assertEquals("en", jdbc.queryForObject("SELECT preferred_locale FROM users", String.class));
    }

    @Test
    void databaseFailureRollsBackWholePatch() throws Exception {
        // Arrange
        final String target = create("nica");
        final String access = login("nica").get(ApiAuthentication.ACCESS_TOKEN_HEADER);
        jdbc.execute("ALTER TABLE users ADD CONSTRAINT identity_test_locale CHECK (preferred_locale <> 'en')");
        try {
            // Exercise
            final HttpTester.Response response = request("PATCH", target, access,
                    "{\"displayName\":\"renamed\",\"preferredLocale\":\"en\"}");
            // Assertions
            assertEquals(500, response.getStatus());
            assertEquals("nica", jdbc.queryForObject("SELECT username FROM users", String.class));
            assertEquals("es", jdbc.queryForObject("SELECT preferred_locale FROM users", String.class));
        } finally {
            jdbc.execute("ALTER TABLE users DROP CONSTRAINT identity_test_locale");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "{\"displayName\":null}", "{\"preferredLocale\":null}",
            "{\"displayName\":\"   \"}", "{\"displayName\":\"newname\",\"preferredLocale\":\"fr\"}",
            "{\"role\":\"admin\"}", "{\"email\":\"change@example.test\"}", "{\"password\":\"password456\"}"})
    void invalidOrForbiddenPatchFieldsCannotModifyAccount(final String body) throws Exception {
        // Arrange
        final String target = create("nica");
        final String access = login("nica").get(ApiAuthentication.ACCESS_TOKEN_HEADER);
        // Exercise
        final HttpTester.Response response = request("PATCH", target, access, body);
        // Assertions
        assertEquals(400, response.getStatus(), response.getContent());
        assertEquals("nica", jdbc.queryForObject("SELECT username FROM users", String.class));
        assertEquals("es", jdbc.queryForObject("SELECT preferred_locale FROM users", String.class));
        assertEquals("user", jdbc.queryForObject("SELECT role FROM users", String.class));
        assertEquals("nica@example.test", jdbc.queryForObject("SELECT email FROM users", String.class));
    }

    @Test
    void cannotPatchAnotherUsersProfile() throws Exception {
        // Arrange
        final String target = create("nica");
        create("other");
        final String access = login("other").get(ApiAuthentication.ACCESS_TOKEN_HEADER);
        // Exercise
        final HttpTester.Response response = request("PATCH", target, access, "{\"displayName\":\"changed\"}");
        // Assertions
        assertEquals(403, response.getStatus());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username = 'nica'", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username = 'changed'", Integer.class));
    }

    @Test
    void conflictsReturn409WithFieldCode() throws Exception {
        // Arrange
        create("nica");
        final String body = registration("nica");
        // Exercise
        final HttpTester.Response response = request("POST", "/webapp/api/users", null, body);
        // Assertions
        assertEquals(409, response.getStatus(), response.getContent());
        final JsonNode error = json.readTree(response.getContent());
        assertEquals("USER_CONFLICT", error.path("code").asText());
        assertEquals("displayName", error.at("/fieldErrors/0/field").asText());
        assertEquals("DUPLICATE", error.at("/fieldErrors/0/code").asText());
        assertEquals(1, count("users"));
        assertEquals(0, count("auth_sessions"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "{\"displayName\":\"x\",\"email\":\"invalid\",\"password\":\"short\"}",
            "{\"displayName\":123,\"email\":\"x@example.test\",\"password\":\"password123\"}",
            "{\"displayName\":\"x\",\"email\":\"x@example.test\",\"password\":12345678}",
            "{\"displayName\":\"x\",\"displayName\":\"y\",\"email\":\"x@example.test\",\"password\":\"password123\"}",
            "{\"displayName\":\"x\",\"email\":\"x@example.test\",\"password\":\"password123\"} {}",
            "{\"displayName\":\"x\",\"email\":\"x@example.test\",\"password\":\"password123\",\"roles\":[\"ADMIN\"]}"})
    void invalidRegistrationDoesNotPersistAnything(final String body) throws Exception {
        // Arrange
        final String target = "/webapp/api/users";
        // Exercise
        final HttpTester.Response response = request("POST", target, null, body);
        // Assertions
        assertEquals(400, response.getStatus(), response.getContent());
        assertEquals(0, count("users"));
        assertEquals(0, count("auth_sessions"));
    }

    @Test
    void rejectsPasswordOverBcryptByteLimit() throws Exception {
        // Arrange
        final String body = json.writeValueAsString(Map.of("displayName", "nica", "email", "nica@example.test",
                "password", "ñ".repeat(37)));
        // Exercise
        final HttpTester.Response response = request("POST", "/webapp/api/users", null, body);
        // Assertions
        assertEquals(400, response.getStatus(), response.getContent());
        assertEquals("password", json.readTree(response.getContent()).at("/fieldErrors/0/field").asText());
        assertEquals(0, count("users"));
    }

    @Test
    void emailConflictIs409WithoutAddingAccount() throws Exception {
        // Arrange
        create("nica");
        final String body = json.writeValueAsString(Map.of("displayName", "other", "email", "nica@example.test", "password", "password123"));
        // Exercise
        final HttpTester.Response response = request("POST", "/webapp/api/users", null, body);
        // Assertions
        assertEquals(409, response.getStatus());
        assertEquals("email", json.readTree(response.getContent()).at("/fieldErrors/0/field").asText());
        assertEquals(1, count("users"));
    }

    @Test
    void registrationRequiresVersionedContentType() throws Exception {
        // Arrange
        final String body = registration("nica");
        // Exercise
        final HttpTester.Response response = raw("POST", "/webapp/api/users", "", body, "application/json");
        // Assertions
        assertEquals(415, response.getStatus(), response.getContent());
        assertEquals(0, count("users"));
    }

    @Test
    void unsupportedResponseVersionDoesNotCreateUser() throws Exception {
        // Arrange
        final String body = registration("nica");
        // Exercise
        final HttpTester.Response response = raw("POST", "/webapp/api/users",
                "Accept: application/vnd.paw.user.v2+json\r\n", body);
        // Assertions
        assertEquals(406, response.getStatus());
        assertEquals(0, count("users"));
    }

    @Test
    void invalidRefreshLogoutIs401AndDoesNotRevoke() throws Exception {
        // Arrange
        create("nica");
        final String session = sessionLink(login("nica"));
        // Exercise
        final HttpTester.Response response = request("DELETE", session, "rt_invalid", null);
        // Assertions
        assertEquals(401, response.getStatus());
        assertEquals("INVALID_TOKEN", json.readTree(response.getContent()).path("code").asText());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM auth_sessions WHERE revoked_at IS NOT NULL", Integer.class));
    }

    @Test
    void invalidUtf8InsidePasswordIsBadRequestWithoutPersistingUser() throws Exception {
        // Arrange
        final String body = "{\"displayName\":\"nica\",\"email\":\"nica@example.test\",\"password\":\"ñpassword\"}";
        final byte[] wire = ("POST /webapp/api/users HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\nContent-Type: "
                + ApiMediaTypes.USER_V1 + "\r\nContent-Length: " + body.length() + "\r\n\r\n" + body)
                .getBytes(StandardCharsets.ISO_8859_1);
        // Exercise
        final HttpTester.Response response = HttpTester.parseResponse(connector.getResponse(java.nio.ByteBuffer.wrap(wire)));
        // Assertions
        assertEquals(400, response.getStatus(), response.getContent());
        assertEquals("INVALID_REQUEST", json.readTree(response.getContent()).path("code").asText());
        assertFalse(response.getContent().contains("password"));
        assertEquals(0, count("users"));
    }

    @Test
    void anotherUsersSessionIs404() throws Exception {
        // Arrange
        create("nica");
        final String session = sessionLink(login("nica"));
        create("other");
        final String access = login("other").get(ApiAuthentication.ACCESS_TOKEN_HEADER);
        // Exercise
        final HttpTester.Response response = request("GET", session, access, null);
        // Assertions
        assertEquals(404, response.getStatus());
        assertFalse(response.getContent().contains("expiresAt"));
    }

    @Test
    void expiredSessionIs404EvenWithStillValidAccess() throws Exception {
        // Arrange
        create("nica");
        final HttpTester.Response login = login("nica");
        jdbc.update("UPDATE auth_sessions SET created_at = ?, expires_at = ?",
                Timestamp.from(ApiTokenTestConfig.NOW.minusSeconds(86400)), Timestamp.from(ApiTokenTestConfig.NOW));
        // Exercise
        final HttpTester.Response response = request("GET", sessionLink(login), login.get(ApiAuthentication.ACCESS_TOKEN_HEADER), null);
        // Assertions
        assertEquals(404, response.getStatus());
        assertEquals(1, count("auth_sessions"));
        assertEquals(1, count("auth_refresh_tokens"));
    }

    @Test
    void accessLogoutIsIdempotentAndRevokedSessionIs404() throws Exception {
        // Arrange
        create("nica");
        final HttpTester.Response login = login("nica");
        final String session = sessionLink(login);
        final String access = login.get(ApiAuthentication.ACCESS_TOKEN_HEADER);
        // Exercise
        final HttpTester.Response response = request("DELETE", session, access, null);
        // Assertions
        assertEquals(204, response.getStatus(), response.getContent());
        assertEquals("LOGOUT", jdbc.queryForObject("SELECT revocation_reason FROM auth_sessions", String.class));
        assertEquals(204, request("DELETE", session, access, null).getStatus());
        assertEquals(404, request("GET", session, access, null).getStatus());
        assertEquals(1, count("auth_refresh_tokens"));
        assertNull(response.get(ApiAuthentication.REFRESH_TOKEN_HEADER));
    }

    @Test
    void refreshLogoutDoesNotRotateAndLeavesExistingAccessValid() throws Exception {
        // Arrange
        final String user = create("nica");
        final HttpTester.Response login = login("nica");
        final String refresh = login.get(ApiAuthentication.REFRESH_TOKEN_HEADER);
        // Exercise
        final HttpTester.Response response = request("DELETE", sessionLink(login), refresh, null);
        // Assertions
        assertEquals(204, response.getStatus(), response.getContent());
        assertNull(response.get(ApiAuthentication.ACCESS_TOKEN_HEADER));
        assertNull(response.get(ApiAuthentication.REFRESH_TOKEN_HEADER));
        assertEquals(1, count("auth_refresh_tokens"));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM auth_refresh_tokens WHERE consumed_at IS NOT NULL", Integer.class));
        assertEquals("LOGOUT", jdbc.queryForObject("SELECT revocation_reason FROM auth_sessions", String.class));
        assertEquals(200, request("GET", user, login.get(ApiAuthentication.ACCESS_TOKEN_HEADER), null).getStatus());
        final HttpTester.Response rejected = request("GET", "/webapp/api", refresh, null);
        assertEquals(401, rejected.getStatus());
        assertEquals("SESSION_REVOKED", json.readTree(rejected.getContent()).path("code").asText());
    }

    @Test
    void logoutCannotRevokeAnotherSessionOfSameUser() throws Exception {
        // Arrange
        create("nica");
        final HttpTester.Response first = login("nica");
        final HttpTester.Response second = login("nica");
        // Exercise
        final HttpTester.Response response = request("DELETE", sessionLink(second), first.get(ApiAuthentication.REFRESH_TOKEN_HEADER), null);
        // Assertions
        assertEquals(404, response.getStatus());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM auth_sessions WHERE revoked_at IS NOT NULL", Integer.class));
        assertEquals(404, request("DELETE", sessionLink(second), first.get(ApiAuthentication.ACCESS_TOKEN_HEADER), null).getStatus());
        assertEquals(2, count("auth_refresh_tokens"));
    }

    @Test
    void consumedRefreshUsedForLogoutRevokesReplacement() throws Exception {
        // Arrange
        create("nica");
        final HttpTester.Response login = login("nica");
        final String consumed = login.get(ApiAuthentication.REFRESH_TOKEN_HEADER);
        final HttpTester.Response renewed = request("GET", "/webapp/api", consumed, null);
        // Exercise
        final HttpTester.Response response = request("DELETE", sessionLink(login), consumed, null);
        // Assertions
        assertEquals(401, response.getStatus());
        assertEquals("REFRESH_TOKEN_REUSED", json.readTree(response.getContent()).path("code").asText());
        assertEquals("REFRESH_REUSE", jdbc.queryForObject("SELECT revocation_reason FROM auth_sessions", String.class));
        final HttpTester.Response rejected = request("GET", "/webapp/api", renewed.get(ApiAuthentication.REFRESH_TOKEN_HEADER), null);
        assertEquals("SESSION_REVOKED", json.readTree(rejected.getContent()).path("code").asText());
        assertEquals(2, count("auth_refresh_tokens"));
    }

    @Test
    void anonymousLogoutCannotRevokeSession() throws Exception {
        // Arrange
        create("nica");
        final String session = sessionLink(login("nica"));
        // Exercise
        final HttpTester.Response response = request("DELETE", session, null, null);
        // Assertions
        assertEquals(401, response.getStatus());
        assertNotNull(response.get("WWW-Authenticate"));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM auth_sessions WHERE revoked_at IS NOT NULL", Integer.class));
    }

    private String create(final String name) throws Exception {
        final HttpTester.Response response = request("POST", "/webapp/api/users", null, registration(name));
        assertEquals(201, response.getStatus(), response.getContent());
        return json.readTree(response.getContent()).at("/_links/self/href").asText();
    }

    private String registration(final String name) throws Exception {
        return json.writeValueAsString(Map.of("displayName", name, "email", name + "@example.test", "password", "password123"));
    }

    private HttpTester.Response login(final String name) throws Exception {
        final String basic = Base64.getEncoder().encodeToString((name + "@example.test:password123").getBytes(StandardCharsets.UTF_8));
        return raw("GET", "/webapp/api", "Authorization: Basic " + basic + "\r\n", null);
    }

    private String sessionLink(final HttpTester.Response response) {
        final String links = String.join(",", response.getValuesList("Link"));
        for (final String link : links.split(",")) {
            if (link.contains(ApiAuthentication.SESSION_RELATION)) {
                return link.substring(link.indexOf('<') + 1, link.indexOf('>'));
            }
        }
        throw new AssertionError("Missing authenticated session link: status=" + response.getStatus());
    }

    private HttpTester.Response request(final String method, final String path, final String token, final String body) throws Exception {
        return raw(method, path, token == null ? "" : "Authorization: Bearer " + token + "\r\n", body);
    }

    private HttpTester.Response raw(final String method, final String path, final String headers, final String body) throws Exception {
        return raw(method, path, headers, body, ApiMediaTypes.USER_V1);
    }

    private HttpTester.Response raw(final String method, final String path, final String headers, final String body,
                                   final String contentType) throws Exception {
        final String content = body == null ? "" : body;
        final String wire = method + " " + path + " HTTP/1.1\r\nHost: localhost\r\n"
                + "Connection: close\r\n" + headers
                + (body == null ? "" : "Content-Type: " + contentType + "\r\n")
                + "Content-Length: " + content.getBytes(StandardCharsets.UTF_8).length + "\r\n\r\n" + content;
        return HttpTester.parseResponse(connector.getResponse(java.nio.ByteBuffer.wrap(wire.getBytes(StandardCharsets.UTF_8))));
    }

    private int count(final String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    @Configuration
    @EnableWebSecurity
    @ComponentScan("ar.edu.itba.paw.webapp.api")
    @Import({AuthSessionTransactionTest.Config.class, ApiSecurityConfig.class, UserJpaDao.class, UserServiceImpl.class,
            UserRegistrationServiceImpl.class, TokenAuthenticationServiceImpl.class, JwtAccessTokenService.class})
    static class Config {
        @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(4); }
        @Bean SecretKey authSigningKey() { return ApiTokenTestConfig.KEY; }
        @Bean ReviewService reviewService() { return mock(ReviewService.class); }
        @Bean CarRequestService carRequestService() { return mock(CarRequestService.class); }
        @Bean CommunityService communityService() { return mock(CommunityService.class); }
    }
}
