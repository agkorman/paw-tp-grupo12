package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public final class TokenAuthenticationResult implements Serializable {

    public enum Status { AUTHENTICATED, INVALID_CREDENTIALS, INVALID_TOKEN, SESSION_EXPIRED,
        SESSION_REVOKED, REFRESH_TOKEN_REUSED }

    private final Status status;
    private final AuthenticationTokens tokens;

    private TokenAuthenticationResult(final Status status, final AuthenticationTokens tokens) {
        this.status = status;
        this.tokens = tokens;
    }

    public static TokenAuthenticationResult authenticated(final AuthenticationTokens tokens) {
        return new TokenAuthenticationResult(Status.AUTHENTICATED, Objects.requireNonNull(tokens));
    }

    public static TokenAuthenticationResult rejected(final Status status) {
        if (Objects.requireNonNull(status) == Status.AUTHENTICATED) {
            throw new IllegalArgumentException("Authentication requires credentials");
        }
        return new TokenAuthenticationResult(status, null);
    }

    public Status getStatus() { return status; }
    public Optional<AuthenticationTokens> getTokens() { return Optional.ofNullable(tokens); }
}
