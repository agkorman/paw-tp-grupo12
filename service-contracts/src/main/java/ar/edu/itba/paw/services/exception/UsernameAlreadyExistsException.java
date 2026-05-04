package ar.edu.itba.paw.services.exception;

public class UsernameAlreadyExistsException extends IllegalArgumentException {

    public UsernameAlreadyExistsException(final String username) {
        super("Username is already registered: " + username);
    }
}
