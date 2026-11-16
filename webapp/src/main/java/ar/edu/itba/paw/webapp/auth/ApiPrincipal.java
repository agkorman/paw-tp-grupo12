package ar.edu.itba.paw.webapp.auth;

import java.io.Serializable;
import java.security.Principal;

/** API identity uses canonical IDs. Profile fields are obtained from the user resource. */
public final class ApiPrincipal implements Principal, Serializable {

    private final long userId;
    private final long sessionId;

    public ApiPrincipal(final long userId, final long sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public long getUserId() { return userId; }
    public long getSessionId() { return sessionId; }
    @Override public String getName() { return Long.toString(userId); }
}
