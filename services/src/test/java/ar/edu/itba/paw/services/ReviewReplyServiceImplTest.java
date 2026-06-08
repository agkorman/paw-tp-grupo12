package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.ReviewReplyDao;
import ar.edu.itba.paw.persistence.UserDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewReplyServiceImplTest {

    private static final long REVIEW_ID = 20L;
    private static final long REPLY_ID = 30L;
    private static final long USER_ID = 40L;

    @Mock
    private ReviewReplyDao reviewReplyDao;
    @Mock
    private ReviewDao reviewDao;
    @Mock
    private UserDao userDao;
    @Mock
    private CarService carService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReviewReplyServiceImpl reviewReplyService;

    private static Review review() {
        return TestModels.review(REVIEW_ID, USER_ID, "u@example.com", 1L, new BigDecimal("4.0"),
                "Title", "Body", "owner", 2026, 1000, true, LocalDateTime.now(), LocalDateTime.now());
    }

    private static User user() {
        return TestModels.user(USER_ID, "user", "u@example.com", "p", "user", LocalDateTime.now());
    }

    private static ReviewReply reply(final long userId) {
        return TestModels.reviewReply(REPLY_ID, REVIEW_ID, userId, "user", "Reply body",
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    public void shouldCreateReplyWithTrimmedBodyWhenReviewAndUserExist() {
        // Arrange
        final ReviewReply created = reply(USER_ID);
        when(reviewDao.findById(REVIEW_ID)).thenReturn(Optional.of(review()));
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(reviewReplyDao.create(REVIEW_ID, USER_ID, "Reply body")).thenReturn(created);

        // Exercise
        final ReviewReply result = reviewReplyService.createReply(REVIEW_ID, USER_ID, "  Reply body  ");

        // Assertions
        assertEquals(REPLY_ID, result.getId());
        assertEquals("Reply body", result.getBody());
    }

    @Test
    public void shouldRejectCreateReplyWhenBodyIsTooLong() {
        // Arrange
        final String body = "a".repeat(ReviewReplyServiceImpl.MAX_BODY_LENGTH + 1);
        when(reviewDao.findById(REVIEW_ID)).thenReturn(Optional.of(review()));
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(user()));

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reviewReplyService.createReply(REVIEW_ID, USER_ID, body));

        // Assertions
        assertEquals("Reply body is too long.", ex.getMessage());
    }

    @Test
    public void shouldWrapDaoFailureWhenCreatingReply() {
        // Arrange
        when(reviewDao.findById(REVIEW_ID)).thenReturn(Optional.of(review()));
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(reviewReplyDao.create(REVIEW_ID, USER_ID, "Reply body")).thenThrow(new DataAccessResourceFailureException("db"));

        // Exercise
        final IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> reviewReplyService.createReply(REVIEW_ID, USER_ID, "Reply body"));

        // Assertions
        assertEquals("Failed to create review reply.", ex.getMessage());
    }

    @Test
    public void shouldRejectDeleteReplyWhenUserDoesNotOwnReply() {
        // Arrange
        final long requestingUserId = 99L;
        when(reviewReplyDao.findById(REPLY_ID)).thenReturn(Optional.of(reply(USER_ID)));

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reviewReplyService.deleteReply(REPLY_ID, requestingUserId));

        // Assertions
        assertEquals("Review reply 30 does not belong to user 99", ex.getMessage());
    }

    @Test
    public void shouldDeleteReplyWhenUserOwnsReply() {
        // Arrange
        when(reviewReplyDao.findById(REPLY_ID)).thenReturn(Optional.of(reply(USER_ID)));
        when(reviewReplyDao.delete(REPLY_ID)).thenReturn(true);

        // Exercise
        final boolean result = reviewReplyService.deleteReply(REPLY_ID, USER_ID);

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldUpdateReplyWithTrimmedBodyWhenUserOwnsReply() {
        // Arrange
        when(reviewReplyDao.findById(REPLY_ID)).thenReturn(Optional.of(reply(USER_ID)));
        when(reviewReplyDao.update(REPLY_ID, "Updated body")).thenReturn(true);

        // Exercise
        final boolean result = reviewReplyService.updateReply(REPLY_ID, USER_ID, "  Updated body  ");

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldRejectUpdateReplyWhenBodyIsBlank() {
        // Arrange
        final String blankBody = "   ";
        when(reviewReplyDao.findById(REPLY_ID)).thenReturn(Optional.of(reply(USER_ID)));

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reviewReplyService.updateReply(REPLY_ID, USER_ID, blankBody));

        // Assertions
        assertEquals("Reply body is required.", ex.getMessage());
    }

    @Test
    public void shouldRejectUpdateReplyWhenUserDoesNotOwnReply() {
        // Arrange
        final long requestingUserId = 99L;
        when(reviewReplyDao.findById(REPLY_ID)).thenReturn(Optional.of(reply(USER_ID)));

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reviewReplyService.updateReply(REPLY_ID, requestingUserId, "Updated body"));

        // Assertions
        assertEquals("Review reply 30 does not belong to user 99", ex.getMessage());
    }

    @Test
    public void shouldHideReplyWhenReplyAndReviewExist() {
        // Arrange
        when(reviewReplyDao.findById(REPLY_ID)).thenReturn(Optional.of(reply(USER_ID)));
        when(reviewDao.findById(REVIEW_ID)).thenReturn(Optional.of(review()));
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(carService.getCarById(1L)).thenReturn(Optional.of(TestModels.car(1L, 2L, "Brand", "Model",
                3L, "Sedan", "Description", LocalDateTime.now())));
        when(reviewReplyDao.delete(REPLY_ID)).thenReturn(true);

        // Exercise
        final boolean result = reviewReplyService.hideReply(REPLY_ID, "Moderation reason");

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenHidingMissingReply() {
        // Arrange
        when(reviewReplyDao.findById(REPLY_ID)).thenReturn(Optional.empty());

        // Exercise
        final boolean result = reviewReplyService.hideReply(REPLY_ID, "Moderation reason");

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldReturnEmptyGroupedRepliesWhenDaoThrows() {
        // Arrange
        when(reviewReplyDao.findFirstNByReviewIds(List.of(REVIEW_ID), 3)).thenThrow(new DataAccessResourceFailureException("db"));

        // Exercise
        final Map<Long, List<ReviewReply>> result = reviewReplyService.getFirstNRepliesByReviewIds(List.of(REVIEW_ID), 3);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyWhenDaoThrowsForGetReplyById() {
        // Arrange
        when(reviewReplyDao.findById(REPLY_ID)).thenThrow(new DataAccessResourceFailureException("db"));

        // Exercise
        final Optional<ReviewReply> result = reviewReplyService.getReplyById(REPLY_ID);

        // Assertions
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldReturnEmptyListForNullReplyIds() {
        // Arrange
        final List<Long> ids = null;

        // Exercise
        final List<ReviewReply> result = reviewReplyService.getRepliesByIds(ids);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyListWhenDaoThrowsForGetRepliesByIds() {
        // Arrange
        when(reviewReplyDao.findByIds(anyCollection())).thenThrow(new DataAccessResourceFailureException("db"));

        // Exercise
        final List<ReviewReply> result = reviewReplyService.getRepliesByIds(List.of(REPLY_ID));

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyPageWhenDaoThrowsForGetRepliesByReview() {
        // Arrange
        when(reviewReplyDao.findByReviewId(REVIEW_ID, 1)).thenThrow(new DataAccessResourceFailureException("db"));

        // Exercise
        final ar.edu.itba.paw.model.Page<ReviewReply> result = reviewReplyService.getRepliesByReview(REVIEW_ID, 1);

        // Assertions
        assertTrue(result.getItems().isEmpty());
        assertEquals(0L, result.getTotalItems());
    }

    @Test
    public void shouldReturnEmptyMapWhenSinceIsNull() {
        // Arrange
        final LocalDateTime since = null;

        // Exercise
        final Map<Long, Long> result = reviewReplyService.countNewRepliesPerReview(USER_ID, since);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldSwallowDaoFailureForCountNewRepliesPerReview() {
        // Arrange
        final LocalDateTime since = LocalDateTime.now().minusDays(1);
        when(reviewReplyDao.countNewRepliesPerReview(USER_ID, since)).thenThrow(new DataAccessResourceFailureException("db"));

        // Exercise
        final Map<Long, Long> result = reviewReplyService.countNewRepliesPerReview(USER_ID, since);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldRejectCreateReplyWhenBodyIsBlank() {
        // Arrange
        final String blankBody = "   ";
        when(reviewDao.findById(REVIEW_ID)).thenReturn(Optional.of(review()));
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(user()));

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reviewReplyService.createReply(REVIEW_ID, USER_ID, blankBody));

        // Assertions
        assertEquals("Reply body is required.", ex.getMessage());
    }

    @Test
    public void shouldRejectDeleteReplyWhenReplyNotFound() {
        // Arrange
        when(reviewReplyDao.findById(REPLY_ID)).thenReturn(Optional.empty());

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reviewReplyService.deleteReply(REPLY_ID, USER_ID));

        // Assertions
        assertEquals("Review reply not found: " + REPLY_ID, ex.getMessage());
    }
}
