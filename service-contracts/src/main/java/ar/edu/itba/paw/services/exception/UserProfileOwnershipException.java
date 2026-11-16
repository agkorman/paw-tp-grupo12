package ar.edu.itba.paw.services.exception;

public class UserProfileOwnershipException extends RuntimeException {
    public UserProfileOwnershipException() { super("Only the owner can update the profile"); }
}
