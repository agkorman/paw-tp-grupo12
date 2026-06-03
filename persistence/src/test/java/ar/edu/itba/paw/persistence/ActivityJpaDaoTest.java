package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ActivityFeedReference;
import ar.edu.itba.paw.model.Page;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivityJpaDaoTest extends AbstractPersistenceTest {

    @Autowired
    private ActivityDao activityDao;

    @Test
    void shouldReturnMixedLatestActivityOrderedByCreatedAt() {
        // Arrange
        final UserData creator = createUserData("activity-owner");
        final long communityId = insertCommunity("activity-classics", "Activity Classics", "desc", creator.id());
        final ReviewData olderReview = createReviewData("older-review", LocalDateTime.now().minusHours(4));
        final long hiddenPostId = insertCommunityPost(communityId, creator.id(), "hidden-post", "Hidden", "Body", LocalDateTime.now().minusHours(3), true);
        final long visiblePostId = insertCommunityPost(communityId, creator.id(), "visible-post", "Visible", "Body", LocalDateTime.now().minusHours(2), false);
        final ReviewData newestReview = createReviewData("newest-review", LocalDateTime.now().minusHours(1));

        // Exercise
        final Page<ActivityFeedReference> result = activityDao.findLatest(1);

        // Assertions
        assertEquals(3L, result.getTotalItems());
        assertEquals(List.of(
                ActivityFeedReference.TYPE_REVIEW,
                ActivityFeedReference.TYPE_COMMUNITY_POST,
                ActivityFeedReference.TYPE_REVIEW
        ), result.getItems().stream().map(ActivityFeedReference::getType).toList());
        assertEquals(List.of(newestReview.reviewId(), visiblePostId, olderReview.reviewId()),
                result.getItems().stream().map(ActivityFeedReference::getItemId).toList());
        assertTrue(result.getItems().stream().noneMatch(ref ->
                ref.isCommunityPost() && ref.getItemId() == hiddenPostId));
    }

    private UserData createUserData(final String suffix) {
        final ar.edu.itba.paw.model.User user = createUser(suffix);
        return new UserData(user.getId(), user.getUsername(), user.getEmail());
    }

    private ReviewData createReviewData(final String suffix, final LocalDateTime createdAt) {
        final ar.edu.itba.paw.model.User user = createUser("review-user-" + suffix);
        final ar.edu.itba.paw.model.Car car = createCar("review-car-" + suffix);
        final ar.edu.itba.paw.model.Review review = insertReview(user.getId(), user.getUsername(), car.getId(),
                new java.math.BigDecimal("4.0"), "Title " + suffix, "Body " + suffix, "owner", 2026, 1000, true);
        jdbcTemplate.update("UPDATE reviews SET created_at = ? WHERE review_id = ?", createdAt, review.getId());
        return new ReviewData(review.getId());
    }

    private long insertCommunity(final String slug, final String name, final String description, final long creatorId) {
        jdbcTemplate.update(
                "INSERT INTO communities (slug, name, description, created_by_user_id) VALUES (?, ?, ?, ?)",
                slug, name, description, creatorId
        );
        return jdbcTemplate.queryForObject("SELECT community_id FROM communities WHERE slug = ?", Long.class, slug);
    }

    private long insertCommunityPost(final long communityId, final long authorUserId, final String slug,
                                     final String title, final String body, final LocalDateTime createdAt,
                                     final boolean hidden) {
        jdbcTemplate.update(
                "INSERT INTO community_posts (community_id, author_user_id, slug, title, body, hidden) VALUES (?, ?, ?, ?, ?, ?)",
                communityId, authorUserId, slug, title, body, hidden
        );
        final long postId = jdbcTemplate.queryForObject(
                "SELECT post_id FROM community_posts WHERE community_id = ? AND slug = ?",
                Long.class,
                communityId,
                slug
        );
        jdbcTemplate.update("UPDATE community_posts SET created_at = ? WHERE post_id = ?", createdAt, postId);
        return postId;
    }

    private static final class UserData {
        private final long id;
        private final String username;
        private final String email;

        private UserData(final long id, final String username, final String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }

        private long id() {
            return id;
        }
    }

    private static final class ReviewData {
        private final long reviewId;

        private ReviewData(final long reviewId) {
            this.reviewId = reviewId;
        }

        private long reviewId() {
            return reviewId;
        }
    }
}
