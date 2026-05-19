package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarRecommendation;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.ReviewTag;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.ReviewTagDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecommendationServiceImplTest {

    @Mock
    private CarService carService;
    @Mock
    private ReviewTagService reviewTagService;
    @Mock
    private ReviewTagDao reviewTagDao;
    @Mock
    private ReviewDao reviewDao;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private static final short COMFORT_TAG_ID = 1;
    private static final short UNCOMFORT_TAG_ID = 2;
    private static final short BIG_TRUNK_TAG_ID = 3;
    private static final short SMALL_TRUNK_TAG_ID = 4;

    private static Car car(final long id) {
        return new Car(id, 1L, "Brand-" + id, "Model-" + id, 2L, 2024, "sedan",
                "desc", LocalDateTime.now(), false, "GASOLINE", 100, 6, "MANUAL",
                new BigDecimal("6.0"), 180, new BigDecimal("20000.00"));
    }

    private static ReviewTag tag(final short id, final String code) {
        return new ReviewTag(id, code, "label-" + code, ReviewTag.SENTIMENT_POSITIVE, code + "-dim",
                LocalDateTime.now());
    }

    private static List<ReviewTag> allTags() {
        return List.of(
                tag(COMFORT_TAG_ID, "comfortable"),
                tag(UNCOMFORT_TAG_ID, "uncomfortable"),
                tag(BIG_TRUNK_TAG_ID, "big_trunk"),
                tag(SMALL_TRUNK_TAG_ID, "small_trunk")
        );
    }

    private static ReviewStats stats(final long carId, final long reviewCount) {
        return new ReviewStats(carId, reviewCount, new BigDecimal("4.0"));
    }

    private void mockSinglePageOfCars(final List<Car> cars) {
        final Page<Car> page = new Page<>(cars, 1, Math.max(cars.size(), 1), cars.size());
        when(carService.searchCars(any(CarSearchCriteria.class))).thenReturn(page);
    }

    @Test
    public void shouldReturnQuestionnaireQuestions() {
        // Arrange
        final String expectedQuestionId = "driving";

        // Exercise
        final List<RecommendationQuestion> questions = recommendationService.getQuestions();

        // Assertions
        assertFalse(questions.isEmpty());
        assertTrue(questions.stream().anyMatch(q -> expectedQuestionId.equals(q.getId())));
    }

    @Test
    public void shouldReturnEmptyWhenCriteriaIsNull() {
        // Arrange
        final RecommendationCriteria criteria = null;
        final int limit = 5;

        // Exercise
        final List<CarRecommendation> result = recommendationService.recommend(criteria, limit);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyWhenAnswersAreEmpty() {
        // Arrange
        final RecommendationCriteria criteria = new RecommendationCriteria(Map.of(), null, null);

        // Exercise
        final List<CarRecommendation> result = recommendationService.recommend(criteria, 5);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyWhenWeightsCancelOut() {
        // Arrange — fuelEconomy "very" gives +1.0/-1.0 for two tags; an opposite combination
        // is not directly possible, so simulate cancellation by selecting the "any"/"not" answers
        // which produce empty weights. Driving=any + firstCar=no => no weights at all.
        final Map<String, String> answers = new HashMap<>();
        answers.put("driving", "any");
        answers.put("firstCar", "no");
        final RecommendationCriteria criteria = new RecommendationCriteria(answers, null, null);

        // Exercise
        final List<CarRecommendation> result = recommendationService.recommend(criteria, 5);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyWhenNoCandidatesAreFound() {
        // Arrange
        final RecommendationCriteria criteria = new RecommendationCriteria(Map.of("comfort", "very"), null, null);
        when(reviewTagService.getAll()).thenReturn(allTags());
        mockSinglePageOfCars(List.of());

        // Exercise
        final List<CarRecommendation> result = recommendationService.recommend(criteria, 5);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldFilterOutCarsWithoutReviews() {
        // Arrange
        final RecommendationCriteria criteria = new RecommendationCriteria(Map.of("comfort", "very"), null, null);
        final Car carA = car(1L);
        when(reviewTagService.getAll()).thenReturn(allTags());
        mockSinglePageOfCars(List.of(carA));
        when(reviewDao.findStatsByCarIds(List.of(1L))).thenReturn(List.of()); // zero reviews
        when(reviewTagDao.getTagCountsForCars(List.of(1L))).thenReturn(Map.of());

        // Exercise
        final List<CarRecommendation> result = recommendationService.recommend(criteria, 5);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldFilterOutCarsWithoutAssociatedTags() {
        // Arrange
        final RecommendationCriteria criteria = new RecommendationCriteria(Map.of("comfort", "very"), null, null);
        final Car carA = car(1L);
        final Car carB = car(2L);
        when(reviewTagService.getAll()).thenReturn(allTags());
        mockSinglePageOfCars(List.of(carA, carB));
        when(reviewDao.findStatsByCarIds(anyCollection())).thenReturn(List.of(stats(1L, 10), stats(2L, 10)));
        when(reviewTagDao.getTagCountsForCars(anyCollection())).thenReturn(Map.of(
                2L, Map.of(COMFORT_TAG_ID, 4)
        ));

        // Exercise
        final List<CarRecommendation> result = recommendationService.recommend(criteria, 5);

        // Assertions
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getCar().getId());
    }

    @Test
    public void shouldRankCarWithHigherPositiveTagFrequencyFirst() {
        // Arrange — both cars have 10 reviews; A has 8 mentions of comfortable, B has 2.
        final RecommendationCriteria criteria = new RecommendationCriteria(Map.of("comfort", "very"), null, null);
        final Car carA = car(1L);
        final Car carB = car(2L);
        when(reviewTagService.getAll()).thenReturn(allTags());
        mockSinglePageOfCars(List.of(carA, carB));
        when(reviewDao.findStatsByCarIds(anyCollection())).thenReturn(List.of(stats(1L, 10), stats(2L, 10)));
        when(reviewTagDao.getTagCountsForCars(anyCollection())).thenReturn(Map.of(
                1L, Map.of(COMFORT_TAG_ID, 8),
                2L, Map.of(COMFORT_TAG_ID, 2)
        ));

        // Exercise
        final List<CarRecommendation> result = recommendationService.recommend(criteria, 5);

        // Assertions
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getCar().getId());
        assertEquals(2L, result.get(1).getCar().getId());
        assertTrue(result.get(0).getScore().compareTo(result.get(1).getScore()) > 0);
    }

    @Test
    public void shouldHonorLimitParameter() {
        // Arrange
        final RecommendationCriteria criteria = new RecommendationCriteria(Map.of("comfort", "very"), null, null);
        final Car carA = car(1L);
        final Car carB = car(2L);
        final Car carC = car(3L);
        when(reviewTagService.getAll()).thenReturn(allTags());
        mockSinglePageOfCars(List.of(carA, carB, carC));
        when(reviewDao.findStatsByCarIds(anyCollection())).thenReturn(List.of(
                stats(1L, 10), stats(2L, 10), stats(3L, 10)));
        when(reviewTagDao.getTagCountsForCars(anyCollection())).thenReturn(Map.of(
                1L, Map.of(COMFORT_TAG_ID, 8),
                2L, Map.of(COMFORT_TAG_ID, 5),
                3L, Map.of(COMFORT_TAG_ID, 2)
        ));

        // Exercise
        final List<CarRecommendation> result = recommendationService.recommend(criteria, 2);

        // Assertions
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getCar().getId());
        assertEquals(2L, result.get(1).getCar().getId());
    }

    @Test
    public void shouldFallBackToDefaultLimitWhenLimitIsZeroOrNegative() {
        // Arrange — 6 cars; default limit is 5
        final RecommendationCriteria criteria = new RecommendationCriteria(Map.of("comfort", "very"), null, null);
        final List<Car> cars = List.of(car(1L), car(2L), car(3L), car(4L), car(5L), car(6L));
        when(reviewTagService.getAll()).thenReturn(allTags());
        mockSinglePageOfCars(cars);
        when(reviewDao.findStatsByCarIds(anyCollection())).thenReturn(List.of(
                stats(1L, 10), stats(2L, 10), stats(3L, 10), stats(4L, 10), stats(5L, 10), stats(6L, 10)));
        final Map<Long, Map<Short, Integer>> counts = new HashMap<>();
        for (long id = 1; id <= 6; id++) {
            counts.put(id, Map.of(COMFORT_TAG_ID, (int) (10 - id)));
        }
        when(reviewTagDao.getTagCountsForCars(anyCollection())).thenReturn(counts);

        // Exercise
        final List<CarRecommendation> result = recommendationService.recommend(criteria, 0);

        // Assertions
        assertEquals(5, result.size());
    }

    @Test
    public void shouldSurfacePositiveAndNegativeHighlightsForFreqAboveThreshold() {
        // Arrange — answer 'comfort=very' weights comfortable=+1.0, uncomfortable=-1.0
        // Car A: 10 reviews, 6 mentions of comfortable (freq 0.6 -> DESTACADO), 5 mentions of uncomfortable (freq 0.5 -> CUESTIONADO)
        final RecommendationCriteria criteria = new RecommendationCriteria(Map.of("comfort", "very"), null, null);
        final Car carA = car(1L);
        when(reviewTagService.getAll()).thenReturn(allTags());
        mockSinglePageOfCars(List.of(carA));
        when(reviewDao.findStatsByCarIds(anyCollection())).thenReturn(List.of(stats(1L, 10)));
        when(reviewTagDao.getTagCountsForCars(anyCollection())).thenReturn(Map.of(
                1L, Map.of(COMFORT_TAG_ID, 6, UNCOMFORT_TAG_ID, 5)));

        // Exercise
        final List<CarRecommendation> result = recommendationService.recommend(criteria, 5);

        // Assertions
        assertEquals(1, result.size());
        final CarRecommendation rec = result.get(0);
        assertEquals(1, rec.getPositiveHighlights().size());
        assertEquals("comfortable", rec.getPositiveHighlights().get(0).getTag().getCode());
        assertEquals(1, rec.getNegativeHighlights().size());
        assertEquals("uncomfortable", rec.getNegativeHighlights().get(0).getTag().getCode());
    }

    @Test
    public void shouldHideHighlightsBelowVisibilityThreshold() {
        // Arrange — frequency 1/10 = 0.1 is below LOW_THRESHOLD 0.20 → tier MINOR → not visible.
        final RecommendationCriteria criteria = new RecommendationCriteria(Map.of("comfort", "very"), null, null);
        final Car carA = car(1L);
        when(reviewTagService.getAll()).thenReturn(allTags());
        mockSinglePageOfCars(List.of(carA));
        when(reviewDao.findStatsByCarIds(anyCollection())).thenReturn(List.of(stats(1L, 10)));
        when(reviewTagDao.getTagCountsForCars(anyCollection())).thenReturn(Map.of(
                1L, Map.of(COMFORT_TAG_ID, 1)));

        // Exercise
        final List<CarRecommendation> result = recommendationService.recommend(criteria, 5);

        // Assertions
        assertEquals(1, result.size());
        assertTrue(result.get(0).getPositiveHighlights().isEmpty());
        assertTrue(result.get(0).getNegativeHighlights().isEmpty());
    }
}
