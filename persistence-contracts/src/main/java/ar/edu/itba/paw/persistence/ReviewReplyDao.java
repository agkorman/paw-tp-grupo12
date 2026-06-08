package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.ReviewReply;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReviewReplyDao {
    Optional<ReviewReply> findById(long id);
    List<ReviewReply> findByIds(Collection<Long> ids);
    Page<ReviewReply> findByReviewId(long reviewId, int page);
    Map<Long, List<ReviewReply>> findFirstNByReviewIds(Collection<Long> reviewIds, int n);
    ReviewReply create(long reviewId, long userId, String body);
    boolean update(long id, String body);
    Map<Long, Long> countRepliesByReviewIds(Collection<Long> reviewIds);
    Map<Long, Long> countNewRepliesPerReview(long userId, LocalDateTime since);
    Map<Long, Long> countNewRepliesPerReviewSince(LocalDateTime since);
    boolean delete(long id);
}
