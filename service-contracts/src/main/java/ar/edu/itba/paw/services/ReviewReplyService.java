package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.ReviewReply;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReviewReplyService {
    Optional<ReviewReply> getReplyById(long id);
    List<ReviewReply> getRepliesByIds(Collection<Long> ids);
    List<ReviewReply> getRepliesByReview(long reviewId);
    Map<Long, List<ReviewReply>> getRepliesByReviewIds(Collection<Long> reviewIds);
    ReviewReply createReply(long reviewId, long userId, String body);
    boolean deleteReply(long id, long userId);
    boolean deleteReplyAsAdmin(long id);

    Map<Long, Long> countNewRepliesPerReview(long userId, LocalDateTime since);
}
