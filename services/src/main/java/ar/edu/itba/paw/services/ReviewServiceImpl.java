package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.ReviewTagDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewDao reviewDao;
    private final ReviewTagDao reviewTagDao;
    private final ReviewTagService reviewTagService;

    @Autowired
    public ReviewServiceImpl(final ReviewDao reviewDao, final ReviewTagDao reviewTagDao,
                             final ReviewTagService reviewTagService) {
        this.reviewDao = reviewDao;
        this.reviewTagDao = reviewTagDao;
        this.reviewTagService = reviewTagService;
    }

    @Override
    @Transactional
    public Review createReview(long userId, long carId, BigDecimal rating, String title, String body,
                               String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend,
                               Collection<Short> tagIds) {
        reviewTagService.validateSelection(tagIds);
        final Review review = reviewDao.create(userId, carId, rating, title, body, ownershipStatus, modelYear,
                mileageKm, wouldRecommend);
        if (tagIds != null && !tagIds.isEmpty()) {
            reviewTagDao.replaceAssignments(review.getId(), tagIds);
            LOGGER.info("created review id={} userId={} carId={} tagCount={}", review.getId(), userId, carId, tagIds.size());
            return reviewDao.findById(review.getId()).orElse(review);
        }
        LOGGER.info("created review id={} userId={} carId={}", review.getId(), userId, carId);
        return review;
    }

    @Override
    public List<Review> getAllReviews() {
        return reviewDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public long countAllReviews() {
        return reviewDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getLatestReviews(final int page, final int pageSize) {
        return reviewDao.findLatest(page, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByFollowedUsers(final long followerId, final int page, final int pageSize) {
        return reviewDao.findByFollowedUsers(followerId, page, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public long countReviewsByFollowedUsers(final long followerId) {
        return reviewDao.countByFollowedUsers(followerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByFavoriteCars(final long userId, final int page, final int pageSize) {
        return reviewDao.findByFavoriteCars(userId, page, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public long countReviewsByFavoriteCars(final long userId) {
        return reviewDao.countByFavoriteCars(userId);
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
    public List<Review> getReviewsByCarIds(final Collection<Long> carIds) {
        if (carIds == null || carIds.isEmpty()) {
            return Collections.emptyList();
        }
        return reviewDao.findByCarIds(carIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> getDefaultPagesForReviewIds(final Collection<Long> reviewIds) {
        return reviewDao.findDefaultPagesByReviewIds(reviewIds);
    }

    @Override
    @Transactional
    public Optional<Review> updateReview(final long id, final long carId,
                                         final BigDecimal rating, final String title, final String body,
                                         final String ownershipStatus, final Integer modelYear,
                                         final Integer mileageKm, final Boolean wouldRecommend,
                                         final Collection<Short> tagIds) {
        reviewTagService.validateSelection(tagIds);
        final Optional<Review> updated = reviewDao.update(id, carId, rating, title, body, ownershipStatus,
                modelYear, mileageKm, wouldRecommend);
        if (updated.isPresent()) {
            reviewTagDao.replaceAssignments(id, tagIds == null ? Collections.emptyList() : tagIds);
            LOGGER.info("updated review id={} carId={} rating={}", id, carId, rating);
            return reviewDao.findById(id);
        }
        return updated;
    }

    @Override
    @Transactional
    public boolean deleteReview(final long id) {
        final boolean deleted = reviewDao.delete(id);
        if (deleted) {
            LOGGER.info("deleted review id={}", id);
        }
        return deleted;
    }

    @Override
    public List<Review> getReviewsByCar(long carId) {
        return reviewDao.findByCarId(carId);
    }

    @Override
    public Page<Review> getReviewsByCar(final long carId, final int page) {
        return reviewDao.findByCarId(carId, page);
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
    public Page<Review> getReviewsByCarOrderByRatingAsc(final long carId, final int page) {
        return reviewDao.findByCarIdOrderByRatingAsc(carId, page);
    }

    @Override
    public List<Review> getReviewsByCarOrderByRatingDesc(final long carId) {
        return reviewDao.findByCarIdOrderByRatingDesc(carId);
    }

    @Override
    public Page<Review> getReviewsByCarOrderByRatingDesc(final long carId, final int page) {
        return reviewDao.findByCarIdOrderByRatingDesc(carId, page);
    }

    @Override
    public List<Review> getReviewsByUser(final long userId) {
        return reviewDao.findByUserId(userId);
    }

    @Override
    public Page<Review> getReviewsByUser(final long userId, final int page) {
        return reviewDao.findByUserId(userId, page);
    }

    @Override
    public long countReviewsByUser(final long userId) {
        return reviewDao.countByUserId(userId);
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
