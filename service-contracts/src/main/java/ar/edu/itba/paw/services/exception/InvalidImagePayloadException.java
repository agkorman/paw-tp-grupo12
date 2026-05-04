package ar.edu.itba.paw.services.exception;

public class InvalidImagePayloadException extends IllegalArgumentException {

    public InvalidImagePayloadException(final String message) {
        super(message);
    }
}
