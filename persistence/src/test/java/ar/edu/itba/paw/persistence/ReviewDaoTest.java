package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.Test;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.beans.factory.annotation.Autowired;

public class ReviewDaoTest extends AbstractPersistenceTest {

    @Autowired
    private ReviewDao reviewDao;

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
        insertReview(user.getId(), user.getUsername(), car.getId(), new BigDecimal("2.0"), "Low", "Body",
                "owner", 2024, 1000, false);
        final Review high = insertReview(user.getId(), user.getUsername(), car.getId(), new BigDecimal("5.0"),
                "High", "Body", "owner", 2025, 500, true);

        // Exercise
        final Page<Review> result = reviewDao.findByCarIdOrderByRatingDesc(car.getId(), 1);

        // Assertions
        assertEquals(2, result.getItems().size());
        assertEquals(high.getId(), result.getItems().get(0).getId());
        assertEquals(new BigDecimal("5.0"), result.getItems().get(0).getRating());
    }

    @Test
    public void shouldPaginateReviewsByCarAndClampOutOfRangePage() {
        // Arrange
        final User user = createUser("review-car-page");
        final Car car = createCar("review-car-page");
        for (int i = 0; i < Pagination.REVIEWS_PAGE_SIZE + 1; i++) {
            insertReview(user.getId(), user.getUsername(), car.getId(), new BigDecimal("4.0"), "Paged " + i,
                    "Body", "owner", 2026, 1000 + i, true);
        }

        // Exercise
        final Page<Review> result = reviewDao.findByCarId(car.getId(), 999);

        // Assertions
        assertEquals(Pagination.REVIEWS_PAGE_SIZE + 1L, result.getTotalItems());
        assertEquals(2, result.getPageNumber());
        assertEquals(1, result.getItems().size());
        assertEquals(car.getId(), result.getItems().get(0).getCarId());
    }

    @Test
    public void shouldPaginateReviewsByFollowedUsersAndFavoriteCarsSeparately() {
        // Arrange
        final User follower = createUser("review-feed-follower");
        final User followed = createUser("review-feed-followed");
        final User other = createUser("review-feed-other");
        final Car favorite = createCar("review-feed-favorite");
        final Car otherCar = createCar("review-feed-other");
        jdbcTemplate.update(
                "INSERT INTO user_follows (follower_id, followed_id) VALUES (?, ?)",
                follower.getId(), followed.getId()
        );
        jdbcTemplate.update(
                "INSERT INTO car_favorites (user_id, car_id) VALUES (?, ?)",
                follower.getId(), favorite.getId()
        );
        insertReview(followed.getId(), followed.getUsername(), otherCar.getId(), new BigDecimal("4.0"),
                "Followed", "Body", "owner", 2026, 1000, true);
        insertReview(other.getId(), other.getUsername(), favorite.getId(), new BigDecimal("5.0"),
                "Favorite", "Body", "owner", 2026, 1000, true);
        insertReview(other.getId(), other.getUsername(), otherCar.getId(), new BigDecimal("1.0"),
                "Ignored", "Body", "owner", 2026, 1000, false);

        // Exercise
        final Page<Review> followedReviews = reviewDao.findByFollowedUsers(follower.getId(), 1);
        final Page<Review> favoriteReviews = reviewDao.findByFavoriteCars(follower.getId(), 1);

        // Assertions
        assertEquals(1L, followedReviews.getTotalItems());
        assertEquals("Followed", followedReviews.getItems().get(0).getTitle());
        assertEquals(1L, favoriteReviews.getTotalItems());
        assertEquals("Favorite", favoriteReviews.getItems().get(0).getTitle());
    }

    @Test
    public void shouldCountOnlyRecentReviewsByCarIds() {
        // Arrange
        final User user = createUser("review-recent-count");
        final Car recentCar = createCar("review-recent-count");
        final Car oldCar = createCar("review-old-count");
        insertReview(user.getId(), user.getUsername(), recentCar.getId(), new BigDecimal("4.0"), "Recent review",
                "Body", "owner", 2026, 1000, true);
        insertReview(user.getId(), user.getUsername(), oldCar.getId(), new BigDecimal("3.0"), "Old review",
                "Body", "owner", 2025, 2000, false);
        jdbcTemplate.update("UPDATE reviews SET created_at = ? WHERE title = ?",
                Timestamp.valueOf(LocalDateTime.now().minusHours(6)), "Recent review");
        jdbcTemplate.update("UPDATE reviews SET created_at = ? WHERE title = ?",
                Timestamp.valueOf(LocalDateTime.now().minusDays(9)), "Old review");
        final LocalDateTime since = LocalDateTime.now().minusDays(7);

        // Exercise
        final Map<Long, Long> result = reviewDao.countByCarIdsSince(List.of(recentCar.getId(), oldCar.getId()), since);

        // Assertions
        assertEquals(1, result.size());
        assertEquals(1L, result.get(recentCar.getId()));
        assertTrue(!result.containsKey(oldCar.getId()));
    }

    @Test
    public void shouldComputeDefaultPagePerReviewPartitionedByCar() {
        // Arrange
        final User user = createUser("default-pages");
        final Car pagedCar = createCar("default-pages");
        final Car otherCar = createCar("default-pages-other");
        final long[] reviewIds = new long[Pagination.REVIEWS_PAGE_SIZE + 1];
        for (int i = 0; i < reviewIds.length; i++) {
            reviewIds[i] = insertReview(user.getId(), user.getUsername(), pagedCar.getId(), new BigDecimal("4.0"),
                    "Paged review " + i, "Body", "owner", 2026, 1000, true).getId();
        }
        final long otherCarReviewId = insertReview(user.getId(), user.getUsername(), otherCar.getId(),
                new BigDecimal("3.0"), "Other car review", "Body", "owner", 2026, 1000, true).getId();

        // Exercise
        final Map<Long, Integer> result = reviewDao.findDefaultPagesByReviewIds(
                List.of(reviewIds[0], reviewIds[reviewIds.length - 1], otherCarReviewId));

        // Assertions
        assertEquals(3, result.size());
        // Default order is newest first: the most recent review sits on page 1 and the
        // oldest one overflows to page 2; the other car's only review starts its own ranking.
        assertEquals(1, result.get(reviewIds[reviewIds.length - 1]));
        assertEquals(2, result.get(reviewIds[0]));
        assertEquals(1, result.get(otherCarReviewId));
    }

    @Test
    public void shouldCalculateReviewStatsForCar() {
        // Arrange
        final User user = createUser("review-stats");
        final Car car = createCar("review-stats");
        insertReview(user.getId(), user.getUsername(), car.getId(), new BigDecimal("3.0"), "Three", "Body",
                "owner", 2024, 1000, true);
        insertReview(user.getId(), user.getUsername(), car.getId(), new BigDecimal("5.0"), "Five", "Body",
                "owner", 2025, 2000, true);

        // Exercise
        final Optional<ReviewStats> result = reviewDao.findStatsByCarId(car.getId());

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(2, result.get().getReviewCount());
        assertEquals(new BigDecimal("4.0"), result.get().getAverageRating());
    }

    @Test
    public void shouldCalculateReviewStatsForMultipleCarsIncludingCarsWithoutReviews() {
        // Arrange
        final User user = createUser("stats-multi");
        final Car reviewedCar = createCar("stats-multi");
        final Car unreviewedCar = createCar("stats-multi-empty");
        insertReview(user.getId(), user.getUsername(), reviewedCar.getId(), new BigDecimal("2.0"), "Two", "Body",
                "owner", 2024, 1000, true);
        insertReview(user.getId(), user.getUsername(), reviewedCar.getId(), new BigDecimal("5.0"), "Five", "Body",
                "owner", 2025, 2000, true);

        // Exercise
        final List<ReviewStats> result = reviewDao.findStatsByCarIds(List.of(reviewedCar.getId(), unreviewedCar.getId()));

        // Assertions
        assertEquals(2, result.size());
        assertEquals(reviewedCar.getId(), result.get(0).getCarId());
        assertEquals(2, result.get(0).getReviewCount());
        assertEquals(new BigDecimal("3.5"), result.get(0).getAverageRating());
        assertEquals(unreviewedCar.getId(), result.get(1).getCarId());
        assertEquals(0, result.get(1).getReviewCount());
        assertEquals(null, result.get(1).getAverageRating());
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
        flushAndClear();
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
        final Review alreadyAttributed = insertReview(otherUser.getId(), otherUser.getUsername(), car.getId(),
                new BigDecimal("4.0"), "Attributed Review", "Attributed body", "owner", 2025, 12000, true);

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
        flushAndClear();

        // Assertions
        assertTrue(result);
        assertEquals(0, countRows("SELECT COUNT(*) FROM reviews WHERE review_id = ?", deleted.getId()));
        assertEquals(1, countRows("SELECT COUNT(*) FROM reviews WHERE review_id = ?", kept.getId()));
    }

    @Test
    public void shouldPaginateLatestReviewsAcrossMultiplePages() {
        // Arrange
        for (int i = 0; i < Pagination.REVIEWS_PAGE_SIZE + 1; i++) {
            createReview("latest-" + i);
        }

        // Exercise
        final Page<Review> page1 = reviewDao.findLatest(1);
        final Page<Review> page2 = reviewDao.findLatest(2);

        // Assertions
        assertEquals(Pagination.REVIEWS_PAGE_SIZE + 1L, page1.getTotalItems());
        assertEquals(Pagination.REVIEWS_PAGE_SIZE, page1.getItems().size());
        assertEquals(1, page2.getItems().size());
    }

    @Test
    public void shouldPaginateReviewsByUserIdAcrossMultiplePages() {
        // Arrange
        final User user = createUser("user-paged");
        final Car car = createCar("user-paged");
        for (int i = 0; i < Pagination.REVIEWS_PAGE_SIZE + 1; i++) {
            insertReview(user.getId(), user.getUsername(), car.getId(), new BigDecimal("4.0"), "User Paged " + i,
                    "Body", "owner", 2026, 1000, true);
        }

        // Exercise
        final Page<Review> result = reviewDao.findByUserId(user.getId(), 2);

        // Assertions
        assertEquals(Pagination.REVIEWS_PAGE_SIZE + 1L, result.getTotalItems());
        assertEquals(2, result.getPageNumber());
        assertEquals(1, result.getItems().size());
    }

    @Test
    public void shouldPaginateReviewsByCarIdOrderedByRatingAscending() {
        // Arrange
        final User user = createUser("car-rating-asc");
        final Car car = createCar("car-rating-asc");
        // Insert 6 reviews (pageSize + 1)
        for (int i = 0; i < Pagination.REVIEWS_PAGE_SIZE + 1; i++) {
            insertReview(user.getId(), user.getUsername(), car.getId(), BigDecimal.valueOf(0.5 + (i * 0.5)),
                    "Rating Asc " + i, "Body", "owner", 2026, 1000, true);
        }

        // Exercise
        final Page<Review> result = reviewDao.findByCarIdOrderByRatingAsc(car.getId(), 2);

        // Assertions
        assertEquals(Pagination.REVIEWS_PAGE_SIZE + 1L, result.getTotalItems());
        assertEquals(2, result.getPageNumber());
        assertEquals(1, result.getItems().size());
        // Page 1 holds the first REVIEWS_PAGE_SIZE ascending ratings; page 2's first item has the next rating.
        final BigDecimal expectedRating = BigDecimal.valueOf(0.5 + Pagination.REVIEWS_PAGE_SIZE * 0.5)
                .setScale(1);
        assertEquals(expectedRating, result.getItems().get(0).getRating());
    }

}
