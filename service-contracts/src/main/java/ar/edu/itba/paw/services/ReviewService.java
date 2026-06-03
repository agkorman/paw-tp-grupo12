package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewImage;
import ar.edu.itba.paw.model.ReviewStats;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface ReviewService {

    String FEED_LATEST = "latest";
    String FEED_FOLLOWING = "following";
    String FEED_FAVORITES = "favorites";

    Review createReview(long userId, long carId, BigDecimal rating, String title, String body,
                        String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend,
                        Collection<Short> tagIds, List<ImagePayload> images);
    List<Review> getAllReviews();
    long countAllReviews();
    Page<Review> getLatestReviews(int page);
    Page<Review> getReviewsByFollowedUsers(long followerId, int page);
    long countReviewsByFollowedUsers(long followerId);
    Page<Review> getReviewsByFavoriteCars(long userId, int page);
    long countReviewsByFavoriteCars(long userId);
    Optional<Review> getReviewById(long id);
    List<Review> getReviewsByIds(Collection<Long> ids);
    List<Review> getReviewsByCarIds(Collection<Long> carIds);
    Map<Long, Long> countNewReviewsByCarIds(Collection<Long> carIds, LocalDateTime since);
    Optional<Review> updateReview(long id, long carId, BigDecimal rating, String title, String body,
                                  String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend,
                                  Collection<Short> tagIds, List<ImagePayload> finalImages);

    List<ReviewImage> getReviewImagesByReviewId(long reviewId);

    Map<Long, List<ReviewImage>> getImagesByReviewIds(Collection<Long> reviewIds);

    Optional<ReviewImage> getReviewImageById(long reviewId, long imageId);

    List<ImagePayload> collectRetainedReviewImagePayloads(long reviewId, List<Long> retainedImageIds);
    boolean deleteReview(long id);
    List<Review> getReviewsByCar(long carId);
    Page<Review> getReviewsByCar(long carId, int page);
    default Optional<Integer> getDefaultPageForReview(final long carId, final long reviewId) {
        return Optional.ofNullable(getDefaultPagesForReviews(getReviewsByCar(carId)).get(reviewId));
    }

    default Map<Long, Integer> getDefaultPagesForReviews(final Collection<Review> reviews) {
        final Map<Long, Integer> pagesByReviewId = new HashMap<>();
        if (reviews == null || reviews.isEmpty()) {
            return pagesByReviewId;
        }

        final Map<Long, Integer> positionsByCarId = new HashMap<>();
        reviews.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(
                                Review::getCreatedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                        .thenComparing(Review::getId, Comparator.reverseOrder()))
                .forEach(review -> {
                    final int position = positionsByCarId.getOrDefault(review.getCarId(), 0);
                    pagesByReviewId.put(review.getId(), (position / Pagination.REVIEWS_PAGE_SIZE)
                            + Pagination.DEFAULT_PAGE);
                    positionsByCarId.put(review.getCarId(), position + 1);
                });
        return pagesByReviewId;
    }

    Map<Long, Integer> getDefaultPagesForReviewIds(Collection<Long> reviewIds);

    Optional<Review> getLatestReviewByCar(long carId);
    Optional<Review> getTopRatedLatestReviewByCar(long carId);
    List<Review> getReviewsByCarOrderByRatingAsc(long carId);
    Page<Review> getReviewsByCarOrderByRatingAsc(long carId, int page);
    List<Review> getReviewsByCarOrderByRatingDesc(long carId);
    Page<Review> getReviewsByCarOrderByRatingDesc(long carId, int page);
    List<Review> getReviewsByUser(long userId);
    Page<Review> getReviewsByUser(long userId, int page);

    long countReviewsByUser(long userId);
    Optional<ReviewStats> getReviewStatsByCar(long carId);
    List<ReviewStats> getReviewStatsByCarIds(Collection<Long> carIds);

    Page<Review> getActivityFeedReviews(String feedMode, Long userId, int page);

    Review getReviewAndCheckAccess(long reviewId, long requestingUserId, boolean isAdmin);

    boolean hideReview(long reviewId, String reason);
}
