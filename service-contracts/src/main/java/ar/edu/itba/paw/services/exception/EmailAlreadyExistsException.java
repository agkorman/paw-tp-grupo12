package ar.edu.itba.paw.services.exception;

public class EmailAlreadyExistsException extends IllegalArgumentException {

    public EmailAlreadyExistsException(final String email) {
        super("Email is already registered: " + email);
    }
}
