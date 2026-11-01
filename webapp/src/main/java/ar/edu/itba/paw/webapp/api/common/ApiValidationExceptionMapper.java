package ar.edu.itba.paw.webapp.api.common;

import javax.annotation.Priority;
import javax.validation.ValidationException;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Overrides Jersey's default ValidationException mapper, including its unsafe diagnostic entity. */
@Component
@Provider
@Priority(Priorities.USER - 1000)
public class ApiValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiValidationExceptionMapper.class);

    @Override
    public Response toResponse(final ValidationException exception) {
        LOGGER.error("API internal validation failed", exception);
        return Response.serverError().type(ApiMediaTypes.ERROR_V1)
                .entity(new ApiError("INTERNAL_ERROR", "Internal Server Error")).build();
    }
}
