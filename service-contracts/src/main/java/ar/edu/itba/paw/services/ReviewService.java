package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.StoredImagePayload;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReviewService {

    Review createReview(Long userId, long carId, BigDecimal rating, String title, String body,
                        String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend,
                        Collection<Short> tagIds, List<ImagePayload> images);
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

    List<ImageMetadata> getReviewImagesByReviewId(long reviewId);

    Map<Long, List<ImageMetadata>> getImagesByReviewIds(Collection<Long> reviewIds);

    Optional<StoredImagePayload> getReviewImageById(long reviewId, long imageId);

    Optional<ImageMetadata> getReviewImageMetadataById(long reviewId, long imageId);

    List<ImagePayload> collectRetainedReviewImagePayloads(long reviewId, List<Long> retainedImageIds);
    boolean deleteReview(long id);
    Page<Review> getReviewsByCar(long carId, int page);

    Map<Long, Integer> getDefaultPagesForReviewIds(Collection<Long> reviewIds);

    Optional<Review> getLatestReviewByCar(long carId);
    Optional<Review> getTopRatedLatestReviewByCar(long carId);
    Page<Review> getReviewsByCarOrderByRatingAsc(long carId, int page);
    Page<Review> getReviewsByCarOrderByRatingDesc(long carId, int page);
    Page<Review> getReviewsByUser(long userId, int page);

    long countReviewsByUser(long userId);
    Optional<ReviewStats> getReviewStatsByCar(long carId);
    List<ReviewStats> getReviewStatsByCarIds(Collection<Long> carIds);
    Review getReviewAndCheckAccess(long reviewId, long requestingUserId, boolean isAdmin);

    boolean hideReview(long reviewId, String reason);
}
