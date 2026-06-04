package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
    public void shouldSendModeratorDigestWithPendingRequestCount() {
        // Arrange
        final List<List<EmailRecipient>> capturedRecipients = new ArrayList<>();
        final List<Long> capturedPendingCounts = new ArrayList<>();
        final List<EmailRecipient> moderatorRecipients = List.of(new EmailRecipient("mod@example.com", "es"));
        when(userService.getModeratorEmailRecipients()).thenReturn(moderatorRecipients);
        when(carRequestService.countCarRequestsByStatus(CarRequestService.STATUS_PENDING))
                .thenReturn(2L);
        when(userService.getAllUsers()).thenReturn(List.of());
        doAnswer(invocation -> {
            capturedRecipients.add(invocation.getArgument(0));
            capturedPendingCounts.add(invocation.getArgument(1));
            return null;
        }).when(emailService).sendWeeklyModeratorDigest(moderatorRecipients, 2L);

        // Exercise
        weeklyDigestService.sendWeeklyDigest();

        // Assertions
        assertEquals(List.of(moderatorRecipients), capturedRecipients);
        assertEquals(List.of(2L), capturedPendingCounts);
    }

    @Test
    public void shouldBuildUserDigestFromReviewAndFavoriteActivity() {
        // Arrange
        final List<List<EmailService.ReviewActivityItem>> capturedReviewActivity = new ArrayList<>();
        final List<List<EmailService.FavoriteActivityItem>> capturedFavoriteActivity = new ArrayList<>();
        final LocalDateTime recent = LocalDateTime.now().minusDays(1);
        when(userService.getModeratorEmailRecipients()).thenReturn(List.of());
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
        doAnswer(invocation -> {
            capturedReviewActivity.add(invocation.getArgument(2));
            capturedFavoriteActivity.add(invocation.getArgument(3));
            return null;
        }).when(emailService).sendWeeklyUserDigest(eq(USER_EMAIL), eq(USERNAME), anyList(), anyList());

        // Exercise
        weeklyDigestService.sendWeeklyDigest();

        // Assertions
        assertEquals(1, capturedReviewActivity.size());
        assertEquals(2, capturedReviewActivity.get(0).size());
        assertTrue(capturedReviewActivity.get(0).stream().anyMatch(item ->
                "Daily driver".equals(item.reviewTitle)
                        && "Toyota Corolla".equals(item.carName)
                        && item.newLikes == 3
                        && item.newReplies == 1));
        assertTrue(capturedReviewActivity.get(0).stream().anyMatch(item ->
                "Road trip".equals(item.reviewTitle)
                        && "un auto".equals(item.carName)
                        && item.newLikes == 0
                        && item.newReplies == 2));
        assertEquals(1, capturedFavoriteActivity.get(0).size());
        assertEquals("Honda Civic", capturedFavoriteActivity.get(0).get(0).carName);
        assertEquals(1L, capturedFavoriteActivity.get(0).get(0).newReviewCount);
    }

    @Test
    public void shouldBuildEmptyDigestWhenNoReviewActivityAndNoFavoriteCars() {
        // Arrange
        final List<List<EmailService.ReviewActivityItem>> capturedReviewActivity = new ArrayList<>();
        final List<List<EmailService.FavoriteActivityItem>> capturedFavoriteActivity = new ArrayList<>();
        when(userService.getModeratorEmailRecipients()).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of(user()));
        when(reviewLikeService.countNewLikesPerReviewSince(any(LocalDateTime.class)))
                .thenReturn(Map.of());
        when(reviewReplyService.countNewRepliesPerReviewSince(any(LocalDateTime.class)))
                .thenReturn(Map.of());
        when(carFavoriteService.findAllFavoriteCarIdsByUser()).thenReturn(Map.of());
        doAnswer(invocation -> {
            capturedReviewActivity.add(invocation.getArgument(2));
            capturedFavoriteActivity.add(invocation.getArgument(3));
            return null;
        }).when(emailService).sendWeeklyUserDigest(eq(USER_EMAIL), eq(USERNAME), anyList(), anyList());

        // Exercise
        weeklyDigestService.sendWeeklyDigest();

        // Assertions
        assertEquals(1, capturedReviewActivity.size());
        assertTrue(capturedReviewActivity.get(0).isEmpty());
        assertTrue(capturedFavoriteActivity.get(0).isEmpty());
    }

    @Test
    public void shouldSkipMissingReviewsAndFavoriteCarsWithoutNewReviews() {
        // Arrange
        final List<List<EmailService.ReviewActivityItem>> capturedReviewActivity = new ArrayList<>();
        final List<List<EmailService.FavoriteActivityItem>> capturedFavoriteActivity = new ArrayList<>();
        when(userService.getModeratorEmailRecipients()).thenReturn(List.of());
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
        doAnswer(invocation -> {
            capturedReviewActivity.add(invocation.getArgument(2));
            capturedFavoriteActivity.add(invocation.getArgument(3));
            return null;
        }).when(emailService).sendWeeklyUserDigest(eq(USER_EMAIL), eq(USERNAME), anyList(), anyList());

        // Exercise
        weeklyDigestService.sendWeeklyDigest();

        // Assertions
        assertEquals(1, capturedReviewActivity.size());
        assertTrue(capturedReviewActivity.get(0).isEmpty());
        assertTrue(capturedFavoriteActivity.get(0).isEmpty());
    }
}
