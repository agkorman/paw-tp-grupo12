package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.persistence.ReviewDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDao reviewDao;

    @Autowired
    public ReviewServiceImpl(final ReviewDao reviewDao) {
        this.reviewDao = reviewDao;
    }

    @Override
    public Review createReview(long userId, long carId, BigDecimal rating, String title, String body,
                               String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend) {
        return reviewDao.create(userId, carId, rating, title, body, ownershipStatus, modelYear, mileageKm, wouldRecommend);
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
    public Optional<Review> updateReview(final long id, final long carId,
                                         final BigDecimal rating, final String title, final String body,
                                         final String ownershipStatus, final Integer modelYear,
                                         final Integer mileageKm, final Boolean wouldRecommend) {
        return reviewDao.update(id, carId, rating, title, body, ownershipStatus, modelYear, mileageKm, wouldRecommend);
    }

    @Override
    public boolean deleteReview(final long id) {
        return reviewDao.delete(id);
    }

    @Override
    public List<Review> getReviewsByCar(long carId) {
        return reviewDao.findByCarId(carId);
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
    public List<Review> getReviewsByCarOrderByRatingDesc(final long carId) {
        return reviewDao.findByCarIdOrderByRatingDesc(carId);
    }

    @Override
    public List<Review> getReviewsByUser(final long userId) {
        return reviewDao.findByUserId(userId);
    }

    @Override
    public List<Review> getAllReviews() {
        return reviewDao.findAll();
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
}
