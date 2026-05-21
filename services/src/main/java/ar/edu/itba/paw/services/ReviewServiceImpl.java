package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewImage;
import ar.edu.itba.paw.model.ReviewStats;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewDao reviewDao;
    private final ReviewTagDao reviewTagDao;
    private final ReviewImageDao reviewImageDao;
    private final ReviewTagService reviewTagService;
    private final CarService carService;
    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public ReviewServiceImpl(final ReviewDao reviewDao, final ReviewTagDao reviewTagDao,
                             final ReviewImageDao reviewImageDao,
                             final ReviewTagService reviewTagService,
                             final CarService carService, final UserService userService,
                             final EmailService emailService) {
        this.reviewDao = reviewDao;
        this.reviewTagDao = reviewTagDao;
        this.reviewImageDao = reviewImageDao;
        this.reviewTagService = reviewTagService;
        this.carService = carService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public Review createReview(long userId, long carId, BigDecimal rating, String title, String body,
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
            return reviewDao.findById(review.getId()).orElse(review);
        }
        LOGGER.info("created review id={} userId={} carId={} imageCount={}", review.getId(), userId, carId,
                images == null ? 0 : images.size());
        return review;
    }

    @Override
    public List<Review> getAllReviews() {
        return reviewDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public long countAllReviews() {
        return reviewDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getLatestReviews(final int page) {
        return reviewDao.findLatest(page);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByFollowedUsers(final long followerId, final int page) {
        return reviewDao.findByFollowedUsers(followerId, page);
    }

    @Override
    @Transactional(readOnly = true)
    public long countReviewsByFollowedUsers(final long followerId) {
        return reviewDao.countByFollowedUsers(followerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByFavoriteCars(final long userId, final int page) {
        return reviewDao.findByFavoriteCars(userId, page);
    }

    @Override
    @Transactional(readOnly = true)
    public long countReviewsByFavoriteCars(final long userId) {
        return reviewDao.countByFavoriteCars(userId);
    }

    @Override
    public Optional<Review> getReviewById(final long id) {
        return reviewDao.findById(id);
    }

    @Override
    public List<Review> getReviewsByIds(final Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return reviewDao.findByIds(ids);
    }

    @Override
    public List<Review> getReviewsByCarIds(final Collection<Long> carIds) {
        if (carIds == null || carIds.isEmpty()) {
            return Collections.emptyList();
        }
        return reviewDao.findByCarIds(carIds);
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
            return reviewDao.findById(id);
        }
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewImage> getReviewImagesByReviewId(final long reviewId) {
        return reviewImageDao.findAllByReviewId(reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<ReviewImage>> getImagesByReviewIds(final Collection<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Collections.emptyMap();
        }
        final List<ReviewImage> images = reviewImageDao.findAllByReviewIds(reviewIds);
        final Map<Long, List<ReviewImage>> result = new java.util.LinkedHashMap<>();
        for (final ReviewImage img : images) {
            result.computeIfAbsent(img.getReviewId(), k -> new ArrayList<>()).add(img);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewImage> getReviewImageById(final long reviewId, final long imageId) {
        return reviewImageDao.findByReviewIdAndImageId(reviewId, imageId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImagePayload> collectRetainedReviewImagePayloads(final long reviewId,
                                                                 final List<Long> retainedImageIds) {
        final List<ImagePayload> payloads = new ArrayList<>();
        if (retainedImageIds == null) {
            return payloads;
        }
        for (final Long imageId : retainedImageIds) {
            if (imageId == null) {
                continue;
            }
            final Optional<ReviewImage> image = reviewImageDao.findByReviewIdAndImageId(reviewId, imageId);
            if (image.isEmpty() || image.get().getImageData() == null) {
                continue;
            }
            payloads.add(new ImagePayload(image.get().getContentType(), image.get().getImageData()));
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
    public List<Review> getReviewsByCar(long carId) {
        return reviewDao.findByCarId(carId);
    }

    @Override
    public Page<Review> getReviewsByCar(final long carId, final int page) {
        return reviewDao.findByCarId(carId, page);
    }

    @Override
    public Optional<Review> getLatestReviewByCar(final long carId) {
        return reviewDao.findLatestByCarId(carId);
    }

    @Override
    public Optional<Review> getTopRatedLatestReviewByCar(final long carId) {
        return reviewDao.findTopRatedLatestByCarId(carId);
    }

    @Override
    public List<Review> getReviewsByCarOrderByRatingAsc(final long carId) {
        return reviewDao.findByCarIdOrderByRatingAsc(carId);
    }

    @Override
    public Page<Review> getReviewsByCarOrderByRatingAsc(final long carId, final int page) {
        return reviewDao.findByCarIdOrderByRatingAsc(carId, page);
    }

    @Override
    public List<Review> getReviewsByCarOrderByRatingDesc(final long carId) {
        return reviewDao.findByCarIdOrderByRatingDesc(carId);
    }

    @Override
    public Page<Review> getReviewsByCarOrderByRatingDesc(final long carId, final int page) {
        return reviewDao.findByCarIdOrderByRatingDesc(carId, page);
    }

    @Override
    public List<Review> getReviewsByUser(final long userId) {
        return reviewDao.findByUserId(userId);
    }

    @Override
    public Page<Review> getReviewsByUser(final long userId, final int page) {
        return reviewDao.findByUserId(userId, page);
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
    public Page<Review> getActivityFeedReviews(final String feedMode, final Long userId,
                                               final int page) {
        if (FEED_FOLLOWING.equals(feedMode) && userId != null) {
            return reviewDao.findByFollowedUsers(userId, page);
        }
        if (FEED_FAVORITES.equals(feedMode) && userId != null) {
            return reviewDao.findByFavoriteCars(userId, page);
        }
        return reviewDao.findLatest(page);
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
        return review;
    }

    @Override
    @Transactional
    public boolean hideReview(final long reviewId, final String reason) {
        final Review review = reviewDao.findById(reviewId).orElse(null);
        if (review == null) {
            return false;
        }
        final String carName = resolveCarDisplayName(review.getCarId());
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

    private String resolveCarDisplayName(final long carId) {
        return carService.getCarById(carId)
                .map(car -> {
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
                })
                .orElse(null);
    }

    private String resolveRecipientEmail(final Review review) {
        final String reviewerEmail = normalizeEmail(review.getReviewerEmail());
        if (reviewerEmail != null) {
            return reviewerEmail;
        }
        if (review.getUserId() == null) {
            return null;
        }
        return userService.getUserById(review.getUserId())
                .map(User::getEmail)
                .map(ReviewServiceImpl::normalizeEmail)
                .orElse(null);
    }

    private static String normalizeEmail(final String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        final String trimmed = email.trim().toLowerCase(Locale.ROOT);
        return trimmed.isEmpty() ? null : trimmed;
    }
}
