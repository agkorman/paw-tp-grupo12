package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ReviewReply;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReviewReplyDao {
    Optional<ReviewReply> findById(long id);
    List<ReviewReply> findByIds(Collection<Long> ids);
    List<ReviewReply> findByReviewId(long reviewId);
    List<ReviewReply> findByReviewIds(Collection<Long> reviewIds);
    ReviewReply insertAndFetch(long reviewId, long userId, String body);
    Map<Long, Long> countNewRepliesPerReview(long userId, LocalDateTime since);
    boolean delete(long id);
}
