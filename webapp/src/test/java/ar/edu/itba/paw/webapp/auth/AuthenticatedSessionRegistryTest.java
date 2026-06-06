package ar.edu.itba.paw.webapp.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

class AuthenticatedSessionRegistryTest {

    private static final String SECURITY_CONTEXT_ATTRIBUTE =
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

    @Test
    void promoteUserToAdmin_updatesAllActiveSessionsForUser() {
        // Arrange
        final AuthenticatedSessionRegistry registry =
            new AuthenticatedSessionRegistry();
        final MockHttpSession firstSession = session("s1", userContext(7L));
        final MockHttpSession secondSession = session("s2", userContext(7L));
        registry.registerSession(
            firstSession,
            (SecurityContext) firstSession.getAttribute(SECURITY_CONTEXT_ATTRIBUTE)
        );
        registry.registerSession(
            secondSession,
            (SecurityContext) secondSession.getAttribute(SECURITY_CONTEXT_ATTRIBUTE)
        );

        // Exercise
        final int updatedCount = registry.promoteUserToAdmin(7L);

        // Assertions
        assertEquals(2, updatedCount);
        assertTrue(hasAuthority(firstSession, "ROLE_ADMIN"));
        assertTrue(hasAuthority(secondSession, "ROLE_ADMIN"));
    }

    @Test
    void promoteUserToAdmin_doesNotUpdateOtherUsersSessions() {
        // Arrange
        final AuthenticatedSessionRegistry registry =
            new AuthenticatedSessionRegistry();
        final MockHttpSession targetSession = session("s1", userContext(7L));
        final MockHttpSession otherSession = session("s2", userContext(8L));
        registry.registerSession(
            targetSession,
            (SecurityContext) targetSession.getAttribute(SECURITY_CONTEXT_ATTRIBUTE)
        );
        registry.registerSession(
            otherSession,
            (SecurityContext) otherSession.getAttribute(SECURITY_CONTEXT_ATTRIBUTE)
        );

        // Exercise
        final int updatedCount = registry.promoteUserToAdmin(7L);

        // Assertions
        assertEquals(1, updatedCount);
        assertTrue(hasAuthority(targetSession, "ROLE_ADMIN"));
        assertFalse(hasAuthority(otherSession, "ROLE_ADMIN"));
    }

    @Test
    void promoteUserToAdmin_ignoresAnonymousSecurityContext() {
        // Arrange
        final AuthenticatedSessionRegistry registry =
            new AuthenticatedSessionRegistry();
        final SecurityContext securityContext = new SecurityContextImpl(
            new UsernamePasswordAuthenticationToken(
                "anonymous",
                "credentials",
                Collections.emptyList()
            )
        );
        final MockHttpSession session = session("s1", securityContext);
        registry.registerSession(session, securityContext);

        // Exercise
        final int updatedCount = registry.promoteUserToAdmin(7L);

        // Assertions
        assertEquals(0, updatedCount);
        assertFalse(hasAuthority(session, "ROLE_ADMIN"));
    }

    @Test
    void unregisterSession_removesSessionReference() {
        // Arrange
        final AuthenticatedSessionRegistry registry =
            new AuthenticatedSessionRegistry();
        final MockHttpSession session = session("s1", userContext(7L));
        registry.registerSession(
            session,
            (SecurityContext) session.getAttribute(SECURITY_CONTEXT_ATTRIBUTE)
        );
        registry.unregisterSession("s1");

        // Exercise
        final int updatedCount = registry.promoteUserToAdmin(7L);

        // Assertions
        assertEquals(0, updatedCount);
        assertFalse(hasAuthority(session, "ROLE_ADMIN"));
    }

    private static MockHttpSession session(
        final String sessionId,
        final SecurityContext securityContext
    ) {
        final MockHttpSession session = new MockHttpSession(null, sessionId);
        session.setAttribute(SECURITY_CONTEXT_ATTRIBUTE, securityContext);
        return session;
    }

    private static SecurityContext userContext(final long userId) {
        final AuthenticatedUser user = new AuthenticatedUser(
            userId,
            "user" + userId,
            "user" + userId + "@test.com",
            "pw",
            RoleAuthorityUtils.authoritiesForRole("user")
        );
        final Authentication authentication =
            new UsernamePasswordAuthenticationToken(
                user,
                user.getPassword(),
                user.getAuthorities()
            );
        return new SecurityContextImpl(authentication);
    }

    private static boolean hasAuthority(
        final MockHttpSession session,
        final String authority
    ) {
        final SecurityContext securityContext =
            (SecurityContext) session.getAttribute(SECURITY_CONTEXT_ATTRIBUTE);
        return securityContext
            .getAuthentication()
            .getAuthorities()
            .stream()
            .anyMatch(grantedAuthority ->
                authority.equals(grantedAuthority.getAuthority())
            );
    }
}
