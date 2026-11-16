package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.AuthenticationTokens;
import ar.edu.itba.paw.model.RefreshResult;
import ar.edu.itba.paw.model.SessionCredentials;
import ar.edu.itba.paw.model.TokenAuthenticationResult;
import ar.edu.itba.paw.model.User;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationServiceImplTest {

    @Mock private UserService users;
    @Mock private AuthSessionService sessions;
    @Mock private AccessTokenService accessTokens;
    @Mock private PasswordEncoder passwords;
    @InjectMocks private TokenAuthenticationServiceImpl service;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void authenticatesAndPassesRememberChoiceToSession(final boolean remember) {
        // Arrange
        final User user = user("user");
        final SessionCredentials session = session(remember ? 2592000 : 86400);
        when(users.findByEmail("nica@example.test")).thenReturn(Optional.of(user));
        when(passwords.matches("password", "hash")).thenReturn(true);
        when(sessions.createSession(7, remember)).thenReturn(session);
        when(accessTokens.issue(session, Set.of("USER"))).thenReturn("signed-access");
        // Exercise
        final TokenAuthenticationResult result = service.authenticate("nica@example.test", "password", remember);
        // Assertions
        assertEquals(TokenAuthenticationResult.Status.AUTHENTICATED, result.getStatus());
        final AuthenticationTokens tokens = result.getTokens().orElseThrow();
        assertEquals("signed-access", tokens.getAccessToken());
        assertEquals("rt_replacement", tokens.getSession().getRefreshToken());
        assertEquals(session.getExpiresAt(), tokens.getSession().getExpiresAt());
        assertEquals(Set.of("USER"), tokens.getRoles());
    }

    @Test
    void rejectsWrongPasswordWithoutCredentials() {
        // Arrange
        when(users.findByEmail("nica@example.test")).thenReturn(Optional.of(user("user")));
        when(passwords.matches("wrong", "hash")).thenReturn(false);
        // Exercise
        final TokenAuthenticationResult result = service.authenticate("nica@example.test", "wrong", false);
        // Assertions
        assertEquals(TokenAuthenticationResult.Status.INVALID_CREDENTIALS, result.getStatus());
        assertTrue(result.getTokens().isEmpty());
    }

    @Test
    void missingUserCannotAuthenticateEvenIfDummyPasswordMatches() {
        // Arrange
        when(users.findByEmail("missing@example.test")).thenReturn(Optional.empty());
        // Mocked encoder returns null for the constructor's dummy hash.
        when(passwords.matches("password", null)).thenReturn(true);
        // Exercise
        final TokenAuthenticationResult result = service.authenticate("missing@example.test", "password", false);
        // Assertions
        assertEquals(TokenAuthenticationResult.Status.INVALID_CREDENTIALS, result.getStatus());
        assertTrue(result.getTokens().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"admin,ADMIN", "moderator,MODERATOR", "ROLE_USER,USER", "'  admin  ',ADMIN", ",USER"})
    void renewalUsesCurrentUserRole(final String role, final String expected) {
        // Arrange
        final SessionCredentials session = session(123);
        when(sessions.rotate("rt_current")).thenReturn(RefreshResult.issued(session));
        when(users.getUserById(7)).thenReturn(Optional.of(user(role)));
        when(accessTokens.issue(session, Set.of(expected))).thenReturn("new-access");
        // Exercise
        final TokenAuthenticationResult result = service.renew("rt_current");
        // Assertions
        assertEquals(TokenAuthenticationResult.Status.AUTHENTICATED, result.getStatus());
        final AuthenticationTokens tokens = result.getTokens().orElseThrow();
        assertEquals(Set.of(expected), tokens.getRoles());
        assertEquals("new-access", tokens.getAccessToken());
        assertEquals(session.getExpiresAt(), tokens.getSession().getExpiresAt());
    }

    @ParameterizedTest
    @CsvSource({"INVALID,INVALID_TOKEN", "EXPIRED,SESSION_EXPIRED", "REVOKED,SESSION_REVOKED", "REUSED,REFRESH_TOKEN_REUSED"})
    void keepsRefreshRejectionsDistinct(final RefreshResult.Status refresh, final TokenAuthenticationResult.Status expected) {
        // Arrange
        when(sessions.rotate("rt_current")).thenReturn(RefreshResult.rejected(refresh));
        // Exercise
        final TokenAuthenticationResult result = service.renew("rt_current");
        // Assertions
        assertEquals(expected, result.getStatus());
        assertTrue(result.getTokens().isEmpty());
    }

    @Test
    void missingUserAfterRotationDoesNotReceiveCredentials() {
        // Arrange
        when(sessions.rotate("rt_current")).thenReturn(RefreshResult.issued(session(123)));
        when(users.getUserById(7)).thenReturn(Optional.empty());
        // Exercise
        final TokenAuthenticationResult result = service.renew("rt_current");
        // Assertions
        assertEquals(TokenAuthenticationResult.Status.SESSION_REVOKED, result.getStatus());
        assertTrue(result.getTokens().isEmpty());
    }

    private static User user(final String role) {
        final User user = new User("nica", "nica@example.test", "hash", role, "es");
        user.setId(7);
        return user;
    }

    private static SessionCredentials session(final long lifetime) {
        final Instant now = Instant.parse("2026-11-15T18:00:00Z");
        return new SessionCredentials(7, 12, now, now.plusSeconds(lifetime), "rt_replacement");
    }
}
