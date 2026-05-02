package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewLikeService;
import ar.edu.itba.paw.services.ReviewReplyService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.exception.InvalidReviewTagSelectionException;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.exception.ForbiddenException;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import ar.edu.itba.paw.webapp.form.ReviewForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.Year;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

    @Autowired
    public CarReviewController(final CarService carService, final CarFavoriteService carFavoriteService,
                               final ReviewService reviewService,
                               final ReviewReplyService reviewReplyService,
                               final ReviewLikeService reviewLikeService) {
        this.carService = carService;
        this.carFavoriteService = carFavoriteService;
        this.reviewService = reviewService;
        this.reviewReplyService = reviewReplyService;
        this.reviewLikeService = reviewLikeService;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "/reviews", method = RequestMethod.GET)
    public String reviewForm(@RequestParam(value = "carId", required = false) final Long carId,
                             @RequestParam(value = "sort", required = false) final String sort,
                             @RequestParam(value = "page", required = false) final Integer page,
                             @RequestParam(value = "reviewForm", required = false) final String reviewFormParam,
                             @ModelAttribute("reviewForm") final ReviewForm reviewForm,
                             @AuthenticationPrincipal final AuthenticatedUser currentUser,
                             final Model model) {
        if (carId == null) {
            return "redirect:/cars";
        }
        if ("true".equalsIgnoreCase(reviewFormParam)) {
            return "redirect:/reviews/new?carId=" + carId;
        }

        final ReviewPageData pageData = resolveReviewPageData(carId, sort, page, currentUserId(currentUser));
        if (pageData == null) {
            throw new ResourceNotFoundException("El auto referenciado no existe.");
        }

        if (reviewForm.getCarId() == null) {
            reviewForm.setCarId(carId);
        }
        populateCarReviewPageModel(model, pageData, currentUser);
        return "car-review.jsp";
    }

    @RequestMapping(value = "/reviews/new", method = RequestMethod.GET)
    public ModelAndView newReview(@RequestParam(value = "carId", required = false) final Long carId) {
        final Optional<Car> car = carId == null ? Optional.empty() : carService.getCarById(carId);
        if (car.isEmpty()) {
            return new ModelAndView("redirect:/cars");
        }
        final ReviewForm reviewForm = new ReviewForm();
        reviewForm.setCarId(carId);
        final ModelAndView mav = new ModelAndView("review-form.jsp");
        mav.addObject("selectedCar", car.get());
        mav.addObject("reviewForm", reviewForm);
        return mav;
    }

    @RequestMapping(value = "/reviews/{reviewId}/edit", method = RequestMethod.GET)
    public ModelAndView editReview(@PathVariable("reviewId") final long reviewId,
                                   @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        final Review review = reviewService.getReviewById(reviewId).orElse(null);
        validateReviewOwnership(review, currentUser);

        final Car car = carService.getCarById(review.getCarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El auto referenciado no existe."));

        final ModelAndView mav = new ModelAndView("review-form.jsp");
        mav.addObject("selectedCar", car);
        mav.addObject("reviewForm", toReviewForm(review));
        mav.addObject("editMode", true);
        mav.addObject("reviewId", reviewId);
        return mav;
    }

    @RequestMapping(value = "/reviews/feed", method = RequestMethod.GET)
    public ModelAndView reviewFeed(@RequestParam("carId") final long carId,
                                   @RequestParam(value = "sort", required = false) final String sort,
                                   @RequestParam(value = "page", required = false) final Integer page,
                                   @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        final ReviewPageData pageData = resolveReviewPageData(carId, sort, page, currentUserId(currentUser));
        if (pageData == null) {
            throw new ResourceNotFoundException("El auto referenciado no existe.");
        }

        final ModelAndView mav = new ModelAndView("reviews-feed-fragment.jsp");
        mav.addObject("selectedCar", pageData.selectedCar);
        mav.addObject("reviews", pageData.reviews);
        mav.addObject("reviewThreads", pageData.reviewThreads);
        mav.addObject("currentSort", pageData.currentSort);
        mav.addObject("currentPage", pageData.currentPage);
        mav.addObject("totalPages", pageData.totalPages);
        mav.addObject("totalItems", pageData.totalItems);
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
            throw new ResourceNotFoundException("El auto referenciado no existe.");
        }

        rejectInvalidReviewFields(errors, reviewForm.getRating(), reviewForm.getOwnershipStatus(),
                reviewForm.getModelYear(), reviewForm.getMileageKm());

        if (errors.hasErrors()) {
            model.addAttribute("selectedCar", car);
            return "review-form.jsp";
        }

        try {
            reviewService.createReview(
                    currentUser.getId(),
                    car.getId(),
                    reviewForm.getRating(),
                    reviewForm.getTitle(),
                    reviewForm.getBody(),
                    normalizeOwnershipStatus(reviewForm.getOwnershipStatus()),
                    reviewForm.getModelYear(),
                    reviewForm.getMileageKm(),
                    reviewForm.getWouldRecommend(),
                    reviewForm.getTagIds());
        } catch (final InvalidReviewTagSelectionException e) {
            errors.rejectValue("tagIds", "tagIds.invalid", e.getMessage());
            model.addAttribute("selectedCar", car);
            return "review-form.jsp";
        }

        return "redirect:/reviews?carId=" + car.getId();
    }

    private ModelAndView carReviewPage(final long carId, final String sort, final String error,
                                       final AuthenticatedUser currentUser) {
        final ReviewPageData pageData = resolveReviewPageData(carId, sort, null, currentUserId(currentUser));
        if (pageData == null) {
            return new ModelAndView("redirect:/cars");
        }

        final ModelAndView mav = new ModelAndView("car-review.jsp");
        populateCarReviewPageModel(mav, pageData, currentUser);
        final ReviewForm reviewForm = new ReviewForm();
        reviewForm.setCarId(carId);
        mav.addObject("reviewForm", reviewForm);
        if (error != null) {
            mav.addObject("error", error);
            mav.addObject("replyError", error);
        }
        return mav;
    }

    private void populateCarReviewPageModel(final Model model, final ReviewPageData pageData,
                                            final AuthenticatedUser currentUser) {
        buildCarReviewPageAttributes(pageData, currentUser).forEach(model::addAttribute);
    }

    private void populateCarReviewPageModel(final ModelAndView mav, final ReviewPageData pageData,
                                            final AuthenticatedUser currentUser) {
        buildCarReviewPageAttributes(pageData, currentUser).forEach(mav::addObject);
    }

    private Map<String, Object> buildCarReviewPageAttributes(final ReviewPageData pageData,
                                                             final AuthenticatedUser currentUser) {
        final Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("selectedCar", pageData.selectedCar);
        attributes.put("selectedCarFavorited", isSelectedCarFavorited(pageData.selectedCar, currentUser));
        attributes.put("reviews", pageData.reviews);
        attributes.put("reviewThreads", pageData.reviewThreads);
        attributes.put("averageRating", pageData.averageRating);
        attributes.put("reviewCount", pageData.totalItems);
        attributes.put("currentSort", pageData.currentSort);
        attributes.put("currentPage", pageData.currentPage);
        attributes.put("totalPages", pageData.totalPages);
        attributes.put("totalItems", pageData.totalItems);
        attributes.put("latestReview", pageData.latestReview.orElse(null));
        attributes.put("latestReviewLikeCount", pageData.latestReviewLikeCount);
        attributes.put("latestReviewLiked", pageData.latestReviewLiked);
        attributes.put("carImages", pageData.carImages);
        attributes.put("yearVariants", buildYearVariants(pageData.selectedCar));
        return attributes;
    }

    private List<CarYearVariant> buildYearVariants(final Car selectedCar) {
        if (selectedCar.getBrandName() == null || selectedCar.getBodyType() == null || selectedCar.getModel() == null) {
            return Collections.emptyList();
        }
        final String selectedModel = selectedCar.getModel().trim().toLowerCase(Locale.ROOT);
        return carService.getCarsByBrandAndBodyType(selectedCar.getBrandName(), selectedCar.getBodyType())
                .stream()
                .filter(car -> car.getModel() != null
                        && car.getModel().trim().toLowerCase(Locale.ROOT).equals(selectedModel))
                .sorted(Comparator
                        .comparing(Car::getYear, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparingLong(Car::getId))
                .map(car -> new CarYearVariant(car.getId(), car.getYear(), car.getId() == selectedCar.getId()))
                .collect(Collectors.toList());
    }

    private ReviewPageData resolveReviewPageData(final long carId, final String sort, final Integer page,
                                                 final Long currentUserId) {
        final Car selectedCar = carService.getCarById(carId).orElse(null);
        if (selectedCar == null) {
            return null;
        }

        final String normalizedSort = normalizeSort(sort);
        final int normalizedPage = Pagination.normalizePage(page);
        final Page<Review> reviewPage = getReviewsForCar(selectedCar.getId(), normalizedSort, normalizedPage);
        final List<Review> reviews = reviewPage.getItems();
        final BigDecimal averageRating = reviewService.getReviewStatsByCar(selectedCar.getId())
                .map(stats -> stats.getAverageRating())
                .orElse(null);
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
                latestReviewLikeCount, latestReviewLiked,
                reviewPage.getPageNumber(), reviewPage.getTotalPages(), reviewPage.getTotalItems(), averageRating);
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

        return reviews.stream()
                .map(review -> new ReviewThread(
                        review,
                        reviewLikeCounts.getOrDefault(review.getId(), 0L),
                        likedReviewIds.contains(review.getId()),
                        repliesByReviewId.getOrDefault(review.getId(), Collections.emptyList())
                                .stream()
                                .map(reply -> new ReviewReplyCard(
                                        reply,
                                        replyLikeCounts.getOrDefault(reply.getId(), 0L),
                                        likedReplyIds.contains(reply.getId()),
                                        currentUserId != null && currentUserId.equals(reply.getUserId())
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    private boolean isSelectedCarFavorited(final Car selectedCar, final AuthenticatedUser currentUser) {
        return currentUser != null && carFavoriteService.isFavorited(currentUser.getId(), selectedCar.getId());
    }

    private Page<Review> getReviewsForCar(final long carId, final String sort, final int page) {
        if (SORT_RATING_ASC.equals(sort)) {
            return reviewService.getReviewsByCarOrderByRatingAsc(carId, page);
        }
        if (SORT_RATING_DESC.equals(sort)) {
            return reviewService.getReviewsByCarOrderByRatingDesc(carId, page);
        }
        return reviewService.getReviewsByCar(carId, page);
    }

    private String normalizeSort(final String sort) {
        if (SORT_RATING_ASC.equals(sort) || SORT_RATING_DESC.equals(sort)) {
            return sort;
        }
        return null;
    }

    @RequestMapping(value = "/reviews/{reviewId}", method = RequestMethod.POST)
    public String updateReview(@PathVariable("reviewId") final long reviewId,
                               @Valid @ModelAttribute("reviewForm") final ReviewForm reviewForm,
                               final BindingResult errors,
                               final Model model,
                               @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        final Review existingReview = reviewService.getReviewById(reviewId).orElse(null);
        validateReviewOwnership(existingReview, currentUser);

        final Car car = carService.getCarById(existingReview.getCarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El auto referenciado no existe."));
        reviewForm.setCarId(existingReview.getCarId());
        rejectInvalidReviewFields(errors, reviewForm.getRating(), reviewForm.getOwnershipStatus(),
                reviewForm.getModelYear(), reviewForm.getMileageKm());

        if (errors.hasErrors()) {
            model.addAttribute("selectedCar", car);
            model.addAttribute("editMode", true);
            model.addAttribute("reviewId", reviewId);
            return "review-form.jsp";
        }

        try {
            reviewService.updateReview(
                    reviewId,
                    existingReview.getCarId(),
                    reviewForm.getRating(),
                    reviewForm.getTitle(),
                    reviewForm.getBody(),
                    normalizeOwnershipStatus(reviewForm.getOwnershipStatus()),
                    reviewForm.getModelYear(),
                    reviewForm.getMileageKm(),
                    reviewForm.getWouldRecommend(),
                    reviewForm.getTagIds()
            );
        } catch (final InvalidReviewTagSelectionException e) {
            errors.rejectValue("tagIds", "tagIds.invalid", e.getMessage());
            model.addAttribute("selectedCar", car);
            model.addAttribute("editMode", true);
            model.addAttribute("reviewId", reviewId);
            return "review-form.jsp";
        }
        return "redirect:/profile";
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
                                    @RequestParam(value = "body", required = false) final String body,
                                    @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }

        final Review review = reviewService.getReviewById(reviewId).orElse(null);
        if (review == null) {
            throw new ResourceNotFoundException();
        }

        final String validationError = validateReplyInput(body);
        if (validationError != null) {
            return carReviewPage(review.getCarId(), null, validationError, currentUser);
        }

        try {
            reviewReplyService.createReply(reviewId, currentUser.getId(), body);
        } catch (final RuntimeException ignored) {
            return carReviewPage(review.getCarId(), null,
                    "No pudimos publicar la respuesta. Intentá de nuevo en unos segundos.",
                    currentUser);
        }
        return new ModelAndView("redirect:/reviews?carId=" + review.getCarId() + "#review-" + reviewId);
    }

    @RequestMapping(value = "/reviews/{reviewId}/like", method = RequestMethod.POST)
    public Object toggleReviewLike(@PathVariable("reviewId") final long reviewId,
                                   @RequestHeader(value = "X-Requested-With", required = false) final String requestedWith,
                                   @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        final boolean ajax = ControllerUtils.isAjaxRequest(requestedWith);
        if (currentUser == null) {
            if (ajax) {
                return new ResponseEntity<String>("/login", HttpStatus.UNAUTHORIZED);
            }
            return new ModelAndView("redirect:/login");
        }

        final Review review = reviewService.getReviewById(reviewId).orElse(null);
        if (review == null) {
            throw new ResourceNotFoundException();
        }

        final boolean liked;
        try {
            liked = reviewLikeService.toggleReviewLike(reviewId, currentUser.getId());
        } catch (final RuntimeException ignored) {
            if (ajax) {
                return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return carReviewPage(review.getCarId(), null,
                    "No pudimos actualizar el like. Intentá de nuevo en unos segundos.",
                    currentUser);
        }
        if (ajax) {
            final long count = reviewLikeService.countReviewLikes(reviewId);
            return new ResponseEntity<String>(liked + "|" + count, HttpStatus.OK);
        }
        return new ModelAndView("redirect:/reviews?carId=" + review.getCarId() + "#review-" + reviewId);
    }

    @RequestMapping(value = "/reviews/replies/{replyId}/like", method = RequestMethod.POST)
    public Object toggleReplyLike(@PathVariable("replyId") final long replyId,
                                  @RequestHeader(value = "X-Requested-With", required = false) final String requestedWith,
                                  @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        final boolean ajax = ControllerUtils.isAjaxRequest(requestedWith);
        if (currentUser == null) {
            if (ajax) {
                return new ResponseEntity<String>("/login", HttpStatus.UNAUTHORIZED);
            }
            return new ModelAndView("redirect:/login");
        }

        final ReviewReply reply = reviewReplyService.getReplyById(replyId).orElse(null);
        if (reply == null) {
            throw new ResourceNotFoundException();
        }
        final Review review = reviewService.getReviewById(reply.getReviewId())
                .orElseThrow(ResourceNotFoundException::new);

        final boolean liked;
        try {
            liked = reviewLikeService.toggleReplyLike(replyId, currentUser.getId());
        } catch (final RuntimeException ignored) {
            if (ajax) {
                return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return carReviewPage(review.getCarId(), null,
                    "No pudimos actualizar el like. Intentá de nuevo en unos segundos.",
                    currentUser);
        }
        if (ajax) {
            final long count = reviewLikeService.countReplyLikes(replyId);
            return new ResponseEntity<String>(liked + "|" + count, HttpStatus.OK);
        }
        return new ModelAndView("redirect:/reviews?carId=" + review.getCarId() + "#review-" + review.getId());
    }

    private void validateReviewOwnership(final Review review, final AuthenticatedUser currentUser) {
        if (review == null) {
            throw new ResourceNotFoundException();
        }
        if (currentUser == null || review.getUserId() == null || !review.getUserId().equals(currentUser.getId())) {
            throw new ForbiddenException();
        }
    }

    private void rejectInvalidReviewFields(final BindingResult errors, final BigDecimal rating,
                                           final String ownershipStatus, final Integer modelYear,
                                           final Integer mileageKm) {
        if (rating != null && rating.multiply(RATING_STEP_DOUBLED).remainder(BigDecimal.ONE).signum() != 0) {
            errors.rejectValue("rating", "rating.step", "La puntuación debe ser múltiplo de 0,5.");
        }

        final String ownership = ownershipStatus == null ? "" : ownershipStatus;
        if (!ALLOWED_OWNERSHIP_STATUSES.contains(ownership)) {
            errors.rejectValue("ownershipStatus", "ownership.invalid", "Estado de propiedad no válido.");
        }

        if (modelYear == null) {
            if (!errors.hasFieldErrors("modelYear")) {
                errors.rejectValue("modelYear", "modelYear.required", "El año del modelo es obligatorio.");
            }
        } else {
            final int maxModelYear = Year.now().getValue() + 1;
            if (modelYear > maxModelYear) {
                errors.rejectValue("modelYear", "modelYear.range",
                        "Ingresá un año entre 1886 y " + maxModelYear + ".");
            }
        }

        if (mileageKm == null && !errors.hasFieldErrors("mileageKm")) {
            errors.rejectValue("mileageKm", "mileageKm.required", "El kilometraje es obligatorio.");
        }
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

    private ReviewForm toReviewForm(final Review review) {
        final ReviewForm form = new ReviewForm();
        form.setCarId(review.getCarId());
        form.setRating(review.getRating());
        form.setTitle(review.getTitle());
        form.setBody(review.getBody());
        form.setOwnershipStatus(review.getOwnershipStatus());
        form.setModelYear(review.getModelYear());
        form.setMileageKm(review.getMileageKm());
        form.setWouldRecommend(review.getWouldRecommend());
        form.setTagIds(review.getTags()
                .stream()
                .map(tag -> tag.getId())
                .collect(Collectors.toSet()));
        return form;
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
        private final int currentPage;
        private final int totalPages;
        private final long totalItems;
        private final BigDecimal averageRating;

        private ReviewPageData(final Car selectedCar, final List<Review> reviews,
                               final List<ReviewThread> reviewThreads, final String currentSort,
                               final Optional<Review> latestReview, final List<CarImage> carImages,
                               final long latestReviewLikeCount, final boolean latestReviewLiked,
                               final int currentPage, final int totalPages, final long totalItems,
                               final BigDecimal averageRating) {
            this.selectedCar = selectedCar;
            this.reviews = reviews;
            this.reviewThreads = reviewThreads;
            this.currentSort = currentSort;
            this.latestReview = latestReview;
            this.carImages = carImages;
            this.latestReviewLikeCount = latestReviewLikeCount;
            this.latestReviewLiked = latestReviewLiked;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalItems = totalItems;
            this.averageRating = averageRating;
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

    public static final class CarYearVariant {
        private final long carId;
        private final Integer year;
        private final boolean selected;

        private CarYearVariant(final long carId, final Integer year, final boolean selected) {
            this.carId = carId;
            this.year = year;
            this.selected = selected;
        }

        public long getCarId() {
            return carId;
        }

        public Integer getYear() {
            return year;
        }

        public boolean isSelected() {
            return selected;
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

}
