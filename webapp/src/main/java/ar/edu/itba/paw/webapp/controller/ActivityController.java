package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.UserFollowService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class ActivityController {

    private static final String TAB_LATEST = "latest";
    private static final String TAB_FOLLOWING = "following";
    private static final String TAB_FAVORITES = "favorites";
    private static final int ACTIVITY_PAGE_SIZE = 6;

    private final ReviewService reviewService;
    private final CarService carService;
    private final UserFollowService userFollowService;
    private final CarFavoriteService carFavoriteService;

    @Autowired
    public ActivityController(final ReviewService reviewService, final CarService carService,
                              final UserFollowService userFollowService,
                              final CarFavoriteService carFavoriteService) {
        this.reviewService = reviewService;
        this.carService = carService;
        this.userFollowService = userFollowService;
        this.carFavoriteService = carFavoriteService;
    }

    @RequestMapping(value = "/activity", method = RequestMethod.GET)
    public ModelAndView activity(@AuthenticationPrincipal final AuthenticatedUser currentUser,
                                 @RequestParam(value = "tab", required = false) final String tab,
                                 @RequestParam(value = "page", defaultValue = "1") final int page) {
        final boolean authenticated = currentUser != null;
        final String activeTab = normalizeActivityTab(tab, authenticated);

        final List<Review> reviews = reviewService.getAllReviews()
                .stream()
                .sorted(Comparator.<Review, LocalDateTime>comparing(
                        review -> review.getCreatedAt() == null ? LocalDateTime.MIN : review.getCreatedAt(),
                        Comparator.reverseOrder()
                ).thenComparing((left, right) -> Long.compare(right.getId(), left.getId())))
                .toList();
        final Map<Long, Car> carsById = carService.getCarsByIds(reviews.stream().map(Review::getCarId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Car::getId, Function.identity(), (left, right) -> left));
        final Map<Long, Integer> reviewPagesById = reviewService.getDefaultPagesForReviews(reviews);
        final List<ActivityReviewCard> allCards = reviews
                .stream()
                .map(review -> new ActivityReviewCard(review, carsById.get(review.getCarId()),
                        reviewPagesById.getOrDefault(review.getId(), Pagination.DEFAULT_PAGE),
                        timeAgo(review.getCreatedAt())))
                .toList();

        final Set<Long> followedUserIds = currentUser == null
                ? Set.of()
                : userFollowService.getFollowing(currentUser.getId())
                    .stream()
                    .map(User::getId)
                    .collect(Collectors.toSet());
        final Set<Long> favoriteCarIds = currentUser == null
                ? Set.of()
                : Set.copyOf(carFavoriteService.findFavoriteCarIdsByUser(currentUser.getId()));

        final List<ActivityReviewCard> followedCards = allCards.stream()
                .filter(card -> card.getReview().getUserId() != null && followedUserIds.contains(card.getReview().getUserId()))
                .toList();
        final List<ActivityReviewCard> favoriteCards = allCards.stream()
                .filter(card -> favoriteCarIds.contains(card.getReview().getCarId()))
                .toList();

        final int pageSize = ACTIVITY_PAGE_SIZE;
        final Page<ActivityReviewCard> activePage;
        if (TAB_FOLLOWING.equals(activeTab)) {
            activePage = paginateList(followedCards, page, pageSize);
        } else if (TAB_FAVORITES.equals(activeTab)) {
            activePage = paginateList(favoriteCards, page, pageSize);
        } else {
            activePage = paginateList(allCards, page, pageSize);
        }

        final ModelAndView mav = new ModelAndView("activity.jsp");
        mav.addObject("activeTab", activeTab);
        mav.addObject("latestCount", allCards.size());
        mav.addObject("followedCount", followedCards.size());
        mav.addObject("favoriteCount", favoriteCards.size());
        mav.addObject("activityReviews", activePage.getItems());
        mav.addObject("activityCurrentPage", activePage.getPageNumber());
        mav.addObject("activityTotalPages", activePage.getTotalPages());
        return mav;
    }

    private Page<ActivityReviewCard> paginateList(final List<ActivityReviewCard> list, final int page, final int pageSize) {
        if (list.isEmpty()) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }
        final int effectivePage = Pagination.clampPage(Pagination.normalizePage(page), list.size(), pageSize);
        final int fromIndex = (int) Pagination.offsetFor(effectivePage, pageSize);
        final int toIndex = Math.min(fromIndex + pageSize, list.size());
        return new Page<>(list.subList(fromIndex, toIndex), effectivePage, pageSize, list.size());
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

    private String timeAgo(final LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }

        final Duration elapsed = Duration.between(createdAt, LocalDateTime.now());
        if (elapsed.toMinutes() < 1) {
            return "recién";
        }
        if (elapsed.toHours() < 1) {
            final long minutes = elapsed.toMinutes();
            return "hace " + minutes + " " + (minutes == 1 ? "minuto" : "minutos");
        }
        if (elapsed.toDays() < 1) {
            final long hours = elapsed.toHours();
            return "hace " + hours + " " + (hours == 1 ? "hora" : "horas");
        }
        if (elapsed.toDays() < 30) {
            final long days = elapsed.toDays();
            return "hace " + days + " " + (days == 1 ? "día" : "días");
        }
        if (elapsed.toDays() < 365) {
            final long months = elapsed.toDays() / 30;
            return "hace " + months + " " + (months == 1 ? "mes" : "meses");
        }

        final long years = elapsed.toDays() / 365;
        return "hace " + years + " " + (years == 1 ? "año" : "años");
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
