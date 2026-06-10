package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Page;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ReviewLikeService {
    boolean toggleReviewLike(long reviewId, long userId);
    boolean toggleReplyLike(long replyId, long userId);

    Map<Long, Long> countReviewLikesByReviewIds(Collection<Long> reviewIds);
    Set<Long> getLikedReviewIds(Collection<Long> reviewIds, long userId);
    Page<Long> getLikedReviewIdsByUser(long userId, int page);

    long countLikedReviewsByUser(long userId);

    Map<Long, Long> countReplyLikesByReplyIds(Collection<Long> replyIds);
    Set<Long> getLikedReplyIds(Collection<Long> replyIds, long userId);

    Map<Long, Long> countNewLikesPerReview(long userId, LocalDateTime since);
    Map<Long, Long> countNewLikesPerReviewSince(LocalDateTime since);
}
