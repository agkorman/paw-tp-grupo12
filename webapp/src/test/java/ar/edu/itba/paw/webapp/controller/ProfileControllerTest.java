package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.AdminRequest;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.services.AdminRequestService;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewLikeService;
import ar.edu.itba.paw.services.ReviewReplyService;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
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

    @Test
    public void ownProfileIncludesLikedReplies() {
        final Review review = review(1L, 8L);
        final ReviewReply reply = new ReviewReply(
                20L,
                review.getId(),
                8L,
                "driver8",
                "Buena respuesta.",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        final ProfileController controller = controller(review, reply);

        final ModelAndView mav = controller.ownProfile(user(7L));

        final List<ProfileController.ProfileLikedReplyCard> likedReplies = likedReplyCards(mav);
        assertEquals(1, likedReplies.size());
        assertEquals(reply.getId(), likedReplies.get(0).getReply().getId());
        assertTrue(likedReplies.get(0).getLiked());
    }

    @SuppressWarnings("unchecked")
    private List<ProfileController.ProfileReviewCard> reviewCards(final ModelAndView mav) {
        return (List<ProfileController.ProfileReviewCard>) mav.getModel().get("profileReviews");
    }

    @SuppressWarnings("unchecked")
    private List<ProfileController.ProfileReviewCard> likedReviewCards(final ModelAndView mav) {
        return (List<ProfileController.ProfileReviewCard>) mav.getModel().get("likedReviews");
    }

    @SuppressWarnings("unchecked")
    private List<ProfileController.ProfileLikedReplyCard> likedReplyCards(final ModelAndView mav) {
        return (List<ProfileController.ProfileLikedReplyCard>) mav.getModel().get("likedReplies");
    }

    private ProfileController controller(final Review review) {
        return controller(review, null);
    }

    private ProfileController controller(final Review review, final ReviewReply likedReply) {
        return new ProfileController(
                new FakeReviewService(review),
                new FakeReviewLikeService(likedReply == null ? Collections.emptyList() : List.of(likedReply.getId())),
                new FakeReviewReplyService(likedReply),
                new FakeCarService(),
                new FakeCarFavoriteService(),
                new FakeUserService(),
                new FakeUserFollowService(),
                new FakeAdminRequestService()
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
                                   final Integer mileageKm, final Boolean wouldRecommend,
                                   final Collection<Short> tagIds) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Review> getAllReviews() {
            return review == null ? Collections.emptyList() : List.of(review);
        }

        @Override
        public Optional<Review> getReviewById(final long id) {
            return review.getId() == id ? Optional.of(review) : Optional.empty();
        }

        @Override
        public List<Review> getReviewsByIds(final Collection<Long> ids) {
            if (ids == null || !ids.contains(review.getId())) {
                return Collections.emptyList();
            }
            return List.of(review);
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
        public ar.edu.itba.paw.model.Page<Review> getReviewsByCar(final long carId, final int page) {
            return ar.edu.itba.paw.model.Page.empty(page, 0);
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
        public ar.edu.itba.paw.model.Page<Review> getReviewsByCarOrderByRatingAsc(final long carId, final int page) {
            return ar.edu.itba.paw.model.Page.empty(page, 0);
        }

        @Override
        public List<Review> getReviewsByCarOrderByRatingDesc(final long carId) {
            return Collections.emptyList();
        }

        @Override
        public ar.edu.itba.paw.model.Page<Review> getReviewsByCarOrderByRatingDesc(final long carId, final int page) {
            return ar.edu.itba.paw.model.Page.empty(page, 0);
        }

        @Override
        public List<Review> getReviewsByUser(final long userId) {
            return review.getUserId() != null && review.getUserId() == userId
                    ? List.of(review)
                    : Collections.emptyList();
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

    private static final class FakeReviewLikeService implements ReviewLikeService {
        private final List<Long> likedReplyIds;

        private FakeReviewLikeService(final List<Long> likedReplyIds) {
            this.likedReplyIds = likedReplyIds;
        }

        @Override
        public boolean toggleReviewLike(final long reviewId, final long userId) {
            return false;
        }

        @Override
        public boolean toggleReplyLike(final long replyId, final long userId) {
            return false;
        }

        @Override
        public long countReviewLikes(final long reviewId) {
            return 0;
        }

        @Override
        public Map<Long, Long> countReviewLikesByReviewIds(final Collection<Long> reviewIds) {
            return Collections.emptyMap();
        }

        @Override
        public Set<Long> getLikedReviewIds(final Collection<Long> reviewIds, final long userId) {
            return Collections.emptySet();
        }

        @Override
        public List<Long> getLikedReviewIdsByUser(final long userId) {
            return Collections.emptyList();
        }

        @Override
        public long countReplyLikes(final long replyId) {
            return 0;
        }

        @Override
        public Map<Long, Long> countReplyLikesByReplyIds(final Collection<Long> replyIds) {
            return Collections.emptyMap();
        }

        @Override
        public Set<Long> getLikedReplyIds(final Collection<Long> replyIds, final long userId) {
            return replyIds == null ? Collections.emptySet() : Set.copyOf(replyIds);
        }

        @Override
        public List<Long> getLikedReplyIdsByUser(final long userId) {
            return likedReplyIds;
        }

        @Override
        public Map<Long, Long> countNewLikesPerReview(final long userId, final LocalDateTime since) {
            return Collections.emptyMap();
        }
    }

    private static final class FakeReviewReplyService implements ReviewReplyService {
        private final ReviewReply likedReply;

        private FakeReviewReplyService(final ReviewReply likedReply) {
            this.likedReply = likedReply;
        }

        @Override
        public Optional<ReviewReply> getReplyById(final long id) {
            return likedReply != null && likedReply.getId() == id ? Optional.of(likedReply) : Optional.empty();
        }

        @Override
        public List<ReviewReply> getRepliesByIds(final Collection<Long> ids) {
            if (likedReply == null || ids == null || !ids.contains(likedReply.getId())) {
                return Collections.emptyList();
            }
            return List.of(likedReply);
        }

        @Override
        public List<ReviewReply> getRepliesByReview(final long reviewId) {
            return Collections.emptyList();
        }

        @Override
        public Map<Long, List<ReviewReply>> getRepliesByReviewIds(final Collection<Long> reviewIds) {
            return Collections.emptyMap();
        }

        @Override
        public ReviewReply createReply(final long reviewId, final long userId, final String body) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean deleteReply(final long id, final long userId) {
            return false;
        }

        @Override
        public boolean deleteReplyAsAdmin(final long id) {
            return false;
        }

        @Override
        public Map<Long, Long> countNewRepliesPerReview(final long userId, final LocalDateTime since) {
            return Collections.emptyMap();
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
        public List<Car> getCarsByBrandAndBodyType(final String brand, final String bodyType) {
            return Collections.emptyList();
        }

        @Override
        public ar.edu.itba.paw.model.Page<Car> searchCars(final ar.edu.itba.paw.model.CarSearchCriteria criteria) {
            return ar.edu.itba.paw.model.Page.empty(1, 0);
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
                                             final Integer year, final long submittedByUserId, final String submitterEmail,
                                             final Optional<String> description,
                                             final List<CarImagePayload> images,
                                             final String fuelType, final Integer horsepower,
                                             final Integer airbagCount, final String transmission,
                                             final java.math.BigDecimal fuelConsumption,
                                             final Integer maxSpeedKmh, final java.math.BigDecimal priceUsd) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Car> updateCar(final long id, final long brandId, final String model,
                                       final long bodyTypeId, final Integer year, final String description,
                                       final Optional<String> imageContentType,
                                       final Optional<byte[]> imageData,
                                       final String fuelType, final Integer horsepower,
                                       final Integer airbagCount, final String transmission,
                                       final java.math.BigDecimal fuelConsumption,
                                       final Integer maxSpeedKmh, final java.math.BigDecimal priceUsd) {
            return Optional.empty();
        }

        @Override
        public boolean deleteCar(final long id) {
            return false;
        }
    }

    private static final class FakeCarFavoriteService implements CarFavoriteService {
        @Override
        public List<Long> findFavoriteCarIdsByUser(final long userId) {
            return Collections.emptyList();
        }

        @Override
        public boolean setFavorite(final long userId, final long carId, final boolean favorite) {
            return false;
        }

        @Override
        public boolean isFavorited(final long userId, final long carId) {
            return false;
        }

        @Override
        public List<Car> getFavoriteCars(final long userId) {
            return Collections.emptyList();
        }

        @Override
        public java.util.Set<Long> getFavoritedCarIds(final long userId, final Collection<Long> carIds) {
            return Collections.emptySet();
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

        @Override
        public List<User> getAllUsers() {
            return Collections.emptyList();
        }

        @Override
        public boolean updateRole(final long userId, final String role) {
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

    private static final class FakeAdminRequestService implements AdminRequestService {
        @Override
        public Optional<AdminRequest> getAdminRequestById(final long id) {
            return Optional.empty();
        }

        @Override
        public List<AdminRequest> getAdminRequestsByStatus(final String status) {
            return Collections.emptyList();
        }

        @Override
        public Page<AdminRequest> getAdminRequestsByStatus(final String status, final int page) {
            return Page.empty(1, 0);
        }

        @Override
        public long countAdminRequestsByStatus(final String status) {
            return 0L;
        }

        @Override
        public boolean hasPendingRequest(final long userId) {
            return false;
        }

        @Override
        public AdminRequest createPendingRequest(final long submittedByUserId, final String submitterEmail,
                                                 final String motivation, final String bio,
                                                 final String justification) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean approvePendingRequest(final long id) {
            return false;
        }

        @Override
        public boolean rejectPendingRequest(final long id) {
            return false;
        }
    }
}
