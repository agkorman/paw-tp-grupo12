package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.services.BrandService;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.EmailService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CarControllerFavoriteTest {

    @Test
    public void ajaxFavoritePersistsAndReturnsState() {
        final FakeCarFavoriteService favoriteService = new FakeCarFavoriteService();
        final CarController controller = controller(new FakeCarService(true), favoriteService);

        final Object response = controller.updateFavorite(10L, true, "XMLHttpRequest", "/cars", user(7L));

        assertTrue(response instanceof ResponseEntity);
        final ResponseEntity<?> entity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals("true", entity.getBody());
        assertEquals(7L, favoriteService.userId);
        assertEquals(10L, favoriteService.carId);
    }

    @Test
    public void fallbackFavoriteRedirectsBackToReferer() {
        final CarController controller = controller(new FakeCarService(true), new FakeCarFavoriteService());

        final Object response = controller.updateFavorite(10L, true, null, "http://localhost/cars?q=supra", user(7L));

        assertTrue(response instanceof ModelAndView);
        assertEquals("redirect:/cars?q=supra", ((ModelAndView) response).getViewName());
    }

    @Test
    public void anonymousAjaxFavoriteReturnsUnauthorized() {
        final CarController controller = controller(new FakeCarService(true), new FakeCarFavoriteService());

        final Object response = controller.updateFavorite(10L, true, "XMLHttpRequest", "/cars", null);

        assertTrue(response instanceof ResponseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, ((ResponseEntity<?>) response).getStatusCode());
    }

    private CarController controller(final CarService carService, final CarFavoriteService favoriteService) {
        return new CarController(
                carService,
                favoriteService,
                new FakeBrandService(),
                new FakeBodyTypeService(),
                new FakeReviewService(),
                new FakeEmailService()
        );
    }

    private AuthenticatedUser user(final long id) {
        return new AuthenticatedUser(id, "driver" + id, "driver" + id + "@example.com", "password", List.of());
    }

    private static final class FakeCarFavoriteService implements CarFavoriteService {
        private long userId;
        private long carId;
        private boolean favorited;

        @Override
        public List<Long> findFavoriteCarIdsByUser(final long userId) {
            return Collections.emptyList();
        }

        @Override
        public boolean setFavorite(final long userId, final long carId, final boolean favorite) {
            this.userId = userId;
            this.carId = carId;
            this.favorited = favorite;
            return true;
        }

        @Override
        public boolean isFavorited(final long userId, final long carId) {
            return favorited;
        }

        @Override
        public List<Car> getFavoriteCars(final long userId) {
            return Collections.emptyList();
        }

        @Override
        public Set<Long> getFavoritedCarIds(final long userId, final Collection<Long> carIds) {
            return Collections.emptySet();
        }
    }

    private static final class FakeCarService implements CarService {
        private final boolean carExists;

        private FakeCarService(final boolean carExists) {
            this.carExists = carExists;
        }

        @Override
        public List<Car> getAllCars() {
            return Collections.emptyList();
        }

        @Override
        public Optional<Car> getCarById(final long id) {
            return carExists
                    ? Optional.of(new Car(id, 1L, "Toyota", "Supra", 1L, "Coupe", "Desc", LocalDateTime.now()))
                    : Optional.empty();
        }

        @Override
        public List<Car> getCarsByIds(final Collection<Long> ids) {
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
        public List<CarImage> getCarImagesByCarId(final long carId) {
            return Collections.emptyList();
        }

        @Override
        public Optional<CarImage> getCarImageById(final long carId, final long imageId) {
            return Optional.empty();
        }

        @Override
        public void saveCarImages(final long carId, final List<CarImagePayload> images) {
        }

        @Override
        public CarRequest requestCarCreation(final long brandId, final String model, final long bodyTypeId,
                                             final long submittedByUserId, final String submitterEmail,
                                             final Optional<String> description,
                                             final List<CarImagePayload> images, final String fuelType,
                                             final Integer horsepower, final Integer airbagCount,
                                             final String transmission, final BigDecimal fuelConsumption,
                                             final Integer maxSpeedKmh) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Car> updateCar(final long id, final long brandId, final String model,
                                       final long bodyTypeId, final String description,
                                       final Optional<String> imageContentType,
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

    private static final class FakeReviewService implements ReviewService {
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
        public Optional<ReviewStats> getReviewStatsByCar(final long carId) {
            return Optional.empty();
        }

        @Override
        public List<ReviewStats> getReviewStatsByCarIds(final Collection<Long> carIds) {
            return Collections.emptyList();
        }
    }

    private static final class FakeBrandService implements BrandService {
        @Override
        public List<Brand> findAll() {
            return Collections.emptyList();
        }

        @Override
        public Optional<Brand> findByName(final String name) {
            return Optional.empty();
        }
    }

    private static final class FakeBodyTypeService implements BodyTypeService {
        @Override
        public List<BodyType> findAll() {
            return Collections.emptyList();
        }

        @Override
        public Optional<BodyType> findByName(final String name) {
            return Optional.empty();
        }
    }

    private static final class FakeEmailService implements EmailService {
        @Override
        public void sendNewCarRequestNotification(final CarRequest request, final String brandName,
                                                  final String bodyTypeName) {
        }

        @Override
        public void sendCarApprovedNotification(final String recipientEmail, final String brandName,
                                                final String model) {
        }

        @Override
        public void sendWeeklyModeratorDigest(final List<String> moderatorEmails, final int pendingRequestCount) {
        }

        @Override
        public void sendWeeklyUserDigest(final String recipientEmail, final String username,
                                         final List<ReviewActivityItem> reviewActivity,
                                         final List<FavoriteActivityItem> favoriteActivity) {
        }
    }
}
