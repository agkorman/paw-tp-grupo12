package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewService {
    Review createReview(long userId, long carId, BigDecimal rating, String title, String body,
                        String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend,
                        Collection<Short> tagIds);
    List<Review> getAllReviews();
    Optional<Review> getReviewById(long id);
    List<Review> getReviewsByIds(Collection<Long> ids);
    Optional<Review> updateReview(long id, long carId, BigDecimal rating, String title, String body,
                                  String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend,
                                  Collection<Short> tagIds);
    boolean deleteReview(long id);
    List<Review> getReviewsByCar(long carId);
    Page<Review> getReviewsByCar(long carId, int page);
    Optional<Review> getLatestReviewByCar(long carId);
    Optional<Review> getTopRatedLatestReviewByCar(long carId);
    List<Review> getReviewsByCarOrderByRatingAsc(long carId);
    Page<Review> getReviewsByCarOrderByRatingAsc(long carId, int page);
    List<Review> getReviewsByCarOrderByRatingDesc(long carId);
    Page<Review> getReviewsByCarOrderByRatingDesc(long carId, int page);
    List<Review> getReviewsByUser(long userId);
    Optional<ReviewStats> getReviewStatsByCar(long carId);
    List<ReviewStats> getReviewStatsByCarIds(Collection<Long> carIds);
}
