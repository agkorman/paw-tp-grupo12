package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.Instant;

/** Internal grant, never a JSON representation. The raw refresh is returned once, never persisted. */
public final class SessionCredentials implements Serializable {

    private final long userId;
    private final long sessionId;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final String refreshToken;

    public SessionCredentials(final long userId, final long sessionId, final Instant issuedAt,
                              final Instant expiresAt, final String refreshToken) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.refreshToken = refreshToken;
    }

    public long getUserId() { return userId; }
    public long getSessionId() { return sessionId; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public String getRefreshToken() { return refreshToken; }
}
