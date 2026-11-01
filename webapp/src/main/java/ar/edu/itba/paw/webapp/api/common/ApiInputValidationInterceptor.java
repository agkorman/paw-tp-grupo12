package ar.edu.itba.paw.webapp.api.common;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.server.spi.ValidationInterceptor;
import org.glassfish.jersey.server.spi.ValidationInterceptorContext;
import org.springframework.stereotype.Component;

/** Jersey invokes this SPI for resource/input validation only, never for return-value validation. */
@Component
@Provider
public class ApiInputValidationInterceptor implements ValidationInterceptor {

    @Override
    public void onValidate(final ValidationInterceptorContext context) {
        try {
            context.proceed();
        } catch (final ConstraintViolationException exception) {
            throw new ApiInputException(new ApiError("VALIDATION_ERROR", "Request validation failed",
                    ApiValidationErrors.fields(exception.getConstraintViolations(),
                            context.getInvocable().getHandlingMethod())), exception);
        }
    }
}
