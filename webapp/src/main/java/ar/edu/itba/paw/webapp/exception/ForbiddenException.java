package ar.edu.itba.paw.webapp.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(final String action, final String resource, final long resourceId) {
        super("Forbidden action '" + action + "' on " + resource + " " + resourceId + ".");
    }
}
