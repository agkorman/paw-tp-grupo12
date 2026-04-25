package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ActivityControllerTest {

    @Test
    public void activityReturnsActivityViewWithReviewCards() {
        final Review review = new Review(
                1L,
                7L,
                null,
                "Alex Carrera",
                10L,
                BigDecimal.valueOf(4.5),
                "Gran experiencia",
                "El auto se siente firme y preciso en ruta.",
                "Propietario actual",
                2020,
                45000,
                true,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(2)
        );
        final ActivityController controller = new ActivityController(
                new FakeReviewService(review),
                new FakeCarService()
        );

        final ModelAndView mav = controller.activity();

        assertEquals("activity.jsp", mav.getViewName());
        final List<ActivityController.ActivityReviewCard> cards = activityCards(mav);
        assertEquals(1, cards.size());
        assertEquals("Porsche 911 GT3 RS", cards.get(0).getCarName());
        assertEquals("Alex Carrera", cards.get(0).getAuthorName());
        assertTrue(cards.get(0).getTimeAgo().startsWith("hace "));
    }

    @SuppressWarnings("unchecked")
    private List<ActivityController.ActivityReviewCard> activityCards(final ModelAndView mav) {
        return (List<ActivityController.ActivityReviewCard>) mav.getModel().get("activityReviews");
    }

    private static final class FakeReviewService implements ReviewService {
        private final Review review;

        private FakeReviewService(final Review review) {
            this.review = review;
        }

        @Override
        public Review createReview(final long userId, final long carId, final BigDecimal rating, final String title,
                                   final String body, final String ownershipStatus, final Integer modelYear,
                                   final Integer mileageKm, final Boolean wouldRecommend) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Review> getReviewById(final long id) {
            return Optional.empty();
        }

        @Override
        public List<Review> getReviewsByIds(final Collection<Long> ids) {
            return Collections.emptyList();
        }

        @Override
        public Optional<Review> updateReview(final long id, final long carId, final BigDecimal rating,
                                             final String title, final String body, final String ownershipStatus,
                                             final Integer modelYear, final Integer mileageKm,
                                             final Boolean wouldRecommend) {
            return Optional.empty();
        }

        @Override
        public boolean deleteReview(final long id) {
            return false;
        }

        @Override
        public List<Review> getReviewsByCar(final long carId) {
            return Collections.emptyList();
        }

        @Override
        public Optional<Review> getLatestReviewByCar(final long carId) {
            return Optional.empty();
        }

        @Override
        public Optional<Review> getTopRatedLatestReviewByCar(final long carId) {
            return Optional.empty();
        }

        @Override
        public List<Review> getReviewsByCarOrderByRatingAsc(final long carId) {
            return Collections.emptyList();
        }

        @Override
        public List<Review> getReviewsByCarOrderByRatingDesc(final long carId) {
            return Collections.emptyList();
        }

        @Override
        public List<Review> getReviewsByUser(final long userId) {
            return Collections.emptyList();
        }

        @Override
        public List<Review> getAllReviews() {
            return List.of(review);
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

    private static final class FakeCarService implements CarService {
        private final Car car = new Car(
                10L,
                1L,
                "Porsche",
                "911 GT3 RS",
                1L,
                "Coupé",
                "Track weapon",
                LocalDateTime.now(),
                true
        );

        @Override
        public List<Car> getAllCars() {
            return Collections.emptyList();
        }

        @Override
        public Optional<Car> getCarById(final long id) {
            return Optional.empty();
        }

        @Override
        public List<Car> getCarsByIds(final Collection<Long> ids) {
            return ids.contains(car.getId()) ? List.of(car) : Collections.emptyList();
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
        public List<Car> searchCars(final CarSearchCriteria criteria) {
            return Collections.emptyList();
        }

        @Override
        public Optional<CarImage> getCarImageByCarId(final long carId) {
            return Optional.empty();
        }

        @Override
        public List<CarImage> getCarImagesByCarId(final long carId) {
            return Collections.emptyList();
        }

        @Override
        public Optional<CarImage> getCarImageById(final long carId, final long imageId) {
            return Optional.empty();
        }

        @Override
        public void saveCarImage(final long carId, final String contentType, final byte[] imageData) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void saveCarImages(final long carId, final List<CarImagePayload> images) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CarRequest requestCarCreation(final long brandId, final String model, final long bodyTypeId,
                                             final long submittedByUserId, final String submitterEmail,
                                             final Optional<String> description,
                                             final Optional<String> imageContentType,
                                             final Optional<byte[]> imageData, final String fuelType,
                                             final Integer horsepower, final Integer airbagCount,
                                             final String transmission, final BigDecimal fuelConsumption,
                                             final Integer maxSpeedKmh) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CarRequest requestCarCreation(final long brandId, final String model, final long bodyTypeId,
                                             final long submittedByUserId, final String submitterEmail,
                                             final Optional<String> description, final List<CarImagePayload> images,
                                             final String fuelType, final Integer horsepower,
                                             final Integer airbagCount, final String transmission,
                                             final BigDecimal fuelConsumption, final Integer maxSpeedKmh) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Car> updateCar(final long id, final long brandId, final String model, final long bodyTypeId,
                                       final String description, final Optional<String> imageContentType,
                                       final Optional<byte[]> imageData, final String fuelType,
                                       final Integer horsepower, final Integer airbagCount,
                                       final String transmission, final BigDecimal fuelConsumption,
                                       final Integer maxSpeedKmh) {
            return Optional.empty();
        }

        @Override
        public boolean deleteCar(final long id) {
            return false;
        }
    }
}
