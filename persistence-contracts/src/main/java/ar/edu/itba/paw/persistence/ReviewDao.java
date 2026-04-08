package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewDao {
    Optional<Review> findById(long id);
    List<Review> findAll();
    List<Review> findByCarId(long carId);
    Optional<Review> findLatestByCarId(long carId);
    Optional<Review> findTopRatedLatestByCarId(long carId);
    List<Review> findByCarIdOrderByRatingAsc(long carId);
    List<Review> findByCarIdOrderByRatingDesc(long carId);
    List<Review> findByUserId(long userId);
    Optional<ReviewStats> findStatsByCarId(long carId);
    List<ReviewStats> findStatsByCarIds(Collection<Long> carIds);
    Review create(Long userId, String reviewerEmail, long carId, BigDecimal rating, String title, String body,
                  String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend);
    boolean delete(long id);
}
