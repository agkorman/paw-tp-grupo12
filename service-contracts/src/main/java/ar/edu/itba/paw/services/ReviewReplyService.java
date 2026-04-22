package ar.edu.itba.paw.services;

import java.time.LocalDateTime;
import java.util.Map;

public interface ReviewReplyService {
    /**
     * Returns a map of reviewId → new reply count for reviews written by the given user
     * in the period [since, now]. Returns an empty map until implemented by the replies PR.
     */
    Map<Long, Long> countNewRepliesPerReview(long userId, LocalDateTime since);
}
