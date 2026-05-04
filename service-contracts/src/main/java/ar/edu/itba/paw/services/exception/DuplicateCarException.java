package ar.edu.itba.paw.services.exception;

public class DuplicateCarException extends IllegalStateException {

    public DuplicateCarException() {
        super("A car with the same brand, body type, model and year already exists.");
    }
}
