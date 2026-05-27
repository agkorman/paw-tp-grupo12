package ar.edu.itba.paw.services.exception;

public class InvalidCommunityTopicSelectionException extends RuntimeException {

    public enum Reason {
        REQUIRED,
        TOO_MANY,
        UNKNOWN_TOPIC
    }

    private final Reason reason;

    public InvalidCommunityTopicSelectionException(final Reason reason, final String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
