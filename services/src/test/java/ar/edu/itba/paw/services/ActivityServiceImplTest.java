package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.ActivityFeedCriteria;
import ar.edu.itba.paw.model.ActivityFeedItem;
import ar.edu.itba.paw.model.ActivityFeedPermissions;
import ar.edu.itba.paw.model.ActivityFeedReference;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.ActivityDao;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceImplTest {

    @Mock
    private ActivityDao activityDao;
    @Mock
    private ReviewService reviewService;
    @Mock
    private ReviewLikeService reviewLikeService;
    @Mock
    private ReviewReplyService reviewReplyService;
    @Mock
    private CommunityService communityService;
    @Mock
    private CarService carService;

    @InjectMocks
    private ActivityServiceImpl activityService;

    @Test
    public void shouldReturnEmptyPageWhenActivityDaoReturnsEmpty() {
        // Arrange
        when(activityDao.findFeed(any(ActivityFeedCriteria.class), any())).thenReturn(Page.empty(1, Pagination.ACTIVITY_PAGE_SIZE));

        // Exercise
        final Page<ActivityFeedItem> result = activityService.getActivityFeed(new ActivityFeedCriteria(), null);

        // Assertions
        assertTrue(result.isEmpty());
        assertEquals(1, result.getPageNumber());
    }

    @Test
    public void shouldBuildMixedFeedPreservingReferenceOrder() {
        // Arrange
        final LocalDateTime now = LocalDateTime.now();
        final Review review = review(now.minusMinutes(5));
        final Car car = TestModels.car(10L, 3L, "Ford", "Focus", 4L, 2021, "Sedan", "desc",
                now.minusYears(1), false, null, null, null, null, null, null, null);
        car.setId(10L);
        final ImageMetadata reviewImage = reviewImage(review.getId(), 300L);

        final Community community = community(20L, "classics", "Classics");
        final CommunityPost post = post(community, now.minusMinutes(2));
        final ImageMetadata postImage = communityPostImage(post.getId(), 400L);

        when(activityDao.findFeed(any(ActivityFeedCriteria.class), any())).thenReturn(new Page<>(
                List.of(
                        new ActivityFeedReference(ActivityFeedReference.TYPE_COMMUNITY_POST, post.getId()),
                        new ActivityFeedReference(ActivityFeedReference.TYPE_REVIEW, review.getId())
                ),
                1,
                Pagination.ACTIVITY_PAGE_SIZE,
                2L
        ));
        when(reviewService.getReviewsByIds(List.of(review.getId()))).thenReturn(List.of(review));
        when(communityService.getPostsByIds(List.of(post.getId()))).thenReturn(List.of(post));
        when(carService.getCarsByIds(List.of(review.getCarId()))).thenReturn(List.of(car));
        when(reviewService.getImagesByReviewIds(List.of(review.getId()))).thenReturn(Map.of(review.getId(), List.of(reviewImage)));
        when(communityService.getImagesByPostIds(List.of(post.getId()))).thenReturn(Map.of(post.getId(), List.of(postImage)));
        when(communityService.countCommentsByPostIds(List.of(post.getId()))).thenReturn(Map.of(post.getId(), 9L));
        when(communityService.countHelpfulReactionsByPostIds(List.of(post.getId()))).thenReturn(Map.of(post.getId(), 7L));
        when(reviewLikeService.countReviewLikesByReviewIds(List.of(review.getId()))).thenReturn(Map.of(review.getId(), 3L));
        when(reviewReplyService.countRepliesByReviewIds(List.of(review.getId()))).thenReturn(Map.of(review.getId(), 2L));

        // Exercise
        final Page<ActivityFeedItem> result = activityService.getActivityFeed(new ActivityFeedCriteria(), null);

        // Assertions
        assertEquals(2, result.getItems().size());
        assertTrue(result.getItems().get(0).isCommunityPost());
        assertEquals(post.getId(), result.getItems().get(0).getCommunityPost().getId());
        assertEquals(7L, result.getItems().get(0).getHelpfulCount());
        assertEquals(9L, result.getItems().get(0).getCommentCount());
        assertEquals(1, result.getItems().get(0).getCommunityPostImages().size());
        assertTrue(result.getItems().get(1).isReview());
        assertEquals(review.getId(), result.getItems().get(1).getReview().getId());
        assertEquals(3L, result.getItems().get(1).getReviewLikeCount());
        assertEquals(2L, result.getItems().get(1).getReviewReplyCount());
        assertEquals(1, result.getItems().get(1).getReviewImages().size());
    }

    @Test
    public void shouldReturnReviewWithoutImagesWhenGalleryIsMissing() {
        // Arrange
        final Review review = review(LocalDateTime.now());
        final Car car = TestModels.car(10L, 3L, "Ford", "Focus", 4L, 2021, "Sedan", "desc",
                LocalDateTime.now().minusYears(1), false, null, null, null, null, null, null, null);
        car.setId(10L);
        when(activityDao.findFeed(any(ActivityFeedCriteria.class), any())).thenReturn(new Page<>(
                List.of(new ActivityFeedReference(ActivityFeedReference.TYPE_REVIEW, review.getId())),
                2,
                Pagination.ACTIVITY_PAGE_SIZE,
                1L
        ));
        when(reviewService.getReviewsByIds(List.of(review.getId()))).thenReturn(List.of(review));
        when(carService.getCarsByIds(List.of(review.getCarId()))).thenReturn(List.of(car));
        when(reviewService.getImagesByReviewIds(List.of(review.getId()))).thenReturn(Collections.emptyMap());
        when(reviewLikeService.countReviewLikesByReviewIds(List.of(review.getId()))).thenReturn(Collections.emptyMap());
        when(reviewReplyService.countRepliesByReviewIds(List.of(review.getId()))).thenReturn(Collections.emptyMap());

        // Exercise
        final Page<ActivityFeedItem> result = activityService.getActivityFeed(new ActivityFeedCriteria(), null);

        // Assertions
        assertEquals(1, result.getItems().size());
        assertTrue(result.getItems().get(0).isReview());
        assertTrue(result.getItems().get(0).getReviewImages().isEmpty());
    }

    @Test
    public void shouldAllowOwnerToEditAndDeleteOwnReview() {
        // Arrange
        final Review review = review(LocalDateTime.now());
        final ActivityFeedItem item = ActivityFeedItem.reviewItem(
                review,
                0L,
                0L,
                null,
                Collections.emptyList()
        );

        // Exercise
        final Map<ActivityFeedReference, ActivityFeedPermissions> result =
                activityService.getActivityFeedPermissions(List.of(item), review.getUserId(), false);

        // Assertions
        final ActivityFeedPermissions permissions = result.get(item.getReference());
        assertTrue(permissions.isEditable());
        assertTrue(permissions.isDeletable());
        assertFalse(permissions.isHideable());
    }

    @Test
    public void shouldAllowAdminToHideReviewOwnedByAnotherUser() {
        // Arrange
        final Review review = review(LocalDateTime.now());
        final long adminUserId = 99L;
        final ActivityFeedItem item = ActivityFeedItem.reviewItem(
                review,
                0L,
                0L,
                null,
                Collections.emptyList()
        );

        // Exercise
        final Map<ActivityFeedReference, ActivityFeedPermissions> result =
                activityService.getActivityFeedPermissions(List.of(item), adminUserId, true);

        // Assertions
        final ActivityFeedPermissions permissions = result.get(item.getReference());
        assertFalse(permissions.isEditable());
        assertFalse(permissions.isDeletable());
        assertTrue(permissions.isHideable());
    }

    @Test
    public void shouldAllowOwnerToEditAndDeleteOwnCommunityPost() {
        // Arrange
        final Community community = community(20L, "classics", "Classics");
        final CommunityPost post = post(community, LocalDateTime.now());
        final ActivityFeedItem item = ActivityFeedItem.communityPostItem(
                post,
                0L,
                0L,
                Collections.emptyList()
        );
        when(communityService.getViewerRoles(post.getAuthorUserId(), List.of(community.getId())))
                .thenReturn(Collections.emptyMap());

        // Exercise
        final Map<ActivityFeedReference, ActivityFeedPermissions> result =
                activityService.getActivityFeedPermissions(List.of(item), post.getAuthorUserId(), false);

        // Assertions
        final ActivityFeedPermissions permissions = result.get(item.getReference());
        assertTrue(permissions.isEditable());
        assertTrue(permissions.isDeletable());
        assertFalse(permissions.isHideable());
    }

    @Test
    public void shouldAllowCommunityModeratorToHidePostOwnedByAnotherUser() {
        // Arrange
        final Community community = community(20L, "classics", "Classics");
        final CommunityPost post = post(community, LocalDateTime.now());
        final long moderatorUserId = 77L;
        final ActivityFeedItem item = ActivityFeedItem.communityPostItem(
                post,
                0L,
                0L,
                Collections.emptyList()
        );
        when(communityService.getViewerRoles(moderatorUserId, List.of(community.getId())))
                .thenReturn(Map.of(community.getId(), "moderator"));

        // Exercise
        final Map<ActivityFeedReference, ActivityFeedPermissions> result =
                activityService.getActivityFeedPermissions(List.of(item), moderatorUserId, false);

        // Assertions
        final ActivityFeedPermissions permissions = result.get(item.getReference());
        assertFalse(permissions.isEditable());
        assertFalse(permissions.isDeletable());
        assertTrue(permissions.isHideable());
    }

    private static Review review(final LocalDateTime createdAt) {
        final Review review = TestModels.review(5L, 2L, "author@example.com", 10L,
                new BigDecimal("4.5"), "Great daily", "Solid all around.", "owner", 2021, 10000, true,
                createdAt, createdAt);
        final User user = TestModels.user(2L, "driver.one", "author@example.com", "pw", "user", createdAt.minusDays(30));
        review.setUser(user);
        return review;
    }

    private static Community community(final long id, final String slug, final String name) {
        final Community community = new Community(slug, name, "Community description");
        community.setId(id);
        return community;
    }

    private static CommunityPost post(final Community community, final LocalDateTime createdAt) {
        final CommunityPost post = new CommunityPost();
        post.setId(8L);
        post.setCommunity(community);
        post.setAuthor(TestModels.user(9L, "mateo.classics", "mateo@classics.com", "pw", "user", createdAt.minusDays(10)));
        post.setSlug("falcon-photos");
        post.setTitle("Falcon photos");
        post.setBody("A few shots from last weekend.");
        post.setCreatedAt(createdAt);
        return post;
    }

    private static ImageMetadata reviewImage(final long reviewId, final long imageId) {
        return new ImageMetadata(imageId, reviewId, 0, "image/png", LocalDateTime.now());
    }

    private static ImageMetadata communityPostImage(final long postId, final long imageId) {
        return new ImageMetadata(imageId, postId, 0, "image/png", LocalDateTime.now());
    }
}
