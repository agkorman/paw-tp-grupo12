package ar.edu.itba.paw.webapp.api;

import ar.edu.itba.paw.webapp.api.common.ApiLink;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Map;

/** GET /api represents the API index, not the authenticated user or session. */
public class ApiIndexResponse implements Serializable {

    private final Map<String, ApiLink> links;

    public ApiIndexResponse(final Map<String, ApiLink> links) {
        this.links = Map.copyOf(links);
    }

    @JsonProperty("_links")
    public Map<String, ApiLink> getLinks() { return links; }
}
