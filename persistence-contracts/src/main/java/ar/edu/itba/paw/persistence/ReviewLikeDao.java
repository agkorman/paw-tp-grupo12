package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Page;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ReviewLikeDao {
    boolean likeReview(long reviewId, long userId);
    boolean unlikeReview(long reviewId, long userId);
    boolean isReviewLikedByUser(long reviewId, long userId);
    Map<Long, Long> countReviewLikesByReviewIds(Collection<Long> reviewIds);
    Map<Long, Long> countNewLikesPerReview(long userId, LocalDateTime since);
    Map<Long, Long> countNewLikesPerReviewSince(LocalDateTime since);
    Set<Long> findLikedReviewIds(Collection<Long> reviewIds, long userId);
    List<Long> findLikedReviewIdsByUserId(long userId);
    Page<Long> findLikedReviewIdsByUserId(long userId, int page);
    long countLikedReviewsByUserId(long userId);

    boolean likeReply(long replyId, long userId);
    boolean unlikeReply(long replyId, long userId);
    boolean isReplyLikedByUser(long replyId, long userId);
    Map<Long, Long> countReplyLikesByReplyIds(Collection<Long> replyIds);
    Set<Long> findLikedReplyIds(Collection<Long> replyIds, long userId);
    List<Long> findLikedReplyIdsByUserId(long userId);
}
