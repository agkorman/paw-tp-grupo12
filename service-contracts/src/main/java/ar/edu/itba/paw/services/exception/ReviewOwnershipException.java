package ar.edu.itba.paw.services.exception;

public class ReviewOwnershipException extends IllegalArgumentException {

    public ReviewOwnershipException(final long reviewId) {
        super("User does not have permission to modify review: " + reviewId);
    }
}
