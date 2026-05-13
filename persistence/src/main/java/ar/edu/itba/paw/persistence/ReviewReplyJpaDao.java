package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public List<ReviewReply> findByReviewId(final long reviewId) {
        return em.createQuery(
                        "SELECT rr FROM ReviewReply rr JOIN FETCH rr.user WHERE rr.reviewId = :reviewId "
                        + "ORDER BY rr.createdAt ASC, rr.id ASC",
                        ReviewReply.class)
                .setParameter("reviewId", reviewId)
                .getResultList();
    }

    @Override
    public List<ReviewReply> findByReviewIds(final Collection<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return List.of();
        }
        return em.createQuery(
                        "SELECT rr FROM ReviewReply rr JOIN FETCH rr.user WHERE rr.reviewId IN :reviewIds "
                        + "ORDER BY rr.reviewId ASC, rr.createdAt ASC, rr.id ASC",
                        ReviewReply.class)
                .setParameter("reviewIds", reviewIds)
                .getResultList();
    }

    @Override
    public ReviewReply create(final long reviewId, final long userId, final String body) {
        final ReviewReply reply = new ReviewReply();
        reply.setReview(em.getReference(Review.class, reviewId));
        reply.setUser(em.getReference(User.class, userId));
        reply.setBody(body);
        em.persist(reply);
        LOGGER.info("created reply id={} reviewId={} userId={}", reply.getId(), reviewId, userId);
        return reply;
    }

    @Override
    public Map<Long, Long> countNewRepliesPerReview(final long userId, final LocalDateTime since) {
        final List<?> rawRows = em.createNativeQuery(
                        "SELECT r.review_id, COUNT(*) AS reply_count "
                        + "FROM reviews r "
                        + "JOIN review_replies rr ON rr.review_id = r.review_id "
                        + "WHERE r.user_id = :userId AND rr.created_at >= :since "
                        + "GROUP BY r.review_id")
                .setParameter("userId", userId)
                .setParameter("since", Timestamp.valueOf(since))
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
