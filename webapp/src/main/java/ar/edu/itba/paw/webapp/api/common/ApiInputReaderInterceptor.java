package ar.edu.itba.paw.webapp.api.common;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;
import java.util.List;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import org.springframework.stereotype.Component;

@Component
@Provider
@Priority(Priorities.ENTITY_CODER)
public class ApiInputReaderInterceptor implements ReaderInterceptor {

    @Override
    public Object aroundReadFrom(final ReaderInterceptorContext context) throws IOException {
        try {
            return context.proceed();
        } catch (final StreamReadException exception) {
            throw new ApiInputException(new ApiError("INVALID_REQUEST", "Malformed JSON request"), exception);
        } catch (final MismatchedInputException exception) {
            final String field = ApiJsonFields.mappingField(exception);
            throw new ApiInputException(new ApiError("INVALID_REQUEST", "Invalid JSON request",
                    field == null ? List.of() : List.of(new ApiFieldError(field, "INVALID_TYPE"))), exception);
        }
        // Definition failures and arbitrary deserializer exceptions describe server defects, not input validation.
    }
}
