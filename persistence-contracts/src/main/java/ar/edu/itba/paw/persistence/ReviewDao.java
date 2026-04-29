package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewDao {
    List<Review> findAll();
    Optional<Review> findById(long id);
    List<Review> findByIds(Collection<Long> ids);
    List<Review> findByCarId(long carId);
    Page<Review> findByCarId(long carId, int page);
    Optional<Review> findLatestByCarId(long carId);
    Optional<Review> findTopRatedLatestByCarId(long carId);
    List<Review> findByCarIdOrderByRatingAsc(long carId);
    Page<Review> findByCarIdOrderByRatingAsc(long carId, int page);
    List<Review> findByCarIdOrderByRatingDesc(long carId);
    Page<Review> findByCarIdOrderByRatingDesc(long carId, int page);
    long countByCarId(long carId);
    List<Review> findByUserId(long userId);
    Optional<ReviewStats> findStatsByCarId(long carId);
    List<ReviewStats> findStatsByCarIds(Collection<Long> carIds);
    Review create(long userId, long carId, BigDecimal rating, String title, String body,
                  String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend);
    int bindReviewsToUserByEmail(long userId, String email);
    Optional<Review> update(long id, long carId, BigDecimal rating, String title, String body,
                            String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend);
    boolean delete(long id);
    int deleteByCarId(long carId);
}
