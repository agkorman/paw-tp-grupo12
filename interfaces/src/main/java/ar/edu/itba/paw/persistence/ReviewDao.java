package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Review;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ReviewDao {
    Optional<Review> findById(long id);
    List<Review> findAll();
    List<Review> findByCarId(long carId);
    List<Review> findByUserId(long userId);
    Review create(long userId, long carId, BigDecimal rating, String title, String body,
                  String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend);
    boolean delete(long id);
}
