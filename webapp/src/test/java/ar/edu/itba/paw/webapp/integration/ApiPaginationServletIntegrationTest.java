package ar.edu.itba.paw.webapp.integration;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.services.CommunityService;
import ar.edu.itba.paw.webapp.api.ApiApplication;
import ar.edu.itba.paw.webapp.api.common.ApiPagination;
import ar.edu.itba.paw.webapp.config.ApiSecurityConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.DispatcherType;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
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
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/** Real Jersey providers and HTTP headers; fixtures use no database or TCP sockets. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiPaginationServletIntegrationTest {

    private static final String PATH = "/webapp/api/pagination-fixture";
    private static final String FILTERS = "q=caf%C3%A9%20%2B%2F%25%20%3C%3E%2C&kind=classic&kind=road&sort=id";
    private final ObjectMapper json = new ObjectMapper();
    private Server server;
    private LocalConnector connector;

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
        server.setHandler(context);
        server.start();
    }

    @AfterAll
    void stopServlet() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void followsNavigationWithRepeatedFiltersAndEncodedValues() throws Exception {
        // Arrange
        final String target = PATH + "?" + FILTERS + "&page=1&page=8&%70age=9";

        // Exercise
        final HttpTester.Response first = get(target);

        // Assertions
        assertPage(first, 5, "9007199254740993", "9007199254740994");
        final Map<String, URI> firstLinks = links(first);
        assertEquals(java.util.Set.of("first", "next", "last"), firstLinks.keySet());
        assertLink(firstLinks.get("first"), 1);
        assertLink(firstLinks.get("last"), 3);
        assertLink(firstLinks.get("next"), 2);
        final HttpTester.Response middle = get(firstLinks.get("next").toASCIIString());
        assertPage(middle, 5, "9007199254740995", "9007199254740996");
        final Map<String, URI> middleLinks = links(middle);
        assertEquals(java.util.Set.of("first", "prev", "next", "last"), middleLinks.keySet());
        assertLink(middleLinks.get("prev"), 1);
        assertPage(get(middleLinks.get("prev").toASCIIString()), 5,
                "9007199254740993", "9007199254740994");
        assertLink(middleLinks.get("next"), 3);
        final HttpTester.Response last = get(middleLinks.get("next").toASCIIString());
        assertPage(last, 5, "9007199254740997");
        final Map<String, URI> lastLinks = links(last);
        assertEquals(java.util.Set.of("first", "prev", "last"), lastLinks.keySet());
        assertLink(lastLinks.get("prev"), 2);
        assertPage(get(lastLinks.get("prev").toASCIIString()), 5,
                "9007199254740995", "9007199254740996");
    }

    @Test
    void emptyCollectionHasNoNavigationLinks() throws Exception {
        // Arrange
        final String target = PATH + "?q=absent&page=1";

        // Exercise
        final HttpTester.Response response = get(target);

        // Assertions
        assertPage(response, 0);
        assertTrue(links(response).isEmpty());
    }

    @Test
    void outOfRangePageStaysEmptyAndNavigatesBackToLastExistingPage() throws Exception {
        // Arrange
        final String target = PATH + "?" + FILTERS + "&page=99";

        // Exercise
        final HttpTester.Response response = get(target);

        // Assertions
        assertPage(response, 5);
        assertNull(response.get("Location"));
        final Map<String, URI> navigation = links(response);
        assertEquals(java.util.Set.of("first", "prev", "last"), navigation.keySet());
        assertLink(navigation.get("first"), 1);
        assertLink(navigation.get("last"), 3);
        assertLink(navigation.get("prev"), 3);
        assertPage(get(navigation.get("prev").toASCIIString()), 5, "9007199254740997");
    }

    private HttpTester.Response get(final String target) throws Exception {
        return HttpTester.parseResponse(connector.getResponse("GET " + target
                + " HTTP/1.1\r\nHost: attacker.example\r\nAccept: application/json\r\nConnection: close\r\n\r\n"));
    }

    private void assertPage(final HttpTester.Response response, final long total, final String... ids) throws Exception {
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.get("Content-Type"));
        assertEquals(Long.toString(total), response.get("X-Total-Count"));
        assertEquals("no-store", response.get("Cache-Control"));
        assertEquals("Accept", response.get("Vary"));
        assertNull(response.get("Set-Cookie"));
        final JsonNode body = json.readTree(response.getContent());
        assertTrue(body.isArray());
        assertEquals(ids.length, body.size());
        for (int index = 0; index < ids.length; index++) {
            assertTrue(body.get(index).path("id").isTextual());
            assertEquals(ids[index], body.get(index).path("id").asText());
        }
    }

    private Map<String, URI> links(final HttpTester.Response response) {
        return response.getValuesList("Link").stream()
                .flatMap(value -> Arrays.stream(value.split(",(?=\\s*<)")))
                .map(Link::valueOf).collect(Collectors.toMap(Link::getRel, Link::getUri));
    }

    private void assertLink(final URI uri, final int page) {
        assertNotNull(uri);
        assertFalse(uri.isAbsolute());
        assertNull(uri.getRawAuthority());
        assertNull(uri.getRawFragment());
        assertEquals(PATH, uri.getRawPath());
        assertFalse(uri.toString().startsWith("//"));
        assertEquals(uri.toASCIIString(), uri.toString());
        final Map<String, List<String>> parameters = new LinkedHashMap<>();
        for (final String pair : uri.getRawQuery().split("&")) {
            final String[] entry = pair.split("=", 2);
            parameters.computeIfAbsent(decode(entry[0]), ignored -> new ArrayList<>()).add(decode(entry[1]));
        }
        assertEquals(Map.of("q", List.of("café +/% <>,"), "kind", List.of("classic", "road"),
                "sort", List.of("id"), "page", List.of(Integer.toString(page))), parameters);
    }

    private String decode(final String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    @Configuration
    @EnableWebSecurity
    @Import(ApiSecurityConfig.class)
    @ComponentScan("ar.edu.itba.paw.webapp.api")
    static class TestConfig {

        @Bean
        CommunityService communityService() {
            return mock(CommunityService.class);
        }

        @Bean
        @Order(0)
        SecurityFilterChain paginationFixtureSecurity(final HttpSecurity http) throws Exception {
            return http.securityMatcher(antMatcher(HttpMethod.GET, "/api/pagination-fixture"))
                    .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll()).build();
        }
    }

    /** Explicit registration only; fixtures are outside production package scans and the WAR. */
    public static class TestApiApplication extends ApiApplication {
        public TestApiApplication() {
            register(PaginationFixture.class);
        }
    }

    @Path("pagination-fixture")
    @Produces(MediaType.APPLICATION_JSON)
    public static class PaginationFixture {

        private static final List<Map<String, String>> ITEMS = java.util.stream.IntStream.rangeClosed(0, 5)
                .mapToObj(index -> Map.of("id", Long.toString(9007199254740993L + index),
                        "name", "café +/% <>, " + index,
                        "kind", index == 5 ? "other" : index % 2 == 0 ? "classic" : "road"))
                .collect(Collectors.toUnmodifiableList());

        @GET
        public Response list(@DefaultValue("1") @QueryParam("page") final int pageNumber,
                             @QueryParam("q") final String query,
                             @QueryParam("kind") final List<String> kinds,
                             @Context final UriInfo uriInfo) {
            final List<Map<String, String>> filtered = ITEMS.stream()
                    .filter(item -> query == null || item.get("name").contains(query))
                    .filter(item -> kinds.isEmpty() || kinds.contains(item.get("kind")))
                    .collect(Collectors.toList());
            final int from = (int) Math.min(((long) pageNumber - 1) * 2, filtered.size());
            final Page<Map<String, String>> page = new Page<>(
                    filtered.subList(from, Math.min(from + 2, filtered.size())), pageNumber, 2, filtered.size());
            return ApiPagination.headers(Response.ok(page.getItems()), page, uriInfo).build();
        }
    }
}
