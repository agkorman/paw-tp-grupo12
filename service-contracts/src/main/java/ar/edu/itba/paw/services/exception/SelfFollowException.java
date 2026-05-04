package ar.edu.itba.paw.services.exception;

public class SelfFollowException extends IllegalArgumentException {

    public SelfFollowException(final long userId) {
        super("User cannot follow themselves: " + userId);
    }
}
