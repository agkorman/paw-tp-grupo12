package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.ReviewLikeDao;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewLikeServiceImplTest {

    private static final long REVIEW_ID = 100L;
    private static final long REPLY_ID = 200L;
    private static final long USER_ID = 7L;

    @Mock
    private ReviewLikeDao reviewLikeDao;
    @Mock
    private ReviewDao reviewDao;
    @Mock
    private ReviewReplyDao reviewReplyDao;
    @Mock
    private UserDao userDao;

    @InjectMocks
    private ReviewLikeServiceImpl reviewLikeService;

    private static Review review() {
        return TestModels.review(REVIEW_ID, USER_ID, "u@x.com", 1L, new BigDecimal("4.0"),
                "title", "body", null, null, null, true, LocalDateTime.now(), LocalDateTime.now());
    }

    private static ReviewReply reply() {
        return TestModels.reviewReply(REPLY_ID, REVIEW_ID, USER_ID, "u", "body",
                LocalDateTime.now(), LocalDateTime.now());
    }

    private static User user() {
        return TestModels.user(USER_ID, "u", "u@x.com", "p", "user", LocalDateTime.now());
    }

    @Test
    public void shouldLikeReviewWhenNotPreviouslyLiked() {
        // Arrange
        when(reviewDao.findById(REVIEW_ID)).thenReturn(java.util.Optional.of(review()));
        when(userDao.findById(USER_ID)).thenReturn(java.util.Optional.of(user()));
        when(reviewLikeDao.isReviewLikedByUser(REVIEW_ID, USER_ID)).thenReturn(false);

        // Exercise
        final boolean result = reviewLikeService.toggleReviewLike(REVIEW_ID, USER_ID);

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldUnlikeReviewWhenPreviouslyLiked() {
        // Arrange
        when(reviewDao.findById(REVIEW_ID)).thenReturn(java.util.Optional.of(review()));
        when(userDao.findById(USER_ID)).thenReturn(java.util.Optional.of(user()));
        when(reviewLikeDao.isReviewLikedByUser(REVIEW_ID, USER_ID)).thenReturn(true);

        // Exercise
        final boolean result = reviewLikeService.toggleReviewLike(REVIEW_ID, USER_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldRejectToggleReviewLikeWhenReviewMissing() {
        // Arrange
        when(reviewDao.findById(REVIEW_ID)).thenReturn(java.util.Optional.empty());

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reviewLikeService.toggleReviewLike(REVIEW_ID, USER_ID));

        // Assertions
        assertEquals("Review not found: 100", ex.getMessage());
    }

    @Test
    public void shouldRejectToggleReviewLikeWhenUserMissing() {
        // Arrange
        when(reviewDao.findById(REVIEW_ID)).thenReturn(java.util.Optional.of(review()));
        when(userDao.findById(USER_ID)).thenReturn(java.util.Optional.empty());

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reviewLikeService.toggleReviewLike(REVIEW_ID, USER_ID));

        // Assertions
        assertEquals("User not found: 7", ex.getMessage());
    }

    @Test
    public void shouldWrapDaoFailureAsIllegalStateOnToggleReviewLike() {
        // Arrange
        when(reviewDao.findById(REVIEW_ID)).thenReturn(java.util.Optional.of(review()));
        when(userDao.findById(USER_ID)).thenReturn(java.util.Optional.of(user()));
        when(reviewLikeDao.isReviewLikedByUser(REVIEW_ID, USER_ID)).thenThrow(new DataAccessResourceFailureException("db"));

        // Exercise
        final IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> reviewLikeService.toggleReviewLike(REVIEW_ID, USER_ID));

        // Assertions
        assertEquals("Failed to toggle review like.", ex.getMessage());
    }

    @Test
    public void shouldLikeReplyWhenNotPreviouslyLiked() {
        // Arrange
        when(reviewReplyDao.findById(REPLY_ID)).thenReturn(java.util.Optional.of(reply()));
        when(userDao.findById(USER_ID)).thenReturn(java.util.Optional.of(user()));
        when(reviewLikeDao.isReplyLikedByUser(REPLY_ID, USER_ID)).thenReturn(false);

        // Exercise
        final boolean result = reviewLikeService.toggleReplyLike(REPLY_ID, USER_ID);

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldUnlikeReplyWhenPreviouslyLiked() {
        // Arrange
        when(reviewReplyDao.findById(REPLY_ID)).thenReturn(java.util.Optional.of(reply()));
        when(userDao.findById(USER_ID)).thenReturn(java.util.Optional.of(user()));
        when(reviewLikeDao.isReplyLikedByUser(REPLY_ID, USER_ID)).thenReturn(true);

        // Exercise
        final boolean result = reviewLikeService.toggleReplyLike(REPLY_ID, USER_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldRejectToggleReplyLikeWhenReplyMissing() {
        // Arrange
        when(reviewReplyDao.findById(REPLY_ID)).thenReturn(java.util.Optional.empty());

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reviewLikeService.toggleReplyLike(REPLY_ID, USER_ID));

        // Assertions
        assertEquals("Review reply not found: 200", ex.getMessage());
    }

    @Test
    public void shouldWrapDaoFailureAsIllegalStateOnToggleReplyLike() {
        // Arrange
        when(reviewReplyDao.findById(REPLY_ID)).thenReturn(java.util.Optional.of(reply()));
        when(userDao.findById(USER_ID)).thenReturn(java.util.Optional.of(user()));
        when(reviewLikeDao.isReplyLikedByUser(REPLY_ID, USER_ID)).thenThrow(new DataAccessResourceFailureException("db"));

        // Exercise
        final IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> reviewLikeService.toggleReplyLike(REPLY_ID, USER_ID));

        // Assertions
        assertEquals("Failed to toggle reply like.", ex.getMessage());
    }

    @Test
    public void shouldReturnEmptyMapWhenReviewIdsCollectionIsNull() {
        // Arrange
        final Collection<Long> reviewIds = null;

        // Exercise
        final Map<Long, Long> result = reviewLikeService.countReviewLikesByReviewIds(reviewIds);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyMapWhenReviewIdsCollectionIsEmpty() {
        // Arrange
        final Collection<Long> reviewIds = List.of();

        // Exercise
        final Map<Long, Long> result = reviewLikeService.countReviewLikesByReviewIds(reviewIds);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyMapWhenDaoThrowsForReviewLikeCounts() {
        // Arrange
        when(reviewLikeDao.countReviewLikesByReviewIds(anyCollection())).thenThrow(new DataAccessResourceFailureException("db"));

        // Exercise
        final Map<Long, Long> result = reviewLikeService.countReviewLikesByReviewIds(List.of(1L, 2L));

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldDelegateLikedReviewIdsLookup() {
        // Arrange
        final Collection<Long> ids = List.of(1L, 2L, 3L);
        when(reviewLikeDao.findLikedReviewIds(ids, USER_ID)).thenReturn(Set.of(1L, 3L));

        // Exercise
        final Set<Long> result = reviewLikeService.getLikedReviewIds(ids, USER_ID);

        // Assertions
        assertEquals(Set.of(1L, 3L), result);
    }

    @Test
    public void shouldReturnEmptySetForLikedReviewIdsWithNullCollection() {
        // Arrange
        final Collection<Long> reviewIds = null;

        // Exercise
        final Set<Long> result = reviewLikeService.getLikedReviewIds(reviewIds, USER_ID);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldSwallowDaoFailureForLikedReviewIds() {
        // Arrange
        when(reviewLikeDao.findLikedReviewIds(anyCollection(), anyLong())).thenThrow(new DataAccessResourceFailureException("db"));

        // Exercise
        final Set<Long> result = reviewLikeService.getLikedReviewIds(List.of(1L), USER_ID);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyMapWhenSinceIsNull() {
        // Arrange
        final LocalDateTime since = null;

        // Exercise
        final Map<Long, Long> result = reviewLikeService.countNewLikesPerReview(USER_ID, since);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyPageWhenDaoThrowsForPagedLikedReviewIdsByUser() {
        // Arrange
        when(reviewLikeDao.findLikedReviewIdsByUserId(USER_ID, 1)).thenThrow(new DataAccessResourceFailureException("db"));

        // Exercise
        final Page<Long> result = reviewLikeService.getLikedReviewIdsByUser(USER_ID, 1);

        // Assertions
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    public void shouldReturnZeroWhenDaoThrowsForCountLikedReviewsByUser() {
        // Arrange
        when(reviewLikeDao.countLikedReviewsByUserId(USER_ID)).thenThrow(new DataAccessResourceFailureException("db"));

        // Exercise
        final long result = reviewLikeService.countLikedReviewsByUser(USER_ID);

        // Assertions
        assertEquals(0L, result);
    }

    @Test
    public void shouldReturnEmptyMapWhenReplyIdsCollectionIsNull() {
        // Arrange
        final Collection<Long> replyIds = null;

        // Exercise
        final Map<Long, Long> result = reviewLikeService.countReplyLikesByReplyIds(replyIds);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyMapWhenDaoThrowsForReplyLikeCounts() {
        // Arrange
        when(reviewLikeDao.countReplyLikesByReplyIds(anyCollection())).thenThrow(new DataAccessResourceFailureException("db"));

        // Exercise
        final Map<Long, Long> result = reviewLikeService.countReplyLikesByReplyIds(List.of(1L, 2L));

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptySetForLikedReplyIdsWithNullCollection() {
        // Arrange
        final Collection<Long> replyIds = null;

        // Exercise
        final Set<Long> result = reviewLikeService.getLikedReplyIds(replyIds, USER_ID);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldSwallowDaoFailureForLikedReplyIds() {
        // Arrange
        when(reviewLikeDao.findLikedReplyIds(anyCollection(), anyLong())).thenThrow(new DataAccessResourceFailureException("db"));

        // Exercise
        final Set<Long> result = reviewLikeService.getLikedReplyIds(List.of(1L), USER_ID);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldDelegateCountNewLikesPerReview() {
        // Arrange
        final LocalDateTime since = LocalDateTime.now().minusDays(1);
        when(reviewLikeDao.countNewLikesPerReview(USER_ID, since)).thenReturn(Map.of(1L, 2L));

        // Exercise
        final Map<Long, Long> result = reviewLikeService.countNewLikesPerReview(USER_ID, since);

        // Assertions
        assertEquals(Map.of(1L, 2L), result);
    }
}
