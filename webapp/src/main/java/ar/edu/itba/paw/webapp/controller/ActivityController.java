package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
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
    public ModelAndView activity(@AuthenticationPrincipal final AuthenticatedUser currentUser) {
        final List<Review> reviews = reviewService.getAllReviews()
                .stream()
                .sorted(Comparator.comparing(
                        review -> review.getCreatedAt() == null ? LocalDateTime.MIN : review.getCreatedAt(),
                        Comparator.reverseOrder()
                ))
                .toList();
        final Map<Long, Car> carsById = carService.getCarsByIds(reviews.stream().map(Review::getCarId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Car::getId, Function.identity(), (left, right) -> left));
        final List<ActivityReviewCard> activityReviews = reviews
                .stream()
                .map(review -> new ActivityReviewCard(review, carsById.get(review.getCarId()), timeAgo(review.getCreatedAt())))
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

        final ModelAndView mav = new ModelAndView("activity.jsp");
        mav.addObject("latestActivityReviews", activityReviews);
        mav.addObject("followedActivityReviews", activityReviews
                .stream()
                .filter(card -> card.getReview().getUserId() != null && followedUserIds.contains(card.getReview().getUserId()))
                .toList());
        mav.addObject("favoriteCarActivityReviews", activityReviews
                .stream()
                .filter(card -> favoriteCarIds.contains(card.getReview().getCarId()))
                .toList());
        return mav;
    }

    public static String timeAgo(final LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }

        final Duration elapsed = Duration.between(createdAt, LocalDateTime.now());
        if (elapsed.isNegative() || elapsed.toMinutes() < 1) {
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
        private final String timeAgo;

        private ActivityReviewCard(final Review review, final Car car, final String timeAgo) {
            this.review = review;
            this.car = car;
            this.timeAgo = timeAgo;
        }

        public Review getReview() {
            return review;
        }

        public Car getCar() {
            return car;
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
