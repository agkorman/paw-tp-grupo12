package ar.edu.itba.paw.persistence.exception;

public class PersistenceOperationException extends RuntimeException {

    public PersistenceOperationException(final String operation, final Throwable cause) {
        super("Persistence operation failed: " + operation, cause);
    }
}
