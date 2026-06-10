package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReviewDao {
    Page<Review> findLatest(int page);
    long countAll();
    Page<Review> findByFollowedUsers(long followerId, int page);
    long countByFollowedUsers(long followerId);
    Page<Review> findByFavoriteCars(long userId, int page);
    long countByFavoriteCars(long userId);
    Map<Long, Integer> findDefaultPagesByReviewIds(Collection<Long> reviewIds);
    Optional<Review> findById(long id);
    List<Review> findByIds(Collection<Long> ids);
    List<Review> findByCarIds(Collection<Long> carIds);
    Map<Long, Long> countByCarIdsSince(Collection<Long> carIds, LocalDateTime since);
    Page<Review> findByCarId(long carId, int page);
    Optional<Review> findLatestByCarId(long carId);
    Optional<Review> findTopRatedLatestByCarId(long carId);
    Page<Review> findByCarIdOrderByRatingAsc(long carId, int page);
    Page<Review> findByCarIdOrderByRatingDesc(long carId, int page);
    long countByCarId(long carId);
    Page<Review> findByUserId(long userId, int page);
    long countByUserId(long userId);
    Optional<ReviewStats> findStatsByCarId(long carId);
    List<ReviewStats> findStatsByCarIds(Collection<Long> carIds);
    Review create(Long userId, long carId, BigDecimal rating, String title, String body,
                  String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend);
    int bindReviewsToUserByEmail(long userId, String email);
    Optional<Review> update(long id, long carId, BigDecimal rating, String title, String body,
                            String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend);
    boolean delete(long id);
}
