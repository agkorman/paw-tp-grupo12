package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.ReviewTag;
import ar.edu.itba.paw.persistence.ReviewTagDao;
import ar.edu.itba.paw.services.exception.InvalidReviewTagSelectionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewTagServiceImplTest {

    @Mock
    private ReviewTagDao reviewTagDao;

    @InjectMocks
    private ReviewTagServiceImpl reviewTagService;

    private static ReviewTag tag(final short id, final String code, final String sentiment, final String dimension) {
        return new ReviewTag(id, code, "label-" + code, sentiment, dimension, LocalDateTime.now());
    }

    @Test
    public void shouldReturnEmptyListWhenValidatingNullSelection() {
        // Arrange

        // Exercise
        final List<ReviewTag> result = reviewTagService.validateSelection(null);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyListWhenValidatingEmptySelection() {
        // Arrange

        // Exercise
        final List<ReviewTag> result = reviewTagService.validateSelection(List.of());

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnResolvedTagsWhenSelectionIsValid() {
        // Arrange
        final ReviewTag t1 = tag((short) 1, "comfort", ReviewTag.SENTIMENT_POSITIVE, "comfort-dim");
        final ReviewTag t2 = tag((short) 2, "agile", ReviewTag.SENTIMENT_POSITIVE, "performance-dim");
        when(reviewTagDao.findByIds(anyCollection())).thenReturn(List.of(t1, t2));

        // Exercise
        final List<ReviewTag> result = reviewTagService.validateSelection(List.of((short) 1, (short) 2));

        // Assertions
        assertEquals(2, result.size());
        assertEquals("comfort", result.get(0).getCode());
        assertEquals("agile", result.get(1).getCode());
    }

    @Test
    public void shouldRejectSelectionExceedingMaxTags() {
        // Arrange
        final List<Short> tooMany = List.of((short) 1, (short) 2, (short) 3, (short) 4, (short) 5, (short) 6, (short) 7);

        // Exercise
        final InvalidReviewTagSelectionException ex = assertThrows(InvalidReviewTagSelectionException.class,
                () -> reviewTagService.validateSelection(tooMany));

        // Assertions
        assertEquals(InvalidReviewTagSelectionException.Reason.TOO_MANY, ex.getReason());
    }

    @Test
    public void shouldRejectSelectionWithUnknownTag() {
        // Arrange
        final ReviewTag t1 = tag((short) 1, "comfort", ReviewTag.SENTIMENT_POSITIVE, "comfort-dim");
        when(reviewTagDao.findByIds(anyCollection())).thenReturn(List.of(t1));

        // Exercise
        final InvalidReviewTagSelectionException ex = assertThrows(InvalidReviewTagSelectionException.class,
                () -> reviewTagService.validateSelection(List.of((short) 1, (short) 99)));

        // Assertions
        assertEquals(InvalidReviewTagSelectionException.Reason.UNKNOWN_TAG, ex.getReason());
    }

    @Test
    public void shouldRejectSelectionWithDuplicateDimension() {
        // Arrange
        final ReviewTag positive = tag((short) 1, "comfortable", ReviewTag.SENTIMENT_POSITIVE, "comfort-dim");
        final ReviewTag negative = tag((short) 2, "uncomfortable", ReviewTag.SENTIMENT_NEGATIVE, "comfort-dim");
        when(reviewTagDao.findByIds(anyCollection())).thenReturn(List.of(positive, negative));

        // Exercise
        final InvalidReviewTagSelectionException ex = assertThrows(InvalidReviewTagSelectionException.class,
                () -> reviewTagService.validateSelection(List.of((short) 1, (short) 2)));

        // Assertions
        assertEquals(InvalidReviewTagSelectionException.Reason.DUPLICATE_DIMENSION, ex.getReason());
    }

    @Test
    public void shouldDeduplicateRepeatedIdsBeforeCountingMax() {
        // Arrange — 7 raw ids but only 1 unique
        final ReviewTag t1 = tag((short) 1, "comfort", ReviewTag.SENTIMENT_POSITIVE, "comfort-dim");
        when(reviewTagDao.findByIds(anyCollection())).thenReturn(List.of(t1));
        final List<Short> repeated = List.of((short) 1, (short) 1, (short) 1, (short) 1, (short) 1, (short) 1, (short) 1);

        // Exercise
        final List<ReviewTag> result = reviewTagService.validateSelection(repeated);

        // Assertions
        assertEquals(1, result.size());
        assertEquals("comfort", result.get(0).getCode());
    }

    @Test
    public void shouldGroupTagsBySentimentPreservingPositiveAndNegativeBuckets() {
        // Arrange
        final ReviewTag positive = tag((short) 1, "comfort", ReviewTag.SENTIMENT_POSITIVE, "comfort-dim");
        final ReviewTag negative = tag((short) 2, "uncomfortable", ReviewTag.SENTIMENT_NEGATIVE, "noise-dim");
        when(reviewTagDao.findAll()).thenReturn(List.of(positive, negative));

        // Exercise
        final Map<String, List<ReviewTag>> grouped = reviewTagService.getAllGroupedBySentiment();

        // Assertions
        assertEquals(2, grouped.size());
        assertEquals(List.of(positive), grouped.get(ReviewTag.SENTIMENT_POSITIVE));
        assertEquals(List.of(negative), grouped.get(ReviewTag.SENTIMENT_NEGATIVE));
    }

    @Test
    public void shouldReturnEmptyBucketsWhenNoTagsExist() {
        // Arrange
        when(reviewTagDao.findAll()).thenReturn(List.of());

        // Exercise
        final Map<String, List<ReviewTag>> grouped = reviewTagService.getAllGroupedBySentiment();

        // Assertions
        assertTrue(grouped.get(ReviewTag.SENTIMENT_POSITIVE).isEmpty());
        assertTrue(grouped.get(ReviewTag.SENTIMENT_NEGATIVE).isEmpty());
    }

    @Test
    public void shouldReturnAllTagsFromDao() {
        // Arrange
        final List<ReviewTag> tags = List.of(tag((short) 1, "c", ReviewTag.SENTIMENT_POSITIVE, "d"));
        when(reviewTagDao.findAll()).thenReturn(tags);

        // Exercise
        final List<ReviewTag> result = reviewTagService.getAll();

        // Assertions
        assertSame(tags, result);
    }

    @Test
    public void shouldAcceptExactlyMaxUniqueTags() {
        // Arrange
        final List<ReviewTag> resolved = List.of(
                tag((short) 1, "a", ReviewTag.SENTIMENT_POSITIVE, "d1"),
                tag((short) 2, "b", ReviewTag.SENTIMENT_POSITIVE, "d2"),
                tag((short) 3, "c", ReviewTag.SENTIMENT_POSITIVE, "d3"),
                tag((short) 4, "d", ReviewTag.SENTIMENT_POSITIVE, "d4"),
                tag((short) 5, "e", ReviewTag.SENTIMENT_POSITIVE, "d5"),
                tag((short) 6, "f", ReviewTag.SENTIMENT_POSITIVE, "d6")
        );
        final Set<Short> ids = new HashSet<>(List.of((short) 1, (short) 2, (short) 3, (short) 4, (short) 5, (short) 6));
        when(reviewTagDao.findByIds(anyCollection())).thenReturn(resolved);

        // Exercise
        final List<ReviewTag> result = reviewTagService.validateSelection(ids);

        // Assertions
        assertEquals(6, result.size());
    }
}
