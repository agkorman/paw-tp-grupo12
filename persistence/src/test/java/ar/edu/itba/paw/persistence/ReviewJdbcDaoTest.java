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
        assertEquals(1, countRows("SELECT COUNT(*) FROM reviews WHERE review_id = ?", result.getId()));
        assertEquals(user.getId(), jdbcTemplate.queryForObject(
                "SELECT user_id FROM reviews WHERE review_id = ?", Long.class, result.getId()
        ));
        assertEquals(car.getId(), jdbcTemplate.queryForObject(
                "SELECT car_id FROM reviews WHERE review_id = ?", Long.class, result.getId()
        ));
        assertEquals(new BigDecimal("4.5"), jdbcTemplate.queryForObject(
                "SELECT rating FROM reviews WHERE review_id = ?", BigDecimal.class, result.getId()
        ));
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
        assertEquals("Updated title", result.get().getTitle());
        assertEquals("Updated title", jdbcTemplate.queryForObject(
                "SELECT title FROM reviews WHERE review_id = ?", String.class, review.getId()
        ));
        assertEquals(new BigDecimal("1.5"), jdbcTemplate.queryForObject(
                "SELECT rating FROM reviews WHERE review_id = ?", BigDecimal.class, review.getId()
        ));
        assertEquals(false, jdbcTemplate.queryForObject(
                "SELECT would_recommend FROM reviews WHERE review_id = ?", Boolean.class, review.getId()
        ));
    }

    @Test
    public void shouldBindAnonymousReviewsToUserByEmailIgnoringCaseAndOnlyTouchingAnonymousRows() {
        // Arrange
        final User boundUser = createUser("review-bind");
        final User otherUser = createUser("review-bind-other");
        final Car car = createCar("review-bind");
        jdbcTemplate.update(
                "INSERT INTO reviews (user_id, reviewer_email, car_id, rating, title, body) "
                        + "VALUES (NULL, ?, ?, ?, ?, ?)",
                "  Reviewer@Example.com  ", car.getId(), new BigDecimal("3.5"), "Anonymous Review", "Anonymous body"
        );
        final long anonymousId = jdbcTemplate.queryForObject(
                "SELECT review_id FROM reviews WHERE title = ?", Long.class, "Anonymous Review"
        );
        final Review alreadyAttributed = reviewDao.create(otherUser.getId(), car.getId(), new BigDecimal("4.0"),
                "Attributed Review", "Attributed body", "owner", 2025, 12000, true);

        // Exercise
        final int result = reviewDao.bindReviewsToUserByEmail(boundUser.getId(), "reviewer@example.com");

        // Assertions
        assertEquals(1, result);
        assertEquals(boundUser.getId(), jdbcTemplate.queryForObject(
                "SELECT user_id FROM reviews WHERE review_id = ?", Long.class, anonymousId
        ));
        assertEquals(otherUser.getId(), jdbcTemplate.queryForObject(
                "SELECT user_id FROM reviews WHERE review_id = ?", Long.class, alreadyAttributed.getId()
        ));
    }

    @Test
    public void shouldDeleteReviewByIdAndLeaveOtherReviewsUntouched() {
        // Arrange
        final Review deleted = createReview("delete-by-id");
        final Review kept = createReview("keep-by-id");

        // Exercise
        final boolean result = reviewDao.delete(deleted.getId());

        // Assertions
        assertTrue(result);
        assertEquals(0, countRows("SELECT COUNT(*) FROM reviews WHERE review_id = ?", deleted.getId()));
        assertEquals(1, countRows("SELECT COUNT(*) FROM reviews WHERE review_id = ?", kept.getId()));
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
        assertEquals(0, countRows("SELECT COUNT(*) FROM reviews WHERE review_id = ?", deleted.getId()));
        assertEquals(1, countRows("SELECT COUNT(*) FROM reviews WHERE review_id = ?", kept.getId()));
    }
}
