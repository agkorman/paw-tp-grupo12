package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;

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
    default Page<Long> getLikedReviewIdsByUser(final long userId, final int page) {
        final List<Long> reviewIds = getLikedReviewIdsByUser(userId);
        final int pageSize = Pagination.REVIEWS_PAGE_SIZE;
        if (reviewIds.isEmpty()) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }
        final int effectivePage = Pagination.clampPage(Pagination.normalizePage(page), reviewIds.size(), pageSize);
        final int fromIndex = (int) Pagination.offsetFor(effectivePage, pageSize);
        final int toIndex = Math.min(fromIndex + pageSize, reviewIds.size());
        return new Page<>(reviewIds.subList(fromIndex, toIndex), effectivePage, pageSize, reviewIds.size());
    }

    default long countLikedReviewsByUser(final long userId) {
        return getLikedReviewIdsByUser(userId).size();
    }

    long countReplyLikes(long replyId);
    Map<Long, Long> countReplyLikesByReplyIds(Collection<Long> replyIds);
    Set<Long> getLikedReplyIds(Collection<Long> replyIds, long userId);
    List<Long> getLikedReplyIdsByUser(long userId);

    Map<Long, Long> countNewLikesPerReview(long userId, LocalDateTime since);
}
