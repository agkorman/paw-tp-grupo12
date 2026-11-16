package ar.edu.itba.paw.webapp.api.users;

import ar.edu.itba.paw.webapp.api.common.ApiLink;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * Private representation of /api/users/{userId}/sessions/{sessionId}.
 * The user is referenced by a link, not an embedded user or a separate userId property.
 * expiresAt is the absolute session deadline, not the access-token expiry.
 * Never exposes refresh tokens, their hashes, or access tokens.
 */
public class SessionResponse implements Serializable {

    private final String id;
    private final String expiresAt;
    private final Map<String, ApiLink> links;

    public SessionResponse(final String id, final Instant expiresAt, final Map<String, ApiLink> links) {
        this.id = id;
        this.expiresAt = expiresAt.toString();
        this.links = Map.copyOf(links);
    }

    public String getId() { return id; }

    public String getExpiresAt() { return expiresAt; }

    @JsonProperty("_links")
    public Map<String, ApiLink> getLinks() { return links; }
}
