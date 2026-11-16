package ar.edu.itba.paw.webapp.api.users;

import ar.edu.itba.paw.model.AuthSession;
import ar.edu.itba.paw.model.SessionRevocationResult;
import ar.edu.itba.paw.services.AuthSessionService;
import ar.edu.itba.paw.webapp.api.common.ApiAuthentication;
import ar.edu.itba.paw.webapp.api.common.ApiError;
import ar.edu.itba.paw.webapp.api.common.ApiLink;
import ar.edu.itba.paw.webapp.api.common.ApiMediaTypes;
import ar.edu.itba.paw.webapp.auth.ApiIdentity;
import ar.edu.itba.paw.webapp.auth.ApiPrincipal;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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
@Path("users/{userId: [1-9][0-9]*}/sessions/{sessionId: [1-9][0-9]*}")
public class SessionResource {

    private final AuthSessionService sessions;

    @Autowired
    public SessionResource(final AuthSessionService sessions) { this.sessions = sessions; }

    @GET
    @Produces(ApiMediaTypes.SESSION_V1)
    public SessionResponse get(@PathParam("userId") final long userId, @PathParam("sessionId") final long sessionId,
                               @Context final SecurityContext security, @Context final UriInfo uri) {
        final AuthSession session = sessions.findActiveSession(ApiIdentity.require(security).getUserId(), userId, sessionId)
                .orElseThrow(NotFoundException::new);
        final String user = uri.getBaseUriBuilder().path(UserResource.class).path(Long.toString(userId)).build().getRawPath();
        return new SessionResponse(Long.toString(session.getId()), session.getExpiresAt(), Map.of(
                "self", new ApiLink(user + "/sessions/" + sessionId), "user", new ApiLink(user)));
    }

    @DELETE
    public Response delete(@PathParam("userId") final long userId, @PathParam("sessionId") final long sessionId,
                           @Context final SecurityContext security, @Context final HttpServletRequest request) {
        final String refresh = (String) request.getAttribute(ApiAuthentication.REVOCATION_REFRESH_ATTRIBUTE);
        final SessionRevocationResult result;
        if (refresh != null) {
            // This credential is still untrusted. The service verifies its exact session before revoking.
            result = sessions.revokeWithRefresh(userId, sessionId, refresh);
        } else {
            final ApiPrincipal identity = ApiIdentity.require(security);
            result = sessions.revoke(identity.getUserId(), identity.getSessionId(), userId, sessionId);
        }
        return switch (result) {
            case REVOKED -> Response.noContent().build();
            case NOT_FOUND, FORBIDDEN -> throw new NotFoundException();
            case INVALID, EXPIRED, REUSED -> Response.status(Response.Status.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Bearer realm=\"paw\", error=\"invalid_token\"")
                    .type(ApiMediaTypes.ERROR_V1).entity(new ApiError(switch (result) {
                        case EXPIRED -> ApiAuthentication.SESSION_EXPIRED;
                        case REUSED -> ApiAuthentication.REFRESH_TOKEN_REUSED;
                        default -> ApiAuthentication.INVALID_TOKEN;
                    }, "Authentication failed")).build();
        };
    }
}
