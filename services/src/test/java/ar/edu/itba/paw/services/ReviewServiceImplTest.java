package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.ReviewTagDao;
import ar.edu.itba.paw.services.exception.InvalidReviewTagSelectionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {

    private static final long REVIEW_ID = 3L;
    private static final long USER_ID = 4L;
    private static final long CAR_ID = 5L;

    @Mock
    private ReviewDao reviewDao;
    @Mock
    private ReviewTagDao reviewTagDao;
    @Mock
    private ReviewTagService reviewTagService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private static Review review(final String title) {
        return TestModels.review(REVIEW_ID, USER_ID, "u@example.com", CAR_ID, new BigDecimal("4.5"),
                title, "Body", "owner", 2026, 1000, true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    public void shouldCreateReviewAndReturnRefreshedReviewWhenTagsArePresent() {
        // Arrange
        final List<Short> tagIds = List.of((short) 1, (short) 2);
        final Review created = review("Original title");
        final Review refreshed = review("Refreshed title");
        when(reviewDao.create(USER_ID, CAR_ID, new BigDecimal("4.5"), "Title", "Body", "owner", 2026, 1000, true))
                .thenReturn(created);
        when(reviewDao.findById(REVIEW_ID)).thenReturn(Optional.of(refreshed));

        // Exercise
        final Review result = reviewService.createReview(USER_ID, CAR_ID, new BigDecimal("4.5"), "Title", "Body", "owner", 2026, 1000, true, tagIds);

        // Assertions
        assertEquals(REVIEW_ID, result.getId());
        assertEquals("Refreshed title", result.getTitle());
    }

    @Test
    public void shouldPropagateInvalidTagSelectionWhenCreatingReview() {
        // Arrange
        final List<Short> tagIds = List.of((short) 99);
        when(reviewTagService.validateSelection(tagIds)).thenThrow(new InvalidReviewTagSelectionException(
                InvalidReviewTagSelectionException.Reason.UNKNOWN_TAG,
                "Unknown tag"
        ));

        // Exercise
        final InvalidReviewTagSelectionException ex = assertThrows(InvalidReviewTagSelectionException.class,
                () -> reviewService.createReview(USER_ID, CAR_ID, new BigDecimal("4.5"), "Title", "Body", "owner", 2026, 1000, true, tagIds));

        // Assertions
        assertEquals(InvalidReviewTagSelectionException.Reason.UNKNOWN_TAG, ex.getReason());
    }

    @Test
    public void shouldCreateReviewAndReturnCreatedReviewWhenTagsAreEmpty() {
        // Arrange
        final List<Short> tagIds = List.of();
        final Review created = review("Created title");
        when(reviewDao.create(USER_ID, CAR_ID, new BigDecimal("4.5"), "Title", "Body", "owner", 2026, 1000, true))
                .thenReturn(created);

        // Exercise
        final Review result = reviewService.createReview(USER_ID, CAR_ID, new BigDecimal("4.5"), "Title", "Body", "owner", 2026, 1000, true, tagIds);

        // Assertions
        assertEquals(REVIEW_ID, result.getId());
        assertEquals("Created title", result.getTitle());
    }

    @Test
    public void shouldUpdateReviewAndReturnRefreshedReviewWhenDaoUpdates() {
        // Arrange
        final List<Short> tagIds = List.of((short) 1);
        final Review updated = review("Updated title");
        final Review refreshed = review("Refreshed title");
        when(reviewDao.update(REVIEW_ID, CAR_ID, new BigDecimal("3.5"), "Updated title", "Updated body",
                "former_owner", 2020, 50000, false)).thenReturn(Optional.of(updated));
        when(reviewDao.findById(REVIEW_ID)).thenReturn(Optional.of(refreshed));

        // Exercise
        final Optional<Review> result = reviewService.updateReview(REVIEW_ID, CAR_ID, new BigDecimal("3.5"), "Updated title", "Updated body", "former_owner", 2020, 50000, false, tagIds);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("Refreshed title", result.get().getTitle());
    }

    @Test
    public void shouldReturnEmptyWhenUpdatingMissingReview() {
        // Arrange
        final List<Short> tagIds = List.of((short) 1);
        when(reviewDao.update(REVIEW_ID, CAR_ID, new BigDecimal("3.5"), "Updated title", "Updated body",
                "former_owner", 2020, 50000, false)).thenReturn(Optional.empty());

        // Exercise
        final Optional<Review> result = reviewService.updateReview(REVIEW_ID, CAR_ID, new BigDecimal("3.5"), "Updated title", "Updated body", "former_owner", 2020, 50000, false, tagIds);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldPropagateInvalidTagSelectionWhenUpdatingReview() {
        // Arrange
        final List<Short> tagIds = List.of((short) 99);
        when(reviewTagService.validateSelection(tagIds)).thenThrow(new InvalidReviewTagSelectionException(
                InvalidReviewTagSelectionException.Reason.UNKNOWN_TAG,
                "Unknown tag"
        ));

        // Exercise
        final InvalidReviewTagSelectionException ex = assertThrows(InvalidReviewTagSelectionException.class,
                () -> reviewService.updateReview(REVIEW_ID, CAR_ID, new BigDecimal("3.5"), "Updated title",
                        "Updated body", "former_owner", 2020, 50000, false, tagIds));

        // Assertions
        assertEquals(InvalidReviewTagSelectionException.Reason.UNKNOWN_TAG, ex.getReason());
    }

    @Test
    public void shouldUpdateReviewAndReturnRefreshedReviewWhenTagIdsAreNull() {
        // Arrange
        final List<Short> tagIds = null;
        final Review updated = review("Updated title");
        final Review refreshed = review("Refreshed title");
        when(reviewDao.update(REVIEW_ID, CAR_ID, new BigDecimal("3.5"), "Updated title", "Updated body",
                "former_owner", 2020, 50000, false)).thenReturn(Optional.of(updated));
        when(reviewDao.findById(REVIEW_ID)).thenReturn(Optional.of(refreshed));

        // Exercise
        final Optional<Review> result = reviewService.updateReview(REVIEW_ID, CAR_ID, new BigDecimal("3.5"), "Updated title", "Updated body", "former_owner", 2020, 50000, false, tagIds);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("Refreshed title", result.get().getTitle());
    }

    @Test
    public void shouldReturnEmptyReviewsListWhenIdsAreNull() {
        // Arrange
        final List<Long> ids = null;

        // Exercise
        final List<Review> result = reviewService.getReviewsByIds(ids);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyReviewsListWhenIdsAreEmpty() {
        // Arrange
        final List<Long> ids = List.of();

        // Exercise
        final List<Review> result = reviewService.getReviewsByIds(ids);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyStatsListWhenCarIdsAreNull() {
        // Arrange
        final List<Long> carIds = null;

        // Exercise
        final List<ReviewStats> result = reviewService.getReviewStatsByCarIds(carIds);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyStatsListWhenCarIdsAreEmpty() {
        // Arrange
        final List<Long> carIds = List.of();

        // Exercise
        final List<ReviewStats> result = reviewService.getReviewStatsByCarIds(carIds);

        // Assertions
        assertTrue(result.isEmpty());
    }
}
