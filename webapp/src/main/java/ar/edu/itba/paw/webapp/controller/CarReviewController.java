package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewLikeService;
import ar.edu.itba.paw.services.ReviewReplyService;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class CarReviewController {

    private static final String SORT_RATING_ASC = "rating_asc";
    private static final String SORT_RATING_DESC = "rating_desc";
    private static final int MAX_REPLY_BODY_LENGTH = 1000;

    private static final BigDecimal RATING_STEP_DOUBLED = BigDecimal.valueOf(2);
    private static final Set<String> ALLOWED_OWNERSHIP_STATUSES =
            Set.of("", "Propietario actual", "Ex propietario");

    private final CarService carService;
    private final CarFavoriteService carFavoriteService;
    private final ReviewService reviewService;
    private final ReviewReplyService reviewReplyService;
    private final ReviewLikeService reviewLikeService;
    private final BrandDao brandDao;
    private final BodyTypeDao bodyTypeDao;

    @Autowired
    public CarReviewController(final CarService carService, final CarFavoriteService carFavoriteService,
                               final ReviewService reviewService,
                               final ReviewReplyService reviewReplyService,
                               final ReviewLikeService reviewLikeService,
                               final BrandDao brandDao, final BodyTypeDao bodyTypeDao) {
        this.carService = carService;
        this.carFavoriteService = carFavoriteService;
        this.reviewService = reviewService;
        this.reviewReplyService = reviewReplyService;
        this.reviewLikeService = reviewLikeService;
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
                             @AuthenticationPrincipal final AuthenticatedUser currentUser,
                             final Model model) {
        if (carId == null) {
            return "redirect:/cars";
        }

        final ReviewPageData pageData = resolveReviewPageData(carId, sort, currentUserId(currentUser));
        if (pageData == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El auto referenciado no existe.");
        }

        if (reviewForm.getCarId() == null) {
            reviewForm.setCarId(carId);
        }
        populateCarReviewPageModel(model, pageData, currentUser);
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
                                   @RequestParam(value = "sort", required = false) final String sort,
                                   @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        final ReviewPageData pageData = resolveReviewPageData(carId, sort, currentUserId(currentUser));
        if (pageData == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El auto referenciado no existe.");
        }

        final ModelAndView mav = new ModelAndView("reviews-feed-fragment.jsp");
        mav.addObject("selectedCar", pageData.selectedCar);
        mav.addObject("reviews", pageData.reviews);
        mav.addObject("reviewThreads", pageData.reviewThreads);
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
            final ReviewPageData pageData = resolveReviewPageData(car.getId(), null, currentUserId(currentUser));
            populateCarReviewPageModel(model, pageData, currentUser);
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
        return carReviewPage(carId, sort, error, false, null);
    }

    private ModelAndView carReviewPage(final long carId, final String sort, final String error,
                                       final boolean openReviewModal, final AuthenticatedUser currentUser) {
        final ReviewPageData pageData = resolveReviewPageData(carId, sort, currentUserId(currentUser));
        if (pageData == null) {
            return new ModelAndView("redirect:/cars");
        }

        final ModelAndView mav = new ModelAndView("car-review.jsp");
        populateCarReviewPageModel(mav, pageData, currentUser);
        final ReviewForm reviewForm = new ReviewForm();
        reviewForm.setCarId(carId);
        mav.addObject("reviewForm", reviewForm);
        mav.addObject("openReviewModal", openReviewModal);
        if (error != null) {
            mav.addObject("error", error);
            mav.addObject("replyError", error);
        }
        return mav;
    }

    private void populateCarReviewPageModel(final Model model, final ReviewPageData pageData,
                                            final AuthenticatedUser currentUser) {
        model.addAttribute("selectedCar", pageData.selectedCar);
        model.addAttribute("selectedCarFavorited", isSelectedCarFavorited(pageData.selectedCar, currentUser));
        model.addAttribute("reviews", pageData.reviews);
        model.addAttribute("reviewThreads", pageData.reviewThreads);
        model.addAttribute("averageRating", calculateAverageRating(pageData.reviews));
        model.addAttribute("reviewCount", pageData.reviews.size());
        model.addAttribute("currentSort", pageData.currentSort);
        model.addAttribute("latestReview", pageData.latestReview.orElse(null));
        model.addAttribute("latestReviewLikeCount", pageData.latestReviewLikeCount);
        model.addAttribute("latestReviewLiked", pageData.latestReviewLiked);
        model.addAttribute("carImages", pageData.carImages);
        model.addAttribute("brands", brandDao.findAll());
        model.addAttribute("bodyTypes", bodyTypeDao.findAll());
        model.addAttribute("carForm", new CarForm());
    }

    private void populateCarReviewPageModel(final ModelAndView mav, final ReviewPageData pageData,
                                            final AuthenticatedUser currentUser) {
        mav.addObject("selectedCar", pageData.selectedCar);
        mav.addObject("selectedCarFavorited", isSelectedCarFavorited(pageData.selectedCar, currentUser));
        mav.addObject("reviews", pageData.reviews);
        mav.addObject("reviewThreads", pageData.reviewThreads);
        mav.addObject("averageRating", calculateAverageRating(pageData.reviews));
        mav.addObject("reviewCount", pageData.reviews.size());
        mav.addObject("currentSort", pageData.currentSort);
        mav.addObject("latestReview", pageData.latestReview.orElse(null));
        mav.addObject("latestReviewLikeCount", pageData.latestReviewLikeCount);
        mav.addObject("latestReviewLiked", pageData.latestReviewLiked);
        mav.addObject("carImages", pageData.carImages);
        mav.addObject("brands", brandDao.findAll());
        mav.addObject("bodyTypes", bodyTypeDao.findAll());
        mav.addObject("carForm", new CarForm());
    }

    private ReviewPageData resolveReviewPageData(final long carId, final String sort, final Long currentUserId) {
        final Car selectedCar = carService.getCarById(carId).orElse(null);
        if (selectedCar == null) {
            return null;
        }

        final String normalizedSort = normalizeSort(sort);
        final List<Review> reviews = getReviewsForCar(selectedCar.getId(), normalizedSort);
        final Optional<Review> latestReview = reviewService.getLatestReviewByCar(selectedCar.getId());
        final List<CarImage> carImages = carService.getCarImagesByCarId(selectedCar.getId());
        final List<ReviewThread> reviewThreads = buildReviewThreads(reviews, currentUserId);
        final long latestReviewLikeCount = latestReview
                .map(review -> reviewLikeService.countReviewLikes(review.getId()))
                .orElse(0L);
        final boolean latestReviewLiked = currentUserId != null
                && latestReview
                .map(review -> reviewLikeService.getLikedReviewIds(List.of(review.getId()), currentUserId)
                        .contains(review.getId()))
                .orElse(false);
        return new ReviewPageData(selectedCar, reviews, reviewThreads, normalizedSort, latestReview, carImages,
                latestReviewLikeCount, latestReviewLiked);
    }

    private List<ReviewThread> buildReviewThreads(final List<Review> reviews, final Long currentUserId) {
        if (reviews.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Long> reviewIds = reviews.stream()
                .map(Review::getId)
                .toList();
        final Map<Long, List<ReviewReply>> repliesByReviewId = reviewReplyService.getRepliesByReviewIds(reviewIds);
        final Map<Long, Long> reviewLikeCounts = reviewLikeService.countReviewLikesByReviewIds(reviewIds);
        final Set<Long> likedReviewIds = currentUserId == null
                ? Collections.emptySet()
                : reviewLikeService.getLikedReviewIds(reviewIds, currentUserId);

        final List<Long> replyIds = repliesByReviewId.values()
                .stream()
                .flatMap(List::stream)
                .map(ReviewReply::getId)
                .toList();
        final Map<Long, Long> replyLikeCounts = reviewLikeService.countReplyLikesByReplyIds(replyIds);
        final Set<Long> likedReplyIds = currentUserId == null
                ? Collections.emptySet()
                : reviewLikeService.getLikedReplyIds(replyIds, currentUserId);

        final Map<Long, List<ReviewReply>> finalRepliesByReviewId = repliesByReviewId;
        final Map<Long, Long> finalReviewLikeCounts = reviewLikeCounts;
        final Set<Long> finalLikedReviewIds = likedReviewIds;
        final Map<Long, Long> finalReplyLikeCounts = replyLikeCounts;
        final Set<Long> finalLikedReplyIds = likedReplyIds;
        final Long finalCurrentUserId = currentUserId;
        return reviews.stream()
                .map(review -> new ReviewThread(
                        review,
                        finalReviewLikeCounts.getOrDefault(review.getId(), 0L),
                        finalLikedReviewIds.contains(review.getId()),
                        finalRepliesByReviewId.getOrDefault(review.getId(), Collections.emptyList())
                                .stream()
                                .map(reply -> new ReviewReplyCard(
                                        reply,
                                        finalReplyLikeCounts.getOrDefault(reply.getId(), 0L),
                                        finalLikedReplyIds.contains(reply.getId()),
                                        finalCurrentUserId != null && reply.getUserId() == finalCurrentUserId
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    private boolean isSelectedCarFavorited(final Car selectedCar, final AuthenticatedUser currentUser) {
        return currentUser != null && carFavoriteService.isFavorited(currentUser.getId(), selectedCar.getId());
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
            return carReviewPage(existingReview.getCarId(), null, validationError, true, currentUser);
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

    @RequestMapping(value = "/reviews/{reviewId}/replies", method = RequestMethod.POST)
    public ModelAndView createReply(@PathVariable("reviewId") final long reviewId,
                                    @RequestParam("body") final String body,
                                    @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }

        final Review review = reviewService.getReviewById(reviewId).orElse(null);
        if (review == null) {
            throw new ReviewNotFoundException();
        }

        final String validationError = validateReplyInput(body);
        if (validationError != null) {
            return carReviewPage(review.getCarId(), null, validationError, false, currentUser);
        }

        try {
            reviewReplyService.createReply(reviewId, currentUser.getId(), body);
        } catch (final RuntimeException ignored) {
            return carReviewPage(review.getCarId(), null,
                    "No pudimos publicar la respuesta. Intentá de nuevo en unos segundos.",
                    false, currentUser);
        }
        return new ModelAndView("redirect:/reviews?carId=" + review.getCarId() + "#review-" + reviewId);
    }

    @RequestMapping(value = "/reviews/{reviewId}/like", method = RequestMethod.POST)
    public ModelAndView toggleReviewLike(@PathVariable("reviewId") final long reviewId,
                                         @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }

        final Review review = reviewService.getReviewById(reviewId).orElse(null);
        if (review == null) {
            throw new ReviewNotFoundException();
        }

        try {
            reviewLikeService.toggleReviewLike(reviewId, currentUser.getId());
        } catch (final RuntimeException ignored) {
            return carReviewPage(review.getCarId(), null,
                    "No pudimos actualizar el like. Intentá de nuevo en unos segundos.",
                    false, currentUser);
        }
        return new ModelAndView("redirect:/reviews?carId=" + review.getCarId() + "#review-" + reviewId);
    }

    @RequestMapping(value = "/reviews/replies/{replyId}/like", method = RequestMethod.POST)
    public ModelAndView toggleReplyLike(@PathVariable("replyId") final long replyId,
                                        @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }

        final ReviewReply reply = reviewReplyService.getReplyById(replyId).orElse(null);
        if (reply == null) {
            throw new ReviewReplyNotFoundException();
        }
        final Review review = reviewService.getReviewById(reply.getReviewId())
                .orElseThrow(ReviewNotFoundException::new);

        try {
            reviewLikeService.toggleReplyLike(replyId, currentUser.getId());
        } catch (final RuntimeException ignored) {
            return carReviewPage(review.getCarId(), null,
                    "No pudimos actualizar el like. Intentá de nuevo en unos segundos.",
                    false, currentUser);
        }
        return new ModelAndView("redirect:/reviews?carId=" + review.getCarId() + "#review-" + review.getId());
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

    private String validateReplyInput(final String body) {
        if (body == null || body.trim().isEmpty()) {
            return "La respuesta no puede estar vacía.";
        }
        if (body.trim().length() > MAX_REPLY_BODY_LENGTH) {
            return "La respuesta debe tener como máximo " + MAX_REPLY_BODY_LENGTH + " caracteres.";
        }
        return null;
    }

    private String normalizeOwnershipStatus(final String ownershipStatus) {
        return ownershipStatus == null || ownershipStatus.isEmpty() ? null : ownershipStatus;
    }

    private Long currentUserId(final AuthenticatedUser currentUser) {
        return currentUser == null ? null : currentUser.getId();
    }

    private static final class ReviewPageData {
        private final Car selectedCar;
        private final List<Review> reviews;
        private final List<ReviewThread> reviewThreads;
        private final String currentSort;
        private final Optional<Review> latestReview;
        private final List<CarImage> carImages;
        private final long latestReviewLikeCount;
        private final boolean latestReviewLiked;

        private ReviewPageData(final Car selectedCar, final List<Review> reviews,
                               final List<ReviewThread> reviewThreads, final String currentSort,
                               final Optional<Review> latestReview, final List<CarImage> carImages,
                               final long latestReviewLikeCount, final boolean latestReviewLiked) {
            this.selectedCar = selectedCar;
            this.reviews = reviews;
            this.reviewThreads = reviewThreads;
            this.currentSort = currentSort;
            this.latestReview = latestReview;
            this.carImages = carImages;
            this.latestReviewLikeCount = latestReviewLikeCount;
            this.latestReviewLiked = latestReviewLiked;
        }
    }

    public static final class ReviewThread {
        private final Review review;
        private final long likeCount;
        private final boolean liked;
        private final List<ReviewReplyCard> replies;

        private ReviewThread(final Review review, final long likeCount, final boolean liked,
                             final List<ReviewReplyCard> replies) {
            this.review = review;
            this.likeCount = likeCount;
            this.liked = liked;
            this.replies = replies;
        }

        public Review getReview() {
            return review;
        }

        public long getLikeCount() {
            return likeCount;
        }

        public boolean getLiked() {
            return liked;
        }

        public List<ReviewReplyCard> getReplies() {
            return replies;
        }
    }

    public static final class ReviewReplyCard {
        private final ReviewReply reply;
        private final long likeCount;
        private final boolean liked;
        private final boolean ownedByCurrentUser;

        private ReviewReplyCard(final ReviewReply reply, final long likeCount, final boolean liked,
                                final boolean ownedByCurrentUser) {
            this.reply = reply;
            this.likeCount = likeCount;
            this.liked = liked;
            this.ownedByCurrentUser = ownedByCurrentUser;
        }

        public ReviewReply getReply() {
            return reply;
        }

        public long getLikeCount() {
            return likeCount;
        }

        public boolean getLiked() {
            return liked;
        }

        public boolean getOwnedByCurrentUser() {
            return ownedByCurrentUser;
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static final class ReviewNotFoundException extends RuntimeException {
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static final class ReviewReplyNotFoundException extends RuntimeException {
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    private static final class ReviewForbiddenException extends RuntimeException {
    }
}
