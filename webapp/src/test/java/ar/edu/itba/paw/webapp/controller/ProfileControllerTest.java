package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.UserFollowService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProfileControllerTest {

    @Test
    public void ownProfileMarksOwnReviewsAsEditable() {
        final ProfileController controller = controller(review(1L, 7L));

        final ModelAndView mav = controller.ownProfile(user(7L));

        final List<ProfileController.ProfileReviewCard> cards = reviewCards(mav);
        assertTrue(cards.get(0).getOwnedByCurrentUser());
    }

    @Test
    public void publicProfileDoesNotMarkOtherUsersReviewsAsEditable() {
        final ProfileController controller = controller(review(1L, 8L));

        final ModelAndView mav = controller.publicProfile(8L, user(7L));

        final List<ProfileController.ProfileReviewCard> cards = reviewCards(mav);
        assertFalse(cards.get(0).getOwnedByCurrentUser());
    }

    @Test
    public void profileReviewCardsForOtherUsersCannotExposeEditMenuState() {
        final ProfileController controller = controller(review(1L, 8L));

        final ModelAndView mav = controller.publicProfile(8L, user(7L));

        final List<ProfileController.ProfileReviewCard> likedReviews = likedReviewCards(mav);
        assertTrue(likedReviews.isEmpty());
        assertFalse(reviewCards(mav).get(0).getOwnedByCurrentUser());
    }

    @SuppressWarnings("unchecked")
    private List<ProfileController.ProfileReviewCard> reviewCards(final ModelAndView mav) {
        return (List<ProfileController.ProfileReviewCard>) mav.getModel().get("profileReviews");
    }

    @SuppressWarnings("unchecked")
    private List<ProfileController.ProfileReviewCard> likedReviewCards(final ModelAndView mav) {
        return (List<ProfileController.ProfileReviewCard>) mav.getModel().get("likedReviews");
    }

    private ProfileController controller(final Review review) {
        return new ProfileController(
                new FakeReviewService(review),
                new FakeCarService(),
                new FakeUserService(),
                new FakeUserFollowService()
        );
    }

    private AuthenticatedUser user(final long id) {
        return new AuthenticatedUser(id, "driver" + id, "driver" + id + "@example.com", "password", List.of());
    }

    private Review review(final long id, final Long userId) {
        return new Review(
                id,
                userId,
                null,
                "driver",
                10L,
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
            return review.getUserId() != null && review.getUserId() == userId
                    ? List.of(review)
                    : Collections.emptyList();
        }

        @Override
        public List<Review> getAllReviews() {
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

    private static final class FakeCarService implements CarService {
        @Override
        public List<Car> getAllCars() {
            throw new UnsupportedOperationException("ProfileController should fetch only reviewed cars.");
        }

        @Override
        public Optional<Car> getCarById(final long id) {
            return Optional.empty();
        }

        @Override
        public List<Car> getCarsByIds(final Collection<Long> ids) {
            return ids.contains(10L)
                    ? List.of(new Car(10L, 1L, "Toyota", "Supra", 1L, "Coupe", "Desc", LocalDateTime.now()))
                    : Collections.emptyList();
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
        public CarRequest requestCarCreation(final long brandId, final String model, final long bodyTypeId,
                                             final long submittedByUserId, final Optional<String> description,
                                             final Optional<String> imageContentType,
                                             final Optional<byte[]> imageData,
                                             final String fuelType, final Integer horsepower,
                                             final Integer airbagCount, final String transmission,
                                             final java.math.BigDecimal fuelConsumption,
                                             final Integer maxSpeedKmh) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Car> updateCar(final long id, final long brandId, final String model,
                                       final long bodyTypeId, final String description,
                                       final Optional<String> imageContentType,
                                       final Optional<byte[]> imageData,
                                       final String fuelType, final Integer horsepower,
                                       final Integer airbagCount, final String transmission,
                                       final java.math.BigDecimal fuelConsumption,
                                       final Integer maxSpeedKmh) {
            return Optional.empty();
        }

        @Override
        public boolean deleteCar(final long id) {
            return false;
        }
    }

    private static final class FakeUserService implements UserService {
        @Override
        public Optional<User> getUserById(final long id) {
            return Optional.of(new User(id, "driver" + id, "driver" + id + "@example.com", "password", "user", LocalDateTime.now()));
        }

        @Override
        public Optional<User> findByEmail(final String email) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByUsername(final String username) {
            return Optional.empty();
        }

        @Override
        public User createUser(final String username, final String email, final String rawPassword) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> getModeratorsEmails() {
            return Collections.emptyList();
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
            return false;
        }

        @Override
        public long countFollowers(final long userId) {
            return 0;
        }

        @Override
        public long countFollowing(final long userId) {
            return 0;
        }

        @Override
        public List<User> getFollowers(final long userId) {
            return Collections.emptyList();
        }

        @Override
        public List<User> getFollowing(final long userId) {
            return Collections.emptyList();
        }
    }
}
