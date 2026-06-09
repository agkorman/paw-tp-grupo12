package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ReviewLikeJpaDao implements ReviewLikeDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewLikeJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean likeReview(final long reviewId, final long userId) {
        final int rows = em.createNativeQuery(
                "INSERT INTO review_likes (review_id, user_id) " +
                "SELECT :reviewId, :userId FROM (SELECT 1) AS d " +
                "WHERE NOT EXISTS (SELECT 1 FROM review_likes WHERE review_id = :reviewId AND user_id = :userId)")
                .setParameter("reviewId", reviewId)
                .setParameter("userId", userId)
                .executeUpdate();
        if (rows == 0) {
            LOGGER.debug("user id={} already liked review id={}", userId, reviewId);
            return false;
        }
        LOGGER.info("user id={} liked review id={}", userId, reviewId);
        return true;
    }

    @Override
    public boolean unlikeReview(final long reviewId, final long userId) {
        final int rows = em.createNativeQuery(
                "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?")
                .setParameter(1, reviewId)
                .setParameter(2, userId)
                .executeUpdate();
        if (rows > 0) {
            LOGGER.info("user id={} unliked review id={}", userId, reviewId);
        }
        return rows > 0;
    }

    @Override
    public boolean isReviewLikedByUser(final long reviewId, final long userId) {
        final Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?")
                .setParameter(1, reviewId)
                .setParameter(2, userId)
                .getSingleResult();
        return count != null && count.longValue() > 0;
    }

    @Override
    public Map<Long, Long> countReviewLikesByReviewIds(final Collection<Long> reviewIds) {
        return countLikesByIds("review_likes", "review_id", reviewIds);
    }

    @Override
    public Map<Long, Long> countNewLikesPerReview(final long userId, final LocalDateTime since) {
        final List<?> rawRows = em.createNativeQuery(
                "SELECT r.review_id, COUNT(*) AS like_count " +
                "FROM reviews r JOIN review_likes rl ON rl.review_id = r.review_id " +
                "WHERE r.user_id = ? AND rl.created_at >= ? GROUP BY r.review_id")
                .setParameter(1, userId)
                .setParameter(2, since)
                .getResultList();
        final Map<Long, Long> counts = new HashMap<>();
        for (final Object element : rawRows) {
            final Object[] row = (Object[]) element;
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }

    @Override
    public Map<Long, Long> countNewLikesPerReviewSince(final LocalDateTime since) {
        final List<?> rawRows = em.createNativeQuery(
                "SELECT rl.review_id, COUNT(*) AS like_count " +
                "FROM review_likes rl WHERE rl.created_at >= ? GROUP BY rl.review_id")
                .setParameter(1, since)
                .getResultList();
        final Map<Long, Long> counts = new HashMap<>();
        for (final Object element : rawRows) {
            final Object[] row = (Object[]) element;
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }

    @Override
    public Set<Long> findLikedReviewIds(final Collection<Long> reviewIds, final long userId) {
        return findLikedIds("review_likes", "review_id", reviewIds, userId);
    }

    @Override
    public Page<Long> findLikedReviewIdsByUserId(final long userId, final int page) {
        final int normalizedPage = Pagination.normalizePage(page);
        final int pageSize = Pagination.REVIEWS_PAGE_SIZE;
        final long total = countLikedReviewsByUserId(userId);
        if (total == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }
        final int effectivePage = Pagination.clampPage(normalizedPage, total, pageSize);
        final long offset = Pagination.offsetFor(effectivePage, pageSize);

        final List<?> ids = em.createNativeQuery(
                "SELECT review_id FROM review_likes WHERE user_id = ? " +
                "ORDER BY created_at DESC, review_id DESC LIMIT ? OFFSET ?")
                .setParameter(1, userId)
                .setParameter(2, pageSize)
                .setParameter(3, offset)
                .getResultList();

        final List<Long> items = ids.stream().map(r -> ((Number) r).longValue()).collect(Collectors.toList());
        return new Page<>(items, effectivePage, pageSize, total);
    }

    @Override
    public long countLikedReviewsByUserId(final long userId) {
        final Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM review_likes WHERE user_id = ?")
                .setParameter(1, userId)
                .getSingleResult();
        return count == null ? 0L : count.longValue();
    }

    @Override
    public boolean likeReply(final long replyId, final long userId) {
        final int rows = em.createNativeQuery(
                "INSERT INTO review_reply_likes (reply_id, user_id) " +
                "SELECT :replyId, :userId FROM (SELECT 1) AS d " +
                "WHERE NOT EXISTS (SELECT 1 FROM review_reply_likes WHERE reply_id = :replyId AND user_id = :userId)")
                .setParameter("replyId", replyId)
                .setParameter("userId", userId)
                .executeUpdate();
        if (rows == 0) {
            LOGGER.debug("user id={} already liked reply id={}", userId, replyId);
            return false;
        }
        LOGGER.info("user id={} liked reply id={}", userId, replyId);
        return true;
    }

    @Override
    public boolean unlikeReply(final long replyId, final long userId) {
        final int rows = em.createNativeQuery(
                "DELETE FROM review_reply_likes WHERE reply_id = ? AND user_id = ?")
                .setParameter(1, replyId)
                .setParameter(2, userId)
                .executeUpdate();
        if (rows > 0) {
            LOGGER.info("user id={} unliked reply id={}", userId, replyId);
        }
        return rows > 0;
    }

    @Override
    public boolean isReplyLikedByUser(final long replyId, final long userId) {
        final Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM review_reply_likes WHERE reply_id = ? AND user_id = ?")
                .setParameter(1, replyId)
                .setParameter(2, userId)
                .getSingleResult();
        return count != null && count.longValue() > 0;
    }

    @Override
    public Map<Long, Long> countReplyLikesByReplyIds(final Collection<Long> replyIds) {
        return countLikesByIds("review_reply_likes", "reply_id", replyIds);
    }

    @Override
    public Set<Long> findLikedReplyIds(final Collection<Long> replyIds, final long userId) {
        return findLikedIds("review_reply_likes", "reply_id", replyIds, userId);
    }

    private Map<Long, Long> countLikesByIds(final String table, final String idCol, final Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        final javax.persistence.Query query = em.createNativeQuery(
                "SELECT " + idCol + " AS liked_id, COUNT(*) AS like_count FROM " + table +
                " WHERE " + idCol + " IN (:ids) GROUP BY " + idCol);
        query.setParameter("ids", ids);
        final List<?> rawRows = query.getResultList();
        final Map<Long, Long> counts = new HashMap<>();
        for (final Object element : rawRows) {
            final Object[] row = (Object[]) element;
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }

    private Set<Long> findLikedIds(final String table, final String idCol,
                                   final Collection<Long> ids, final long userId) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        final javax.persistence.Query query = em.createNativeQuery(
                "SELECT " + idCol + " FROM " + table +
                " WHERE user_id = :userId AND " + idCol + " IN (:ids)");
        query.setParameter("userId", userId);
        query.setParameter("ids", ids);
        final List<?> result = query.getResultList();
        return result.stream().map(r -> ((Number) r).longValue()).collect(Collectors.toCollection(HashSet::new));
    }
}
