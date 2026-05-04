package ar.edu.itba.paw.webapp.exception;

public class ETagGenerationException extends RuntimeException {

    public ETagGenerationException(final String resource, final long resourceId, final Throwable cause) {
        super("Failed to generate ETag for " + resource + " " + resourceId + ".", cause);
    }
}
