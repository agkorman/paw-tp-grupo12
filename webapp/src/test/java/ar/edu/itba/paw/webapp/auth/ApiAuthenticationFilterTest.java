package ar.edu.itba.paw.webapp.auth;

import ar.edu.itba.paw.model.AccessTokenIdentity;
import ar.edu.itba.paw.model.AccessTokenResult;
import ar.edu.itba.paw.model.AuthenticationTokens;
import ar.edu.itba.paw.model.SessionCredentials;
import ar.edu.itba.paw.model.TokenAuthenticationResult;
import ar.edu.itba.paw.services.AccessTokenService;
import ar.edu.itba.paw.services.TokenAuthenticationService;
import ar.edu.itba.paw.webapp.api.common.ApiAuthentication;
import ar.edu.itba.paw.webapp.api.common.ApiSecurityErrorHandler;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiAuthenticationFilterTest {

    @AfterEach
    void clearIdentity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void invalidRememberMeCannotReachSessionCreationOrResource() throws Exception {
        // Arrange
        final TokenAuthenticationService authentication = mock(TokenAuthenticationService.class);
        when(authentication.authenticate(anyString(), anyString(), anyBoolean()))
                .thenThrow(new AssertionError("Authentication must not run for an invalid header"));
        final ApiAuthenticationFilter filter = new ApiAuthenticationFilter(authentication,
                mock(AccessTokenService.class), new ApiSecurityErrorHandler());
        final MockHttpServletRequest request = request("GET", "Basic dXNlcjpwYXNz");
        request.addHeader("X-Remember-Me", "yes");
        final MockHttpServletResponse response = new MockHttpServletResponse();
        // Exercise
        filter.doFilter(request, response, (req, res) -> fail("Resource must not run"));
        // Assertions
        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("INVALID_REQUEST"));
        assertNull(request.getSession(false));
        assertNull(response.getHeader(ApiAuthentication.REFRESH_TOKEN_HEADER));
    }

    @Test
    void expiredAccessStopsMutationAndRefreshRetryExecutesItOnce() throws Exception {
        // Arrange
        final TokenAuthenticationService authentication = mock(TokenAuthenticationService.class);
        final AccessTokenService access = mock(AccessTokenService.class);
        when(access.validate("expired")).thenReturn(AccessTokenResult.rejected(AccessTokenResult.Status.EXPIRED));
        when(authentication.renew("rt_current")).thenReturn(grant());
        final ApiAuthenticationFilter filter = new ApiAuthenticationFilter(authentication, access, new ApiSecurityErrorHandler());
        final AtomicInteger mutations = new AtomicInteger();
        final MockHttpServletResponse expired = new MockHttpServletResponse();
        filter.doFilter(request("POST", "Bearer expired"), expired, (req, res) -> mutations.incrementAndGet());
        assertEquals(0, mutations.get());
        assertEquals(401, expired.getStatus());
        final MockHttpServletResponse renewed = new MockHttpServletResponse();
        // Exercise
        filter.doFilter(request("POST", "Bearer rt_current"), renewed, (req, res) -> mutations.incrementAndGet());
        // Assertions
        assertEquals(1, mutations.get());
        assertEquals(200, renewed.getStatus());
        assertEquals("rt_replacement", renewed.getHeader(ApiAuthentication.REFRESH_TOKEN_HEADER));
    }

    @Test
    void unmappedResourceFailureKeepsNewCredentialsAndReturnsJson() throws Exception {
        // Arrange
        final TokenAuthenticationService authentication = mock(TokenAuthenticationService.class);
        when(authentication.renew("rt_current")).thenReturn(grant());
        final ApiAuthenticationFilter filter = new ApiAuthenticationFilter(authentication,
                mock(AccessTokenService.class), new ApiSecurityErrorHandler());
        final MockHttpServletResponse response = new MockHttpServletResponse();
        // Exercise
        filter.doFilter(request("GET", "Bearer rt_current"), response,
                (req, res) -> { throw new ServletException("private-resource-detail"); });
        // Assertions
        assertEquals(500, response.getStatus());
        assertTrue(response.getContentAsString().contains("INTERNAL_ERROR"));
        assertFalse(response.getContentAsString().contains("private-resource-detail"));
        assertEquals("rt_replacement", response.getHeader(ApiAuthentication.REFRESH_TOKEN_HEADER));
        assertEquals("new-access", response.getHeader(ApiAuthentication.ACCESS_TOKEN_HEADER));
        assertEquals("no-store", response.getHeader("Cache-Control"));
    }

    @Test
    void replacementSurvivesResponseResetAndCoexistsWithResourceLinks() throws Exception {
        // Arrange
        final TokenAuthenticationService authentication = mock(TokenAuthenticationService.class);
        when(authentication.renew("rt_current")).thenReturn(grant());
        final ApiAuthenticationFilter filter = new ApiAuthenticationFilter(authentication,
                mock(AccessTokenService.class), new ApiSecurityErrorHandler());
        final MockHttpServletResponse response = new MockHttpServletResponse();
        // Exercise
        filter.doFilter(request("GET", "Bearer rt_current"), response, (req, res) -> {
            final var http = (javax.servlet.http.HttpServletResponse) res;
            http.reset();
            http.setHeader("Cache-Control", "public, max-age=3600");
            http.setHeader("Link", "</webapp/api>; rel=\"self\"");
        });
        // Assertions
        assertEquals("rt_replacement", response.getHeader(ApiAuthentication.REFRESH_TOKEN_HEADER));
        assertEquals("no-store", response.getHeader("Cache-Control"));
        assertTrue(response.getHeader("Link").contains("/sessions/12>"));
        assertTrue(response.getHeader("Link").contains("rel=\"self\""));
    }

    @Test
    void accessEstablishesCredentialFreePrincipalBeforeResourceExecution() throws Exception {
        // Arrange
        final AccessTokenService access = mock(AccessTokenService.class);
        when(access.validate("valid")).thenReturn(AccessTokenResult.valid(
                new AccessTokenIdentity(7, 12, Set.of("ADMIN"), Instant.parse("2026-11-15T18:15:00Z"))));
        final ApiAuthenticationFilter filter = new ApiAuthenticationFilter(mock(TokenAuthenticationService.class),
                access, new ApiSecurityErrorHandler());
        final MockHttpServletResponse response = new MockHttpServletResponse();
        // Exercise
        filter.doFilter(request("GET", "Bearer valid"), response, (req, res) -> {
            final var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertNull(authentication.getCredentials());
            final ApiPrincipal principal = assertInstanceOf(ApiPrincipal.class, authentication.getPrincipal());
            assertEquals(7, principal.getUserId());
            assertEquals(12, principal.getSessionId());
            res.getWriter().write(principal.getName());
        });
        // Assertions
        assertEquals("7", response.getContentAsString());
        assertNull(response.getHeader(ApiAuthentication.ACCESS_TOKEN_HEADER));
    }

    private static MockHttpServletRequest request(final String method, final String authorization) {
        final MockHttpServletRequest request = new MockHttpServletRequest(method, "/webapp/api");
        request.setContextPath("/webapp");
        request.addHeader("Authorization", authorization);
        return request;
    }

    private static TokenAuthenticationResult grant() {
        final Instant now = Instant.parse("2026-11-15T18:00:00Z");
        return TokenAuthenticationResult.authenticated(new AuthenticationTokens(
                new SessionCredentials(7, 12, now, now.plusSeconds(86400), "rt_replacement"),
                "new-access", Set.of("USER")));
    }
}
