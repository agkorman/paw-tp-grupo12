package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReviewJdbcDaoTest extends AbstractPersistenceTest {

    @Test
    public void shouldCreateReviewAndAttachReviewerUsernameWhenFindingById() {
        // Arrange
        final User user = createUser("review-create");
        final Car car = createCar("review-create");

        // Exercise
        final Review result = reviewDao.create(user.getId(), car.getId(), new BigDecimal("4.5"),
                "Great car", "Detailed body", "owner", 2026, 1500, true);

        // Assertions
        final Review persisted = reviewDao.findById(result.getId()).orElseThrow();
        assertEquals(user.getId(), persisted.getUserId());
        assertEquals("user-review-create", persisted.getReviewerUsername());
        assertEquals(car.getId(), persisted.getCarId());
        assertEquals(new BigDecimal("4.5"), persisted.getRating());
    }

    @Test
    public void shouldReturnReviewsForCarOrderedByRatingDescending() {
        // Arrange
        final User user = createUser("review-order");
        final Car car = createCar("review-order");
        reviewDao.create(user.getId(), car.getId(), new BigDecimal("2.0"), "Low", "Body", "owner", 2024, 1000, false);
        final Review high = reviewDao.create(user.getId(), car.getId(), new BigDecimal("5.0"), "High", "Body", "owner", 2025, 500, true);

        // Exercise
        final List<Review> result = reviewDao.findByCarIdOrderByRatingDesc(car.getId());

        // Assertions
        assertEquals(2, result.size());
        assertEquals(high.getId(), result.get(0).getId());
        assertEquals(new BigDecimal("5.0"), result.get(0).getRating());
    }

    @Test
    public void shouldCalculateReviewStatsForCar() {
        // Arrange
        final User user = createUser("review-stats");
        final Car car = createCar("review-stats");
        reviewDao.create(user.getId(), car.getId(), new BigDecimal("3.0"), "Three", "Body", "owner", 2024, 1000, true);
        reviewDao.create(user.getId(), car.getId(), new BigDecimal("5.0"), "Five", "Body", "owner", 2025, 2000, true);

        // Exercise
        final Optional<ReviewStats> result = reviewDao.findStatsByCarId(car.getId());

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(2, result.get().getReviewCount());
        assertEquals(new BigDecimal("4.0"), result.get().getAverageRating());
    }

    @Test
    public void shouldUpdateReviewAndPersistNewValues() {
        // Arrange
        final Review review = createReview("update");

        // Exercise
        final Optional<Review> result = reviewDao.update(review.getId(), review.getCarId(), new BigDecimal("1.5"),
                "Updated title", "Updated body", "former_owner", 2020, 90000, false);

        // Assertions
        assertTrue(result.isPresent());
        final Review persisted = reviewDao.findById(review.getId()).orElseThrow();
        assertEquals("Updated title", persisted.getTitle());
        assertEquals(new BigDecimal("1.5"), persisted.getRating());
        assertEquals(false, persisted.getWouldRecommend());
    }

    @Test
    public void shouldDeleteReviewsByCarIdAndLeaveOtherReviewsUntouched() {
        // Arrange
        final Review deleted = createReview("delete-by-car");
        final Review kept = createReview("keep-by-car");

        // Exercise
        final int result = reviewDao.deleteByCarId(deleted.getCarId());

        // Assertions
        assertEquals(1, result);
        assertFalse(reviewDao.findById(deleted.getId()).isPresent());
        assertTrue(reviewDao.findById(kept.getId()).isPresent());
    }
}
