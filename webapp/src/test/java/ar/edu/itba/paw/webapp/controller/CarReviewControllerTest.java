package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewLikeService;
import ar.edu.itba.paw.services.ReviewReplyService;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    @Test
    public void authenticatedUserCanReplyToReview() {
        final FakeReviewService reviewService = new FakeReviewService();
        reviewService.review = review(3L, 8L, 10L);
        final FakeReviewReplyService replyService = new FakeReviewReplyService();
        final CarReviewController controller = new CarReviewController(
                new FakeCarService(),
                new FakeCarFavoriteService(),
                reviewService,
                replyService,
                new FakeReviewLikeService(),
                new FakeBrandDao(),
                new FakeBodyTypeDao()
        );

        final ModelAndView mav = controller.createReply(3L, "Totalmente de acuerdo.", user(7L));

        assertEquals("redirect:/reviews?carId=10#review-3", mav.getViewName());
        assertTrue(replyService.created);
        assertEquals(3L, replyService.createdReviewId);
        assertEquals(7L, replyService.createdUserId);
    }

    @Test
    public void blankReplyDoesNotCallService() {
        final FakeReviewService reviewService = new FakeReviewService();
        reviewService.review = review(3L, 8L, 10L);
        final FakeReviewReplyService replyService = new FakeReviewReplyService();
        final CarReviewController controller = new CarReviewController(
                new FakeCarService(),
                new FakeCarFavoriteService(),
                reviewService,
                replyService,
                new FakeReviewLikeService(),
                new FakeBrandDao(),
                new FakeBodyTypeDao()
        );

        final ModelAndView mav = controller.createReply(3L, "   ", user(7L));

        assertEquals("car-review.jsp", mav.getViewName());
        assertFalse(replyService.created);
    }

    @Test
    public void authenticatedUserCanToggleReviewLike() {
        final FakeReviewService reviewService = new FakeReviewService();
        reviewService.review = review(3L, 8L, 10L);
        final FakeReviewLikeService likeService = new FakeReviewLikeService();
        final CarReviewController controller = new CarReviewController(
                new FakeCarService(),
                new FakeCarFavoriteService(),
                reviewService,
                new FakeReviewReplyService(),
                likeService,
                new FakeBrandDao(),
                new FakeBodyTypeDao()
        );

        final ModelAndView mav = controller.toggleReviewLike(3L, user(7L));

        assertEquals("redirect:/reviews?carId=10#review-3", mav.getViewName());
        assertEquals(3L, likeService.toggledReviewId);
        assertEquals(7L, likeService.toggledUserId);
    }

    @Test
    public void authenticatedUserCanToggleReplyLike() {
        final FakeReviewService reviewService = new FakeReviewService();
        reviewService.review = review(3L, 8L, 10L);
        final FakeReviewReplyService replyService = new FakeReviewReplyService();
        replyService.reply = new ReviewReply(4L, 3L, 8L, "driver", "Respuesta.",
                LocalDateTime.now(), LocalDateTime.now());
        final FakeReviewLikeService likeService = new FakeReviewLikeService();
        final CarReviewController controller = new CarReviewController(
                new FakeCarService(),
                new FakeCarFavoriteService(),
                reviewService,
                replyService,
                likeService,
                new FakeBrandDao(),
                new FakeBodyTypeDao()
        );

        final ModelAndView mav = controller.toggleReplyLike(4L, user(7L));

        assertEquals("redirect:/reviews?carId=10#review-3", mav.getViewName());
        assertEquals(4L, likeService.toggledReplyId);
        assertEquals(7L, likeService.toggledUserId);
    }

    private CarReviewController controller(final FakeReviewService reviewService) {
        return new CarReviewController(
                new FakeCarService(),
                new FakeCarFavoriteService(),
                reviewService,
                new FakeReviewReplyService(),
                new FakeReviewLikeService(),
                new FakeBrandDao(),
                new FakeBodyTypeDao()
        );
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
        public List<Car> getCarsByIds(final Collection<Long> ids) {
            return Collections.emptyList();
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
        public List<CarImage> getCarImagesByCarId(final long carId) {
            return Collections.emptyList();
        }

        @Override
        public Optional<CarImage> getCarImageById(final long carId, final long imageId) {
            return Optional.empty();
        }

        @Override
        public void saveCarImage(final long carId, final String contentType, final byte[] imageData) {
        }

        @Override
        public void saveCarImages(final long carId, final List<CarImagePayload> images) {
        }

        @Override
        public CarRequest requestCarCreation(final long brandId, final String model, final long bodyTypeId,
                                             final long submittedByUserId, final String submitterEmail,
                                             final Optional<String> description,
                                             final Optional<String> imageContentType,
                                             final Optional<byte[]> imageData,
                                             final String fuelType, final Integer horsepower,
                                             final Integer airbagCount, final String transmission,
                                             final java.math.BigDecimal fuelConsumption,
                                             final Integer maxSpeedKmh) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CarRequest requestCarCreation(final long brandId, final String model, final long bodyTypeId,
                                             final long submittedByUserId, final String submitterEmail,
                                             final Optional<String> description,
                                             final List<CarImagePayload> images,
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
        public List<Review> getReviewsByIds(final Collection<Long> ids) {
            if (review == null || ids == null || !ids.contains(review.getId())) {
                return Collections.emptyList();
            }
            return List.of(review);
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

    private static final class FakeReviewReplyService implements ReviewReplyService {
        private ReviewReply reply;
        private boolean created;
        private long createdReviewId;
        private long createdUserId;

        @Override
        public Optional<ReviewReply> getReplyById(final long id) {
            return reply != null && reply.getId() == id ? Optional.of(reply) : Optional.empty();
        }

        @Override
        public List<ReviewReply> getRepliesByIds(final Collection<Long> ids) {
            if (reply == null || ids == null || !ids.contains(reply.getId())) {
                return Collections.emptyList();
            }
            return List.of(reply);
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
            this.created = true;
            this.createdReviewId = reviewId;
            this.createdUserId = userId;
            return new ReviewReply(1L, reviewId, userId, "driver", body, LocalDateTime.now(), LocalDateTime.now());
        }

        @Override
        public boolean deleteReply(final long id, final long userId) {
            return false;
        }

        @Override
        public Map<Long, Long> countNewRepliesPerReview(final long userId, final LocalDateTime since) {
            return Collections.emptyMap();
        }
    }

    private static final class FakeReviewLikeService implements ReviewLikeService {
        private long toggledReviewId;
        private long toggledReplyId;
        private long toggledUserId;

        @Override
        public boolean toggleReviewLike(final long reviewId, final long userId) {
            this.toggledReviewId = reviewId;
            this.toggledUserId = userId;
            return true;
        }

        @Override
        public boolean toggleReplyLike(final long replyId, final long userId) {
            this.toggledReplyId = replyId;
            this.toggledUserId = userId;
            return true;
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
            return Collections.emptySet();
        }

        @Override
        public List<Long> getLikedReplyIdsByUser(final long userId) {
            return Collections.emptyList();
        }

        @Override
        public Map<Long, Long> countNewLikesPerReview(final long userId, final LocalDateTime since) {
            return Collections.emptyMap();
        }
    }

    private static final class FakeBrandDao implements BrandDao {
        @Override
        public List<Brand> findAll() {
            return Collections.emptyList();
        }

        @Override
        public Optional<Brand> findById(final long id) {
            return Optional.empty();
        }

        @Override
        public Optional<Brand> findByName(final String name) {
            return Optional.empty();
        }

        @Override
        public Brand create(final String name) {
            throw new UnsupportedOperationException("Not needed by this test fake.");
        }
    }

    private static final class FakeBodyTypeDao implements BodyTypeDao {
        @Override
        public List<BodyType> findAll() {
            return Collections.emptyList();
        }

        @Override
        public Optional<BodyType> findById(final long id) {
            return Optional.empty();
        }

        @Override
        public Optional<BodyType> findByName(final String name) {
            return Optional.empty();
        }

        @Override
        public BodyType create(final String name) {
            throw new UnsupportedOperationException("Not needed by this test fake.");
        }
    }
}
