package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.UserFollowService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import org.junit.Test;
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

public class ActivityControllerTest {

    @Test
    public void activityReturnsActivityViewWithReviewCards() {
        final Review review = review(1L, 7L, 10L);
        final ActivityController controller = new ActivityController(
                new FakeReviewService(List.of(review)),
                new FakeCarService(),
                new FakeUserFollowService(),
                new FakeCarFavoriteService()
        );

        final ModelAndView mav = controller.activity(user());

        assertEquals("activity.jsp", mav.getViewName());
        final List<ActivityController.ActivityReviewCard> cards = activityCards(mav, "latestActivityReviews");
        assertEquals(1, cards.size());
        assertEquals("Porsche 911 GT3 RS", cards.get(0).getCarName());
        assertEquals("Alex Carrera", cards.get(0).getAuthorName());
        assertTrue(cards.get(0).getTimeAgo().startsWith("hace "));
    }

    @Test
    public void activitySplitsReviewsByFollowingAndFavoriteCars() {
        final Review followedReview = review(1L, 7L, 10L);
        final Review favoriteCarReview = review(2L, 8L, 20L);
        final Review unrelatedReview = review(3L, 9L, 30L);
        final ActivityController controller = new ActivityController(
                new FakeReviewService(List.of(followedReview, favoriteCarReview, unrelatedReview)),
                new FakeCarService(),
                new FakeUserFollowService(),
                new FakeCarFavoriteService()
        );

        final ModelAndView mav = controller.activity(user());

        assertEquals(3, activityCards(mav, "latestActivityReviews").size());
        assertEquals(1, activityCards(mav, "followedActivityReviews").size());
        assertEquals(followedReview.getId(), activityCards(mav, "followedActivityReviews").get(0).getReview().getId());
        assertEquals(1, activityCards(mav, "favoriteCarActivityReviews").size());
        assertEquals(favoriteCarReview.getId(), activityCards(mav, "favoriteCarActivityReviews").get(0).getReview().getId());
    }

    @Test
    public void activityAllowsAnonymousUsersToSeeLatestReviewsOnly() {
        final Review review = review(1L, 7L, 10L);
        final ActivityController controller = new ActivityController(
                new FakeReviewService(List.of(review)),
                new FakeCarService(),
                new FakeUserFollowService(),
                new FakeCarFavoriteService()
        );

        final ModelAndView mav = controller.activity(null);

        assertEquals("activity.jsp", mav.getViewName());
        assertEquals(1, activityCards(mav, "latestActivityReviews").size());
        assertTrue(activityCards(mav, "followedActivityReviews").isEmpty());
        assertTrue(activityCards(mav, "favoriteCarActivityReviews").isEmpty());
    }

    private Review review(final long id, final long userId, final long carId) {
        return new Review(
                id,
                userId,
                null,
                "Alex Carrera",
                carId,
                BigDecimal.valueOf(4.5),
                "Gran experiencia",
                "El auto se siente firme y preciso en ruta.",
                "Propietario actual",
                2020,
                45000,
                true,
                LocalDateTime.now().minusHours(id),
                LocalDateTime.now().minusHours(id)
        );
    }

    private AuthenticatedUser user() {
        return new AuthenticatedUser(100L, "driver100", "driver100@example.com", "password", List.of());
    }

    @SuppressWarnings("unchecked")
    private List<ActivityController.ActivityReviewCard> activityCards(final ModelAndView mav, final String attribute) {
        return (List<ActivityController.ActivityReviewCard>) mav.getModel().get(attribute);
    }

    private static final class FakeReviewService implements ReviewService {
        private final List<Review> reviews;

        private FakeReviewService(final List<Review> reviews) {
            this.reviews = reviews;
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
            return reviews;
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
        private final List<Car> cars = List.of(
                car(10L, "Porsche", "911 GT3 RS"),
                car(20L, "Honda", "Civic Type R"),
                car(30L, "Toyota", "GR86")
        );

        private static Car car(final long id, final String brand, final String model) {
            return new Car(
                    id,
                    1L,
                    brand,
                    model,
                    1L,
                    "Coupé",
                    "Track weapon",
                    LocalDateTime.now(),
                    true
            );
        }

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
            return cars.stream().filter(car -> ids.contains(car.getId())).toList();
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

    private static final class FakeUserFollowService implements UserFollowService {

        @Override
        public boolean followUser(final long followerId, final long followedId) {
            return false;
        }

        @Override
        public boolean unfollowUser(final long followerId, final long followedId) {
            return false;
        }

        @Override
        public boolean isFollowing(final long followerId, final long followedId) {
            return followedId == 7L;
        }

        @Override
        public long countFollowers(final long userId) {
            return 0;
        }

        @Override
        public long countFollowing(final long userId) {
            return 1;
        }

        @Override
        public List<User> getFollowers(final long userId) {
            return Collections.emptyList();
        }

        @Override
        public List<User> getFollowing(final long userId) {
            return List.of(new User(7L, "Alex Carrera", "alex@example.com", "password", "USER", LocalDateTime.now()));
        }
    }

    private static final class FakeCarFavoriteService implements CarFavoriteService {

        @Override
        public List<Long> findFavoriteCarIdsByUser(final long userId) {
            return List.of(20L);
        }

        @Override
        public boolean setFavorite(final long userId, final long carId, final boolean favorite) {
            return false;
        }

        @Override
        public boolean isFavorited(final long userId, final long carId) {
            return carId == 20L;
        }

        @Override
        public List<Car> getFavoriteCars(final long userId) {
            return Collections.emptyList();
        }

        @Override
        public Set<Long> getFavoritedCarIds(final long userId, final Collection<Long> carIds) {
            return Set.of(20L);
        }
    }
}
