package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRecommendation;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.ReviewTag;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.ReviewTagDao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationServiceImplTest {

    private static final ReviewTag LOW_FUEL = tag((short) 1, "low_fuel_consumption", "Consume poco");
    private static final ReviewTag HIGH_FUEL = tag((short) 2, "high_fuel_consumption", "Consume mucho");
    private static final ReviewTag COMFORTABLE = tag((short) 3, "comfortable", "Comodo");
    private static final ReviewTag AGILE = tag((short) 4, "agile_engine", "Motor agil");

    @Test
    void emptyAnswersReturnNoRecommendations() {
        final RecommendationService service = service(List.of(car(1L, "Sedan", "combustion")), Map.of(), Map.of());

        final List<CarRecommendation> recommendations = service.recommend(new RecommendationCriteria(Map.of(), null, null), 5);

        assertTrue(recommendations.isEmpty());
    }

    @Test
    void higherTagFrequencyRanksFirst() {
        final Car carOne = car(1L, "Sedan", "combustion");
        final Car carTwo = car(2L, "Sedan", "combustion");
        final RecommendationService service = service(
                List.of(carOne, carTwo),
                Map.of(1L, stats(1L, 2), 2L, stats(2L, 3)),
                Map.of(
                        1L, Map.of(LOW_FUEL.getId(), 1),
                        2L, Map.of(LOW_FUEL.getId(), 3)
                )
        );

        final List<CarRecommendation> recommendations = service.recommend(criteria("fuelEconomy", "very"), 5);

        assertEquals(2L, recommendations.get(0).getCar().getId());
        assertEquals(1L, recommendations.get(1).getCar().getId());
    }

    @Test
    void hardPrefiltersConstrainCandidates() {
        final Car sedan = car(1L, "Sedan", "combustion");
        final Car suv = car(2L, "SUV", "hybrid");
        final RecommendationService service = service(
                List.of(sedan, suv),
                Map.of(1L, stats(1L, 1), 2L, stats(2L, 1)),
                Map.of(1L, Map.of(LOW_FUEL.getId(), 1), 2L, Map.of(LOW_FUEL.getId(), 1))
        );

        final RecommendationCriteria criteria = criteria("fuelEconomy", "very");
        criteria.setBodyType("SUV");
        criteria.setFuelType("hybrid");

        final List<CarRecommendation> recommendations = service.recommend(criteria, 5);

        assertEquals(1, recommendations.size());
        assertEquals(2L, recommendations.get(0).getCar().getId());
    }

    @Test
    void zeroReviewCarsAreSkipped() {
        final RecommendationService service = service(
                List.of(car(1L, "Sedan", "combustion")),
                Map.of(1L, stats(1L, 0)),
                Map.of(1L, Map.of(LOW_FUEL.getId(), 1))
        );

        final List<CarRecommendation> recommendations = service.recommend(criteria("fuelEconomy", "very"), 5);

        assertTrue(recommendations.isEmpty());
    }

    @Test
    void highlightsIncludeOnlyPositiveContributions() {
        final RecommendationService service = service(
                List.of(car(1L, "Sedan", "combustion")),
                Map.of(1L, stats(1L, 2)),
                Map.of(1L, Map.of(LOW_FUEL.getId(), 1, HIGH_FUEL.getId(), 2, COMFORTABLE.getId(), 1, AGILE.getId(), 1))
        );

        final RecommendationCriteria criteria = new RecommendationCriteria(
                Map.of("fuelEconomy", "very", "comfort", "very", "performance", "very"),
                null,
                null
        );
        final List<CarRecommendation> recommendations = service.recommend(criteria, 5);

        assertEquals(3, recommendations.get(0).getHighlights().size());
        assertTrue(recommendations.get(0).getHighlights().stream()
                .noneMatch(highlight -> HIGH_FUEL.getCode().equals(highlight.getTag().getCode())));
    }

    private RecommendationService service(final List<Car> cars, final Map<Long, ReviewStats> stats,
                                          final Map<Long, Map<Short, Integer>> tagCounts) {
        return new RecommendationServiceImpl(
                new FakeCarService(cars),
                new FakeReviewTagService(),
                new FakeReviewTagDao(tagCounts),
                new FakeReviewDao(stats)
        );
    }

    private RecommendationCriteria criteria(final String question, final String answer) {
        return new RecommendationCriteria(Map.of(question, answer), null, null);
    }

    private static ReviewTag tag(final short id, final String code, final String label) {
        return new ReviewTag(id, code, label, "positive", "dimension", LocalDateTime.now());
    }

    private static ReviewStats stats(final long carId, final long count) {
        return new ReviewStats(carId, count, BigDecimal.valueOf(4));
    }

    private static Car car(final long id, final String bodyType, final String fuelType) {
        return new Car(id, 1L, "Brand", "Model " + id, 1L, bodyType, "Desc", LocalDateTime.now(), true,
                fuelType, 100, 6, "automatic", BigDecimal.valueOf(7), 180, BigDecimal.valueOf(20000));
    }

    private static final class FakeCarService implements CarService {
        private final List<Car> cars;

        private FakeCarService(final List<Car> cars) {
            this.cars = cars;
        }

        @Override
        public List<Car> getAllCars() { return cars; }

        @Override
        public Optional<Car> getCarById(final long id) { return Optional.empty(); }

        @Override
        public List<Car> getCarsByIds(final Collection<Long> ids) { return Collections.emptyList(); }

        @Override
        public List<Car> getCarsByBrandAndBodyType(final String brand, final String bodyType) { return Collections.emptyList(); }

        @Override
        public Page<Car> searchCars(final CarSearchCriteria criteria) {
            final List<Car> filtered = cars.stream()
                    .filter(car -> criteria.getBodyType() == null || criteria.getBodyType().equals(car.getBodyType()))
                    .filter(car -> criteria.getFuelTypes().isEmpty() || criteria.getFuelTypes().contains(car.getFuelType()))
                    .toList();
            return new Page<>(filtered, 1, 16, filtered.size());
        }

        @Override
        public Optional<CarImage> getCarImageByCarId(final long carId) { return Optional.empty(); }

        @Override
        public List<CarImage> getCarImagesByCarId(final long carId) { return Collections.emptyList(); }

        @Override
        public Optional<CarImage> getCarImageById(final long carId, final long imageId) { return Optional.empty(); }

        @Override
        public void saveCarImages(final long carId, final List<CarImagePayload> images) {}

        @Override
        public CarRequest requestCarCreation(final long brandId, final String model, final long bodyTypeId,
                                             final Integer year, final long submittedByUserId, final String submitterEmail,
                                             final Optional<String> description, final List<CarImagePayload> images,
                                             final String fuelType, final Integer horsepower,
                                             final Integer airbagCount, final String transmission,
                                             final BigDecimal fuelConsumption, final Integer maxSpeedKmh,
                                             final BigDecimal priceUsd) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Car> updateCar(final long id, final long brandId, final String model,
                                       final long bodyTypeId, final Integer year, final String description,
                                       final Optional<String> imageContentType, final Optional<byte[]> imageData,
                                       final String fuelType, final Integer horsepower,
                                       final Integer airbagCount, final String transmission,
                                       final BigDecimal fuelConsumption, final Integer maxSpeedKmh,
                                       final BigDecimal priceUsd) {
            return Optional.empty();
        }

        @Override
        public boolean deleteCar(final long id) { return false; }
    }

    private static final class FakeReviewTagService implements ReviewTagService {
        @Override
        public List<ReviewTag> getAll() { return List.of(LOW_FUEL, HIGH_FUEL, COMFORTABLE, AGILE); }

        @Override
        public Map<String, List<ReviewTag>> getAllGroupedBySentiment() { return Map.of(); }

        @Override
        public List<ReviewTag> validateSelection(final Collection<Short> tagIds) { return Collections.emptyList(); }
    }

    private static final class FakeReviewTagDao implements ReviewTagDao {
        private final Map<Long, Map<Short, Integer>> tagCounts;

        private FakeReviewTagDao(final Map<Long, Map<Short, Integer>> tagCounts) {
            this.tagCounts = tagCounts;
        }

        @Override
        public List<ReviewTag> findAll() { return Collections.emptyList(); }

        @Override
        public Optional<ReviewTag> findById(final short tagId) { return Optional.empty(); }

        @Override
        public List<ReviewTag> findByIds(final Collection<Short> tagIds) { return Collections.emptyList(); }

        @Override
        public void replaceAssignments(final long reviewId, final Collection<Short> tagIds) {}

        @Override
        public Map<Long, List<ReviewTag>> findByReviewIds(final Collection<Long> reviewIds) { return Map.of(); }

        @Override
        public Map<Long, Map<Short, Integer>> getTagCountsForCars(final Collection<Long> carIds) {
            final Map<Long, Map<Short, Integer>> result = new HashMap<>();
            for (final Long carId : carIds) {
                if (tagCounts.containsKey(carId)) {
                    result.put(carId, tagCounts.get(carId));
                }
            }
            return result;
        }
    }

    private static final class FakeReviewDao implements ReviewDao {
        private final Map<Long, ReviewStats> stats;

        private FakeReviewDao(final Map<Long, ReviewStats> stats) {
            this.stats = stats;
        }

        @Override
        public List<Review> findAll() { return Collections.emptyList(); }

        @Override
        public Optional<Review> findById(final long id) { return Optional.empty(); }

        @Override
        public List<Review> findByIds(final Collection<Long> ids) { return Collections.emptyList(); }

        @Override
        public List<Review> findByCarId(final long carId) { return Collections.emptyList(); }

        @Override
        public Page<Review> findByCarId(final long carId, final int page) { return Page.empty(page, 0); }

        @Override
        public Optional<Review> findLatestByCarId(final long carId) { return Optional.empty(); }

        @Override
        public Optional<Review> findTopRatedLatestByCarId(final long carId) { return Optional.empty(); }

        @Override
        public List<Review> findByCarIdOrderByRatingAsc(final long carId) { return Collections.emptyList(); }

        @Override
        public Page<Review> findByCarIdOrderByRatingAsc(final long carId, final int page) { return Page.empty(page, 0); }

        @Override
        public List<Review> findByCarIdOrderByRatingDesc(final long carId) { return Collections.emptyList(); }

        @Override
        public Page<Review> findByCarIdOrderByRatingDesc(final long carId, final int page) { return Page.empty(page, 0); }

        @Override
        public long countByCarId(final long carId) { return 0; }

        @Override
        public List<Review> findByUserId(final long userId) { return Collections.emptyList(); }

        @Override
        public Page<Review> findByUserId(final long userId, final int page) { return Page.empty(page, 0); }

        @Override
        public long countByUserId(final long userId) { return 0; }

        @Override
        public Optional<ReviewStats> findStatsByCarId(final long carId) { return Optional.ofNullable(stats.get(carId)); }

        @Override
        public List<ReviewStats> findStatsByCarIds(final Collection<Long> carIds) {
            return carIds.stream().filter(stats::containsKey).map(stats::get).toList();
        }

        @Override
        public Review create(final long userId, final long carId, final BigDecimal rating, final String title,
                             final String body, final String ownershipStatus, final Integer modelYear,
                             final Integer mileageKm, final Boolean wouldRecommend) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int bindReviewsToUserByEmail(final long userId, final String email) { return 0; }

        @Override
        public Optional<Review> update(final long id, final long carId, final BigDecimal rating, final String title,
                                       final String body, final String ownershipStatus, final Integer modelYear,
                                       final Integer mileageKm, final Boolean wouldRecommend) {
            return Optional.empty();
        }

        @Override
        public boolean delete(final long id) { return false; }

        @Override
        public int deleteByCarId(final long carId) { return 0; }
    }
}
