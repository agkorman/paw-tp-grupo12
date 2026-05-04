package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.controller.support.RelativeTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class ActivityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityController.class);

    private static final String TAB_LATEST = "latest";
    private static final String TAB_FOLLOWING = "following";
    private static final String TAB_FAVORITES = "favorites";
    private static final int ACTIVITY_PAGE_SIZE = 10;

    private final ReviewService reviewService;
    private final CarService carService;
    private final RelativeTimeFormatter relativeTimeFormatter;

    @Autowired
    public ActivityController(final ReviewService reviewService, final CarService carService,
                              final RelativeTimeFormatter relativeTimeFormatter) {
        this.reviewService = reviewService;
        this.carService = carService;
        this.relativeTimeFormatter = relativeTimeFormatter;
    }

    @RequestMapping(value = "/activity", method = RequestMethod.GET)
    public ModelAndView activity(@AuthenticationPrincipal final AuthenticatedUser currentUser,
                                 @RequestParam(value = "tab", required = false) final String tab,
                                 @RequestParam(value = "page", defaultValue = "1") final int page) {
        final boolean authenticated = currentUser != null;
        final String activeTab = normalizeActivityTab(tab, authenticated);
        LOGGER.debug("rendering activity feed tab={} page={} authenticated={}", activeTab, page, authenticated);
        final int pageSize = ACTIVITY_PAGE_SIZE;

        final Page<Review> reviewsPage;
        if (TAB_FOLLOWING.equals(activeTab)) {
            reviewsPage = reviewService.getReviewsByFollowedUsers(currentUser.getId(), page, pageSize);
        } else if (TAB_FAVORITES.equals(activeTab)) {
            reviewsPage = reviewService.getReviewsByFavoriteCars(currentUser.getId(), page, pageSize);
        } else {
            reviewsPage = reviewService.getLatestReviews(page, pageSize);
        }

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
                        reviewPagesById.getOrDefault(review.getId(), Pagination.DEFAULT_PAGE),
                        relativeTimeFormatter.format(review.getCreatedAt())))
                .toList();
    }

    private String normalizeActivityTab(final String tab, final boolean authenticated) {
        if (tab == null || tab.isBlank()) {
            return TAB_LATEST;
        }
        final String normalized = tab.trim().toLowerCase(Locale.ROOT);
        if (authenticated && (TAB_FOLLOWING.equals(normalized) || TAB_FAVORITES.equals(normalized))) {
            return normalized;
        }
        return TAB_LATEST;
    }

    public static final class ActivityReviewCard {
        private final Review review;
        private final Car car;
        private final int reviewPage;
        private final String timeAgo;

        private ActivityReviewCard(final Review review, final Car car, final int reviewPage, final String timeAgo) {
            this.review = review;
            this.car = car;
            this.reviewPage = reviewPage;
            this.timeAgo = timeAgo;
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

        public String getTimeAgo() {
            return timeAgo;
        }
    }
}
