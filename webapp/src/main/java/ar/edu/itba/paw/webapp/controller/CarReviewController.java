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
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Controller
public class CarReviewController {

    private static final String SORT_RATING_ASC = "rating_asc";
    private static final String SORT_RATING_DESC = "rating_desc";

    private static final Pattern SIMPLE_EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    // DB-protecting caps (VARCHAR / CHECK).
    private static final int MAX_EMAIL_LENGTH = 100;                 // reviews.reviewer_email VARCHAR(100)
    private static final int MAX_TITLE_LENGTH = 200;                 // reviews.title VARCHAR(200)
    private static final int MAX_OWNERSHIP_STATUS_LENGTH = 20;       // reviews.ownership_status VARCHAR(20)
    private static final BigDecimal MIN_RATING = BigDecimal.ZERO;    // reviews.rating CHECK ≥ 0.0
    private static final BigDecimal MAX_RATING = BigDecimal.valueOf(5); // reviews.rating CHECK ≤ 5.0

    // UX-tier rules mirrored from the frontend so bypassed clients still get
    // the same banner instead of polluting the table with junk values.
    private static final BigDecimal RATING_STEP_DOUBLED = BigDecimal.valueOf(2);
    private static final int MIN_MODEL_YEAR = 1886;
    private static final int MIN_MILEAGE_KM = 0;
    private static final int MAX_MILEAGE_KM = 2_000_000;
    private static final Set<String> ALLOWED_OWNERSHIP_STATUSES =
            Set.of("", "Propietario actual", "Ex propietario");

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
                                   @RequestParam(value = "reviewFormError", required = false) final String reviewFormError) {
        if (carId == null) {
            return new ModelAndView("redirect:/cars");
        }
        return carReviewPage(carId, sort, reviewFormError);
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

    private ModelAndView carReviewPage(final long carId, final String sort, final String reviewFormError) {
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
        if (reviewFormError != null) {
            mav.addObject("reviewFormError", reviewFormError);
        }
        return mav;
    }

    private ModelAndView redirectToReviewWithError(final long carId, final String error) {
        final String redirectUrl = UriComponentsBuilder.fromPath("/reviews")
                .queryParam("carId", carId)
                .queryParam("reviewFormError", error)
                .build()
                .encode()
                .toUriString();
        return new ModelAndView("redirect:" + redirectUrl);
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


        if (carService.getCarById(carId).isEmpty()) {
            return new ModelAndView("redirect:/cars");
        }

        if (rating == null || rating.compareTo(MIN_RATING) < 0 || rating.compareTo(MAX_RATING) > 0) {
            return redirectToReviewWithError(carId, "La puntuación debe estar entre 0 y 5.");
        }
        // Step 0.5 matches the half-star widget on the frontend.
        if (rating.multiply(RATING_STEP_DOUBLED).remainder(BigDecimal.ONE).signum() != 0) {
            return redirectToReviewWithError(carId, "La puntuación debe ser múltiplo de 0,5.");
        }

        final String normalizedEmail = reviewerEmail == null ? "" : reviewerEmail.trim();
        if (normalizedEmail.isEmpty() || normalizedEmail.length() > MAX_EMAIL_LENGTH) {
            return redirectToReviewWithError(carId, "El email es obligatorio.");
        }
        if (!SIMPLE_EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            return redirectToReviewWithError(carId, "Ingresá un email válido.");
        }

        final String trimmedTitle = title == null ? "" : title.trim();
        if (trimmedTitle.isEmpty() || trimmedTitle.length() > MAX_TITLE_LENGTH) {
            return redirectToReviewWithError(carId, "El título es obligatorio.");
        }

        final String trimmedBody = body == null ? "" : body.trim();
        if (trimmedBody.isEmpty()) {
            return redirectToReviewWithError(carId, "La descripción es obligatoria.");
        }

        final String normalizedOwnership = ownershipStatus == null ? "" : ownershipStatus.trim();
        if (normalizedOwnership.length() > MAX_OWNERSHIP_STATUS_LENGTH
                || !ALLOWED_OWNERSHIP_STATUSES.contains(normalizedOwnership)) {
            return redirectToReviewWithError(carId, "Estado de propiedad no válido.");
        }

        if (modelYear != null) {
            final int maxModelYear = Year.now().getValue() + 1;
            if (modelYear < MIN_MODEL_YEAR || modelYear > maxModelYear) {
                return redirectToReviewWithError(carId,
                        "Ingresá un año entre " + MIN_MODEL_YEAR + " y " + maxModelYear + ".");
            }
        }

        if (mileageKm != null && (mileageKm < MIN_MILEAGE_KM || mileageKm > MAX_MILEAGE_KM)) {
            return redirectToReviewWithError(carId, "Ingresá un kilometraje entre 0 y 2.000.000 km.");
        }

        final String ownershipToPersist = normalizedOwnership.isEmpty() ? null : normalizedOwnership;
        reviewService.createReview(null, normalizedEmail, carId, rating, trimmedTitle, trimmedBody,
                ownershipToPersist, modelYear, mileageKm, wouldRecommend);
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
