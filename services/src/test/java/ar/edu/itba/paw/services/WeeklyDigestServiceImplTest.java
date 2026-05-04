package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarRequest;
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
        return new User(USER_ID, USERNAME, USER_EMAIL, "p", "user", LocalDateTime.now());
    }

    private static Car car(final long id, final String brand, final String model) {
        return new Car(id, 1L, brand, model, 2L, "sedan", "desc", LocalDateTime.now());
    }

    private static Review review(final long id, final long carId, final String title,
                                 final LocalDateTime createdAt) {
        return new Review(id, USER_ID, USER_EMAIL, carId, new BigDecimal("4.0"), title, "body",
                "owner", 2026, 1000, true, createdAt, createdAt);
    }

    @Test
    public void shouldSendModeratorDigestWithPendingRequestCount() {
        // Arrange
        final List<List<String>> capturedRecipients = new ArrayList<>();
        final List<Integer> capturedPendingCounts = new ArrayList<>();
        when(userService.getModeratorsEmails()).thenReturn(List.of("mod@example.com"));
        when(carRequestService.getCarRequestsByStatus(CarRequestService.STATUS_PENDING))
                .thenReturn(List.of(new CarRequest(), new CarRequest()));
        when(userService.getAllUsers()).thenReturn(List.of());
        doAnswer(invocation -> {
            capturedRecipients.add(invocation.getArgument(0));
            capturedPendingCounts.add(invocation.getArgument(1));
            return null;
        }).when(emailService).sendWeeklyModeratorDigest(List.of("mod@example.com"), 2);

        // Exercise
        weeklyDigestService.sendWeeklyDigest();

        // Assertions
        assertEquals(List.of(List.of("mod@example.com")), capturedRecipients);
        assertEquals(List.of(2), capturedPendingCounts);
    }

    @Test
    public void shouldBuildUserDigestFromReviewAndFavoriteActivity() {
        // Arrange
        final List<List<EmailService.ReviewActivityItem>> capturedReviewActivity = new ArrayList<>();
        final List<List<EmailService.FavoriteActivityItem>> capturedFavoriteActivity = new ArrayList<>();
        final LocalDateTime recent = LocalDateTime.now().minusDays(1);
        final LocalDateTime old = LocalDateTime.now().minusDays(10);
        when(userService.getModeratorsEmails()).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of(user()));
        when(reviewLikeService.countNewLikesPerReview(eq(USER_ID), any(LocalDateTime.class)))
                .thenReturn(Map.of(REVIEW_ID, 3L));
        when(reviewReplyService.countNewRepliesPerReview(eq(USER_ID), any(LocalDateTime.class)))
                .thenReturn(Map.of(REVIEW_ID, 1L, SECOND_REVIEW_ID, 2L));
        when(reviewService.getReviewsByIds(anyCollection()))
                .thenReturn(List.of(
                        review(REVIEW_ID, CAR_ID, "Daily driver", recent),
                        review(SECOND_REVIEW_ID, 99L, "Road trip", recent)
                ));
        when(carService.getCarsByIds(argThat(c -> c != null && c.contains(CAR_ID))))
                .thenReturn(List.of(car(CAR_ID, "Toyota", "Corolla")));
        when(carFavoriteService.findFavoriteCarIdsByUser(USER_ID)).thenReturn(List.of(FAVORITE_CAR_ID));
        when(carService.getCarsByIds(argThat(c -> c != null && c.contains(FAVORITE_CAR_ID))))
                .thenReturn(List.of(car(FAVORITE_CAR_ID, "Honda", "Civic")));
        when(reviewService.getReviewsByCarIds(anyCollection()))
                .thenReturn(List.of(
                        review(40L, FAVORITE_CAR_ID, "New favorite review", recent),
                        review(41L, FAVORITE_CAR_ID, "Old favorite review", old),
                        review(42L, FAVORITE_CAR_ID, "Undated favorite review", null)
                ));
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
        when(userService.getModeratorsEmails()).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of(user()));
        when(reviewLikeService.countNewLikesPerReview(eq(USER_ID), any(LocalDateTime.class)))
                .thenReturn(Map.of());
        when(reviewReplyService.countNewRepliesPerReview(eq(USER_ID), any(LocalDateTime.class)))
                .thenReturn(Map.of());
        when(carFavoriteService.findFavoriteCarIdsByUser(USER_ID)).thenReturn(List.of());
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
        when(userService.getModeratorsEmails()).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of(user()));
        when(reviewLikeService.countNewLikesPerReview(eq(USER_ID), any(LocalDateTime.class)))
                .thenReturn(Map.of(REVIEW_ID, 1L));
        when(reviewReplyService.countNewRepliesPerReview(eq(USER_ID), any(LocalDateTime.class)))
                .thenReturn(Map.of());
        when(carFavoriteService.findFavoriteCarIdsByUser(USER_ID)).thenReturn(List.of(FAVORITE_CAR_ID));
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
