package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Controller
public class CarReviewController {

    private static final Pattern SIMPLE_EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final String SORT_RATING_ASC = "rating_asc";
    private static final String SORT_RATING_DESC = "rating_desc";

    private final CarService carService;
    private final ReviewService reviewService;

    @Autowired
    public CarReviewController(final CarService carService, final ReviewService reviewService) {
        this.carService = carService;
        this.reviewService = reviewService;
    }

    @RequestMapping(value = "/reviews", method = RequestMethod.GET)
    public ModelAndView reviewForm(@RequestParam(value = "carId", required = false) final Long carId,
                                   @RequestParam(value = "sort", required = false) final String sort) {
        if (carId == null) {
            return new ModelAndView("redirect:/cars");
        }
        return carReviewPage(carId, sort, null);
    }

    @RequestMapping(value = "/reviews/feed", method = RequestMethod.GET)
    public ModelAndView reviewFeed(@RequestParam("carId") final long carId,
                                   @RequestParam(value = "sort", required = false) final String sort) {
        final ReviewPageData pageData = resolveReviewPageData(carId, sort);
        if (pageData == null) {
            return new ModelAndView("redirect:/cars");
        }

        final ModelAndView mav = new ModelAndView("reviews-feed-fragment.jsp");
        mav.addObject("selectedCar", pageData.selectedCar);
        mav.addObject("reviews", pageData.reviews);
        mav.addObject("currentSort", pageData.currentSort);
        return mav;
    }

    private ModelAndView carReviewPage(final long carId, final String sort, final String error) {
        final ReviewPageData pageData = resolveReviewPageData(carId, sort);
        if (pageData == null) {
            return new ModelAndView("redirect:/cars");
        }

        final ModelAndView mav = new ModelAndView("car-review.jsp");
        mav.addObject("selectedCar", pageData.selectedCar);
        mav.addObject("reviews", pageData.reviews);
        mav.addObject("averageRating", calculateAverageRating(pageData.reviews));
        mav.addObject("reviewCount", pageData.reviews.size());
        mav.addObject("currentSort", pageData.currentSort);
        mav.addObject("latestReview", pageData.latestReview.orElse(null));
        if (error != null) {
            mav.addObject("error", error);
        }
        return mav;
    }

    private ReviewPageData resolveReviewPageData(final long carId, final String sort) {
        final Car selectedCar = carService.getCarById(carId).orElse(null);
        if (selectedCar == null) {
            return null;
        }

        final String normalizedSort = normalizeSort(sort);
        final List<Review> reviews = getReviewsForCar(selectedCar.getId(), normalizedSort);
        final Optional<Review> latestReview = reviewService.getLatestReviewByCar(selectedCar.getId());
        return new ReviewPageData(selectedCar, reviews, normalizedSort, latestReview);
    }

    private List<Review> getReviewsForCar(final long carId, final String sort) {
        if (SORT_RATING_ASC.equals(sort)) {
            return reviewService.getReviewsByCarOrderByRatingAsc(carId);
        }
        if (SORT_RATING_DESC.equals(sort)) {
            return reviewService.getReviewsByCarOrderByRatingDesc(carId);
        }
        return reviewService.getReviewsByCar(carId);
    }

    private String normalizeSort(final String sort) {
        if (SORT_RATING_ASC.equals(sort) || SORT_RATING_DESC.equals(sort)) {
            return sort;
        }
        return null;
    }

    private BigDecimal calculateAverageRating(final List<Review> reviews) {
        if (reviews.isEmpty()) {
            return null;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (Review review : reviews) {
            if (review.getRating() != null) {
                sum = sum.add(review.getRating());
            }
        }

        return sum.divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP);
    }

    @RequestMapping(value = "/reviews", method = RequestMethod.POST)
    public ModelAndView createReview(@RequestParam("carId") final long carId,
                                     @RequestParam("reviewerEmail") final String reviewerEmail,
                                     @RequestParam("rating") final BigDecimal rating,
                                     @RequestParam("title") final String title,
                                     @RequestParam("body") final String body,
                                     @RequestParam(value = "ownershipStatus", required = false) final String ownershipStatus,
                                     @RequestParam(value = "modelYear", required = false) final Integer modelYear,
                                     @RequestParam(value = "mileageKm", required = false) final Integer mileageKm,
                                     @RequestParam(value = "wouldRecommend", required = false) final Boolean wouldRecommend) {

        // Server-side validation to prevent DB constraint violations
        // Validate rating: must be between 0 and 5 inclusive
        if (rating == null || rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(BigDecimal.valueOf(5)) > 0) {
            return carReviewPage(carId, null, "Rating must be between 0 and 5.");
        }

        final String normalizedEmail = reviewerEmail == null ? "" : reviewerEmail.trim();
        if (normalizedEmail.isEmpty()) {
            return carReviewPage(carId, null, "Email is required.");
        }
        if (normalizedEmail.length() > 100 || !SIMPLE_EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            return carReviewPage(carId, null, "Enter a valid email address.");
        }

        // Validate ownershipStatus length according to DB schema (e.g., VARCHAR(20))
        if (ownershipStatus != null && ownershipStatus.length() > 20) {
            return carReviewPage(carId, null, "Ownership status must be at most 20 characters long.");
        }

        reviewService.createReview(null, normalizedEmail, carId, rating, title, body, ownershipStatus, modelYear, mileageKm, wouldRecommend);
        return new ModelAndView("redirect:/reviews?carId=" + carId);
    }

    private static final class ReviewPageData {
        private final Car selectedCar;
        private final List<Review> reviews;
        private final String currentSort;
        private final Optional<Review> latestReview;

        private ReviewPageData(final Car selectedCar, final List<Review> reviews,
                               final String currentSort, final Optional<Review> latestReview) {
            this.selectedCar = selectedCar;
            this.reviews = reviews;
            this.currentSort = currentSort;
            this.latestReview = latestReview;
        }
    }
}
