package ar.edu.itba.paw.webapp.api.common;

import ar.edu.itba.paw.services.exception.DuplicateUserException;
import ar.edu.itba.paw.services.exception.EmailAlreadyExistsException;
import ar.edu.itba.paw.services.exception.UsernameAlreadyExistsException;
import ar.edu.itba.paw.services.exception.UserNotFoundException;
import ar.edu.itba.paw.services.exception.UserProfileOwnershipException;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Provider
public class ApiExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionMapper.class);

    @Override
    public Response toResponse(final Exception exception) {
        final Response userFailure = userFailure(exception);
        if (userFailure != null) {
            LOGGER.warn("API user request rejected status={}", userFailure.getStatus());
            return userFailure;
        }
        final Response original = exception instanceof WebApplicationException webException
                ? webException.getResponse() : Response.serverError().build();
        final int status = original.getStatus();
        final String code = switch (status) {
            case 400 -> "INVALID_REQUEST";
            case 401 -> "AUTHENTICATION_REQUIRED";
            case 403 -> "ACCESS_DENIED";
            case 404 -> "RESOURCE_NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 406 -> "NOT_ACCEPTABLE";
            case 415 -> "UNSUPPORTED_MEDIA_TYPE";
            default -> status >= 500 ? "INTERNAL_ERROR" : "REQUEST_FAILED";
        };
        if (status >= 500) {
            LOGGER.error("API request failed status={}", status, exception);
        } else {
            LOGGER.warn("API request rejected status={} code={}", status, code);
        }
        return Response.fromResponse(original)
                .type(ApiMediaTypes.ERROR_V1)
                .entity(exception instanceof ApiInputException inputException
                        ? inputException.getError()
                        : new ApiError(code, Response.Status.fromStatusCode(status) == null
                                ? "Request failed" : Response.Status.fromStatusCode(status).getReasonPhrase()))
                .build();
    }

    private Response userFailure(final Exception exception) {
        if (exception instanceof UsernameAlreadyExistsException) {
            return Response.status(409).type(ApiMediaTypes.ERROR_V1).entity(new ApiError("USER_CONFLICT",
                    "Display name is already registered", List.of(new ApiFieldError("displayName", "DUPLICATE")))).build();
        }
        if (exception instanceof EmailAlreadyExistsException) {
            return Response.status(409).type(ApiMediaTypes.ERROR_V1).entity(new ApiError("USER_CONFLICT",
                    "Email is already registered", List.of(new ApiFieldError("email", "DUPLICATE")))).build();
        }
        if (exception instanceof DuplicateUserException) {
            return Response.status(409).type(ApiMediaTypes.ERROR_V1)
                    .entity(new ApiError("USER_CONFLICT", "User data conflicts with an existing account")).build();
        }
        if (exception instanceof UserNotFoundException) {
            return Response.status(404).type(ApiMediaTypes.ERROR_V1)
                    .entity(new ApiError("RESOURCE_NOT_FOUND", "Not Found")).build();
        }
        if (exception instanceof UserProfileOwnershipException) {
            return Response.status(403).type(ApiMediaTypes.ERROR_V1)
                    .entity(new ApiError("ACCESS_DENIED", "Access is denied")).build();
        }
        return null;
    }
}
