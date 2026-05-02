package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ActivityControllerTest {

    @Test
    public void activityReturnsActivityViewWithReviewCards() {
        final Review review = review(1L, 7L, 10L);
        final ActivityController controller = new ActivityController(
                new FakeReviewService(List.of(review)),
                new FakeCarService()
        );

        final ModelAndView mav = controller.activity(user(), null, 1);

        assertEquals("activity.jsp", mav.getViewName());
        final List<ActivityController.ActivityReviewCard> cards = activityCards(mav);
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
                new FakeCarService()
        );

        final ModelAndView latestMav = controller.activity(user(), "latest", 1);
        final ModelAndView followingMav = controller.activity(user(), "following", 1);
        final ModelAndView favoritesMav = controller.activity(user(), "favorites", 1);

        assertEquals(3, activityCards(latestMav).size());
        assertEquals(1, activityCards(followingMav).size());
        assertEquals(followedReview.getId(), activityCards(followingMav).get(0).getReview().getId());
        assertEquals(1, activityCards(favoritesMav).size());
        assertEquals(favoriteCarReview.getId(), activityCards(favoritesMav).get(0).getReview().getId());
    }

    @Test
    public void activityAllowsAnonymousUsersToSeeLatestReviewsOnly() {
        final Review review = review(1L, 7L, 10L);
        final ActivityController controller = new ActivityController(
                new FakeReviewService(List.of(review)),
                new FakeCarService()
        );

        final ModelAndView mav = controller.activity(null, null, 1);

        assertEquals("activity.jsp", mav.getViewName());
        assertEquals(1, activityCards(mav).size());
        assertEquals(0L, mav.getModel().get("followedCount"));
        assertEquals(0L, mav.getModel().get("favoriteCount"));
    }

    @Test
    public void activityReviewCardIncludesTargetReviewPage() {
        final List<Review> reviews = List.of(
                review(1L, 7L, 10L),
                review(2L, 7L, 10L),
                review(3L, 7L, 10L),
                review(4L, 7L, 10L),
                review(5L, 7L, 10L),
                review(6L, 7L, 10L)
        );
        final ActivityController controller = new ActivityController(
                new FakeReviewService(reviews),
                new FakeCarService()
        );

        final ModelAndView mav = controller.activity(user(), null, 1);

        assertEquals(2, activityCards(mav).stream()
                .filter(card -> card.getReview().getId() == 6L)
                .findFirst()
                .orElseThrow()
                .getReviewPage());
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
    private List<ActivityController.ActivityReviewCard> activityCards(final ModelAndView mav) {
        return (List<ActivityController.ActivityReviewCard>) mav.getModel().get("activityReviews");
    }

    private static final class FakeReviewService implements ReviewService {
        private final List<Review> reviews;

        private FakeReviewService(final List<Review> reviews) {
            this.reviews = reviews;
        }

        @Override
        public Review createReview(final long userId, final long carId, final BigDecimal rating, final String title,
                                   final String body, final String ownershipStatus, final Integer modelYear,
                                   final Integer mileageKm, final Boolean wouldRecommend,
                                   final Collection<Short> tagIds) {
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
                                             final Boolean wouldRecommend, final Collection<Short> tagIds) {
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
        public Page<Review> getReviewsByCar(final long carId, final int page) {
            return Page.empty(page, 0);
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
        public Page<Review> getReviewsByCarOrderByRatingAsc(final long carId, final int page) {
            return Page.empty(page, 0);
        }

        @Override
        public List<Review> getReviewsByCarOrderByRatingDesc(final long carId) {
            return Collections.emptyList();
        }

        @Override
        public Page<Review> getReviewsByCarOrderByRatingDesc(final long carId, final int page) {
            return Page.empty(page, 0);
        }

        @Override
        public List<Review> getReviewsByUser(final long userId) {
            return Collections.emptyList();
        }

        @Override
        public Page<Review> getReviewsByUser(final long userId, final int page) {
            return Page.empty(page, 0);
        }

        @Override
        public long countReviewsByUser(final long userId) {
            return 0L;
        }

        @Override
        public List<Review> getAllReviews() {
            return reviews;
        }

        @Override
        public Page<Review> getLatestReviews(final int page, final int pageSize) {
            return paginate(reviews, page, pageSize);
        }

        @Override
        public long countAllReviews() {
            return reviews.size();
        }

        @Override
        public Page<Review> getReviewsByFollowedUsers(final long followerId, final int page, final int pageSize) {
            return paginate(reviews.stream()
                    .filter(review -> review.getUserId() != null && review.getUserId() == 7L)
                    .toList(), page, pageSize);
        }

        @Override
        public long countReviewsByFollowedUsers(final long followerId) {
            return reviews.stream()
                    .filter(review -> review.getUserId() != null && review.getUserId() == 7L)
                    .count();
        }

        @Override
        public Page<Review> getReviewsByFavoriteCars(final long userId, final int page, final int pageSize) {
            return paginate(reviews.stream()
                    .filter(review -> review.getCarId() == 20L)
                    .toList(), page, pageSize);
        }

        @Override
        public long countReviewsByFavoriteCars(final long userId) {
            return reviews.stream()
                    .filter(review -> review.getCarId() == 20L)
                    .count();
        }

        @Override
        public Map<Long, Integer> getDefaultPagesForReviewIds(final Collection<Long> reviewIds) {
            return getDefaultPagesForReviews(reviews);
        }

        private Page<Review> paginate(final List<Review> items, final int page, final int pageSize) {
            if (items.isEmpty()) {
                return Page.empty(1, pageSize);
            }
            final int effectivePage = Math.max(1, Math.min(page, (items.size() + pageSize - 1) / pageSize));
            final int fromIndex = (effectivePage - 1) * pageSize;
            final int toIndex = Math.min(fromIndex + pageSize, items.size());
            return new Page<>(items.subList(fromIndex, toIndex), effectivePage, pageSize, items.size());
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
        public List<Car> getCarsByBrandAndBodyType(final String brand, final String bodyType) {
            return Collections.emptyList();
        }

        @Override
        public Page<Car> searchCars(final CarSearchCriteria criteria) {
            return Page.empty(1, 0);
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
            throw new UnsupportedOperationException();
        }

        @Override
        public void appendCarImages(final long carId, final List<CarImagePayload> images) {
            throw new UnsupportedOperationException();
        }

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
        public Optional<Car> updateCar(final long id, final long brandId, final String model, final long bodyTypeId,
                                       final Integer year, final String description, final Optional<String> imageContentType,
                                       final Optional<byte[]> imageData, final String fuelType,
                                       final Integer horsepower, final Integer airbagCount,
                                       final String transmission, final BigDecimal fuelConsumption,
                                       final Integer maxSpeedKmh, final BigDecimal priceUsd) {
            return Optional.empty();
        }

        @Override
        public boolean deleteCar(final long id) {
            return false;
        }
    }

}
