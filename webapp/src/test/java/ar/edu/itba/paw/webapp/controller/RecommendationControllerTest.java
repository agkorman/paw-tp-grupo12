package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarRecommendation;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.services.RecommendationCriteria;
import ar.edu.itba.paw.services.RecommendationQuestion;
import ar.edu.itba.paw.services.RecommendationService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.form.RecommendationForm;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RecommendationControllerTest {

    @Test
    public void recommendReturnsWizardView() {
        final RecommendationController controller = controller(new FakeRecommendationService());
        final ExtendedModelMap model = new ExtendedModelMap();

        final String view = controller.recommend(new RecommendationForm(), model);

        assertEquals("recommend.jsp", view);
        assertTrue(model.containsAttribute("questions"));
        assertTrue(model.containsAttribute("bodyTypes"));
    }

    @Test
    public void resultsWithValidParamsPopulateRecommendations() {
        final FakeRecommendationService recommendationService = new FakeRecommendationService();
        final RecommendationController controller = controller(recommendationService);
        final RecommendationForm form = new RecommendationForm();
        form.setFuelEconomy("very");
        final BindingResult errors = new BeanPropertyBindingResult(form, "recommendationForm");
        final ExtendedModelMap model = new ExtendedModelMap();

        final String view = controller.results(form, errors, model);

        assertEquals("recommend-results.jsp", view);
        assertTrue(model.containsAttribute("recommendations"));
        assertTrue((Boolean) model.get("hasRecommendations"));
        assertEquals("very", recommendationService.criteria.getAnswers().get("fuelEconomy"));
    }

    @Test
    public void invalidBodyTypeReturnsWizardWithErrors() {
        final RecommendationController controller = controller(new FakeRecommendationService());
        final RecommendationForm form = new RecommendationForm();
        form.setBodyType("Truck");
        final BindingResult errors = new BeanPropertyBindingResult(form, "recommendationForm");
        final ExtendedModelMap model = new ExtendedModelMap();

        final String view = controller.results(form, errors, model);

        assertEquals("recommend.jsp", view);
        assertTrue(errors.hasFieldErrors("bodyType"));
    }

    private RecommendationController controller(final RecommendationService recommendationService) {
        return new RecommendationController(recommendationService, new FakeBodyTypeService(), new FakeReviewService());
    }

    private static final class FakeRecommendationService implements RecommendationService {
        private RecommendationCriteria criteria;

        @Override
        public List<RecommendationQuestion> getQuestions() {
            return Collections.emptyList();
        }

        @Override
        public List<CarRecommendation> recommend(final RecommendationCriteria criteria, final int limit) {
            this.criteria = criteria;
            return List.of(new CarRecommendation(
                    new Car(1L, 1L, "Toyota", "Corolla", 1L, "Sedan", "Desc", LocalDateTime.now(), true),
                    BigDecimal.ONE,
                    2,
                    Collections.emptyList()
            ));
        }
    }

    private static final class FakeBodyTypeService implements BodyTypeService {
        @Override
        public List<BodyType> findAll() {
            return List.of(new BodyType(1L, "Sedan", LocalDateTime.now()));
        }

        @Override
        public Optional<BodyType> findByName(final String name) {
            return findAll().stream().filter(bodyType -> bodyType.getName().equals(name)).findFirst();
        }
    }

    private static final class FakeReviewService implements ReviewService {
        @Override
        public Review createReview(final long userId, final long carId, final BigDecimal rating, final String title,
                                   final String body, final String ownershipStatus, final Integer modelYear,
                                   final Integer mileageKm, final Boolean wouldRecommend,
                                   final Collection<Short> tagIds) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Review> getReviewById(final long id) { return Optional.empty(); }

        @Override
        public List<Review> getReviewsByIds(final Collection<Long> ids) { return Collections.emptyList(); }

        @Override
        public Optional<Review> updateReview(final long id, final long carId, final BigDecimal rating,
                                             final String title, final String body, final String ownershipStatus,
                                             final Integer modelYear, final Integer mileageKm,
                                             final Boolean wouldRecommend, final Collection<Short> tagIds) {
            return Optional.empty();
        }

        @Override
        public boolean deleteReview(final long id) { return false; }

        @Override
        public List<Review> getReviewsByCar(final long carId) { return Collections.emptyList(); }

        @Override
        public Page<Review> getReviewsByCar(final long carId, final int page) { return Page.empty(page, 0); }

        @Override
        public Optional<Review> getLatestReviewByCar(final long carId) { return Optional.empty(); }

        @Override
        public Optional<Review> getTopRatedLatestReviewByCar(final long carId) { return Optional.empty(); }

        @Override
        public List<Review> getReviewsByCarOrderByRatingAsc(final long carId) { return Collections.emptyList(); }

        @Override
        public Page<Review> getReviewsByCarOrderByRatingAsc(final long carId, final int page) { return Page.empty(page, 0); }

        @Override
        public List<Review> getReviewsByCarOrderByRatingDesc(final long carId) { return Collections.emptyList(); }

        @Override
        public Page<Review> getReviewsByCarOrderByRatingDesc(final long carId, final int page) { return Page.empty(page, 0); }

        @Override
        public List<Review> getReviewsByUser(final long userId) { return Collections.emptyList(); }

        @Override
        public Optional<ReviewStats> getReviewStatsByCar(final long carId) {
            return Optional.of(new ReviewStats(carId, 2, BigDecimal.valueOf(4.5)));
        }

        @Override
        public List<ReviewStats> getReviewStatsByCarIds(final Collection<Long> carIds) {
            return carIds.stream()
                    .map(carId -> new ReviewStats(carId, 2, BigDecimal.valueOf(4.5)))
                    .toList();
        }
    }
}
