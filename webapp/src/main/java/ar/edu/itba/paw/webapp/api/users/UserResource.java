package ar.edu.itba.paw.webapp.api.users;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.UserRegistrationService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.api.common.ApiLink;
import ar.edu.itba.paw.webapp.api.common.ApiMediaTypes;
import ar.edu.itba.paw.webapp.auth.ApiIdentity;
import java.net.URI;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("users")
@Produces(ApiMediaTypes.USER_V1)
public class UserResource {

    private final UserService users;
    private final UserRegistrationService registration;

    @Autowired
    public UserResource(final UserService users, final UserRegistrationService registration) {
        this.users = users;
        this.registration = registration;
    }

    @POST
    @Consumes(ApiMediaTypes.USER_V1)
    public Response create(@NotNull @Valid final UserRegistrationRequest input, @Context final UriInfo uri) {
        final User user = registration.register(input.getDisplayName(), input.getEmail(), input.getPassword());
        final URI location = userUri(uri, user.getId());
        // Creating an account does not authenticate the request. React follows with Basic GET /api.
        return Response.created(location).entity(UserResponse.publicProfile(Long.toString(user.getId()),
                user.getUsername(), Map.of("self", new ApiLink(location.getRawPath())))).build();
    }

    @GET
    @Path("{userId: [1-9][0-9]*}")
    public Response get(@PathParam("userId") final long userId, @Context final SecurityContext security,
                        @Context final UriInfo uri) {
        final User user = users.getUserById(userId).orElseThrow(NotFoundException::new);
        return Response.ok(representation(user, security, uri)).header("Accept-Patch", ApiMediaTypes.USER_V1).build();
    }

    @PATCH
    @Path("{userId: [1-9][0-9]*}")
    @Consumes(ApiMediaTypes.USER_V1)
    public UserResponse patch(@PathParam("userId") final long userId, @NotNull @Valid final UserPatchRequest input,
                              @Context final SecurityContext security, @Context final UriInfo uri) {
        final User updated = users.updateProfile(ApiIdentity.require(security).getUserId(), userId,
                input.getDisplayName(), input.getPreferredLocale());
        return representation(updated, security, uri);
    }

    private UserResponse representation(final User user, final SecurityContext security, final UriInfo uri) {
        final Map<String, ApiLink> links = Map.of("self", new ApiLink(userUri(uri, user.getId()).getRawPath()));
        if (ApiIdentity.current(security).filter(identity -> identity.getUserId() == user.getId()).isPresent()) {
            return UserResponse.ownerProfile(Long.toString(user.getId()), user.getUsername(), user.getEmail(),
                    user.getPreferredLocale(), ApiIdentity.roles(security), links);
        }
        return UserResponse.publicProfile(Long.toString(user.getId()), user.getUsername(), links);
    }

    private static URI userUri(final UriInfo uri, final long userId) {
        return uri.getBaseUriBuilder().path(UserResource.class).path(Long.toString(userId)).build();
    }
}
