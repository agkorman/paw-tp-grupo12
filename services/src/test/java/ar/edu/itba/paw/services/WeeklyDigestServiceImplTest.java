package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.EmailRecipient;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WeeklyDigestServiceImplTest {

    private static final long USER_ID = 10L;
    private static final long REVIEW_ID = 20L;
    private static final long SECOND_REVIEW_ID = 21L;
    private static final long CAR_ID = 30L;
    private static final long FAVORITE_CAR_ID = 31L;
    private static final String USER_EMAIL = "user@example.com";
    private static final String USERNAME = "driver";

    @Mock
    private UserService userService;
    @Mock
    private CarService carService;
    @Mock
    private ReviewService reviewService;
    @Mock
    private ReviewLikeService reviewLikeService;
    @Mock
    private ReviewReplyService reviewReplyService;
    @Mock
    private CarFavoriteService carFavoriteService;
    @Mock
    private CarRequestService carRequestService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private WeeklyDigestServiceImpl weeklyDigestService;

    private static User user() {
        return TestModels.user(USER_ID, USERNAME, USER_EMAIL, "p", "user", LocalDateTime.now());
    }

    private static Car car(final long id, final String brand, final String model) {
        return TestModels.car(id, 1L, brand, model, 2L, "sedan", "desc", LocalDateTime.now());
    }

    private static Review review(final long id, final long carId, final String title,
                                 final LocalDateTime createdAt) {
        return TestModels.review(id, USER_ID, USER_EMAIL, carId, new BigDecimal("4.0"), title, "body",
                "owner", 2026, 1000, true, createdAt, createdAt);
    }

    @Test
    public void shouldBuildModeratorDigestWithPendingRequestCount() {
        // Arrange
        final List<EmailRecipient> moderatorRecipients = List.of(new EmailRecipient("mod@example.com", "es"));
        when(userService.getModeratorEmailRecipients()).thenReturn(moderatorRecipients);
        when(carRequestService.countCarRequestsByStatus(CarRequestService.STATUS_PENDING))
                .thenReturn(2L);

        // Exercise
        final Optional<WeeklyDigestServiceImpl.ModeratorDigest> result = weeklyDigestService.buildModeratorDigest();

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(moderatorRecipients, result.get().recipients);
        assertEquals(2L, result.get().pendingCount);
    }

    @Test
    public void shouldNotBuildModeratorDigestWhenNoModeratorRecipients() {
        // Arrange
        final List<EmailRecipient> noRecipients = List.of();
        when(userService.getModeratorEmailRecipients()).thenReturn(noRecipients);

        // Exercise
        final Optional<WeeklyDigestServiceImpl.ModeratorDigest> result = weeklyDigestService.buildModeratorDigest();

        // Assertions
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldSwallowFailureLoadingModeratorRecipients() {
        // Arrange
        final RuntimeException failure = new RuntimeException("boom");
        when(userService.getModeratorEmailRecipients()).thenThrow(failure);

        // Exercise
        final Optional<WeeklyDigestServiceImpl.ModeratorDigest> result = weeklyDigestService.buildModeratorDigest();

        // Assertions
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldBuildUserDigestFromReviewAndFavoriteActivity() {
        // Arrange
        final LocalDateTime since = LocalDateTime.now().minusDays(7);
        final LocalDateTime recent = LocalDateTime.now().minusDays(1);
        when(userService.getAllUsers()).thenReturn(List.of(user()));
        when(reviewLikeService.countNewLikesPerReviewSince(any(LocalDateTime.class)))
                .thenReturn(Map.of(REVIEW_ID, 3L));
        when(reviewReplyService.countNewRepliesPerReviewSince(any(LocalDateTime.class)))
                .thenReturn(Map.of(REVIEW_ID, 1L, SECOND_REVIEW_ID, 2L));
        when(reviewService.getReviewsByIds(anyCollection()))
                .thenReturn(List.of(
                        review(REVIEW_ID, CAR_ID, "Daily driver", recent),
                        review(SECOND_REVIEW_ID, 99L, "Road trip", recent)
                ));
        when(carService.getCarsByIds(argThat(c -> c != null && c.contains(CAR_ID))))
                .thenReturn(List.of(car(CAR_ID, "Toyota", "Corolla")));
        when(carFavoriteService.findAllFavoriteCarIdsByUser())
                .thenReturn(Map.of(USER_ID, List.of(FAVORITE_CAR_ID)));
        when(carService.getCarsByIds(argThat(c -> c != null && c.contains(FAVORITE_CAR_ID))))
                .thenReturn(List.of(car(FAVORITE_CAR_ID, "Honda", "Civic")));
        when(reviewService.countNewReviewsByCarIds(anyCollection(), any(LocalDateTime.class)))
                .thenReturn(Map.of(FAVORITE_CAR_ID, 1L));

        // Exercise
        final List<WeeklyDigestServiceImpl.UserDigest> digests = weeklyDigestService.buildUserDigests(since);

        // Assertions
        assertEquals(1, digests.size());
        final WeeklyDigestServiceImpl.UserDigest digest = digests.get(0);
        assertEquals(USER_EMAIL, digest.email);
        assertEquals(USERNAME, digest.username);
        assertEquals(2, digest.reviewActivities.size());
        assertTrue(digest.reviewActivities.stream().anyMatch(item ->
                "Daily driver".equals(item.reviewTitle)
                        && "Toyota Corolla".equals(item.carName)
                        && item.newLikes == 3
                        && item.newReplies == 1));
        assertTrue(digest.reviewActivities.stream().anyMatch(item ->
                "Road trip".equals(item.reviewTitle)
                        && item.carName == null
                        && item.newLikes == 0
                        && item.newReplies == 2));
        assertEquals(1, digest.favoriteActivities.size());
        assertEquals("Honda Civic", digest.favoriteActivities.get(0).carName);
        assertEquals(1L, digest.favoriteActivities.get(0).newReviewCount);
    }

    @Test
    public void shouldBuildEmptyDigestWhenNoReviewActivityAndNoFavoriteCars() {
        // Arrange
        final LocalDateTime since = LocalDateTime.now().minusDays(7);
        when(userService.getAllUsers()).thenReturn(List.of(user()));
        when(reviewLikeService.countNewLikesPerReviewSince(any(LocalDateTime.class)))
                .thenReturn(Map.of());
        when(reviewReplyService.countNewRepliesPerReviewSince(any(LocalDateTime.class)))
                .thenReturn(Map.of());
        when(carFavoriteService.findAllFavoriteCarIdsByUser()).thenReturn(Map.of());

        // Exercise
        final List<WeeklyDigestServiceImpl.UserDigest> digests = weeklyDigestService.buildUserDigests(since);

        // Assertions
        assertEquals(1, digests.size());
        assertTrue(digests.get(0).reviewActivities.isEmpty());
        assertTrue(digests.get(0).favoriteActivities.isEmpty());
    }

    @Test
    public void shouldSkipMissingReviewsAndFavoriteCarsWithoutNewReviews() {
        // Arrange
        final LocalDateTime since = LocalDateTime.now().minusDays(7);
        when(userService.getAllUsers()).thenReturn(List.of(user()));
        when(reviewLikeService.countNewLikesPerReviewSince(any(LocalDateTime.class)))
                .thenReturn(Map.of(REVIEW_ID, 1L));
        when(reviewReplyService.countNewRepliesPerReviewSince(any(LocalDateTime.class)))
                .thenReturn(Map.of());
        when(carFavoriteService.findAllFavoriteCarIdsByUser())
                .thenReturn(Map.of(USER_ID, List.of(FAVORITE_CAR_ID)));
        when(carService.getCarsByIds(anyCollection())).thenReturn(List.of());
        when(reviewService.countNewReviewsByCarIds(anyCollection(), any(LocalDateTime.class)))
                .thenReturn(Map.of());

        // Exercise
        final List<WeeklyDigestServiceImpl.UserDigest> digests = weeklyDigestService.buildUserDigests(since);

        // Assertions
        assertEquals(1, digests.size());
        assertTrue(digests.get(0).reviewActivities.isEmpty());
        assertTrue(digests.get(0).favoriteActivities.isEmpty());
    }
}
