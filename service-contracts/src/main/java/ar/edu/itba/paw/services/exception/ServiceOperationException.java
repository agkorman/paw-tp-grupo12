package ar.edu.itba.paw.services.exception;

public class ServiceOperationException extends IllegalStateException {

    public ServiceOperationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
