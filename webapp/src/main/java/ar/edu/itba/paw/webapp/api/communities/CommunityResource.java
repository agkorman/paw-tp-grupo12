package ar.edu.itba.paw.webapp.api.communities;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.services.CommunityService;
import ar.edu.itba.paw.webapp.api.common.ApiLink;
import ar.edu.itba.paw.webapp.api.common.ApiMediaTypes;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("communities")
public class CommunityResource {

    private final CommunityService communityService;

    @Autowired
    public CommunityResource(final CommunityService communityService) {
        this.communityService = communityService;
    }

    @GET
    @Path("{slug}")
    @Produces(ApiMediaTypes.COMMUNITY_V1)
    public CommunityResponse getCommunity(@PathParam("slug") final String slug, @Context final UriInfo uriInfo) {
        final Community community = communityService.getCommunityBySlug(slug).orElseThrow(NotFoundException::new);
        final String self = uriInfo.getBaseUriBuilder().path(CommunityResource.class)
                .path("{slug}").build(community.getSlug()).getRawPath();
        return new CommunityResponse(Long.toString(community.getId()), community.getSlug(), community.getName(),
                community.getDescription(), Map.of("self", new ApiLink(self)));
    }
}
