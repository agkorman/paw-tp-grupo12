package ar.edu.itba.paw.services.exception;

public class PendingAdminRequestExistsException extends IllegalStateException {

    public PendingAdminRequestExistsException(final long userId) {
        super("User already has a pending admin request: " + userId);
    }
}
