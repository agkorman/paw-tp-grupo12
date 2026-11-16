package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.services.AccessTokenService;
import ar.edu.itba.paw.services.TokenAuthenticationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class WebAuthConfigTest {
    @Configuration
    @Import(WebAuthConfig.class)
    static class Config {
        @Bean AccessTokenService accessTokens() { return mock(AccessTokenService.class); }
        @Bean TokenAuthenticationService tokens() { return mock(TokenAuthenticationService.class); }
    }

    @Test
    void ignoresOldCookieIdentityAndDisablesLegacyFormPosts() throws Exception {
        try (AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.register(Config.class);
            context.refresh();
            final FilterChainProxy security = context.getBean(FilterChainProxy.class);
            final MockHttpSession oldSession = new MockHttpSession();
            oldSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    new SecurityContextImpl(new UsernamePasswordAuthenticationToken("old-admin", null,
                            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))));
            final MockHttpServletRequest privatePage = new MockHttpServletRequest("GET", "/admin");
            privatePage.setServletPath("/admin");
            privatePage.setSession(oldSession);
            final MockHttpServletResponse redirected = new MockHttpServletResponse();

            security.doFilter(privatePage, redirected, (request, response) -> fail("Old session authenticated the request"));

            assertEquals("/app/login", redirected.getRedirectedUrl());
            for (final String path : List.of("/login", "/register", "/logout")) {
                final MockHttpServletRequest post = new MockHttpServletRequest("POST", path);
                post.setServletPath(path);
                final MockHttpServletResponse response = new MockHttpServletResponse();
                security.doFilter(post, response, (request, output) -> fail("Old form request reached a controller"));
                assertEquals(401, response.getStatus());
                assertNull(post.getSession(false));
            }
            final MockHttpServletRequest spa = new MockHttpServletRequest("GET", "/app/login");
            spa.setServletPath("/app/login");
            final MockHttpServletResponse publicPage = new MockHttpServletResponse();
            security.doFilter(spa, publicPage, (request, response) -> response.getWriter().write("SPA"));
            assertEquals("SPA", publicPage.getContentAsString());
            assertNull(spa.getSession(false));
        }
    }
}
