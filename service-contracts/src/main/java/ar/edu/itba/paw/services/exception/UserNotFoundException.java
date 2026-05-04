package ar.edu.itba.paw.services.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(final long userId) {
        super("User not found: " + userId);
    }
}
