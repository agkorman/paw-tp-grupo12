package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.persistence.ReviewDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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
    public List<Review> getReviewsByCar(long carId) {
        return reviewDao.findByCarId(carId);
    }

    @Override
    public List<Review> getAllReviews() {
        return reviewDao.findAll();
    }
}
