package ar.edu.itba.paw.webapp.integration;

import ar.edu.itba.paw.model.RefreshResult;
import ar.edu.itba.paw.model.SessionCredentials;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.AuthSessionService;
import ar.edu.itba.paw.services.JwtAccessTokenService;
import ar.edu.itba.paw.services.TokenAuthenticationServiceImpl;
import ar.edu.itba.paw.services.UserService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Real authentication/crypto with isolated service fixtures, never production component-scanned. */
@Configuration
@Import({JwtAccessTokenService.class, TokenAuthenticationServiceImpl.class})
class ApiTokenTestConfig {

    static final Instant NOW = Instant.parse("2026-11-15T18:00:00Z");
    static final SecretKey KEY = new SecretKeySpec(new byte[32], "HmacSHA256");

    @Bean Clock authClock() { return Clock.fixed(NOW, ZoneOffset.UTC); }
    @Bean SecretKey authSigningKey() { return KEY; }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(4); }

    @Bean
    UserService userService(final PasswordEncoder passwords) {
        final UserService users = mock(UserService.class);
        final User login = new User("nica", "nica@example.test", passwords.encode("contraseña:123"), "user", "es");
        login.setId(7);
        final User updated = new User("nica", "nica@example.test", login.getPassword(), "admin", "es");
        updated.setId(7);
        when(users.findByEmail("nica@example.test")).thenReturn(Optional.of(login));
        when(users.findByEmail("broken@example.test")).thenThrow(new IllegalStateException("private-error-detail"));
        when(users.getUserById(7)).thenReturn(Optional.of(updated));
        return users;
    }

    @Bean
    AuthSessionService authSessionService() {
        final AuthSessionService sessions = mock(AuthSessionService.class);
        when(sessions.createSession(7, false)).thenReturn(credentials("rt_initial", 12, 86400));
        when(sessions.createSession(7, true)).thenReturn(credentials("rt_remembered", 13, 2592000));
        when(sessions.rotate(anyString())).thenReturn(RefreshResult.rejected(RefreshResult.Status.INVALID));
        when(sessions.rotate("rt_current")).thenReturn(RefreshResult.issued(credentials("rt_next", 12, 86400)));
        when(sessions.rotate("rt_expired")).thenReturn(RefreshResult.rejected(RefreshResult.Status.EXPIRED));
        when(sessions.rotate("rt_revoked")).thenReturn(RefreshResult.rejected(RefreshResult.Status.REVOKED));
        when(sessions.rotate("rt_reused")).thenReturn(RefreshResult.rejected(RefreshResult.Status.REUSED));
        return sessions;
    }

    static SessionCredentials credentials(final String refresh, final long sessionId, final long lifetime) {
        return new SessionCredentials(7, sessionId, NOW, NOW.plusSeconds(lifetime), refresh);
    }
}
