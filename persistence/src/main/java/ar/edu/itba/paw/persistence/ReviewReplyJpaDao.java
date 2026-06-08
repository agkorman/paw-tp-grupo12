package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ReviewReplyJpaDao implements ReviewReplyDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewReplyJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<ReviewReply> findById(final long id) {
        return em.createQuery(
                        "SELECT rr FROM ReviewReply rr JOIN FETCH rr.user WHERE rr.id = :id",
                        ReviewReply.class)
                .setParameter("id", id)
                .getResultList()
                .stream().findFirst();
    }

    @Override
    public List<ReviewReply> findByIds(final Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return em.createQuery(
                        "SELECT rr FROM ReviewReply rr JOIN FETCH rr.user WHERE rr.id IN :ids",
                        ReviewReply.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    @Override
    public Page<ReviewReply> findByReviewId(final long reviewId, final int page) {
        final int pageSize = Pagination.REPLIES_PAGE_SIZE;

        final Number total = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM review_replies WHERE review_id = ?")
                .setParameter(1, reviewId)
                .getSingleResult();
        final long totalItems = total == null ? 0L : total.longValue();
        if (totalItems == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }

        final int clampedPage = Pagination.clampPage(Pagination.normalizePage(page), totalItems, pageSize);
        final long offset = Pagination.offsetFor(clampedPage, pageSize);

        @SuppressWarnings("unchecked")
        final List<Number> rawIds = em.createNativeQuery(
                        "SELECT reply_id FROM review_replies WHERE review_id = ? "
                        + "ORDER BY created_at ASC, reply_id ASC LIMIT ? OFFSET ?")
                .setParameter(1, reviewId)
                .setParameter(2, pageSize)
                .setParameter(3, offset)
                .getResultList();
        if (rawIds.isEmpty()) {
            return Page.empty(clampedPage, pageSize);
        }

        final List<Long> ids = rawIds.stream().map(Number::longValue).collect(Collectors.toList());
        final List<ReviewReply> replies = em.createQuery(
                        "SELECT rr FROM ReviewReply rr JOIN FETCH rr.user "
                        + "WHERE rr.id IN :ids ORDER BY rr.createdAt ASC, rr.id ASC",
                        ReviewReply.class)
                .setParameter("ids", ids)
                .getResultList();
        return new Page<>(replies, clampedPage, pageSize, totalItems);
    }

    @Override
    public Map<Long, List<ReviewReply>> findFirstNByReviewIds(final Collection<Long> reviewIds, final int n) {
        if (reviewIds == null || reviewIds.isEmpty() || n <= 0) {
            return Collections.emptyMap();
        }
        final List<Long> distinctIds = reviewIds.stream().distinct().collect(Collectors.toList());
        final String placeholders = distinctIds.stream().map(id -> "?").collect(Collectors.joining(","));

        final Query idsQuery = em.createNativeQuery(
                "SELECT r1.reply_id FROM review_replies r1 "
                + "WHERE r1.review_id IN (" + placeholders + ") "
                + "AND ("
                + "  SELECT COUNT(*) FROM review_replies r2 "
                + "  WHERE r2.review_id = r1.review_id "
                + "    AND (r2.created_at < r1.created_at "
                + "      OR (r2.created_at = r1.created_at AND r2.reply_id < r1.reply_id))"
                + ") < ?");
        for (int i = 0; i < distinctIds.size(); i++) {
            idsQuery.setParameter(i + 1, distinctIds.get(i));
        }
        idsQuery.setParameter(distinctIds.size() + 1, n);

        @SuppressWarnings("unchecked")
        final List<Number> rawIds = idsQuery.getResultList();
        if (rawIds.isEmpty()) {
            final Map<Long, List<ReviewReply>> empty = new LinkedHashMap<>();
            for (final Long id : distinctIds) {
                empty.put(id, Collections.emptyList());
            }
            return empty;
        }
        final List<Long> ids = rawIds.stream().map(Number::longValue).collect(Collectors.toList());

        final List<ReviewReply> replies = em.createQuery(
                        "SELECT rr FROM ReviewReply rr JOIN FETCH rr.user "
                        + "WHERE rr.id IN :ids "
                        + "ORDER BY rr.review.id ASC, rr.createdAt ASC, rr.id ASC",
                        ReviewReply.class)
                .setParameter("ids", ids)
                .getResultList();

        final Map<Long, List<ReviewReply>> grouped = new LinkedHashMap<>();
        for (final Long reviewId : distinctIds) {
            grouped.put(reviewId, new ArrayList<>());
        }
        for (final ReviewReply reply : replies) {
            grouped.computeIfAbsent(reply.getReviewId(), k -> new ArrayList<>()).add(reply);
        }
        return grouped;
    }

    @Override
    public ReviewReply create(final long reviewId, final long userId, final String body) {
        final ReviewReply reply = new ReviewReply(
                em.getReference(Review.class, reviewId), em.getReference(User.class, userId), body);
        em.persist(reply);
        LOGGER.info("created reply id={} reviewId={} userId={}", reply.getId(), reviewId, userId);
        return reply;
    }

    @Override
    public boolean update(final long id, final String body) {
        final ReviewReply reply = em.find(ReviewReply.class, id);
        if (reply == null) {
            LOGGER.warn("reply update affected 0 rows id={}", id);
            return false;
        }
        reply.setBody(body);
        reply.setUpdatedAt(LocalDateTime.now());
        LOGGER.info("updated reply id={}", id);
        return true;
    }

    @Override
    public Map<Long, Long> countRepliesByReviewIds(final Collection<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Collections.emptyMap();
        }
        final List<?> rawRows = em.createQuery(
                        "SELECT rr.review.id, COUNT(rr.id) "
                        + "FROM ReviewReply rr "
                        + "WHERE rr.review.id IN :reviewIds "
                        + "GROUP BY rr.review.id")
                .setParameter("reviewIds", reviewIds)
                .getResultList();
        final Map<Long, Long> counts = new HashMap<>();
        for (final Object element : rawRows) {
            final Object[] row = (Object[]) element;
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }

    @Override
    public Map<Long, Long> countNewRepliesPerReview(final long userId, final LocalDateTime since) {
        final List<?> rawRows = em.createQuery(
                        "SELECT rr.review.id, COUNT(rr.id) "
                        + "FROM ReviewReply rr "
                        + "WHERE rr.review.user.id = :userId AND rr.createdAt >= :since "
                        + "GROUP BY rr.review.id")
                .setParameter("userId", userId)
                .setParameter("since", since)
                .getResultList();
        final Map<Long, Long> counts = new HashMap<>();
        for (final Object element : rawRows) {
            final Object[] row = (Object[]) element;
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }

    @Override
    public Map<Long, Long> countNewRepliesPerReviewSince(final LocalDateTime since) {
        final List<?> rawRows = em.createQuery(
                        "SELECT rr.review.id, COUNT(rr.id) "
                        + "FROM ReviewReply rr "
                        + "WHERE rr.createdAt >= :since "
                        + "GROUP BY rr.review.id")
                .setParameter("since", since)
                .getResultList();
        final Map<Long, Long> counts = new HashMap<>();
        for (final Object element : rawRows) {
            final Object[] row = (Object[]) element;
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }

    @Override
    public boolean delete(final long id) {
        final ReviewReply reply = em.find(ReviewReply.class, id);
        if (reply == null) {
            LOGGER.warn("reply delete affected 0 rows id={}", id);
            return false;
        }
        em.remove(reply);
        LOGGER.info("deleted reply id={}", id);
        return true;
    }
}
