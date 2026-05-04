package ar.edu.itba.paw.services.exception;

public class ReviewReplyOwnershipException extends IllegalArgumentException {

    public ReviewReplyOwnershipException(final long replyId, final long userId) {
        super("Review reply " + replyId + " does not belong to user " + userId);
    }
}
