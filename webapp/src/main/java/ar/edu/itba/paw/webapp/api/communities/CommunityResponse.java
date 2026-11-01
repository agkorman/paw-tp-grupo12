package ar.edu.itba.paw.webapp.api.communities;

import ar.edu.itba.paw.webapp.api.common.ApiLink;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Map;

public class CommunityResponse implements Serializable {

    private final String id;
    private final String slug;
    private final String name;
    private final String description;
    private final Map<String, ApiLink> links;

    public CommunityResponse(final String id, final String slug, final String name,
                             final String description, final Map<String, ApiLink> links) {
        this.id = id;
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.links = Map.copyOf(links);
    }

    public String getId() { return id; }

    public String getSlug() { return slug; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    @JsonProperty("_links")
    public Map<String, ApiLink> getLinks() { return links; }
}
