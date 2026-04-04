package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewService {
    Review createReview(Long userId, String reviewerEmail, long carId, BigDecimal rating, String title, String body,
                        String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend);
    List<Review> getReviewsByCar(long carId);
    Optional<Review> getLatestReviewByCar(long carId);
    List<Review> getReviewsByCarOrderByRatingAsc(long carId);
    List<Review> getReviewsByCarOrderByRatingDesc(long carId);
    List<Review> getAllReviews();
    Optional<ReviewStats> getReviewStatsByCar(long carId);
    List<ReviewStats> getReviewStatsByCarIds(Collection<Long> carIds);
}
