package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.Set;

/** Internal authentication grant. Exposed over HTTP only through credential headers. */
public final class AuthenticationTokens implements Serializable {

    private final SessionCredentials session;
    private final String accessToken;
    private final Set<String> roles;

    public AuthenticationTokens(final SessionCredentials session, final String accessToken, final Set<String> roles) {
        this.session = session;
        this.accessToken = accessToken;
        this.roles = Set.copyOf(roles);
    }

    public SessionCredentials getSession() { return session; }
    public String getAccessToken() { return accessToken; }
    public Set<String> getRoles() { return roles; }
}
