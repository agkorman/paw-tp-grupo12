package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Review;

import java.math.BigDecimal;
import java.util.List;

public interface ReviewService {
    Review createReview(long userId, long carId, BigDecimal rating, String title, String body,
                        String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend);
    List<Review> getReviewsByCar(long carId);
    List<Review> getAllReviews();
}
