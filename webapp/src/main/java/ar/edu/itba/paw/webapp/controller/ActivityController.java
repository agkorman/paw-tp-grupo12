package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class ActivityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityController.class);

    private static final int ACTIVITY_PAGE_SIZE = 10;

    private final ReviewService reviewService;
    private final CarService carService;

    @Autowired
    public ActivityController(final ReviewService reviewService, final CarService carService) {
        this.reviewService = reviewService;
        this.carService = carService;
    }

    @RequestMapping(value = "/activity", method = RequestMethod.GET)
    public ModelAndView activity(@AuthenticationPrincipal final AuthenticatedUser currentUser,
                                 @RequestParam(value = "tab", required = false) final String tab,
                                 @RequestParam(value = "page", defaultValue = "1") final int page) {
        final boolean authenticated = currentUser != null;
        final String activeTab = normalizeActivityTab(tab, authenticated);
        LOGGER.debug("rendering activity feed tab={} page={} authenticated={}", activeTab, page, authenticated);

        final Page<Review> reviewsPage = reviewService.getActivityFeedReviews(
                activeTab, authenticated ? currentUser.getId() : null, page);

        final List<ActivityReviewCard> activityReviews = toActivityReviewCards(reviewsPage.getItems());
        final long latestCount = reviewService.countAllReviews();
        final long followedCount = authenticated
                ? reviewService.countReviewsByFollowedUsers(currentUser.getId())
                : 0L;
        final long favoriteCount = authenticated
                ? reviewService.countReviewsByFavoriteCars(currentUser.getId())
                : 0L;

        final ModelAndView mav = new ModelAndView("activity.jsp");
        mav.addObject("activeTab", activeTab);
        mav.addObject("latestCount", latestCount);
        mav.addObject("followedCount", followedCount);
        mav.addObject("favoriteCount", favoriteCount);
        mav.addObject("activityReviews", activityReviews);
        mav.addObject("activityCurrentPage", reviewsPage.getPageNumber());
        mav.addObject("activityTotalPages", reviewsPage.getTotalPages());
        return mav;
    }

    private List<ActivityReviewCard> toActivityReviewCards(final List<Review> reviews) {
        final Map<Long, Car> carsById = carService.getCarsByIds(reviews.stream().map(Review::getCarId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Car::getId, Function.identity(), (left, right) -> left));
        final Map<Long, Integer> reviewPagesById = reviewService.getDefaultPagesForReviewIds(
                reviews.stream().map(Review::getId).toList());
        return reviews
                .stream()
                .map(review -> new ActivityReviewCard(review, carsById.get(review.getCarId()),
                        reviewPagesById.getOrDefault(review.getId(), Pagination.DEFAULT_PAGE)))
                .toList();
    }

    private String normalizeActivityTab(final String tab, final boolean authenticated) {
        if (tab == null || tab.isBlank()) {
            return ReviewService.FEED_LATEST;
        }
        final String normalized = tab.trim().toLowerCase(Locale.ROOT);
        if (authenticated && (ReviewService.FEED_FOLLOWING.equals(normalized) || ReviewService.FEED_FAVORITES.equals(normalized))) {
            return normalized;
        }
        return ReviewService.FEED_LATEST;
    }

    public static final class ActivityReviewCard {
        private final Review review;
        private final Car car;
        private final int reviewPage;

        private ActivityReviewCard(final Review review, final Car car, final int reviewPage) {
            this.review = review;
            this.car = car;
            this.reviewPage = reviewPage;
        }

        public Review getReview() {
            return review;
        }

        public Car getCar() {
            return car;
        }

        public int getReviewPage() {
            return reviewPage;
        }

        public String getCarName() {
            if (car == null) {
                return "Auto no disponible";
            }
            return car.getBrandName() + " " + car.getModel();
        }

        public boolean getHasCarImage() {
            return car != null && car.getHasImage();
        }

        public String getAuthorName() {
            if (review.getReviewerUsername() != null && !review.getReviewerUsername().trim().isEmpty()) {
                return review.getReviewerUsername().trim();
            }
            if (review.getReviewerEmail() != null && !review.getReviewerEmail().trim().isEmpty()) {
                return review.getReviewerEmail().trim();
            }
            return "Usuario";
        }

        public String getAuthorInitials() {
            final String value = getAuthorName();
            final String[] parts = value.trim().split("\\s+");
            if (parts.length > 1) {
                return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase(Locale.ROOT);
            }
            return value.substring(0, Math.min(2, value.length())).toUpperCase(Locale.ROOT);
        }
    }
}
