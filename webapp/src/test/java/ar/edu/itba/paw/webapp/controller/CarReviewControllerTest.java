package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.form.ReviewForm;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class CarReviewControllerTest {

    @Test
    public void createReviewUsesAuthenticatedUserId() {
        final FakeReviewService reviewService = new FakeReviewService();
        final CarReviewController controller = controller(reviewService);
        final ReviewForm reviewForm = reviewForm(
                10L,
                BigDecimal.valueOf(4.5),
                "Gran auto",
                "Muy buena experiencia.",
                "Propietario actual",
                2020,
                45000,
                true
        );
        final BindingResult errors = new BeanPropertyBindingResult(reviewForm, "reviewForm");

        final String view = controller.createReview(
                reviewForm,
                errors,
                new ExtendedModelMap(),
                user(7L)
        );

        assertEquals("redirect:/reviews?carId=10", view);
        assertFalse(errors.hasErrors());
        assertEquals(7L, reviewService.createdUserId);
        assertEquals(10L, reviewService.createdCarId);
    }

    @Test
    public void authenticatedUserCanEditOwnReview() {
        final FakeReviewService reviewService = new FakeReviewService();
        reviewService.review = review(3L, 7L, 10L);
        final CarReviewController controller = controller(reviewService);

        final ModelAndView mav = controller.updateReview(
                3L,
                BigDecimal.valueOf(4),
                "Editada",
                "Texto editado.",
                "Propietario actual",
                2021,
                30000,
                true,
                user(7L)
        );

        assertEquals("redirect:/profile", mav.getViewName());
        assertTrue(reviewService.updated);
        assertEquals(10L, reviewService.updatedCarId);
    }

    @Test
    public void authenticatedUserCannotEditOtherUsersReview() {
        final FakeReviewService reviewService = new FakeReviewService();
        reviewService.review = review(3L, 8L, 10L);
        final CarReviewController controller = controller(reviewService);

        final RuntimeException exception = assertThrows(RuntimeException.class, () -> controller.updateReview(
                3L,
                BigDecimal.valueOf(4),
                "Editada",
                "Texto editado.",
                null,
                null,
                null,
                null,
                user(7L)
        ));

        assertResponseStatus(exception, HttpStatus.FORBIDDEN);
        assertFalse(reviewService.updated);
    }

    @Test
    public void authenticatedUserCanDeleteOwnReview() {
        final FakeReviewService reviewService = new FakeReviewService();
        reviewService.review = review(3L, 7L, 10L);
        final CarReviewController controller = controller(reviewService);

        final ModelAndView mav = controller.deleteReview(3L, user(7L));

        assertEquals("redirect:/profile", mav.getViewName());
        assertTrue(reviewService.deleted);
        assertEquals(3L, reviewService.deletedId);
    }

    @Test
    public void authenticatedUserCannotDeleteOtherUsersReview() {
        final FakeReviewService reviewService = new FakeReviewService();
        reviewService.review = review(3L, 8L, 10L);
        final CarReviewController controller = controller(reviewService);

        final RuntimeException exception = assertThrows(RuntimeException.class,
                () -> controller.deleteReview(3L, user(7L)));

        assertResponseStatus(exception, HttpStatus.FORBIDDEN);
        assertFalse(reviewService.deleted);
    }

    @Test
    public void missingReviewReturnsNotFoundOnUpdate() {
        final FakeReviewService reviewService = new FakeReviewService();
        final CarReviewController controller = controller(reviewService);

        final RuntimeException exception = assertThrows(RuntimeException.class, () -> controller.updateReview(
                99L,
                BigDecimal.valueOf(4),
                "Editada",
                "Texto editado.",
                null,
                null,
                null,
                null,
                user(7L)
        ));

        assertResponseStatus(exception, HttpStatus.NOT_FOUND);
        assertFalse(reviewService.updated);
    }

    private CarReviewController controller(final FakeReviewService reviewService) {
        return new CarReviewController(new FakeCarService(), reviewService);
    }

    private AuthenticatedUser user(final long id) {
        return new AuthenticatedUser(id, "driver" + id, "driver" + id + "@example.com", "password", List.of());
    }

    private Review review(final long id, final Long userId, final long carId) {
        return new Review(
                id,
                userId,
                null,
                "driver",
                carId,
                BigDecimal.valueOf(4.5),
                "Gran auto",
                "Muy buena experiencia.",
                "Propietario actual",
                2020,
                45000,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private ReviewForm reviewForm(final long carId, final BigDecimal rating, final String title, final String body,
                                  final String ownershipStatus, final Integer modelYear, final Integer mileageKm,
                                  final Boolean wouldRecommend) {
        final ReviewForm form = new ReviewForm();
        form.setCarId(carId);
        form.setRating(rating);
        form.setTitle(title);
        form.setBody(body);
        form.setOwnershipStatus(ownershipStatus);
        form.setModelYear(modelYear);
        form.setMileageKm(mileageKm);
        form.setWouldRecommend(wouldRecommend);
        return form;
    }

    private void assertResponseStatus(final RuntimeException exception, final HttpStatus expectedStatus) {
        final ResponseStatus responseStatus = exception.getClass().getAnnotation(ResponseStatus.class);
        assertEquals(expectedStatus, responseStatus.value());
    }

    private static final class FakeCarService implements CarService {
        @Override
        public List<Car> getAllCars() {
            return Collections.emptyList();
        }

        @Override
        public Optional<Car> getCarById(final long id) {
            return Optional.of(new Car(id, 1L, "Toyota", "Supra", 1L, "Coupe", "Desc", LocalDateTime.now()));
        }

        @Override
        public List<Car> getCarsByBodyType(final String bodyType) {
            return Collections.emptyList();
        }

        @Override
        public List<Car> getCarsByBrand(final String brand) {
            return Collections.emptyList();
        }

        @Override
        public List<Car> getCarsByBrandAndBodyType(final String brand, final String bodyType) {
            return Collections.emptyList();
        }

        @Override
        public List<Car> searchCars(final ar.edu.itba.paw.model.CarSearchCriteria criteria) {
            return Collections.emptyList();
        }

        @Override
        public Optional<CarImage> getCarImageByCarId(final long carId) {
            return Optional.empty();
        }

        @Override
        public void saveCarImage(final long carId, final String contentType, final byte[] imageData) {
        }

        @Override
        public Car createCar(final long brandId, final String model, final long bodyTypeId,
                             final long submittedByUserId, final Optional<String> description,
                             final Optional<String> imageContentType, final Optional<byte[]> imageData,
                             final String fuelType, final Integer horsepower, final Integer airbagCount,
                             final String transmission, final java.math.BigDecimal fuelConsumption,
                             final Integer maxSpeedKmh) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FakeReviewService implements ReviewService {
        private Review review;
        private long createdUserId;
        private long createdCarId;
        private boolean updated;
        private long updatedCarId;
        private boolean deleted;
        private long deletedId;

        @Override
        public Review createReview(final long userId, final long carId, final BigDecimal rating, final String title,
                                   final String body, final String ownershipStatus, final Integer modelYear,
                                   final Integer mileageKm, final Boolean wouldRecommend) {
            this.createdUserId = userId;
            this.createdCarId = carId;
            return review == null ? new Review() : review;
        }

        @Override
        public Optional<Review> getReviewById(final long id) {
            return review != null && review.getId() == id ? Optional.of(review) : Optional.empty();
        }

        @Override
        public Optional<Review> updateReview(final long id, final long carId, final BigDecimal rating,
                                             final String title, final String body, final String ownershipStatus,
                                             final Integer modelYear, final Integer mileageKm,
                                             final Boolean wouldRecommend) {
            this.updated = true;
            this.updatedCarId = carId;
            return Optional.ofNullable(review);
        }

        @Override
        public boolean deleteReview(final long id) {
            this.deleted = true;
            this.deletedId = id;
            return true;
        }

        @Override
        public List<Review> getReviewsByCar(final long carId) {
            return review == null ? Collections.emptyList() : List.of(review);
        }

        @Override
        public Optional<Review> getLatestReviewByCar(final long carId) {
            return Optional.ofNullable(review);
        }

        @Override
        public Optional<Review> getTopRatedLatestReviewByCar(final long carId) {
            return Optional.ofNullable(review);
        }

        @Override
        public List<Review> getReviewsByCarOrderByRatingAsc(final long carId) {
            return getReviewsByCar(carId);
        }

        @Override
        public List<Review> getReviewsByCarOrderByRatingDesc(final long carId) {
            return getReviewsByCar(carId);
        }

        @Override
        public List<Review> getReviewsByUser(final long userId) {
            return review != null && review.getUserId() != null && review.getUserId() == userId
                    ? List.of(review)
                    : Collections.emptyList();
        }

        @Override
        public List<Review> getAllReviews() {
            return review == null ? Collections.emptyList() : List.of(review);
        }

        @Override
        public Optional<ReviewStats> getReviewStatsByCar(final long carId) {
            return Optional.empty();
        }

        @Override
        public List<ReviewStats> getReviewStatsByCarIds(final Collection<Long> carIds) {
            return Collections.emptyList();
        }
    }
}
