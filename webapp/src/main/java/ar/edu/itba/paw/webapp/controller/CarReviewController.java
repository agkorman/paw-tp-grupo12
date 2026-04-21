package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.form.CarForm;
import ar.edu.itba.paw.webapp.form.ReviewForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    private final BrandDao brandDao;
    private final BodyTypeDao bodyTypeDao;

    @Autowired
    public CarReviewController(final CarService carService, final ReviewService reviewService,
                               final BrandDao brandDao, final BodyTypeDao bodyTypeDao) {
        this.carService = carService;
        this.reviewService = reviewService;
        this.brandDao = brandDao;
        this.bodyTypeDao = bodyTypeDao;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "/reviews", method = RequestMethod.GET)
    public String reviewForm(@RequestParam(value = "carId", required = false) final Long carId,
                             @RequestParam(value = "sort", required = false) final String sort,
                             @RequestParam(value = "reviewForm", required = false) final String reviewFormParam,
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
        if ("true".equalsIgnoreCase(reviewFormParam)) {
            model.addAttribute("openReviewModal", true);
        }
        return "car-review.jsp";
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
                               final Model model,
                               @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            final Long carId = reviewForm.getCarId();
            return carId == null ? "redirect:/cars" : "redirect:/reviews/new?carId=" + carId;
        }

        final Car car = reviewForm.getCarId() == null
                ? null
                : carService.getCarById(reviewForm.getCarId()).orElse(null);
        if (car == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El auto referenciado no existe.");
        }

        rejectInvalidReviewFields(errors, reviewForm.getRating(), reviewForm.getOwnershipStatus(),
                reviewForm.getModelYear());

        if (errors.hasErrors()) {
            final ReviewPageData pageData = resolveReviewPageData(car.getId(), null);
            populateCarReviewPageModel(model, pageData);
            model.addAttribute("openReviewModal", true);
            return "car-review.jsp";
        }

        reviewService.createReview(
                currentUser.getId(),
                car.getId(),
                reviewForm.getRating(),
                reviewForm.getTitle(),
                reviewForm.getBody(),
                normalizeOwnershipStatus(reviewForm.getOwnershipStatus()),
                reviewForm.getModelYear(),
                reviewForm.getMileageKm(),
                reviewForm.getWouldRecommend());

        return "redirect:/reviews?carId=" + car.getId();
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
        populateCarReviewPageModel(mav, pageData);
        final ReviewForm reviewForm = new ReviewForm();
        reviewForm.setCarId(carId);
        mav.addObject("reviewForm", reviewForm);
        mav.addObject("openReviewModal", openReviewModal);
        if (error != null) {
            mav.addObject("error", error);
        }
        return mav;
    }

    private void populateCarReviewPageModel(final Model model, final ReviewPageData pageData) {
        model.addAttribute("selectedCar", pageData.selectedCar);
        model.addAttribute("reviews", pageData.reviews);
        model.addAttribute("averageRating", calculateAverageRating(pageData.reviews));
        model.addAttribute("reviewCount", pageData.reviews.size());
        model.addAttribute("currentSort", pageData.currentSort);
        model.addAttribute("latestReview", pageData.latestReview.orElse(null));
        model.addAttribute("brands", brandDao.findAll());
        model.addAttribute("bodyTypes", bodyTypeDao.findAll());
        model.addAttribute("carForm", new CarForm());
    }

    private void populateCarReviewPageModel(final ModelAndView mav, final ReviewPageData pageData) {
        mav.addObject("selectedCar", pageData.selectedCar);
        mav.addObject("reviews", pageData.reviews);
        mav.addObject("averageRating", calculateAverageRating(pageData.reviews));
        mav.addObject("reviewCount", pageData.reviews.size());
        mav.addObject("currentSort", pageData.currentSort);
        mav.addObject("latestReview", pageData.latestReview.orElse(null));
        mav.addObject("brands", brandDao.findAll());
        mav.addObject("bodyTypes", bodyTypeDao.findAll());
        mav.addObject("carForm", new CarForm());
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

        final String validationError = validateReviewInput(rating, title, body, ownershipStatus, modelYear);
        if (validationError != null) {
            return carReviewPage(existingReview.getCarId(), null, validationError, true);
        }

        reviewService.updateReview(
                reviewId,
                existingReview.getCarId(),
                rating,
                title,
                body,
                normalizeOwnershipStatus(ownershipStatus),
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

    private void rejectInvalidReviewFields(final BindingResult errors, final BigDecimal rating,
                                           final String ownershipStatus, final Integer modelYear) {
        if (rating != null && rating.multiply(RATING_STEP_DOUBLED).remainder(BigDecimal.ONE).signum() != 0) {
            errors.rejectValue("rating", "rating.step", "La puntuación debe ser múltiplo de 0,5.");
        }

        final String ownership = ownershipStatus == null ? "" : ownershipStatus;
        if (!ALLOWED_OWNERSHIP_STATUSES.contains(ownership)) {
            errors.rejectValue("ownershipStatus", "ownership.invalid", "Estado de propiedad no válido.");
        }

        if (modelYear != null) {
            final int maxModelYear = Year.now().getValue() + 1;
            if (modelYear > maxModelYear) {
                errors.rejectValue("modelYear", "modelYear.range",
                        "Ingresá un año entre 1886 y " + maxModelYear + ".");
            }
        }
    }

    private String validateReviewInput(final BigDecimal rating, final String title, final String body,
                                       final String ownershipStatus, final Integer modelYear) {
        if (rating == null || rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(BigDecimal.valueOf(5)) > 0) {
            return "La puntuación debe estar entre 0 y 5.";
        }
        if (rating.multiply(RATING_STEP_DOUBLED).remainder(BigDecimal.ONE).signum() != 0) {
            return "La puntuación debe ser múltiplo de 0,5.";
        }
        if (title == null || title.isEmpty() || title.length() > 200) {
            return "El título es obligatorio y debe tener como máximo 200 caracteres.";
        }
        if (body == null || body.isEmpty()) {
            return "La descripción es obligatoria.";
        }
        final String ownership = ownershipStatus == null ? "" : ownershipStatus;
        if (!ALLOWED_OWNERSHIP_STATUSES.contains(ownership)) {
            return "Estado de propiedad no válido.";
        }
        if (modelYear != null) {
            final int maxModelYear = Year.now().getValue() + 1;
            if (modelYear < 1886 || modelYear > maxModelYear) {
                return "Ingresá un año entre 1886 y " + maxModelYear + ".";
            }
        }
        return null;
    }

    private String normalizeOwnershipStatus(final String ownershipStatus) {
        return ownershipStatus == null || ownershipStatus.isEmpty() ? null : ownershipStatus;
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
