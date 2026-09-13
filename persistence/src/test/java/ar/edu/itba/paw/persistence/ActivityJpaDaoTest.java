package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ActivityFeedCriteria;
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
    void shouldReturnMixedFeedOrderedByCreatedAtWhenSortLatest() {
        // Arrange
        final ar.edu.itba.paw.model.User creator = createUser("activity-owner");
        final long communityId = insertCommunity("activity-classics", "Activity Classics", "desc", creator.getId());
        final long olderReviewId = createReviewAt("older-review", LocalDateTime.now().minusHours(4));
        final long hiddenPostId = insertCommunityPost(communityId, creator.getId(), "hidden-post", "Hidden", "Body", LocalDateTime.now().minusHours(3), true);
        final long visiblePostId = insertCommunityPost(communityId, creator.getId(), "visible-post", "Visible", "Body", LocalDateTime.now().minusHours(2), false);
        final long newestReviewId = createReviewAt("newest-review", LocalDateTime.now().minusHours(1));

        // Exercise
        final Page<ActivityFeedReference> result = activityDao.findFeed(criteria(null, null, ActivityFeedCriteria.SORT_LATEST), null);

        // Assertions
        assertEquals(3L, result.getTotalItems());
        assertEquals(List.of(
                ActivityFeedReference.TYPE_REVIEW,
                ActivityFeedReference.TYPE_COMMUNITY_POST,
                ActivityFeedReference.TYPE_REVIEW
        ), result.getItems().stream().map(ActivityFeedReference::getType).toList());
        assertEquals(List.of(newestReviewId, visiblePostId, olderReviewId),
                result.getItems().stream().map(ActivityFeedReference::getItemId).toList());
        assertTrue(result.getItems().stream().noneMatch(ref ->
                ref.isCommunityPost() && ref.getItemId() == hiddenPostId));
    }

    @Test
    void shouldRankHigherEngagementFirstWhenTrending() {
        // Arrange: both reviews are recent (same recency bucket), so weighted engagement decides order.
        final LocalDateTime recent = LocalDateTime.now().minusHours(2);
        final long popularReviewId = createReviewAt("popular", recent);
        final long quietReviewId = createReviewAt("quiet", recent);
        likeReview(popularReviewId, "liker-a");
        likeReview(popularReviewId, "liker-b");
        replyToReview(popularReviewId, "replier-a");
        replyToReview(quietReviewId, "replier-b");

        // Exercise
        final Page<ActivityFeedReference> result = activityDao.findFeed(criteria(null, null, ActivityFeedCriteria.SORT_TRENDING), null);

        // Assertions
        assertEquals(2L, result.getTotalItems());
        assertEquals(List.of(popularReviewId, quietReviewId),
                result.getItems().stream().map(ActivityFeedReference::getItemId).toList());
    }

    @Test
    void shouldRankHighDiscussionLowApprovalFirstWhenControversial() {
        // Arrange
        final LocalDateTime recent = LocalDateTime.now().minusHours(2);
        final long approvedReviewId = createReviewAt("approved", recent);
        final long debatedReviewId = createReviewAt("debated", recent);
        likeReview(approvedReviewId, "fan-a");
        likeReview(approvedReviewId, "fan-b");
        likeReview(approvedReviewId, "fan-c");
        replyToReview(debatedReviewId, "arguer-a");
        replyToReview(debatedReviewId, "arguer-b");

        // Exercise
        final Page<ActivityFeedReference> result = activityDao.findFeed(criteria(null, null, ActivityFeedCriteria.SORT_CONTROVERSIAL), null);

        // Assertions
        assertEquals(List.of(debatedReviewId, approvedReviewId),
                result.getItems().stream().map(ActivityFeedReference::getItemId).toList());
    }

    @Test
    void shouldReturnOnlyReviewsWhenTypeIsReviews() {
        // Arrange
        final ar.edu.itba.paw.model.User creator = createUser("type-owner");
        final long communityId = insertCommunity("type-community", "Type Community", "desc", creator.getId());
        final long reviewId = createReviewAt("only-review", LocalDateTime.now().minusHours(1));
        insertCommunityPost(communityId, creator.getId(), "type-post", "Post", "Body", LocalDateTime.now().minusHours(2), false);

        // Exercise
        final Page<ActivityFeedReference> result = activityDao.findFeed(criteria(ActivityFeedCriteria.TYPE_REVIEWS, null, ActivityFeedCriteria.SORT_LATEST), null);

        // Assertions
        assertEquals(1L, result.getTotalItems());
        assertTrue(result.getItems().stream().allMatch(ActivityFeedReference::isReview));
        assertEquals(reviewId, result.getItems().get(0).getItemId());
    }

    @Test
    void shouldReturnOnlyCommunityPostsWhenTypeIsCommunity() {
        // Arrange
        final ar.edu.itba.paw.model.User creator = createUser("type-owner-2");
        final long communityId = insertCommunity("type-community-2", "Type Community 2", "desc", creator.getId());
        createReviewAt("ignored-review", LocalDateTime.now().minusHours(1));
        final long postId = insertCommunityPost(communityId, creator.getId(), "kept-post", "Post", "Body", LocalDateTime.now().minusHours(2), false);

        // Exercise
        final Page<ActivityFeedReference> result = activityDao.findFeed(criteria(ActivityFeedCriteria.TYPE_COMMUNITY, null, ActivityFeedCriteria.SORT_LATEST), null);

        // Assertions
        assertEquals(1L, result.getTotalItems());
        assertTrue(result.getItems().stream().allMatch(ActivityFeedReference::isCommunityPost));
        assertEquals(postId, result.getItems().get(0).getItemId());
    }

    @Test
    void shouldExcludeRowsOlderThanTimeframeAndCountAccordingly() {
        // Arrange
        final long recentReviewId = createReviewAt("recent-review", LocalDateTime.now().minusDays(2));
        createReviewAt("stale-review", LocalDateTime.now().minusDays(40));

        // Exercise
        final Page<ActivityFeedReference> result = activityDao.findFeed(criteria(null, ActivityFeedCriteria.TIMEFRAME_MONTH, ActivityFeedCriteria.SORT_LATEST), null);

        // Assertions
        assertEquals(1L, result.getTotalItems());
        assertEquals(recentReviewId, result.getItems().get(0).getItemId());
    }

    @Test
    void shouldReturnOnlyFollowedAuthorsContentWhenScopeIsFollowing() {
        // Arrange
        final ar.edu.itba.paw.model.User viewer = createUser("scope-viewer");
        final ar.edu.itba.paw.model.User followed = createUser("scope-followed");
        final ar.edu.itba.paw.model.User notFollowed = createUser("scope-not-followed");
        final long communityId = insertCommunity("scope-community", "Scope Community", "desc", followed.getId());
        follow(viewer.getId(), followed.getId());
        final long followedReviewId = createReviewAt("followed-review", LocalDateTime.now().minusHours(1),
                followed.getId(), followed.getUsername());
        createReviewAt("not-followed-review", LocalDateTime.now().minusHours(1),
                notFollowed.getId(), notFollowed.getUsername());
        final long followedPostId = insertCommunityPost(communityId, followed.getId(), "followed-post", "Post",
                "Body", LocalDateTime.now().minusHours(1), false);
        insertCommunityPost(communityId, notFollowed.getId(), "not-followed-post", "Post",
                "Body", LocalDateTime.now().minusHours(1), false);

        // Exercise
        final Page<ActivityFeedReference> result = activityDao.findFeed(
                criteria(null, null, ActivityFeedCriteria.SORT_LATEST, ActivityFeedCriteria.SCOPE_FOLLOWING),
                viewer.getId());

        // Assertions
        assertEquals(2L, result.getTotalItems());
        assertEquals(java.util.Set.of(followedReviewId, followedPostId),
                result.getItems().stream().map(ActivityFeedReference::getItemId).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void shouldExcludeAnonymousReviewWithNullUserIdWhenScopeIsFollowing() {
        // Arrange
        final ar.edu.itba.paw.model.User viewer = createUser("scope-viewer-anon");
        final ar.edu.itba.paw.model.Car car = createCar("scope-anon-car");
        jdbcTemplate.update(
                "INSERT INTO reviews (user_id, reviewer_email, car_id, rating, title, body) "
                        + "VALUES (NULL, ?, ?, ?, ?, ?)",
                "anon@example.com", car.getId(), new java.math.BigDecimal("3.5"), "Anon Review", "Anon body"
        );

        // Exercise
        final Page<ActivityFeedReference> result = activityDao.findFeed(
                criteria(null, null, ActivityFeedCriteria.SORT_LATEST, ActivityFeedCriteria.SCOPE_FOLLOWING),
                viewer.getId());

        // Assertions
        assertEquals(0L, result.getTotalItems());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void shouldReturnOnlyJoinedCommunityPostsAndNoReviewsWhenScopeIsJoined() {
        // Arrange
        final ar.edu.itba.paw.model.User viewer = createUser("scope-joined-viewer");
        final ar.edu.itba.paw.model.User creator = createUser("scope-joined-creator");
        final long joinedCommunityId = insertCommunity("joined-community", "Joined Community", "desc", creator.getId());
        final long notJoinedCommunityId = insertCommunity("not-joined-community", "Not Joined Community", "desc", creator.getId());
        joinCommunity(joinedCommunityId, viewer.getId());
        createReviewAt("joined-scope-review", LocalDateTime.now().minusHours(1));
        final long joinedPostId = insertCommunityPost(joinedCommunityId, creator.getId(), "joined-post", "Post",
                "Body", LocalDateTime.now().minusHours(1), false);
        insertCommunityPost(notJoinedCommunityId, creator.getId(), "not-joined-post", "Post",
                "Body", LocalDateTime.now().minusHours(1), false);

        // Exercise
        final Page<ActivityFeedReference> result = activityDao.findFeed(
                criteria(ActivityFeedCriteria.TYPE_ALL, null, ActivityFeedCriteria.SORT_LATEST, ActivityFeedCriteria.SCOPE_JOINED),
                viewer.getId());

        // Assertions
        assertEquals(1L, result.getTotalItems());
        assertEquals(joinedPostId, result.getItems().get(0).getItemId());
        assertTrue(result.getItems().get(0).isCommunityPost());
    }

    @Test
    void shouldReturnEmptyPageWhenScopeRequiresAuthenticationButViewerIsMissing() {
        // Arrange
        createReviewAt("no-viewer-review", LocalDateTime.now().minusHours(1));
        final Long missingViewerId = null;

        // Exercise
        final Page<ActivityFeedReference> result = activityDao.findFeed(
                criteria(null, null, ActivityFeedCriteria.SORT_LATEST, ActivityFeedCriteria.SCOPE_FOLLOWING),
                missingViewerId);

        // Assertions
        assertEquals(0L, result.getTotalItems());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void shouldCombineScopeWithTimeframeAndTrendingSortWithoutBreakingParameterOrder() {
        // Arrange
        final ar.edu.itba.paw.model.User viewer = createUser("scope-combo-viewer");
        final ar.edu.itba.paw.model.User followed = createUser("scope-combo-followed");
        follow(viewer.getId(), followed.getId());
        final long recentFollowedReviewId = createReviewAt("combo-recent-followed", LocalDateTime.now().minusHours(2),
                followed.getId(), followed.getUsername());
        createReviewAt("combo-stale-followed", LocalDateTime.now().minusDays(40),
                followed.getId(), followed.getUsername());
        likeReview(recentFollowedReviewId, "combo-liker");

        // Exercise
        final Page<ActivityFeedReference> result = activityDao.findFeed(
                criteria(ActivityFeedCriteria.TYPE_REVIEWS, ActivityFeedCriteria.TIMEFRAME_MONTH,
                        ActivityFeedCriteria.SORT_TRENDING, ActivityFeedCriteria.SCOPE_FOLLOWING),
                viewer.getId());

        // Assertions
        assertEquals(1L, result.getTotalItems());
        assertEquals(recentFollowedReviewId, result.getItems().get(0).getItemId());
    }

    private ActivityFeedCriteria criteria(final String type, final String timeframe, final String sort) {
        return criteria(type, timeframe, sort, null);
    }

    private ActivityFeedCriteria criteria(final String type, final String timeframe, final String sort,
                                          final String scope) {
        final ActivityFeedCriteria criteria = new ActivityFeedCriteria();
        criteria.setType(type);
        criteria.setTimeframe(timeframe);
        criteria.setSort(sort);
        criteria.setScope(scope);
        criteria.setPage(1);
        return criteria;
    }

    private void follow(final long followerId, final long followedId) {
        jdbcTemplate.update("INSERT INTO user_follows (follower_id, followed_id) VALUES (?, ?)",
                followerId, followedId);
    }

    private void joinCommunity(final long communityId, final long userId) {
        jdbcTemplate.update("INSERT INTO community_memberships (community_id, user_id) VALUES (?, ?)",
                communityId, userId);
    }

    private long createReviewAt(final String suffix, final LocalDateTime createdAt) {
        final ar.edu.itba.paw.model.User user = createUser("review-user-" + suffix);
        return createReviewAt(suffix, createdAt, user.getId(), user.getUsername());
    }

    private long createReviewAt(final String suffix, final LocalDateTime createdAt, final long authorUserId,
                                final String authorUsername) {
        final ar.edu.itba.paw.model.Car car = createCar("review-car-" + suffix);
        final ar.edu.itba.paw.model.Review review = insertReview(authorUserId, authorUsername, car.getId(),
                new java.math.BigDecimal("4.0"), "Title " + suffix, "Body " + suffix, "owner", 2026, 1000, true);
        jdbcTemplate.update("UPDATE reviews SET created_at = ? WHERE review_id = ?", createdAt, review.getId());
        return review.getId();
    }

    private void likeReview(final long reviewId, final String userSuffix) {
        final ar.edu.itba.paw.model.User user = createUser(userSuffix);
        jdbcTemplate.update("INSERT INTO review_likes (review_id, user_id) VALUES (?, ?)", reviewId, user.getId());
    }

    private void replyToReview(final long reviewId, final String userSuffix) {
        final ar.edu.itba.paw.model.User user = createUser(userSuffix);
        insertReviewReply(reviewId, user.getId(), user.getUsername(), "reply by " + userSuffix);
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
}
