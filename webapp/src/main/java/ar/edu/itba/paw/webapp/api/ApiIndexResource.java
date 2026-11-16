package ar.edu.itba.paw.webapp.api;

import ar.edu.itba.paw.webapp.api.common.ApiLink;
import ar.edu.itba.paw.webapp.api.common.ApiMediaTypes;
import ar.edu.itba.paw.webapp.api.users.UserResource;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.springframework.stereotype.Component;

@Component
@Path("/")
public class ApiIndexResource {

    @GET
    @Produces(ApiMediaTypes.INDEX_V1)
    public ApiIndexResponse get(@Context final UriInfo uri) {
        return new ApiIndexResponse(Map.of(
                "self", new ApiLink(uri.getBaseUri().getRawPath()),
                "users", new ApiLink(uri.getBaseUriBuilder().path(UserResource.class).build().getRawPath())));
    }
}
