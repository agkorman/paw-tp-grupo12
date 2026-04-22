package ar.edu.itba.paw.services;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ReviewLikeService {
    boolean toggleReviewLike(long reviewId, long userId);
    boolean toggleReplyLike(long replyId, long userId);

    long countReviewLikes(long reviewId);
    Map<Long, Long> countReviewLikesByReviewIds(Collection<Long> reviewIds);
    Set<Long> getLikedReviewIds(Collection<Long> reviewIds, long userId);
    List<Long> getLikedReviewIdsByUser(long userId);

    long countReplyLikes(long replyId);
    Map<Long, Long> countReplyLikesByReplyIds(Collection<Long> replyIds);
    Set<Long> getLikedReplyIds(Collection<Long> replyIds, long userId);
    List<Long> getLikedReplyIdsByUser(long userId);

    Map<Long, Long> countNewLikesPerReview(long userId, LocalDateTime since);
}
