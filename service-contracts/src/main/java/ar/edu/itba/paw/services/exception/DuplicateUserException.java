package ar.edu.itba.paw.services.exception;

public class DuplicateUserException extends IllegalStateException {

    public DuplicateUserException(final Throwable cause) {
        super("A user with the same username or email already exists.", cause);
    }
}
