package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.AuthenticationTokens;
import ar.edu.itba.paw.model.RefreshResult;
import ar.edu.itba.paw.model.SessionCredentials;
import ar.edu.itba.paw.model.TokenAuthenticationResult;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.UserRole;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class TokenAuthenticationServiceImpl implements TokenAuthenticationService {

    private final UserService users;
    private final AuthSessionService sessions;
    private final AccessTokenService accessTokens;
    private final PasswordEncoder passwords;
    private final String missingUserPassword;

    @Autowired
    public TokenAuthenticationServiceImpl(final UserService users, final AuthSessionService sessions,
                                           final AccessTokenService accessTokens, final PasswordEncoder passwords) {
        this.users = users;
        this.sessions = sessions;
        this.accessTokens = accessTokens;
        this.passwords = passwords;
        // Perform the same password hashing work even if the email does not exist.
        this.missingUserPassword = passwords.encode(UUID.randomUUID().toString());
    }

    @Override
    public TokenAuthenticationResult authenticate(final String email, final String password, final boolean rememberMe) {
        final Optional<User> user = users.findByEmail(email);
        final boolean matches = passwords.matches(password, user.map(User::getPassword).orElse(missingUserPassword));
        if (user.isEmpty() || !matches) {
            return TokenAuthenticationResult.rejected(TokenAuthenticationResult.Status.INVALID_CREDENTIALS);
        }
        final Set<String> roles = roles(user.get());
        final SessionCredentials session = sessions.createSession(user.get().getId(), rememberMe);
        return issue(session, roles);
    }

    @Override
    public TokenAuthenticationResult renew(final String refreshToken) {
        final RefreshResult result = sessions.rotate(refreshToken);
        if (result.getStatus() != RefreshResult.Status.ISSUED) {
            return TokenAuthenticationResult.rejected(switch (result.getStatus()) {
                case INVALID -> TokenAuthenticationResult.Status.INVALID_TOKEN;
                case EXPIRED -> TokenAuthenticationResult.Status.SESSION_EXPIRED;
                case REVOKED -> TokenAuthenticationResult.Status.SESSION_REVOKED;
                case REUSED -> TokenAuthenticationResult.Status.REFRESH_TOKEN_REUSED;
                default -> throw new IllegalStateException("Unexpected refresh result");
            });
        }
        final SessionCredentials session = result.getCredentials().orElseThrow();
        // Reload current global roles; never recover them from an earlier access token.
        final Optional<User> user = users.getUserById(session.getUserId());
        if (user.isEmpty()) {
            // Deleting the user also deletes its sessions through the database FK.
            return TokenAuthenticationResult.rejected(TokenAuthenticationResult.Status.SESSION_REVOKED);
        }
        return issue(session, roles(user.get()));
    }

    private TokenAuthenticationResult issue(final SessionCredentials session, final Set<String> roles) {
        return TokenAuthenticationResult.authenticated(
                new AuthenticationTokens(session, accessTokens.issue(session, roles), roles));
    }

    private static Set<String> roles(final User user) {
        final String stored = user.getRole();
        final String role = stored == null || stored.isBlank() ? UserRole.USER : stored.trim();
        final String normalized = role.toUpperCase(Locale.ROOT);
        return Set.of(normalized.startsWith("ROLE_") ? normalized.substring(5) : normalized);
    }
}
