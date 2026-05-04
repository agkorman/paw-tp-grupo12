package ar.edu.itba.paw.services.exception;

public class ReviewReplyNotFoundException extends IllegalArgumentException {

    public ReviewReplyNotFoundException(final long replyId) {
        super("Review reply not found: " + replyId);
    }
}
