package ar.edu.itba.paw.webapp.api.common;

import javax.ws.rs.BadRequestException;

/** Only request-reading and Jersey's input-validation boundary create this marker. */
final class ApiInputException extends BadRequestException {

    private final ApiError error;

    ApiInputException(final ApiError error, final Throwable cause) {
        super("Invalid request", cause);
        this.error = error;
    }

    ApiError getError() {
        return error;
    }
}
