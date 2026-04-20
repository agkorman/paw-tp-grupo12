package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Controller
public class CarReviewController {

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
                                   @RequestParam(value = "sort", required = false) final String sort,
                                   @RequestParam(value = "reviewForm", required = false) final String reviewForm) {
        if (carId == null) {
            return new ModelAndView("redirect:/cars");
        }
        return carReviewPage(carId, sort, null, "true".equalsIgnoreCase(reviewForm));
    }

    @RequestMapping(value = "/reviews/new", method = RequestMethod.GET)
    public ModelAndView newReview(@RequestParam(value = "carId", required = false) final Long carId) {
        if (carId == null || carService.getCarById(carId).isEmpty()) {
            return new ModelAndView("redirect:/cars");
        }
        return new ModelAndView("redirect:/reviews?carId=" + carId + "&reviewForm=true");
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
        return carReviewPage(carId, sort, error, false);
    }

    private ModelAndView carReviewPage(final long carId, final String sort, final String error,
                                       final boolean openReviewModal) {
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
        mav.addObject("openReviewModal", openReviewModal);
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
                                     @RequestParam("rating") final BigDecimal rating,
                                     @RequestParam("title") final String title,
                                     @RequestParam("body") final String body,
                                     @RequestParam(value = "ownershipStatus", required = false) final String ownershipStatus,
                                     @RequestParam(value = "modelYear", required = false) final Integer modelYear,
                                     @RequestParam(value = "mileageKm", required = false) final Integer mileageKm,
                                     @RequestParam(value = "wouldRecommend", required = false) final Boolean wouldRecommend,
                                     @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/reviews/new?carId=" + carId);
        }

        final String validationError = validateReviewInput(rating, ownershipStatus);
        if (validationError != null) {
            return carReviewPage(carId, null, validationError);
        }

        reviewService.createReview(currentUser.getId(), carId, rating, title, body, ownershipStatus, modelYear, mileageKm, wouldRecommend);
        return new ModelAndView("redirect:/reviews?carId=" + carId);
    }

    @RequestMapping(value = "/reviews/{reviewId}", method = RequestMethod.POST)
    public ModelAndView updateReview(@PathVariable("reviewId") final long reviewId,
                                     @RequestParam("rating") final BigDecimal rating,
                                     @RequestParam("title") final String title,
                                     @RequestParam("body") final String body,
                                     @RequestParam(value = "ownershipStatus", required = false) final String ownershipStatus,
                                     @RequestParam(value = "modelYear", required = false) final Integer modelYear,
                                     @RequestParam(value = "mileageKm", required = false) final Integer mileageKm,
                                     @RequestParam(value = "wouldRecommend", required = false) final Boolean wouldRecommend,
                                     @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        final Review existingReview = reviewService.getReviewById(reviewId).orElse(null);
        validateReviewOwnership(existingReview, currentUser);

        final String validationError = validateReviewInput(rating, ownershipStatus);
        if (validationError != null) {
            return carReviewPage(existingReview.getCarId(), null, validationError, true);
        }


        reviewService.updateReview(
                reviewId,
                existingReview.getCarId(),
                rating,
                title,
                body,
                ownershipStatus,
                modelYear,
                mileageKm,
                wouldRecommend
        );
        return new ModelAndView("redirect:/profile");
    }

    @RequestMapping(value = "/reviews/{reviewId}/delete", method = RequestMethod.POST)
    public ModelAndView deleteReview(@PathVariable("reviewId") final long reviewId,
                                     @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        final Review existingReview = reviewService.getReviewById(reviewId).orElse(null);
        validateReviewOwnership(existingReview, currentUser);
        reviewService.deleteReview(reviewId);
        return new ModelAndView("redirect:/profile");
    }

    private void validateReviewOwnership(final Review review, final AuthenticatedUser currentUser) {
        if (review == null) {
            throw new ReviewNotFoundException();
        }
        if (currentUser == null || review.getUserId() == null || !review.getUserId().equals(currentUser.getId())) {
            throw new ReviewForbiddenException();
        }
    }

    private String validateReviewInput(final BigDecimal rating, final String ownershipStatus) {
        if (rating == null || rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(BigDecimal.valueOf(5)) > 0) {
            return "La puntuación debe estar entre 0 y 5.";
        }
        if (ownershipStatus != null && ownershipStatus.length() > 20) {
            return "El estado de propiedad debe tener como máximo 20 caracteres.";
        }
        return null;
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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static final class ReviewNotFoundException extends RuntimeException {
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    private static final class ReviewForbiddenException extends RuntimeException {
    }
}
