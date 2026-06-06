package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ReviewImageJpaDao implements ReviewImageDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewImageJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ReviewImage> findAllByReviewId(final long reviewId) {
        final List<?> rawRows = em.createQuery(
                        "SELECT i.imageId, i.review.id, i.displayOrder, i.contentType, i.updatedAt "
                        + "FROM ReviewImage i WHERE i.review.id = :reviewId ORDER BY i.displayOrder ASC, i.imageId ASC")
                .setParameter("reviewId", reviewId)
                .getResultList();
        final Review reviewRef = em.getReference(Review.class, reviewId);
        final List<ReviewImage> result = new ArrayList<>();
        for (final Object element : rawRows) {
            final Object[] row = (Object[]) element;
            final ReviewImage image = new ReviewImage();
            image.setImageId(((Number) row[0]).longValue());
            image.setReview(reviewRef);
            image.setDisplayOrder(((Number) row[2]).intValue());
            image.setContentType((String) row[3]);
            image.setImageData(null);
            image.setUpdatedAt((java.time.LocalDateTime) row[4]);
            result.add(image);
        }
        return result;
    }

    @Override
    public List<ReviewImage> findAllByReviewIds(final Collection<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Collections.emptyList();
        }
        final List<?> rawRows = em.createQuery(
                        "SELECT i.imageId, i.review.id, i.displayOrder, i.contentType, i.updatedAt "
                        + "FROM ReviewImage i WHERE i.review.id IN :reviewIds "
                        + "ORDER BY i.review.id ASC, i.displayOrder ASC, i.imageId ASC")
                .setParameter("reviewIds", reviewIds)
                .getResultList();
        final List<ReviewImage> result = new ArrayList<>();
        for (final Object element : rawRows) {
            final Object[] row = (Object[]) element;
            final ReviewImage image = new ReviewImage();
            image.setImageId(((Number) row[0]).longValue());
            image.setReview(em.getReference(Review.class, ((Number) row[1]).longValue()));
            image.setDisplayOrder(((Number) row[2]).intValue());
            image.setContentType((String) row[3]);
            image.setImageData(null);
            image.setUpdatedAt((java.time.LocalDateTime) row[4]);
            result.add(image);
        }
        return result;
    }

    @Override
    public List<ReviewImage> findByReviewIdAndImageIdsWithData(final long reviewId, final Collection<Long> imageIds) {
        if (imageIds == null) {
            return List.of();
        }
        final List<Long> ids = imageIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (ids.isEmpty()) {
            return List.of();
        }
        return em.createQuery(
                        "SELECT i FROM ReviewImage i WHERE i.review.id = :reviewId AND i.imageId IN :imageIds "
                        + "ORDER BY i.displayOrder ASC, i.imageId ASC",
                        ReviewImage.class)
                .setParameter("reviewId", reviewId)
                .setParameter("imageIds", ids)
                .getResultList();
    }

    @Override
    public Optional<ReviewImage> findByReviewIdAndImageId(final long reviewId, final long imageId) {
        return em.createQuery(
                        "SELECT i FROM ReviewImage i WHERE i.review.id = :reviewId AND i.imageId = :imageId",
                        ReviewImage.class)
                .setParameter("reviewId", reviewId)
                .setParameter("imageId", imageId)
                .getResultList()
                .stream().findFirst();
    }

    @Override
    public void replaceAll(final long reviewId, final List<ImagePayload> images) {
        em.createQuery("DELETE FROM ReviewImage i WHERE i.review.id = :reviewId")
                .setParameter("reviewId", reviewId)
                .executeUpdate();
        if (images == null || images.isEmpty()) {
            LOGGER.info("cleared image gallery for review id={}", reviewId);
            return;
        }
        final Review reviewRef = em.getReference(Review.class, reviewId);
        for (int i = 0; i < images.size(); i++) {
            final ImagePayload payload = images.get(i);
            final ReviewImage img = new ReviewImage();
            img.setReview(reviewRef);
            img.setDisplayOrder(i);
            img.setContentType(payload.getContentType());
            img.setImageData(payload.getImageData());
            em.persist(img);
        }
        LOGGER.info("replaced image gallery for review id={} imageCount={}", reviewId, images.size());
    }
}
