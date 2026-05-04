package ar.edu.itba.paw.webapp.exception;

public class UploadedImageReadException extends RuntimeException {

    public UploadedImageReadException(final String operation, final Throwable cause) {
        super("Failed to read uploaded image while " + operation + ".", cause);
    }
}
