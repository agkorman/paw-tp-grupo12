package ar.edu.itba.paw.webapp.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(final String resource, final long resourceId) {
        super(resource + " not found: " + resourceId);
    }

    public ResourceNotFoundException(final String message) {
        super(message);
    }
}
