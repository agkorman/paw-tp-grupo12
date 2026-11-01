package ar.edu.itba.paw.webapp.integration;

import ar.edu.itba.paw.webapp.api.ApiApplication;
import ar.edu.itba.paw.webapp.api.common.ApiMediaTypes;
import ar.edu.itba.paw.services.CommunityService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import com.fasterxml.jackson.annotation.JsonProperty;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** HTTP-level API validation coverage using a test-only resource and Jersey providers. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiValidationServletIntegrationTest {

    private final ObjectMapper json = new ObjectMapper();
    private Server server;
    private LocalConnector connector;
    private String cookie;
    private String csrf;

    @BeforeAll
    void startServlet() throws Exception {
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
        context.addServlet(new ServletHolder(new TestServlet()), "/");
        server.setHandler(context);
        server.start();

        final HttpTester.Response session = request("GET", "/webapp/test-csrf", "", "");
        cookie = session.get("Set-Cookie").split(";", 2)[0];
        csrf = session.get("X-CSRF-TOKEN");
        assertNotNull(csrf);
    }

    @AfterAll
    void stopServlet() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void validBodyPassesInputValidation() throws Exception {
        // Arrange
        final String body = "{\"display_name\":\"Valid\",\"rank\":3,\"license\":\"AB123\","
                + "\"children\":[{\"postal_code\":\"12345\"}]}";

        // Exercise
        final HttpTester.Response response = post("input", body, ApiMediaTypes.ERROR_V1, "application/json");

        // Assertions
        assertEquals(200, response.getStatus());
        assertEquals(ApiMediaTypes.ERROR_V1, response.get("Content-Type"));
        assertEquals("true", json.readTree(response.getContent()).path("accepted").asText());
    }

    @Test
    void inputViolationsMapToSafeCodesAndWhitelistedParams() throws Exception {
        // Arrange
        final String body = "{\"display_name\":\"\",\"rank\":99,\"license\":\"secret-marker\","
                + "\"policy\":\"BAD\",\"children\":[{\"postal_code\":\"\"}]}";

        // Exercise
        final HttpTester.Response response = post("input", body, ApiMediaTypes.ERROR_V1, "application/json");

        // Assertions
        final JsonNode error = assertError(response, 400, "VALIDATION_ERROR");
        final Set<String> pathsAndCodes = fieldErrors(error).stream()
                .map(field -> field.get("field").asText() + ":" + field.get("code").asText())
                .collect(Collectors.toSet());
        assertTrue(pathsAndCodes.contains("display_name:REQUIRED"));
        assertTrue(pathsAndCodes.contains("display_name:SIZE"));
        assertTrue(pathsAndCodes.contains("rank:OUT_OF_RANGE"));
        assertTrue(pathsAndCodes.contains("license:INVALID_FORMAT"));
        assertTrue(pathsAndCodes.contains("policy:INVALID_VALUE"));
        assertTrue(pathsAndCodes.contains("children[0].postal_code:REQUIRED"));
        assertTrue(fieldErrors(error).stream().filter(field -> "rank".equals(field.path("field").asText()))
                .anyMatch(field -> field.path("params").path("max").asInt() == 5));
        assertFalse(response.getContent().contains("secret-marker"));
        assertFalse(response.getContent().contains("BAD"));
        assertFalse(error.has("message") && error.get("message").asText().contains("secret-marker"));
    }

    @Test
    void objectLevelViolationHasNoInventedField() throws Exception {
        // Arrange
        final String body = "{\"display_name\":\"valid\",\"left\":\"one\",\"right\":\"two\"}";

        // Exercise
        final HttpTester.Response response = post("global", body, ApiMediaTypes.ERROR_V1, "application/json");

        // Assertions
        final JsonNode error = assertError(response, 400, "VALIDATION_ERROR");
        assertFalse(error.has("fieldErrors"));
    }

    @Test
    void malformedJsonIsInvalidRequestAndTypeMismatchUsesSafeJsonPath() throws Exception {
        // Arrange
        final String malformed = "{\"display_name\":";

        // Exercise
        final HttpTester.Response syntaxResponse = post("input", malformed, ApiMediaTypes.ERROR_V1, "application/json");

        // Assertions
        assertError(syntaxResponse, 400, "INVALID_REQUEST");

        // Arrange
        final String typeMismatch = "{\"display_name\":\"valid\",\"rank\":\"not-a-number\"}";

        // Exercise
        final HttpTester.Response typeResponse = post("input", typeMismatch, ApiMediaTypes.ERROR_V1, "application/json");

        // Assertions
        final JsonNode error = assertError(typeResponse, 400, "INVALID_REQUEST");
        assertTrue(fieldErrors(error).stream().anyMatch(field -> "rank".equals(field.path("field").asText())
                && "INVALID_TYPE".equals(field.path("code").asText())));
        assertFalse(typeResponse.getContent().contains("not-a-number"));
    }

    @Test
    void emptyRequiredBodyIsAClientError() throws Exception {
        // Arrange
        final String path = "input";

        // Exercise
        final HttpTester.Response response = post(path, "", ApiMediaTypes.ERROR_V1, "application/json");

        // Assertions
        assertEquals(400, response.getStatus());
        assertEquals(ApiMediaTypes.ERROR_V1, response.get("Content-Type"));
        assertEquals("no-store", response.get("Cache-Control"));
        assertEquals("Accept", response.get("Vary"));
    }

    @Test
    void invalidResponseAndGetterFailureAreInternalErrors() throws Exception {
        // Arrange
        final String invalidOutputPath = "output-invalid";

        // Exercise
        final HttpTester.Response invalidResponse = post(invalidOutputPath, "{}", ApiMediaTypes.ERROR_V1, "application/json");

        // Assertions
        assertError(invalidResponse, 500, "INTERNAL_ERROR");

        // Arrange
        final String brokenOutputPath = "output-broken";

        // Exercise
        final HttpTester.Response serializationFailure = post(brokenOutputPath, "{}", ApiMediaTypes.ERROR_V1, "application/json");

        // Assertions
        assertError(serializationFailure, 500, "INTERNAL_ERROR");
        assertFalse(serializationFailure.getContent().contains("private-output-detail"));
    }

    @Test
    void preservesHttpMethodAndMediaTypeErrors() throws Exception {
        // Arrange
        final String inputPath = "/webapp/api/validation/input";

        // Exercise
        final HttpTester.Response method = request("GET", inputPath,
                "Accept: " + ApiMediaTypes.ERROR_V1 + "\r\n", "");

        // Assertions
        assertError(method, 405, "METHOD_NOT_ALLOWED");
        assertTrue(method.get("Allow").contains("POST"));

        // Arrange
        final String notAcceptableBody = "{}";

        // Exercise
        final HttpTester.Response notAcceptable = post("input", notAcceptableBody, "text/plain", "application/json");

        // Assertions
        assertError(notAcceptable, 406, "NOT_ACCEPTABLE");

        // Arrange
        final String unsupportedBody = "{}";

        // Exercise
        final HttpTester.Response unsupported = post("input", unsupportedBody, ApiMediaTypes.ERROR_V1, "text/plain");

        // Assertions
        assertError(unsupported, 415, "UNSUPPORTED_MEDIA_TYPE");
    }

    private HttpTester.Response post(final String path, final String body, final String accept, final String contentType)
            throws Exception {
        final String headers = "Cookie: " + cookie + "\r\nX-CSRF-TOKEN: " + csrf + "\r\n"
                + "Accept: " + accept + "\r\nContent-Type: " + contentType + "\r\nContent-Length: "
                + body.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + "\r\n";
        return request("POST", "/webapp/api/validation/" + path, headers, body);
    }

    private HttpTester.Response request(final String method, final String path, final String headers, final String body)
            throws Exception {
        return HttpTester.parseResponse(connector.getResponse(method + " " + path
                + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n" + headers + "\r\n" + body));
    }

    private JsonNode assertError(final HttpTester.Response response, final int status, final String code) throws IOException {
        assertEquals(status, response.getStatus());
        assertEquals(ApiMediaTypes.ERROR_V1, response.get("Content-Type"));
        assertEquals("no-store", response.get("Cache-Control"));
        assertEquals("Accept", response.get("Vary"));
        final JsonNode body = json.readTree(response.getContent());
        assertEquals(code, body.path("code").asText());
        return body;
    }

    private java.util.List<JsonNode> fieldErrors(final JsonNode error) {
        final java.util.List<JsonNode> fields = new java.util.ArrayList<>();
        error.path("fieldErrors").forEach(fields::add);
        return fields;
    }

    @Configuration
    @EnableWebSecurity
    @Import(ar.edu.itba.paw.webapp.config.ApiSecurityConfig.class)
    @ComponentScan("ar.edu.itba.paw.webapp.api")
    static class TestConfig {

        @Bean
        CommunityService communityService() {
            final CommunityService service = mock(CommunityService.class);
            when(service.getCommunityBySlug(org.mockito.ArgumentMatchers.anyString())).thenReturn(java.util.Optional.empty());
            return service;
        }

        @Bean
        @Order(0)
        SecurityFilterChain validationFixtureSecurity(final HttpSecurity http) throws Exception {
            return http.securityMatcher(antMatcher("/api/validation/**"))
                    .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                    .build(); // CSRF remains enabled; POSTs below send the session's real token.
        }

        @Bean
        @Order(3)
        SecurityFilterChain legacySecurity(final HttpSecurity http) throws Exception {
            return http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll()).build();
        }
    }

    /** Isolated test-only Jersey app: no package scan can discover these fixtures in the WAR. */
    public static class TestApiApplication extends ApiApplication {
        public TestApiApplication() {
            register(ApiValidationResource.class);
        }
    }

    @Path("validation")
    @Produces(ApiMediaTypes.ERROR_V1)
    public static class ApiValidationResource {
        @POST
        @Path("input")
        @Consumes(MediaType.APPLICATION_JSON)
        public Response input(@NotNull @Valid final Input input) {
            return Response.ok(Map.of("accepted", true)).build();
        }

        @POST
        @Path("global")
        @Consumes(MediaType.APPLICATION_JSON)
        public Response global(@NotNull @Valid final Input input) {
            return Response.ok("ok").build();
        }

        @POST
        @Path("output-invalid")
        @Consumes(MediaType.APPLICATION_JSON)
        @Valid
        public InvalidOutput invalidOutput(final Object ignored) {
            return new InvalidOutput();
        }

        @POST
        @Path("output-broken")
        @Consumes(MediaType.APPLICATION_JSON)
        public BrokenOutput brokenOutput(final Object ignored) {
            return new BrokenOutput();
        }
    }

    @ValidPair
    public static class Input {
        @JsonProperty("display_name")
        @NotBlank
        @Size(min = 3, max = 8)
        public String displayName;
        @Min(2)
        @Max(5)
        public Integer rank;
        @Pattern(regexp = "[A-Z]{2}[0-9]{3}")
        public String license;
        @UnsupportedValue
        public String policy;
        @Valid
        public java.util.List<Child> children;
        public String left;
        public String right;
    }

    public static class Child {
        @JsonProperty("postal_code")
        @NotBlank
        public String postalCode;
    }

    public static class InvalidOutput {
        @NotNull
        public String value;
    }

    public static class BrokenOutput {
        public String getValue() {
            throw new IllegalStateException("private-output-detail");
        }
    }

    @Constraint(validatedBy = PairValidator.class)
    @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE)
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public @interface ValidPair {
        String message() default "invalid pair";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    public static class PairValidator implements ConstraintValidator<ValidPair, Input> {
        @Override
        public boolean isValid(final Input value, final ConstraintValidatorContext context) {
            return value == null || value.left == null || value.right == null || value.left.equals(value.right);
        }
    }

    @Constraint(validatedBy = UnsupportedValueValidator.class)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.METHOD})
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public @interface UnsupportedValue {
        String message() default "unsupported value";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    public static class UnsupportedValueValidator implements ConstraintValidator<UnsupportedValue, String> {
        @Override
        public boolean isValid(final String value, final ConstraintValidatorContext context) {
            return value == null || !"BAD".equals(value);
        }
    }

    /** Test-only endpoint that exposes the actual session CSRF token for LocalConnector requests. */
    static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
            final CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            request.getSession();
            response.setHeader("X-CSRF-TOKEN", token.getToken());
            response.setContentType("text/plain");
            response.getWriter().write("test-session");
        }
    }
}
