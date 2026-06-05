package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.ProfileActivityItem;
import ar.edu.itba.paw.model.ProfileActivityItem.ItemType;
import ar.edu.itba.paw.model.User;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UserActivityJpaDaoTest extends AbstractPersistenceTest {

    @Autowired
    private UserActivityDao userActivityDao;

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 1, 1, 12, 0, 0);

    @Test
    void shouldMergeAuthoredReviewsAndPostsOrderedByCreatedAtDesc() {
        // Arrange
        final User author = insertUser("authored-merge", "authored-merge@example.com", "secret", "user");
        final Car car = createCar("authored-merge");
        final long communityId = insertCommunity("authored-merge", author.getId());
        final long reviewId = insertReview(author.getId(), car.getId(), "Authored review", BASE.plusMinutes(30));
        final long postId = insertPost(communityId, author.getId(), "authored-post", "Authored post", BASE.plusMinutes(10), false);

        // Exercise
        final Page<ProfileActivityItem> result = userActivityDao.findAuthoredActivity(author.getId(), 1);

        // Assertions
        assertEquals(2L, result.getTotalItems());
        assertEquals(2, result.getItems().size());
        assertEquals(ItemType.REVIEW, result.getItems().get(0).getType());
        assertEquals(reviewId, result.getItems().get(0).getEntityId());
        assertEquals(ItemType.POST, result.getItems().get(1).getType());
        assertEquals(postId, result.getItems().get(1).getEntityId());
    }

    @Test
    void shouldExcludeHiddenPostsFromAuthoredActivity() {
        // Arrange
        final User author = insertUser("authored-hidden", "authored-hidden@example.com", "secret", "user");
        final long communityId = insertCommunity("authored-hidden", author.getId());
        final long visiblePostId = insertPost(communityId, author.getId(), "visible-post", "Visible post", BASE.plusMinutes(20), false);
        final long hiddenPostId = insertPost(communityId, author.getId(), "hidden-post", "Hidden post", BASE.plusMinutes(10), true);

        // Exercise
        final Page<ProfileActivityItem> result = userActivityDao.findAuthoredActivity(author.getId(), 1);

        // Assertions
        assertEquals(1L, result.getTotalItems());
        assertEquals(1L, userActivityDao.countAuthoredActivity(author.getId()));
        assertEquals(1, result.getItems().size());
        assertEquals(visiblePostId, result.getItems().get(0).getEntityId());
        assertFalse(containsPost(result.getItems(), hiddenPostId));
    }

    @Test
    void shouldMergeLikedReviewsAndHelpfulReactedPostsOrderedByReactionCreatedAtDesc() {
        // Arrange
        final User liker = insertUser("liked-merge", "liked-merge@example.com", "secret", "user");
        final User author = insertUser("liked-merge-author", "liked-merge-author@example.com", "secret", "user");
        final Car car = createCar("liked-merge");
        final long communityId = insertCommunity("liked-merge", author.getId());
        final long reviewId = insertReview(author.getId(), car.getId(), "Liked review", BASE.minusDays(5));
        final long postId = insertPost(communityId, author.getId(), "liked-post", "Liked post", BASE.minusDays(5), false);
        insertReviewLike(reviewId, liker.getId(), BASE.plusMinutes(30));
        insertHelpfulReaction(postId, liker.getId(), BASE.plusMinutes(10));

        // Exercise
        final Page<ProfileActivityItem> result = userActivityDao.findLikedActivity(liker.getId(), 1);

        // Assertions
        assertEquals(2L, result.getTotalItems());
        assertEquals(2, result.getItems().size());
        assertEquals(ItemType.REVIEW, result.getItems().get(0).getType());
        assertEquals(reviewId, result.getItems().get(0).getEntityId());
        assertEquals(ItemType.POST, result.getItems().get(1).getType());
        assertEquals(postId, result.getItems().get(1).getEntityId());
    }

    @Test
    void shouldExcludeHiddenPostsFromLikedActivity() {
        // Arrange
        final User liker = insertUser("liked-hidden", "liked-hidden@example.com", "secret", "user");
        final User author = insertUser("liked-hidden-author", "liked-hidden-author@example.com", "secret", "user");
        final long communityId = insertCommunity("liked-hidden", author.getId());
        final long visiblePostId = insertPost(communityId, author.getId(), "visible-liked", "Visible liked", BASE.minusDays(5), false);
        final long hiddenPostId = insertPost(communityId, author.getId(), "hidden-liked", "Hidden liked", BASE.minusDays(5), true);
        insertHelpfulReaction(visiblePostId, liker.getId(), BASE.plusMinutes(20));
        insertHelpfulReaction(hiddenPostId, liker.getId(), BASE.plusMinutes(10));

        // Exercise
        final Page<ProfileActivityItem> result = userActivityDao.findLikedActivity(liker.getId(), 1);

        // Assertions
        assertEquals(1L, result.getTotalItems());
        assertEquals(1L, userActivityDao.countLikedActivity(liker.getId()));
        assertEquals(1, result.getItems().size());
        assertEquals(visiblePostId, result.getItems().get(0).getEntityId());
        assertFalse(containsPost(result.getItems(), hiddenPostId));
    }

    private boolean containsPost(final List<ProfileActivityItem> items, final long postId) {
        return items.stream()
                .anyMatch(item -> item.getType() == ItemType.POST && item.getEntityId() == postId);
    }

    private long insertCommunity(final String slug, final long createdByUserId) {
        jdbcTemplate.update(
                "INSERT INTO communities (slug, name, description, created_by_user_id) VALUES (?, ?, ?, ?)",
                slug, "Name " + slug, "Description " + slug, createdByUserId
        );
        return jdbcTemplate.queryForObject(
                "SELECT community_id FROM communities WHERE slug = ?", Long.class, slug
        );
    }

    private long insertReview(final long userId, final long carId, final String title, final LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO reviews (user_id, car_id, rating, title, body, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                userId, carId, new BigDecimal("4.0"), title, "Body " + title,
                Timestamp.valueOf(createdAt), Timestamp.valueOf(createdAt)
        );
        return jdbcTemplate.queryForObject(
                "SELECT review_id FROM reviews WHERE title = ?", Long.class, title
        );
    }

    private long insertPost(final long communityId, final long authorUserId, final String slug,
                            final String title, final LocalDateTime createdAt, final boolean hidden) {
        jdbcTemplate.update(
                "INSERT INTO community_posts (community_id, author_user_id, slug, title, body, created_at, updated_at, hidden) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                communityId, authorUserId, slug, title, "Body " + title,
                Timestamp.valueOf(createdAt), Timestamp.valueOf(createdAt), hidden
        );
        return jdbcTemplate.queryForObject(
                "SELECT post_id FROM community_posts WHERE community_id = ? AND slug = ?",
                Long.class, communityId, slug
        );
    }

    private void insertReviewLike(final long reviewId, final long userId, final LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO review_likes (review_id, user_id, created_at) VALUES (?, ?, ?)",
                reviewId, userId, Timestamp.valueOf(createdAt)
        );
    }

    private void insertHelpfulReaction(final long postId, final long userId, final LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO community_post_helpful_reactions (post_id, user_id, created_at) VALUES (?, ?, ?)",
                postId, userId, Timestamp.valueOf(createdAt)
        );
    }
}
