package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.form.ReviewForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
public class CarReviewController {

    private static final String SORT_RATING_ASC = "rating_asc";
    private static final String SORT_RATING_DESC = "rating_desc";

    private static final BigDecimal RATING_STEP_DOUBLED = BigDecimal.valueOf(2);
    private static final Set<String> ALLOWED_OWNERSHIP_STATUSES =
            Set.of("", "Propietario actual", "Ex propietario");

    private final CarService carService;
    private final ReviewService reviewService;

    @Autowired
    public CarReviewController(final CarService carService, final ReviewService reviewService) {
        this.carService = carService;
        this.reviewService = reviewService;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "/reviews", method = RequestMethod.GET)
    public String reviewForm(@RequestParam(value = "carId", required = false) final Long carId,
                             @RequestParam(value = "sort", required = false) final String sort,
                             @ModelAttribute("reviewForm") final ReviewForm reviewForm,
                             final Model model) {
        if (carId == null) {
            return "redirect:/cars";
        }
        final ReviewPageData pageData = resolveReviewPageData(carId, sort);
        if (pageData == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El auto referenciado no existe.");
        }

        if (reviewForm.getCarId() == null) {
            reviewForm.setCarId(carId);
        }
        populateCarReviewPageModel(model, pageData);
        return "car-review.jsp";
    }

    @RequestMapping(value = "/reviews/feed", method = RequestMethod.GET)
    public ModelAndView reviewFeed(@RequestParam("carId") final long carId,
                                   @RequestParam(value = "sort", required = false) final String sort) {
        final ReviewPageData pageData = resolveReviewPageData(carId, sort);
        if (pageData == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El auto referenciado no existe.");
        }

        final ModelAndView mav = new ModelAndView("reviews-feed-fragment.jsp");
        mav.addObject("selectedCar", pageData.selectedCar);
        mav.addObject("reviews", pageData.reviews);
        mav.addObject("currentSort", pageData.currentSort);
        return mav;
    }

    @RequestMapping(value = "/reviews", method = RequestMethod.POST)
    public String createReview(@Valid @ModelAttribute("reviewForm") final ReviewForm reviewForm,
                               final BindingResult errors,
                               final Model model) {

        final Car car = reviewForm.getCarId() == null
                ? null
                : carService.getCarById(reviewForm.getCarId()).orElse(null);
        if (car == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El auto referenciado no existe.");
        }

        if (reviewForm.getRating() != null
                && reviewForm.getRating().multiply(RATING_STEP_DOUBLED).remainder(BigDecimal.ONE).signum() != 0) {
            errors.rejectValue("rating", "rating.step", "La puntuación debe ser múltiplo de 0,5.");
        }

        final String ownership = reviewForm.getOwnershipStatus() == null ? "" : reviewForm.getOwnershipStatus();
        if (!ALLOWED_OWNERSHIP_STATUSES.contains(ownership)) {
            errors.rejectValue("ownershipStatus", "ownership.invalid", "Estado de propiedad no válido.");
        }

        if (reviewForm.getModelYear() != null) {
            final int maxModelYear = Year.now().getValue() + 1;
            if (reviewForm.getModelYear() > maxModelYear) {
                errors.rejectValue("modelYear", "modelYear.range",
                        "Ingresá un año entre 1886 y " + maxModelYear + ".");
            }
        }

        if (errors.hasErrors()) {
            final ReviewPageData pageData = resolveReviewPageData(car.getId(), null);
            populateCarReviewPageModel(model, pageData);
            model.addAttribute("openReviewModal", true);
            return "car-review.jsp";
        }

        final String ownershipToPersist = ownership.isEmpty() ? null : ownership;
        reviewService.createReview(
                null,
                reviewForm.getReviewerEmail(),
                car.getId(),
                reviewForm.getRating(),
                reviewForm.getTitle(),
                reviewForm.getBody(),
                ownershipToPersist,
                reviewForm.getModelYear(),
                reviewForm.getMileageKm(),
                reviewForm.getWouldRecommend());

        return "redirect:/reviews?carId=" + car.getId();
    }

    private void populateCarReviewPageModel(final Model model, final ReviewPageData pageData) {
        model.addAttribute("selectedCar", pageData.selectedCar);
        model.addAttribute("reviews", pageData.reviews);
        model.addAttribute("averageRating", calculateAverageRating(pageData.reviews));
        model.addAttribute("reviewCount", pageData.reviews.size());
        model.addAttribute("currentSort", pageData.currentSort);
        model.addAttribute("latestReview", pageData.latestReview.orElse(null));
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
