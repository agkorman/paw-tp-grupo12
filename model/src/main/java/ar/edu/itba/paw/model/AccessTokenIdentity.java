package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

/** Verified access identity; deliberately contains no profile data or credentials. */
public final class AccessTokenIdentity implements Serializable {

    private final long userId;
    private final long sessionId;
    private final Set<String> roles;
    private final Instant expiresAt;

    public AccessTokenIdentity(final long userId, final long sessionId, final Set<String> roles,
                               final Instant expiresAt) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.roles = Set.copyOf(roles);
        this.expiresAt = expiresAt;
    }

    public long getUserId() { return userId; }
    public long getSessionId() { return sessionId; }
    public Set<String> getRoles() { return roles; }
    public Instant getExpiresAt() { return expiresAt; }
}
