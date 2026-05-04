package ar.edu.itba.paw.services.exception;

public class ReviewNotFoundException extends IllegalArgumentException {

    public ReviewNotFoundException(final long reviewId) {
        super("Review not found: " + reviewId);
    }
}
