package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.EmailService;
import ar.edu.itba.paw.services.ReviewLikeService;
import ar.edu.itba.paw.services.ReviewReplyService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.exception.InvalidReviewTagSelectionException;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.exception.ForbiddenException;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import ar.edu.itba.paw.webapp.form.ReviewForm;
import ar.edu.itba.paw.webapp.validation.ReviewFormValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
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
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.math.BigDecimal;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(CarReviewController.class);

    private static final String SORT_RATING_ASC = "rating_asc";
    private static final String SORT_RATING_DESC = "rating_desc";
    private static final int MAX_REPLY_BODY_LENGTH = 1000;
    private static final int REVIEW_HIDE_REASON_MIN_LENGTH = 10;
    private static final int REVIEW_HIDE_REASON_MAX_LENGTH = 600;

    private final CarService carService;
    private final CarFavoriteService carFavoriteService;
    private final ReviewService reviewService;
    private final ReviewReplyService reviewReplyService;
    private final ReviewLikeService reviewLikeService;
    private final EmailService emailService;
    private final UserService userService;
    private final MessageSource messageSource;
    private final ReviewFormValidator reviewFormValidator;

    @Autowired
    public CarReviewController(final CarService carService, final CarFavoriteService carFavoriteService,
                               final ReviewService reviewService,
                               final ReviewReplyService reviewReplyService,
                               final ReviewLikeService reviewLikeService,
                               final EmailService emailService,
                               final UserService userService,
                               final MessageSource messageSource,
                               final ReviewFormValidator reviewFormValidator) {
        this.carService = carService;
        this.carFavoriteService = carFavoriteService;
        this.reviewService = reviewService;
        this.reviewReplyService = reviewReplyService;
        this.reviewLikeService = reviewLikeService;
        this.emailService = emailService;
        this.userService = userService;
        this.messageSource = messageSource;
        this.reviewFormValidator = reviewFormValidator;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @InitBinder("reviewForm")
    public void initReviewBinder(final WebDataBinder binder) {
        binder.addValidators(reviewFormValidator);
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
        validateReviewOwnership(reviewId, review, currentUser);

        final Car car = carService.getCarById(review.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("El auto referenciado no existe."));

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
        mav.addObject("currentUserId", currentUserId(currentUser));
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

        if (errors.hasErrors()) {
            LOGGER.warn("create review rejected: validation errors carId={} userId={} errorCount={}",
                    car.getId(), currentUser.getId(), errors.getErrorCount());
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
                    null,
                    reviewForm.getMileageKm(),
                    reviewForm.getWouldRecommend(),
                    reviewForm.getTagIds());
            LOGGER.info("created review carId={} userId={}", car.getId(), currentUser.getId());
        } catch (final InvalidReviewTagSelectionException e) {
            LOGGER.warn("create review rejected: invalid tag selection carId={} userId={}",
                    car.getId(), currentUser.getId());
            errors.rejectValue("tagIds", "tagIds.invalid", e.getMessage());
            model.addAttribute("selectedCar", car);
            return "review-form.jsp";
        }

        return "redirect:/reviews?carId=" + car.getId() + "&reviewCreated=1";
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
        attributes.put("currentUserId", currentUserId(currentUser));
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
        final List<CarImage> carImages = carService.getCarImagesByCarId(selectedCar.getId());
        final List<ReviewThread> reviewThreads = buildReviewThreads(reviews, currentUserId);
        return new ReviewPageData(selectedCar, reviews, reviewThreads, normalizedSort, carImages,
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
        validateReviewOwnership(reviewId, existingReview, currentUser);

        final Car car = carService.getCarById(existingReview.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("El auto referenciado no existe."));
        reviewForm.setCarId(existingReview.getCarId());

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
                    null,
                    reviewForm.getMileageKm(),
                    reviewForm.getWouldRecommend(),
                    reviewForm.getTagIds()
            );
            LOGGER.info("updated review id={} userId={}", reviewId, currentUser.getId());
        } catch (final InvalidReviewTagSelectionException e) {
            LOGGER.warn("update review rejected: invalid tag selection reviewId={} userId={}",
                    reviewId, currentUser.getId());
            errors.rejectValue("tagIds", "tagIds.invalid", e.getMessage());
            model.addAttribute("selectedCar", car);
            model.addAttribute("editMode", true);
            model.addAttribute("reviewId", reviewId);
            return "review-form.jsp";
        }
        return "redirect:/profile";
    }

    @RequestMapping(value = "/reviews/{reviewId}/delete", method = RequestMethod.POST)
    public Object deleteReview(@PathVariable("reviewId") final long reviewId,
                               @RequestHeader(value = "X-Requested-With", required = false) final String requestedWith,
                               @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        final boolean ajax = ControllerUtils.isAjaxRequest(requestedWith);
        final Review existingReview = reviewService.getReviewById(reviewId).orElse(null);
        validateReviewOwnership(reviewId, existingReview, currentUser);
        reviewService.deleteReview(reviewId);
        LOGGER.info("user id={} deleted review id={}", currentUser.getId(), reviewId);
        if (ajax) {
            return new ResponseEntity<String>("ok", HttpStatus.OK);
        }
        return new ModelAndView("redirect:/profile");
    }

    @RequestMapping(value = "/reviews/{reviewId}/hide", method = RequestMethod.POST)
    public Object hideReview(@PathVariable("reviewId") final long reviewId,
                             @RequestParam(value = "reason", required = false) final String reason,
                             @RequestHeader(value = "X-Requested-With", required = false) final String requestedWith,
                             @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        final boolean ajax = ControllerUtils.isAjaxRequest(requestedWith);
        if (currentUser == null) {
            if (ajax) {
                return new ResponseEntity<String>("/login", HttpStatus.UNAUTHORIZED);
            }
            return new ModelAndView("redirect:/login");
        }
        final String normalizedReason = ControllerUtils.normalize(reason);
        final String validationError = validateReviewHideReason(normalizedReason);
        if (validationError != null) {
            if (ajax) {
                return new ResponseEntity<String>(validationError, HttpStatus.BAD_REQUEST);
            }
            return new ModelAndView("redirect:/reviews");
        }

        final Review review = reviewService.getReviewById(reviewId).orElse(null);
        if (review == null) {
            throw new ResourceNotFoundException("Review", reviewId);
        }

        final Car car = carService.getCarById(review.getCarId()).orElse(null);
        final String recipientEmail = resolveReviewRecipientEmail(review);
        final String reviewTitle = review.getTitle();
        final String carName = carDisplayName(car);

        if (!reviewService.deleteReview(reviewId)) {
            LOGGER.warn("hide review failed: delete returned false reviewId={}", reviewId);
            return reviewHideError(ajax, review.getCarId());
        }
        LOGGER.info("admin id={} hid review id={}", currentUser.getId(), reviewId);

        if (recipientEmail != null) {
            emailService.sendReviewHiddenNotification(
                    recipientEmail,
                    message("review.hide.email.subject"),
                    message("review.hide.email.heading"),
                    message("review.hide.email.intro"),
                    message("review.hide.email.review"),
                    message("review.hide.email.car"),
                    message("review.hide.email.reason"),
                    reviewTitle,
                    carName,
                    normalizedReason
            );
        }

        if (ajax) {
            return new ResponseEntity<String>("ok", HttpStatus.OK);
        }
        return new ModelAndView("redirect:/reviews?carId=" + review.getCarId());
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
            throw new ResourceNotFoundException("Review", reviewId);
        }

        final String validationError = validateReplyInput(body);
        if (validationError != null) {
            return carReviewPage(review.getCarId(), null, validationError, currentUser);
        }

        reviewReplyService.createReply(reviewId, currentUser.getId(), body);
        LOGGER.info("user id={} replied to review id={}", currentUser.getId(), reviewId);
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
            throw new ResourceNotFoundException("Review", reviewId);
        }

        final boolean liked = reviewLikeService.toggleReviewLike(reviewId, currentUser.getId());
        LOGGER.info("user id={} toggled review like reviewId={} liked={}", currentUser.getId(), reviewId, liked);
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
            throw new ResourceNotFoundException("Review reply", replyId);
        }
        final Review review = reviewService.getReviewById(reply.getReviewId())
                .orElseThrow(() -> new ResourceNotFoundException("Review", reply.getReviewId()));

        final boolean liked = reviewLikeService.toggleReplyLike(replyId, currentUser.getId());
        LOGGER.info("user id={} toggled reply like replyId={} liked={}", currentUser.getId(), replyId, liked);
        if (ajax) {
            final long count = reviewLikeService.countReplyLikes(replyId);
            return new ResponseEntity<String>(liked + "|" + count, HttpStatus.OK);
        }
        return new ModelAndView("redirect:/reviews?carId=" + review.getCarId() + "#review-" + review.getId());
    }

    private void validateReviewOwnership(final long reviewId, final Review review, final AuthenticatedUser currentUser) {
        if (review == null) {
            throw new ResourceNotFoundException("Review", reviewId);
        }
        if (currentUser == null) {
            throw new ForbiddenException("modify", "review", review.getId());
        }
        final boolean isOwner = review.getUserId() != null && review.getUserId().equals(currentUser.getId());
        if (!isOwner && !isAdmin(currentUser)) {
            throw new ForbiddenException("modify", "review", review.getId());
        }
    }

    private Object reviewHideError(final boolean ajax, final long carId) {
        if (ajax) {
            return new ResponseEntity<String>(message("review.hide.toast.error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ModelAndView("redirect:/reviews?carId=" + carId);
    }

    private String validateReviewHideReason(final String reason) {
        if (reason == null) {
            return message("review.hide.reason.required");
        }
        if (reason.length() < REVIEW_HIDE_REASON_MIN_LENGTH) {
            return message("review.hide.reason.min", REVIEW_HIDE_REASON_MIN_LENGTH);
        }
        if (reason.length() > REVIEW_HIDE_REASON_MAX_LENGTH) {
            return message("review.hide.reason.max", REVIEW_HIDE_REASON_MAX_LENGTH);
        }
        return null;
    }

    private String resolveReviewRecipientEmail(final Review review) {
        final String reviewerEmail = ControllerUtils.normalizeEmail(review.getReviewerEmail());
        if (reviewerEmail != null) {
            return reviewerEmail;
        }
        if (review.getUserId() == null) {
            return null;
        }
        return userService.getUserById(review.getUserId())
                .map(User::getEmail)
                .map(ControllerUtils::normalizeEmail)
                .orElse(null);
    }

    private String carDisplayName(final Car car) {
        if (car == null) {
            return message("review.hide.email.carFallback");
        }
        final String brand = ControllerUtils.normalize(car.getBrandName());
        final String model = ControllerUtils.normalize(car.getModel());
        if (brand == null && model == null) {
            return message("review.hide.email.carFallback");
        }
        if (brand == null) {
            return model;
        }
        if (model == null) {
            return brand;
        }
        return brand + " " + model;
    }

    private boolean isAdmin(final AuthenticatedUser currentUser) {
        return currentUser.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private String message(final String code, final Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    private String validateReplyInput(final String body) {
        if (body == null || body.trim().isEmpty()) {
            return message("review.reply.body.required");
        }
        if (body.trim().length() > MAX_REPLY_BODY_LENGTH) {
            return message("review.reply.body.max", MAX_REPLY_BODY_LENGTH);
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
        private final List<CarImage> carImages;
        private final int currentPage;
        private final int totalPages;
        private final long totalItems;
        private final BigDecimal averageRating;

        private ReviewPageData(final Car selectedCar, final List<Review> reviews,
                               final List<ReviewThread> reviewThreads, final String currentSort,
                               final List<CarImage> carImages,
                               final int currentPage, final int totalPages, final long totalItems,
                               final BigDecimal averageRating) {
            this.selectedCar = selectedCar;
            this.reviews = reviews;
            this.reviewThreads = reviewThreads;
            this.currentSort = currentSort;
            this.carImages = carImages;
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
