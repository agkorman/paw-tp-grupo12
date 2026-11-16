package ar.edu.itba.paw.webapp.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

class SpaControllerTest {
    private MockMvc mvc() {
        final MockServletContext context = new MockServletContext() {
            @Override public InputStream getResourceAsStream(final String path) {
                return new ByteArrayInputStream(("<head><base href=\"__PAW_SPA_BASE_PATH__/\">"
                        + "<meta name=\"paw-api-base-path\" content=\"__PAW_API_BASE_PATH__\">"
                        + "<script src=\"./assets/index.js\"></script></head>").getBytes(StandardCharsets.UTF_8));
            }
        };
        return MockMvcBuilders.standaloneSetup(new SpaController(context)).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/app", "/app/", "/app/login", "/app/register", "/app/cars/42"})
    void servesDirectRoutesWithActualDeploymentContext(final String route) throws Exception {
        final MockMvc mvc = mvc();
        mvc.perform(get("/garage" + route).contextPath("/garage"))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().string(containsString("href=\"/garage/app/\"")))
                .andExpect(content().string(containsString("content=\"/garage/api\"")))
                .andExpect(content().string(not(containsString("__PAW_"))));
    }

    @Test
    void supportsRootContext() throws Exception {
        final MockMvc mvc = mvc();
        mvc.perform(get("/app/login")).andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/app/\"")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/app/assets/missing.js", "/api/users/1", "/cars"})
    void doesNotHijackAssetsApiOrMvcRoutes(final String path) throws Exception {
        final MockMvc mvc = mvc();
        mvc.perform(get(path)).andExpect(status().isNotFound());
    }

    @Test
    void legacyLoginRedirectsOnlyToSpaDestinations() throws Exception {
        final MockMvc mvc = mvc();
        mvc.perform(get("/garage/login").contextPath("/garage").param("redirect", "/garage/app/cars?brand=1"))
                .andExpect(redirectedUrl("/garage/app/login?returnTo=%2Fcars%3Fbrand%3D1"));
        mvc.perform(get("/login").param("redirect", "https://other.test"))
                .andExpect(redirectedUrl("/app/login?returnTo=%2F"));
        mvc.perform(get("/register")).andExpect(redirectedUrl("/app/register"));
    }

    @Test
    void oldFormPostsNoLongerCreateAccountsOrSessions() throws Exception {
        final MockMvc mvc = mvc();
        mvc.perform(post("/register")).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/login")).andExpect(status().isMethodNotAllowed());
    }
}
