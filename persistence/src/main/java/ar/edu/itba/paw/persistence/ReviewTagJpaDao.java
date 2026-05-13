package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ReviewTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class ReviewTagJpaDao implements ReviewTagDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewTagJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ReviewTag> findAll() {
        return em.createQuery(
                        "SELECT t FROM ReviewTag t ORDER BY t.dimension, t.sentiment, t.labelEs",
                        ReviewTag.class)
                .getResultList();
    }

    @Override
    public Optional<ReviewTag> findById(final short tagId) {
        return Optional.ofNullable(em.find(ReviewTag.class, tagId));
    }

    @Override
    public List<ReviewTag> findByIds(final Collection<Short> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        return em.createQuery(
                        "SELECT t FROM ReviewTag t WHERE t.id IN :ids ORDER BY t.dimension, t.sentiment, t.labelEs",
                        ReviewTag.class)
                .setParameter("ids", tagIds)
                .getResultList();
    }

    @Override
    public void replaceAssignments(final long reviewId, final Collection<Short> tagIds) {
        em.createNativeQuery("DELETE FROM review_tag_assignments WHERE review_id = :reviewId")
                .setParameter("reviewId", reviewId)
                .executeUpdate();
        if (tagIds == null || tagIds.isEmpty()) {
            LOGGER.info("cleared tag assignments for review id={}", reviewId);
            return;
        }
        final Set<Short> uniqueIds = new LinkedHashSet<>(tagIds);
        int count = 0;
        for (final Short tagId : uniqueIds) {
            if (tagId != null) {
                em.createNativeQuery(
                                "INSERT INTO review_tag_assignments (review_id, tag_id) VALUES (:reviewId, :tagId)")
                        .setParameter("reviewId", reviewId)
                        .setParameter("tagId", tagId)
                        .executeUpdate();
                count++;
            }
        }
        LOGGER.info("replaced tag assignments for review id={} tagCount={}", reviewId, count);
    }

    @Override
    public Map<Long, List<ReviewTag>> findByReviewIds(final Collection<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Map.of();
        }
        final List<?> rawRows = em.createNativeQuery(
                        "SELECT rta.review_id, rt.tag_id, rt.code, rt.label_es, rt.sentiment, rt.dimension, rt.created_at "
                        + "FROM review_tag_assignments rta "
                        + "JOIN review_tags rt ON rt.tag_id = rta.tag_id "
                        + "WHERE rta.review_id IN (:ids) "
                        + "ORDER BY rta.review_id, rt.sentiment, rt.dimension, rt.label_es")
                .setParameter("ids", reviewIds)
                .getResultList();
        final Map<Long, List<ReviewTag>> result = new HashMap<>();
        for (final Object element : rawRows) {
            final Object[] row = (Object[]) element;
            final long reviewId = ((Number) row[0]).longValue();
            final ReviewTag tag = new ReviewTag(
                    ((Number) row[1]).shortValue(),
                    (String) row[2],
                    (String) row[3],
                    (String) row[4],
                    (String) row[5],
                    ((Timestamp) row[6]).toLocalDateTime()
            );
            result.computeIfAbsent(reviewId, k -> new ArrayList<>()).add(tag);
        }
        return result;
    }

    @Override
    public Map<Long, Map<Short, Integer>> getTagCountsForCars(final Collection<Long> carIds) {
        if (carIds == null || carIds.isEmpty()) {
            return Map.of();
        }
        final List<?> rawRows = em.createNativeQuery(
                        "SELECT r.car_id, rta.tag_id, COUNT(*) AS mentions "
                        + "FROM review_tag_assignments rta "
                        + "JOIN reviews r ON r.review_id = rta.review_id "
                        + "WHERE r.car_id IN (:carIds) "
                        + "GROUP BY r.car_id, rta.tag_id")
                .setParameter("carIds", carIds)
                .getResultList();
        final Map<Long, Map<Short, Integer>> result = new HashMap<>();
        for (final Object element : rawRows) {
            final Object[] row = (Object[]) element;
            final long carId = ((Number) row[0]).longValue();
            final short tagId = ((Number) row[1]).shortValue();
            final int mentions = ((Number) row[2]).intValue();
            result.computeIfAbsent(carId, k -> new HashMap<>()).put(tagId, mentions);
        }
        return result;
    }
}
