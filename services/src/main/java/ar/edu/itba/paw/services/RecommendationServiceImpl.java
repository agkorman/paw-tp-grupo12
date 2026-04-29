package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarRecommendation;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.ReviewTag;
import ar.edu.itba.paw.model.TagHighlight;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.ReviewTagDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final int SCORE_SCALE = 6;
    private static final int DEFAULT_LIMIT = 5;
    private static final int HIGHLIGHT_LIMIT = 3;

    private final CarService carService;
    private final ReviewTagService reviewTagService;
    private final ReviewTagDao reviewTagDao;
    private final ReviewDao reviewDao;

    @Autowired
    public RecommendationServiceImpl(final CarService carService, final ReviewTagService reviewTagService,
                                     final ReviewTagDao reviewTagDao, final ReviewDao reviewDao) {
        this.carService = carService;
        this.reviewTagService = reviewTagService;
        this.reviewTagDao = reviewTagDao;
        this.reviewDao = reviewDao;
    }

    @Override
    public List<RecommendationQuestion> getQuestions() {
        return RecommendationQuestionnaire.questions();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarRecommendation> recommend(final RecommendationCriteria criteria, final int limit) {
        final Map<String, BigDecimal> tagCodeWeights = buildTagCodeWeights(criteria);
        if (tagCodeWeights.isEmpty()) {
            return List.of();
        }

        final List<ReviewTag> tags = reviewTagService.getAll();
        final Map<String, ReviewTag> tagsByCode = tags.stream()
                .collect(Collectors.toMap(ReviewTag::getCode, Function.identity()));
        final Map<Short, BigDecimal> tagIdWeights = toTagIdWeights(tagCodeWeights, tagsByCode);
        if (tagIdWeights.isEmpty()) {
            return List.of();
        }

        final List<Car> candidates = loadCandidates(criteria);
        if (candidates.isEmpty()) {
            return List.of();
        }

        final List<Long> carIds = candidates.stream().map(Car::getId).collect(Collectors.toList());
        final Map<Long, Integer> reviewCounts = reviewDao.findStatsByCarIds(carIds).stream()
                .collect(Collectors.toMap(ReviewStats::getCarId, stats -> Math.toIntExact(stats.getReviewCount())));
        final Map<Long, Map<Short, Integer>> tagCounts = reviewTagDao.getTagCountsForCars(carIds);

        final Map<Short, ReviewTag> tagsById = tags.stream()
                .collect(Collectors.toMap(ReviewTag::getId, Function.identity()));
        final int effectiveLimit = limit > 0 ? limit : DEFAULT_LIMIT;

        return candidates.stream()
                .map(car -> score(car, reviewCounts.getOrDefault(car.getId(), 0), tagCounts.get(car.getId()),
                        tagIdWeights, tagsById))
                .filter(recommendation -> recommendation.getReviewCount() > 0)
                .sorted(Comparator.comparing(CarRecommendation::getScore).reversed()
                        .thenComparing(CarRecommendation::getReviewCount, Comparator.reverseOrder())
                        .thenComparing(recommendation -> recommendation.getCar().getId()))
                .limit(effectiveLimit)
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> buildTagCodeWeights(final RecommendationCriteria criteria) {
        if (criteria == null || criteria.getAnswers().isEmpty()) {
            return Map.of();
        }
        final Map<String, BigDecimal> weights = new HashMap<>();
        for (final Map.Entry<String, String> answer : criteria.getAnswers().entrySet()) {
            for (final Map.Entry<String, BigDecimal> tagWeight :
                    RecommendationQuestionnaire.weightsFor(answer.getKey(), answer.getValue()).entrySet()) {
                weights.merge(tagWeight.getKey(), tagWeight.getValue(), BigDecimal::add);
            }
        }
        weights.entrySet().removeIf(entry -> entry.getValue().compareTo(BigDecimal.ZERO) == 0);
        return weights;
    }

    private Map<Short, BigDecimal> toTagIdWeights(final Map<String, BigDecimal> tagCodeWeights,
                                                  final Map<String, ReviewTag> tagsByCode) {
        final Map<Short, BigDecimal> tagIdWeights = new HashMap<>();
        for (final Map.Entry<String, BigDecimal> entry : tagCodeWeights.entrySet()) {
            final ReviewTag tag = tagsByCode.get(entry.getKey());
            if (tag != null) {
                tagIdWeights.put(tag.getId(), entry.getValue());
            }
        }
        return tagIdWeights;
    }

    private List<Car> loadCandidates(final RecommendationCriteria criteria) {
        final CarSearchCriteria searchCriteria = new CarSearchCriteria();
        if (criteria != null) {
            searchCriteria.setBodyType(criteria.getBodyType());
            searchCriteria.setFuelType(criteria.getFuelType());
        }

        final List<Car> cars = new ArrayList<>();
        int pageNumber = 1;
        Page<Car> page;
        do {
            searchCriteria.setPage(pageNumber);
            page = carService.searchCars(searchCriteria);
            cars.addAll(page.getItems());
            pageNumber++;
        } while (page.hasNext());
        return cars;
    }

    private CarRecommendation score(final Car car, final int reviewCount, final Map<Short, Integer> carTagCounts,
                                    final Map<Short, BigDecimal> tagIdWeights,
                                    final Map<Short, ReviewTag> tagsById) {
        if (reviewCount <= 0 || carTagCounts == null || carTagCounts.isEmpty()) {
            return new CarRecommendation(car, BigDecimal.ZERO, reviewCount, List.of());
        }

        BigDecimal score = BigDecimal.ZERO;
        final List<WeightedHighlight> weightedHighlights = new ArrayList<>();
        for (final Map.Entry<Short, BigDecimal> entry : tagIdWeights.entrySet()) {
            final int mentionCount = carTagCounts.getOrDefault(entry.getKey(), 0);
            if (mentionCount <= 0) {
                continue;
            }
            final BigDecimal frequency = frequency(mentionCount, reviewCount);
            final BigDecimal contribution = entry.getValue().multiply(frequency);
            score = score.add(contribution);
            if (contribution.compareTo(BigDecimal.ZERO) > 0) {
                final ReviewTag tag = tagsById.get(entry.getKey());
                if (tag != null) {
                    weightedHighlights.add(new WeightedHighlight(
                            contribution,
                            new TagHighlight(tag, mentionCount, reviewCount, frequency)
                    ));
                }
            }
        }

        final List<TagHighlight> highlights = weightedHighlights.stream()
                .sorted(Comparator.comparing(WeightedHighlight::getContribution).reversed())
                .limit(HIGHLIGHT_LIMIT)
                .map(WeightedHighlight::getHighlight)
                .collect(Collectors.toList());
        return new CarRecommendation(car, score, reviewCount, highlights);
    }

    private BigDecimal frequency(final int mentionCount, final int reviewCount) {
        return BigDecimal.valueOf(mentionCount).divide(BigDecimal.valueOf(reviewCount), SCORE_SCALE, RoundingMode.HALF_UP);
    }

    private static final class WeightedHighlight {
        private final BigDecimal contribution;
        private final TagHighlight highlight;

        private WeightedHighlight(final BigDecimal contribution, final TagHighlight highlight) {
            this.contribution = contribution;
            this.highlight = highlight;
        }

        private BigDecimal getContribution() {
            return contribution;
        }

        private TagHighlight getHighlight() {
            return highlight;
        }
    }
}
