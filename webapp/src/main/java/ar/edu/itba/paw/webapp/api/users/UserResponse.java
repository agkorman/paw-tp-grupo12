package ar.edu.itba.paw.webapp.api.users;

import ar.edu.itba.paw.webapp.api.common.ApiLink;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Representation of /api/users/{userId}. Callers must authorize access before selecting
 * the owner representation; the DTO itself does not establish ownership or permissions.
 * Links must target canonical resources and must not disclose private owner-only relations
 * in a public representation. Session data and credentials never belong in this resource.
 */
public class UserResponse implements Serializable {

    private final String id;
    private final String displayName;
    private final String email;
    private final String preferredLocale;
    private final List<String> roles;
    private final Map<String, ApiLink> links;

    private UserResponse(final String id, final String displayName, final String email,
                         final String preferredLocale, final List<String> roles,
                         final Map<String, ApiLink> links) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.preferredLocale = preferredLocale;
        this.roles = roles == null ? null : List.copyOf(roles);
        this.links = Map.copyOf(links);
    }

    public static UserResponse publicProfile(final String id, final String displayName,
                                              final Map<String, ApiLink> links) {
        return new UserResponse(id, displayName, null, null, null, links);
    }

    /** Roles are the effective roles of the authenticated request, without the ROLE_ prefix. */
    public static UserResponse ownerProfile(final String id, final String displayName, final String email,
                                             final String preferredLocale, final List<String> roles,
                                             final Map<String, ApiLink> links) {
        return new UserResponse(id, displayName, email, preferredLocale, roles, links);
    }

    public String getId() { return id; }

    public String getDisplayName() { return displayName; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getEmail() { return email; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getPreferredLocale() { return preferredLocale; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getRoles() { return roles; }

    @JsonProperty("_links")
    public Map<String, ApiLink> getLinks() { return links; }
}
