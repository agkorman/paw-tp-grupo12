package ar.edu.itba.paw.services;

import java.time.LocalDateTime;
import java.util.Map;

public interface ReviewLikeService {
    /**
     * Returns a map of reviewId → new like count for reviews written by the given user
     * in the period [since, now]. Returns an empty map until implemented by the likes PR.
     */
    Map<Long, Long> countNewLikesPerReview(long userId, LocalDateTime since);
}
