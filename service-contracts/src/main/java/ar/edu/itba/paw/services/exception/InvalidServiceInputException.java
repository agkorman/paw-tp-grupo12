package ar.edu.itba.paw.services.exception;

public class InvalidServiceInputException extends IllegalArgumentException {

    public InvalidServiceInputException(final String message) {
        super(message);
    }
}
