package ar.edu.itba.paw.services.exception;

public class InvalidReviewTagSelectionException extends RuntimeException {

    public enum Reason {
        TOO_MANY,
        UNKNOWN_TAG,
        DUPLICATE_DIMENSION
    }

    private final Reason reason;

    public InvalidReviewTagSelectionException(final Reason reason, final String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
