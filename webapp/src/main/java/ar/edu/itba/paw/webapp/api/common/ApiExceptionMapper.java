package ar.edu.itba.paw.webapp.api.common;

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
}
