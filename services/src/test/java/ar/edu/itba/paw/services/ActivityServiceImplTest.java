package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.ActivityFeedCriteria;
import ar.edu.itba.paw.model.ActivityFeedItem;
import ar.edu.itba.paw.model.ActivityFeedPermissions;
import ar.edu.itba.paw.model.ActivityFeedReference;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.CommunityPostImage;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewImage;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.ActivityDao;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CommunityDao;
import ar.edu.itba.paw.persistence.CommunityPostImageDao;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.ReviewImageDao;
import ar.edu.itba.paw.persistence.ReviewLikeDao;
import ar.edu.itba.paw.persistence.ReviewReplyDao;
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
    private ReviewDao reviewDao;
    @Mock
    private ReviewImageDao reviewImageDao;
    @Mock
    private ReviewLikeDao reviewLikeDao;
    @Mock
    private ReviewReplyDao reviewReplyDao;
    @Mock
    private CommunityDao communityDao;
    @Mock
    private CommunityPostImageDao communityPostImageDao;
    @Mock
    private CarDao carDao;

    @InjectMocks
    private ActivityServiceImpl activityService;

    @Test
    public void shouldReturnEmptyPageWhenActivityDaoReturnsEmpty() {
        // Arrange
        when(activityDao.findFeed(any(ActivityFeedCriteria.class))).thenReturn(Page.empty(1, Pagination.ACTIVITY_PAGE_SIZE));

        // Exercise
        final Page<ActivityFeedItem> result = activityService.getActivityFeed(new ActivityFeedCriteria());

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
        final ReviewImage reviewImage = reviewImage(review.getId(), 300L);

        final Community community = community(20L, "classics", "Classics");
        final CommunityPost post = post(community, now.minusMinutes(2));
        final CommunityPostImage postImage = communityPostImage(post.getId(), 400L);

        when(activityDao.findFeed(any(ActivityFeedCriteria.class))).thenReturn(new Page<>(
                List.of(
                        new ActivityFeedReference(ActivityFeedReference.TYPE_COMMUNITY_POST, post.getId()),
                        new ActivityFeedReference(ActivityFeedReference.TYPE_REVIEW, review.getId())
                ),
                1,
                Pagination.ACTIVITY_PAGE_SIZE,
                2L
        ));
        when(reviewDao.findByIds(List.of(review.getId()))).thenReturn(List.of(review));
        when(communityDao.findPostsByIds(List.of(post.getId()))).thenReturn(List.of(post));
        when(carDao.findByIds(List.of(review.getCarId()))).thenReturn(List.of(car));
        when(reviewDao.findDefaultPagesByReviewIds(List.of(review.getId()))).thenReturn(Map.of(review.getId(), 2));
        when(reviewImageDao.findAllByReviewIds(List.of(review.getId()))).thenReturn(List.of(reviewImage));
        when(communityPostImageDao.findAllByPostIds(List.of(post.getId()))).thenReturn(List.of(postImage));
        when(communityDao.countCommentsByPostIds(List.of(post.getId()))).thenReturn(Map.of(post.getId(), 9L));
        when(communityDao.countHelpfulReactionsByPostIds(List.of(post.getId()))).thenReturn(Map.of(post.getId(), 7L));
        when(reviewLikeDao.countReviewLikesByReviewIds(List.of(review.getId()))).thenReturn(Map.of(review.getId(), 3L));
        when(reviewReplyDao.countRepliesByReviewIds(List.of(review.getId()))).thenReturn(Map.of(review.getId(), 2L));

        // Exercise
        final Page<ActivityFeedItem> result = activityService.getActivityFeed(new ActivityFeedCriteria());

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
        assertEquals(2, result.getItems().get(1).getReviewPage());
        assertEquals(1, result.getItems().get(1).getReviewImages().size());
    }

    @Test
    public void shouldReturnReviewWithoutImagesWhenGalleryIsMissing() {
        // Arrange
        final Review review = review(LocalDateTime.now());
        final Car car = TestModels.car(10L, 3L, "Ford", "Focus", 4L, 2021, "Sedan", "desc",
                LocalDateTime.now().minusYears(1), false, null, null, null, null, null, null, null);
        car.setId(10L);
        when(activityDao.findFeed(any(ActivityFeedCriteria.class))).thenReturn(new Page<>(
                List.of(new ActivityFeedReference(ActivityFeedReference.TYPE_REVIEW, review.getId())),
                2,
                Pagination.ACTIVITY_PAGE_SIZE,
                1L
        ));
        when(reviewDao.findByIds(List.of(review.getId()))).thenReturn(List.of(review));
        when(carDao.findByIds(List.of(review.getCarId()))).thenReturn(List.of(car));
        when(reviewDao.findDefaultPagesByReviewIds(List.of(review.getId()))).thenReturn(Map.of(review.getId(), 1));
        when(reviewImageDao.findAllByReviewIds(List.of(review.getId()))).thenReturn(List.of());
        when(reviewLikeDao.countReviewLikesByReviewIds(List.of(review.getId()))).thenReturn(Collections.emptyMap());
        when(reviewReplyDao.countRepliesByReviewIds(List.of(review.getId()))).thenReturn(Collections.emptyMap());

        // Exercise
        final Page<ActivityFeedItem> result = activityService.getActivityFeed(new ActivityFeedCriteria());

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
                Pagination.DEFAULT_PAGE,
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
                Pagination.DEFAULT_PAGE,
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
        when(communityDao.findMembershipRoles(post.getAuthorUserId(), List.of(community.getId())))
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
        when(communityDao.findMembershipRoles(moderatorUserId, List.of(community.getId())))
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
        final Community community = new Community();
        community.setId(id);
        community.setSlug(slug);
        community.setName(name);
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

    private static ReviewImage reviewImage(final long reviewId, final long imageId) {
        final ReviewImage image = new ReviewImage();
        final Review review = new Review();
        review.setId(reviewId);
        image.setReview(review);
        image.setImageId(imageId);
        return image;
    }

    private static CommunityPostImage communityPostImage(final long postId, final long imageId) {
        final CommunityPostImage image = new CommunityPostImage();
        final CommunityPost post = new CommunityPost();
        post.setId(postId);
        image.setPost(post);
        image.setImageId(imageId);
        return image;
    }
}
