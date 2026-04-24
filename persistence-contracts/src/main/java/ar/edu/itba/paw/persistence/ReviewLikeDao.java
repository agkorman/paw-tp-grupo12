package ar.edu.itba.paw.persistence;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ReviewLikeDao {
    boolean likeReview(long reviewId, long userId);
    boolean unlikeReview(long reviewId, long userId);
    boolean isReviewLikedByUser(long reviewId, long userId);
    long countReviewLikes(long reviewId);
    Map<Long, Long> countReviewLikesByReviewIds(Collection<Long> reviewIds);
    Map<Long, Long> countNewLikesPerReview(long userId, LocalDateTime since);
    Set<Long> findLikedReviewIds(Collection<Long> reviewIds, long userId);
    List<Long> findLikedReviewIdsByUserId(long userId);

    boolean likeReply(long replyId, long userId);
    boolean unlikeReply(long replyId, long userId);
    boolean isReplyLikedByUser(long replyId, long userId);
    long countReplyLikes(long replyId);
    Map<Long, Long> countReplyLikesByReplyIds(Collection<Long> replyIds);
    Set<Long> findLikedReplyIds(Collection<Long> replyIds, long userId);
    List<Long> findLikedReplyIdsByUserId(long userId);
}
