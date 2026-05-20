package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ReviewTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        final List<Short> validIds = new LinkedHashSet<>(tagIds).stream()
                .filter(t -> t != null).collect(Collectors.toList());
        if (validIds.isEmpty()) {
            LOGGER.info("cleared tag assignments for review id={}", reviewId);
            return;
        }
        final StringBuilder sql = new StringBuilder(
                "INSERT INTO review_tag_assignments (review_id, tag_id) VALUES ");
        for (int i = 0; i < validIds.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("(?, ?)");
        }
        final javax.persistence.Query q = em.createNativeQuery(sql.toString());
        for (int i = 0; i < validIds.size(); i++) {
            q.setParameter(2 * i + 1, reviewId);
            q.setParameter(2 * i + 2, validIds.get(i));
        }
        q.executeUpdate();
        LOGGER.info("replaced tag assignments for review id={} tagCount={}", reviewId, validIds.size());
    }

    @Override
    public Map<Long, List<ReviewTag>> findByReviewIds(final Collection<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Map.of();
        }
        final List<?> assignmentRows = em.createNativeQuery(
                        "SELECT rta.review_id, rta.tag_id FROM review_tag_assignments rta "
                        + "WHERE rta.review_id IN (:ids)")
                .setParameter("ids", reviewIds)
                .getResultList();
        if (assignmentRows.isEmpty()) {
            return Map.of();
        }
        final Set<Short> tagIds = assignmentRows.stream()
                .map(r -> ((Number) ((Object[]) r)[1]).shortValue())
                .collect(Collectors.toSet());
        final Map<Short, ReviewTag> tagsById = new HashMap<>();
        for (final ReviewTag t : em.createQuery(
                "SELECT t FROM ReviewTag t WHERE t.id IN :ids ORDER BY t.dimension, t.sentiment, t.labelEs",
                ReviewTag.class)
                .setParameter("ids", tagIds)
                .getResultList()) {
            tagsById.put(t.getId(), t);
        }
        final Map<Long, List<ReviewTag>> result = new HashMap<>();
        for (final Object element : assignmentRows) {
            final Object[] row = (Object[]) element;
            final long reviewId = ((Number) row[0]).longValue();
            final ReviewTag tag = tagsById.get(((Number) row[1]).shortValue());
            if (tag != null) {
                result.computeIfAbsent(reviewId, k -> new ArrayList<>()).add(tag);
            }
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
