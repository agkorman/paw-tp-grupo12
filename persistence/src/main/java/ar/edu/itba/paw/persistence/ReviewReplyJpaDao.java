package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
                        "SELECT rr FROM ReviewReply rr JOIN FETCH rr.user WHERE rr.review.id = :reviewId "
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
                        "SELECT rr FROM ReviewReply rr JOIN FETCH rr.user WHERE rr.review.id IN :reviewIds "
                        + "ORDER BY rr.review.id ASC, rr.createdAt ASC, rr.id ASC",
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
