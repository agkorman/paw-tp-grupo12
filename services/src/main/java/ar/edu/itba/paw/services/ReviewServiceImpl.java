package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.ReviewTag;
import ar.edu.itba.paw.model.StoredImagePayload;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.ReviewImageDao;
import ar.edu.itba.paw.persistence.ReviewTagDao;
import ar.edu.itba.paw.services.exception.ReviewNotFoundException;
import ar.edu.itba.paw.services.exception.ReviewOwnershipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewDao reviewDao;
    private final ReviewTagDao reviewTagDao;
    private final ReviewImageDao reviewImageDao;
    private final ReviewTagService reviewTagService;
    private final EmailService emailService;

    @Autowired
    public ReviewServiceImpl(final ReviewDao reviewDao, final ReviewTagDao reviewTagDao,
                             final ReviewImageDao reviewImageDao,
                             final ReviewTagService reviewTagService,
                             final EmailService emailService) {
        this.reviewDao = reviewDao;
        this.reviewTagDao = reviewTagDao;
        this.reviewImageDao = reviewImageDao;
        this.reviewTagService = reviewTagService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public Review createReview(Long userId, long carId, BigDecimal rating, String title, String body,
                               String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend,
                               Collection<Short> tagIds, List<ImagePayload> images) {
        reviewTagService.validateSelection(tagIds);
        final Review review = reviewDao.create(userId, carId, rating, title, body, ownershipStatus, modelYear,
                mileageKm, wouldRecommend);
        if (images != null && !images.isEmpty()) {
            reviewImageDao.replaceAll(review.getId(), ImagePayloadUtils.normalizeImages(images));
        }
        if (tagIds != null && !tagIds.isEmpty()) {
            reviewTagDao.replaceAssignments(review.getId(), tagIds);
            LOGGER.info("created review id={} userId={} carId={} tagCount={} imageCount={}", review.getId(), userId, carId,
                    tagIds.size(), images == null ? 0 : images.size());
            return withTags(reviewDao.findById(review.getId()).orElse(review));
        }
        LOGGER.info("created review id={} userId={} carId={} imageCount={}", review.getId(), userId, carId,
                images == null ? 0 : images.size());
        return review;
    }

    @Override
    @Transactional(readOnly = true)
    public long countAllReviews() {
        return reviewDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getLatestReviews(final int page) {
        return withTags(reviewDao.findLatest(page));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByFollowedUsers(final long followerId, final int page) {
        return withTags(reviewDao.findByFollowedUsers(followerId, page));
    }

    @Override
    @Transactional(readOnly = true)
    public long countReviewsByFollowedUsers(final long followerId) {
        return reviewDao.countByFollowedUsers(followerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByFavoriteCars(final long userId, final int page) {
        return withTags(reviewDao.findByFavoriteCars(userId, page));
    }

    @Override
    @Transactional(readOnly = true)
    public long countReviewsByFavoriteCars(final long userId) {
        return reviewDao.countByFavoriteCars(userId);
    }

    @Override
    public Optional<Review> getReviewById(final long id) {
        return withTags(reviewDao.findById(id));
    }

    @Override
    public List<Review> getReviewsByIds(final Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return withTags(reviewDao.findByIds(ids));
    }

    @Override
    public List<Review> getReviewsByCarIds(final Collection<Long> carIds) {
        if (carIds == null || carIds.isEmpty()) {
            return Collections.emptyList();
        }
        return withTags(reviewDao.findByCarIds(carIds));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countNewReviewsByCarIds(final Collection<Long> carIds, final LocalDateTime since) {
        if (carIds == null || carIds.isEmpty() || since == null) {
            return Collections.emptyMap();
        }
        return reviewDao.countByCarIdsSince(carIds, since);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> getDefaultPagesForReviewIds(final Collection<Long> reviewIds) {
        return reviewDao.findDefaultPagesByReviewIds(reviewIds);
    }

    @Override
    @Transactional
    public Optional<Review> updateReview(final long id, final long carId,
                                         final BigDecimal rating, final String title, final String body,
                                         final String ownershipStatus, final Integer modelYear,
                                         final Integer mileageKm, final Boolean wouldRecommend,
                                         final Collection<Short> tagIds,
                                         final List<ImagePayload> finalImages) {
        reviewTagService.validateSelection(tagIds);
        final Optional<Review> updated = reviewDao.update(id, carId, rating, title, body, ownershipStatus,
                modelYear, mileageKm, wouldRecommend);
        if (updated.isPresent()) {
            reviewTagDao.replaceAssignments(id, tagIds == null ? Collections.emptyList() : tagIds);
            final List<ImagePayload> normalized = ImagePayloadUtils.normalizeImages(
                    finalImages == null ? Collections.emptyList() : finalImages);
            reviewImageDao.replaceAll(id, normalized);
            LOGGER.info("updated review id={} carId={} rating={} imageCount={}", id, carId, rating, normalized.size());
            return withTags(reviewDao.findById(id));
        }
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageMetadata> getReviewImagesByReviewId(final long reviewId) {
        return reviewImageDao.findAllByReviewId(reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<ImageMetadata>> getImagesByReviewIds(final Collection<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Collections.emptyMap();
        }
        final List<ImageMetadata> images = reviewImageDao.findAllByReviewIds(reviewIds);
        final Map<Long, List<ImageMetadata>> result = new java.util.LinkedHashMap<>();
        for (final ImageMetadata img : images) {
            result.computeIfAbsent(img.getOwnerId(), k -> new ArrayList<>()).add(img);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StoredImagePayload> getReviewImageById(final long reviewId, final long imageId) {
        return reviewImageDao.findByReviewIdAndImageId(reviewId, imageId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ImageMetadata> getReviewImageMetadataById(final long reviewId, final long imageId) {
        return reviewImageDao.findMetadataByReviewIdAndImageId(reviewId, imageId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImagePayload> collectRetainedReviewImagePayloads(final long reviewId,
                                                                 final List<Long> retainedImageIds) {
        final List<ImagePayload> payloads = new ArrayList<>();
        if (retainedImageIds == null) {
            return payloads;
        }
        final Map<Long, StoredImagePayload> imagesById = new java.util.LinkedHashMap<>();
        for (final StoredImagePayload image : reviewImageDao.findByReviewIdAndImageIdsWithData(reviewId, retainedImageIds)) {
            imagesById.putIfAbsent(image.getImageId(), image);
        }
        for (final Long imageId : retainedImageIds) {
            if (imageId == null) {
                continue;
            }
            final StoredImagePayload image = imagesById.get(imageId);
            if (image == null || image.getImageData() == null) {
                continue;
            }
            payloads.add(new ImagePayload(image.getContentType(), image.getImageData()));
        }
        return payloads;
    }

    @Override
    @Transactional
    public boolean deleteReview(final long id) {
        final boolean deleted = reviewDao.delete(id);
        if (deleted) {
            LOGGER.info("deleted review id={}", id);
        }
        return deleted;
    }

    @Override
    public Page<Review> getReviewsByCar(final long carId, final int page) {
        return withTags(reviewDao.findByCarId(carId, page));
    }

    @Override
    public Optional<Review> getLatestReviewByCar(final long carId) {
        return withTags(reviewDao.findLatestByCarId(carId));
    }

    @Override
    public Optional<Review> getTopRatedLatestReviewByCar(final long carId) {
        return withTags(reviewDao.findTopRatedLatestByCarId(carId));
    }

    @Override
    public Page<Review> getReviewsByCarOrderByRatingAsc(final long carId, final int page) {
        return withTags(reviewDao.findByCarIdOrderByRatingAsc(carId, page));
    }

    @Override
    public Page<Review> getReviewsByCarOrderByRatingDesc(final long carId, final int page) {
        return withTags(reviewDao.findByCarIdOrderByRatingDesc(carId, page));
    }

    @Override
    public Page<Review> getReviewsByUser(final long userId, final int page) {
        return withTags(reviewDao.findByUserId(userId, page));
    }

    @Override
    public long countReviewsByUser(final long userId) {
        return reviewDao.countByUserId(userId);
    }

    @Override
    public Optional<ReviewStats> getReviewStatsByCar(final long carId) {
        return reviewDao.findStatsByCarId(carId);
    }

    @Override
    public List<ReviewStats> getReviewStatsByCarIds(final Collection<Long> carIds) {
        if (carIds == null || carIds.isEmpty()) {
            return Collections.emptyList();
        }
        return reviewDao.findStatsByCarIds(carIds);
    }

    @Override
    public Review getReviewAndCheckAccess(final long reviewId, final long requestingUserId,
                                          final boolean isAdmin) {
        final Review review = reviewDao.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        final boolean isOwner = review.getUserId() != null && review.getUserId().equals(requestingUserId);
        if (!isOwner && !isAdmin) {
            throw new ReviewOwnershipException(reviewId);
        }
        return withTags(review);
    }

    @Override
    @Transactional
    public boolean hideReview(final long reviewId, final String reason) {
        final Review review = reviewDao.findById(reviewId).orElse(null);
        if (review == null) {
            return false;
        }
        final String carName = resolveCarDisplayName(review.getCar());
        final String recipientEmail = resolveRecipientEmail(review);
        final boolean deleted = reviewDao.delete(reviewId);
        if (!deleted) {
            return false;
        }
        LOGGER.info("deleted review id={} (hidden by moderator)", reviewId);
        if (recipientEmail != null) {
            emailService.sendReviewHiddenNotification(recipientEmail, review.getTitle(), carName, reason);
        }
        return true;
    }

    private String resolveCarDisplayName(final Car car) {
        if (car == null) {
            return null;
        }
        final String brand = car.getBrandName();
        final String model = car.getModel();
        if (brand == null && model == null) {
            return null;
        }
        if (brand == null) {
            return model;
        }
        if (model == null) {
            return brand;
        }
        return brand + " " + model;
    }

    private String resolveRecipientEmail(final Review review) {
        final String reviewerEmail = normalizeEmail(review.getReviewerEmail());
        if (reviewerEmail != null) {
            return reviewerEmail;
        }
        final User user = review.getUser();
        return user == null ? null : normalizeEmail(user.getEmail());
    }

    private static String normalizeEmail(final String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        final String trimmed = email.trim().toLowerCase(Locale.ROOT);
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Review withTags(final Review review) {
        if (review != null) {
            attachTags(List.of(review));
        }
        return review;
    }

    private Optional<Review> withTags(final Optional<Review> review) {
        review.ifPresent(r -> attachTags(List.of(r)));
        return review;
    }

    private List<Review> withTags(final List<Review> reviews) {
        attachTags(reviews);
        return reviews;
    }

    private Page<Review> withTags(final Page<Review> page) {
        attachTags(page.getItems());
        return page;
    }

    private void attachTags(final Collection<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return;
        }
        final List<Long> ids = reviews.stream().map(Review::getId).collect(Collectors.toList());
        final Map<Long, List<ReviewTag>> tagsByReview = reviewTagDao.findByReviewIds(ids);
        for (final Review review : reviews) {
            review.setTags(tagsByReview.getOrDefault(review.getId(), Collections.emptyList()));
        }
    }
}
