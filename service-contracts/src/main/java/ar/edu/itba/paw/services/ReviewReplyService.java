package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.ReviewReply;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReviewReplyService {
    Optional<ReviewReply> getReplyById(long id);
    List<ReviewReply> getRepliesByIds(Collection<Long> ids);
    Page<ReviewReply> getRepliesByReview(long reviewId, int page);
    Map<Long, List<ReviewReply>> getFirstNRepliesByReviewIds(Collection<Long> reviewIds, int n);
    ReviewReply createReply(long reviewId, long userId, String body);
    boolean updateReply(long id, long userId, String body);
    boolean deleteReply(long id, long userId);
    boolean hideReply(long replyId, String reason);

    Map<Long, Long> countNewRepliesPerReview(long userId, LocalDateTime since);
    Map<Long, Long> countRepliesByReviewIds(Collection<Long> reviewIds);
    Map<Long, Long> countNewRepliesPerReviewSince(LocalDateTime since);
}
