package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.CarYearVariant;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewOwnershipStatus;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.StoredImagePayload;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewLikeService;
import ar.edu.itba.paw.services.ReviewReplyService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.exception.InvalidReviewTagSelectionException;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.auth.LoginRedirectUtils;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import ar.edu.itba.paw.webapp.exception.UploadedImageReadException;
import ar.edu.itba.paw.webapp.form.ReviewForm;
import ar.edu.itba.paw.webapp.form.ReviewHideForm;
import ar.edu.itba.paw.webapp.form.ReviewReplyForm;
import ar.edu.itba.paw.webapp.validation.ReviewFormValidator;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CarReviewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        CarReviewController.class
    );

    private static final String ACTION_TOAST_ATTRIBUTE = "actionToastCode";

    private static final String SORT_RATING_ASC = "rating_asc";
    private static final String SORT_RATING_DESC = "rating_desc";
    private static final int MAX_REVIEW_IMAGE_COUNT = 5;
    private static final int TEASER_REPLIES_LIMIT = 3;

    private final CarService carService;
    private final CarFavoriteService carFavoriteService;
    private final ReviewService reviewService;
    private final ReviewReplyService reviewReplyService;
    private final ReviewLikeService reviewLikeService;
    private final ReviewFormValidator reviewFormValidator;

    @Autowired
    public CarReviewController(
        final CarService carService,
        final CarFavoriteService carFavoriteService,
        final ReviewService reviewService,
        final ReviewReplyService reviewReplyService,
        final ReviewLikeService reviewLikeService,
        final ReviewFormValidator reviewFormValidator
    ) {
        this.carService = carService;
        this.carFavoriteService = carFavoriteService;
        this.reviewService = reviewService;
        this.reviewReplyService = reviewReplyService;
        this.reviewLikeService = reviewLikeService;
        this.reviewFormValidator = reviewFormValidator;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(
            String.class,
            new StringTrimmerEditor(true)
        );
    }

    @InitBinder("reviewForm")
    public void initReviewBinder(final WebDataBinder binder) {
        binder.addValidators(reviewFormValidator);
    }

    @RequestMapping(value = "/reviews/car/{carId}", method = RequestMethod.GET)
    public String reviewForm(
        @PathVariable("carId") final Long carId,
        @RequestParam(value = "sort", required = false) final String sort,
        @RequestParam(value = "page", required = false) final Integer page,
        @RequestParam(
            value = "reviewForm",
            required = false
        ) final String reviewFormParam,
        @ModelAttribute("reviewForm") final ReviewForm reviewForm,
        @AuthenticationPrincipal final AuthenticatedUser currentUser,
        final Model model
    ) {
        if ("true".equalsIgnoreCase(reviewFormParam)) {
            return "redirect:/reviews/new?carId=" + carId;
        }

        final ReviewPageData pageData = resolveReviewPageData(
            carId,
            sort,
            page,
            currentUserId(currentUser)
        );
        if (pageData == null) {
            throw new ResourceNotFoundException(
                "El auto referenciado no existe."
            );
        }

        if (reviewForm.getCarId() == null) {
            reviewForm.setCarId(carId);
        }
        populateCarReviewPageModel(model, pageData, currentUser);
        return "car-review.jsp";
    }

    @RequestMapping(value = "/reviews/new", method = RequestMethod.GET)
    public ModelAndView newReview(
        @RequestParam(value = "carId", required = false) final Long carId
    ) {
        final Optional<Car> car =
            carId == null ? Optional.empty() : carService.getCarById(carId);
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

    @RequestMapping(
        value = "/reviews/{reviewId}/edit",
        method = RequestMethod.GET
    )
    public ModelAndView editReview(
        @PathVariable("reviewId") final long reviewId,
        @RequestParam(value = "redirect", required = false) final String redirect,
        final HttpServletRequest request,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        final Review review = reviewService.getReviewAndCheckAccess(
            reviewId,
            currentUser.getId(),
            request.isUserInRole("ADMIN")
        );

        final Car car = carService
            .getCarById(review.getCarId())
            .orElseThrow(() ->
                new ResourceNotFoundException("El auto referenciado no existe.")
            );

        final ModelAndView mav = new ModelAndView("review-form.jsp");
        mav.addObject("selectedCar", car);
        mav.addObject("reviewForm", toReviewForm(review));
        mav.addObject("editMode", true);
        mav.addObject("reviewId", reviewId);
        mav.addObject("existingReviewImageIds", reviewImageIdsCsv(reviewId));
        LoginRedirectUtils.safeRedirect(redirect, request.getContextPath()).ifPresent(r -> mav.addObject("editRedirect", r));
        return mav;
    }

    @RequestMapping(value = "/reviews", method = RequestMethod.POST)
    public String createReview(
        @Valid @ModelAttribute("reviewForm") final ReviewForm reviewForm,
        final BindingResult errors,
        final Model model,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            final Long carId = reviewForm.getCarId();
            return carId == null
                ? "redirect:/cars"
                : "redirect:/reviews/new?carId=" + carId;
        }

        final Car car =
            reviewForm.getCarId() == null
                ? null
                : carService.getCarById(reviewForm.getCarId()).orElse(null);
        if (car == null) {
            throw new ResourceNotFoundException(
                "El auto referenciado no existe."
            );
        }

        final List<MultipartFile> uploadedFiles = nonEmptyFiles(reviewForm.getFiles());
        validateReviewImageUploads(uploadedFiles, 0, errors);

        if (errors.hasErrors()) {
            LOGGER.warn(
                "create review rejected: validation errors carId={} userId={} errorCount={}",
                car.getId(),
                currentUser.getId(),
                errors.getErrorCount()
            );
            model.addAttribute("selectedCar", car);
            return "review-form.jsp";
        }

        final List<ImagePayload> imagePayloads;
        try {
            imagePayloads = toImagePayloads(uploadedFiles);
        } catch (final IOException e) {
            LOGGER.error(
                "failed to read uploaded image during review creation carId={} userId={}",
                car.getId(),
                currentUser.getId(),
                e
            );
            throw new UploadedImageReadException(
                "creating review for car " + car.getId() + " by user " + currentUser.getId(), e);
        }
        final Review createdReview;
        try {
            createdReview = reviewService.createReview(
                currentUser.getId(),
                car.getId(),
                reviewForm.getRating(),
                reviewForm.getTitle(),
                reviewForm.getBody(),
                normalizeOwnershipStatus(reviewForm.getOwnershipStatus()),
                null,
                reviewForm.getMileageKm(),
                reviewForm.getWouldRecommend(),
                reviewForm.getTagIds(),
                imagePayloads
            );
            LOGGER.info(
                "created review carId={} userId={}",
                car.getId(),
                currentUser.getId()
            );
        } catch (final InvalidReviewTagSelectionException e) {
            LOGGER.warn(
                "create review rejected: invalid tag selection carId={} userId={}",
                car.getId(),
                currentUser.getId()
            );
            errors.rejectValue("tagIds", "tagIds.invalid", e.getMessage());
            model.addAttribute("selectedCar", car);
            return "review-form.jsp";
        }

        return "redirect:/reviews/car/" + car.getId() + "?reviewCreated=1#review-" + createdReview.getId();
    }

    private ModelAndView carReviewPageWithReplyError(
        final long carId,
        final String sort,
        final Integer page,
        final long replyErrorReviewId,
        final String replyErrorBody,
        final String replyError,
        final AuthenticatedUser currentUser
    ) {
        final ReviewPageData pageData = resolveReviewPageData(
            carId,
            sort,
            page,
            currentUserId(currentUser)
        );
        if (pageData == null) {
            return new ModelAndView("redirect:/cars");
        }

        final ModelAndView mav = new ModelAndView("car-review.jsp");
        populateCarReviewPageModel(mav, pageData, currentUser);
        final ReviewForm reviewForm = new ReviewForm();
        reviewForm.setCarId(carId);
        mav.addObject("reviewForm", reviewForm);
        mav.addObject("replyError", replyError);
        mav.addObject("replyErrorReviewId", replyErrorReviewId);
        mav.addObject("replyErrorBody", replyErrorBody);
        return mav;
    }

    private void populateCarReviewPageModel(
        final Model model,
        final ReviewPageData pageData,
        final AuthenticatedUser currentUser
    ) {
        buildCarReviewPageAttributes(pageData, currentUser).forEach(
            model::addAttribute
        );
    }

    private void populateCarReviewPageModel(
        final ModelAndView mav,
        final ReviewPageData pageData,
        final AuthenticatedUser currentUser
    ) {
        buildCarReviewPageAttributes(pageData, currentUser).forEach(
            mav::addObject
        );
    }

    private Map<String, Object> buildCarReviewPageAttributes(
        final ReviewPageData pageData,
        final AuthenticatedUser currentUser
    ) {
        final Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("selectedCar", pageData.selectedCar);
        attributes.put(
            "selectedCarFavorited",
            isSelectedCarFavorited(pageData.selectedCar, currentUser)
        );
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
        return carService.getYearVariants(selectedCar.getId());
    }

    private ReviewPageData resolveReviewPageData(final long carId, final String sort, final Integer page,
                                                 final Long currentUserId) {
        final Car selectedCar = carService.getCarById(carId).orElse(null);
        if (selectedCar == null) {
            return null;
        }

        final String normalizedSort = normalizeSort(sort);
        final int normalizedPage = Pagination.normalizePage(page);
        final Page<Review> reviewPage = getReviewsForCar(
            selectedCar.getId(),
            normalizedSort,
            normalizedPage
        );
        final List<Review> reviews = reviewPage.getItems();
        populateReviewImages(reviews);
        final BigDecimal averageRating = reviewService
            .getReviewStatsByCar(selectedCar.getId())
            .map(stats -> stats.getAverageRating())
            .orElse(null);
        final List<ImageMetadata> carImages = carService.getCarImagesByCarId(
            selectedCar.getId()
        );
        final List<ReviewThread> reviewThreads = buildReviewThreads(
            reviews,
            currentUserId
        );
        return new ReviewPageData(
            selectedCar,
            reviews,
            reviewThreads,
            normalizedSort,
            carImages,
            reviewPage.getPageNumber(),
            reviewPage.getTotalPages(),
            reviewPage.getTotalItems(),
            averageRating
        );
    }

    private List<ReviewThread> buildReviewThreads(
        final List<Review> reviews,
        final Long currentUserId
    ) {
        if (reviews.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Long> reviewIds = reviews
            .stream()
            .map(Review::getId)
            .toList();
        final Map<Long, List<ReviewReply>> repliesByReviewId =
            reviewReplyService.getFirstNRepliesByReviewIds(reviewIds, TEASER_REPLIES_LIMIT);
        final Map<Long, Long> replyCountsByReviewId =
            reviewReplyService.countRepliesByReviewIds(reviewIds);
        final Map<Long, Long> reviewLikeCounts =
            reviewLikeService.countReviewLikesByReviewIds(reviewIds);
        final Set<Long> likedReviewIds =
            currentUserId == null
                ? Collections.emptySet()
                : reviewLikeService.getLikedReviewIds(reviewIds, currentUserId);

        final List<Long> replyIds = repliesByReviewId
            .values()
            .stream()
            .flatMap(List::stream)
            .map(ReviewReply::getId)
            .toList();
        final Map<Long, Long> replyLikeCounts =
            reviewLikeService.countReplyLikesByReplyIds(replyIds);
        final Set<Long> likedReplyIds =
            currentUserId == null
                ? Collections.emptySet()
                : reviewLikeService.getLikedReplyIds(replyIds, currentUserId);

        return reviews
            .stream()
            .map(review ->
                new ReviewThread(
                    review,
                    reviewLikeCounts.getOrDefault(review.getId(), 0L),
                    likedReviewIds.contains(review.getId()),
                    repliesByReviewId
                        .getOrDefault(review.getId(), Collections.emptyList())
                        .stream()
                        .map(reply ->
                            new ReviewReplyCard(
                                reply,
                                replyLikeCounts.getOrDefault(reply.getId(), 0L),
                                likedReplyIds.contains(reply.getId()),
                                currentUserId != null &&
                                    currentUserId.equals(reply.getUserId())
                            )
                        )
                        .collect(Collectors.toList()),
                    replyCountsByReviewId.getOrDefault(review.getId(), 0L)
                )
            )
            .collect(Collectors.toList());
    }

    private boolean isSelectedCarFavorited(
        final Car selectedCar,
        final AuthenticatedUser currentUser
    ) {
        return (
            currentUser != null &&
            carFavoriteService.isFavorited(
                currentUser.getId(),
                selectedCar.getId()
            )
        );
    }

    private Page<Review> getReviewsForCar(
        final long carId,
        final String sort,
        final int page
    ) {
        if (SORT_RATING_ASC.equals(sort)) {
            return reviewService.getReviewsByCarOrderByRatingAsc(carId, page);
        }
        if (SORT_RATING_DESC.equals(sort)) {
            return reviewService.getReviewsByCarOrderByRatingDesc(carId, page);
        }
        return reviewService.getReviewsByCar(carId, page);
    }

    private void populateReviewImages(final List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return;
        }
        final List<Long> ids = reviews.stream().map(Review::getId).collect(Collectors.toList());
        final java.util.Map<Long, List<ImageMetadata>> byId = reviewService.getImagesByReviewIds(ids);
        for (final Review r : reviews) {
            r.setImages(byId.getOrDefault(r.getId(), Collections.emptyList()));
        }
    }

    private String normalizeSort(final String sort) {
        if (SORT_RATING_ASC.equals(sort) || SORT_RATING_DESC.equals(sort)) {
            return sort;
        }
        return null;
    }

    private static List<MultipartFile> nonEmptyFiles(final List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        return files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .collect(Collectors.toList());
    }

    private void validateReviewImageUploads(final List<MultipartFile> files,
                                            final int retainedCount,
                                            final BindingResult errors) {
        final int total = retainedCount + files.size();
        if (total > MAX_REVIEW_IMAGE_COUNT) {
            errors.rejectValue("files", "validation.review.files.maxCount",
                    new Object[]{MAX_REVIEW_IMAGE_COUNT}, null);
            return;
        }
        for (final MultipartFile file : files) {
            final String errorKey = ControllerUtils.validateUploadedImage(file, false);
            if (errorKey != null) {
                errors.rejectValue("files", errorKey);
                return;
            }
        }
    }

    private static List<ImagePayload> toImagePayloads(final List<MultipartFile> files) throws IOException {
        final List<ImagePayload> result = new ArrayList<>();
        for (final MultipartFile file : files) {
            result.add(new ImagePayload(file.getContentType(), file.getBytes()));
        }
        return result;
    }

    private String reviewImageIdsCsv(final long reviewId) {
        final List<ImageMetadata> images = reviewService.getReviewImagesByReviewId(reviewId);
        if (images.isEmpty()) {
            return "";
        }
        return images.stream()
                .map(img -> Long.toString(img.getImageId()))
                .collect(Collectors.joining(","));
    }

    @RequestMapping(value = "/reviews/{reviewId}", method = RequestMethod.GET)
    public ModelAndView reviewDetail(
        @PathVariable("reviewId") final long reviewId,
        @RequestParam(value = "repliesPage", required = false) final Integer repliesPage,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        final Review review = reviewService
            .getReviewById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
        final Car car = carService
            .getCarById(review.getCarId())
            .orElseThrow(() -> new ResourceNotFoundException("Car", review.getCarId()));
        populateReviewImages(List.of(review));

        final Long currentUserId = currentUserId(currentUser);
        final int normalizedRepliesPage = Pagination.normalizePage(repliesPage);
        final Page<ReviewReply> repliesPageResult =
            reviewReplyService.getRepliesByReview(reviewId, normalizedRepliesPage);
        final ReviewThread thread = buildReviewDetailThread(review, repliesPageResult, currentUserId);

        final ModelAndView mav = new ModelAndView("review-detail.jsp");
        mav.addObject("selectedCar", car);
        mav.addObject("reviewThread", thread);
        mav.addObject("reviews", List.of(review));
        mav.addObject("reviewThreads", List.of(thread));
        mav.addObject("repliesCurrentPage", repliesPageResult.getPageNumber());
        mav.addObject("repliesTotalPages", repliesPageResult.getTotalPages());
        mav.addObject("repliesTotalItems", repliesPageResult.getTotalItems());
        mav.addObject("currentUserId", currentUserId);
        return mav;
    }

    private ReviewThread buildReviewDetailThread(
        final Review review,
        final Page<ReviewReply> repliesPage,
        final Long currentUserId
    ) {
        final List<ReviewReply> replies = repliesPage.getItems();
        final long reviewLikeCount = reviewLikeService.countReviewLikesByReviewIds(List.of(review.getId()))
            .getOrDefault(review.getId(), 0L);
        final boolean likedReview = currentUserId != null &&
            reviewLikeService.getLikedReviewIds(List.of(review.getId()), currentUserId).contains(review.getId());
        final List<Long> replyIds = replies.stream().map(ReviewReply::getId).toList();
        final Map<Long, Long> replyLikeCounts = reviewLikeService.countReplyLikesByReplyIds(replyIds);
        final Set<Long> likedReplyIds = currentUserId == null
            ? Collections.emptySet()
            : reviewLikeService.getLikedReplyIds(replyIds, currentUserId);

        final List<ReviewReplyCard> cards = replies.stream()
            .map(reply -> new ReviewReplyCard(
                reply,
                replyLikeCounts.getOrDefault(reply.getId(), 0L),
                likedReplyIds.contains(reply.getId()),
                currentUserId != null && currentUserId.equals(reply.getUserId())
            ))
            .collect(Collectors.toList());

        return new ReviewThread(review, reviewLikeCount, likedReview, cards, repliesPage.getTotalItems());
    }

    @RequestMapping(value = "/reviews/{reviewId}/images/{imageId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getReviewImage(
            @PathVariable("reviewId") final long reviewId,
            @PathVariable("imageId") final long imageId,
            @RequestHeader(value = "If-None-Match", required = false) final String ifNoneMatch
    ) {
        final Optional<ImageMetadata> metadata = reviewService.getReviewImageMetadataById(reviewId, imageId);
        if (metadata.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        final ImageMetadata meta = metadata.get();
        final String updatedAtStamp = meta.getUpdatedAt() == null ? "0" : meta.getUpdatedAt().toString();
        final String etag = "\"r" + reviewId + "-i" + imageId + "-" + updatedAtStamp + "\"";
        final CacheControl cacheControl = CacheControl.maxAge(Duration.ofDays(7)).cachePublic();
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .cacheControl(cacheControl)
                    .build();
        }
        final Optional<StoredImagePayload> image = reviewService.getReviewImageById(reviewId, imageId);
        if (image.isEmpty() || image.get().getImageData() == null) {
            return ResponseEntity.notFound().build();
        }
        final StoredImagePayload img = image.get();
        final String contentType = img.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE : img.getContentType();
        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(cacheControl)
                .contentType(MediaType.parseMediaType(contentType))
                .body(img.getImageData());
    }

    @RequestMapping(value = "/reviews/{reviewId}", method = RequestMethod.POST)
    public String updateReview(
        @PathVariable("reviewId") final long reviewId,
        @Valid @ModelAttribute("reviewForm") final ReviewForm reviewForm,
        final BindingResult errors,
        final Model model,
        @RequestParam(value = "redirect", required = false) final String redirect,
        final HttpServletRequest request,
        @AuthenticationPrincipal final AuthenticatedUser currentUser,
        final RedirectAttributes redirectAttributes
    ) {
        final Review existingReview = reviewService.getReviewAndCheckAccess(
            reviewId,
            currentUser.getId(),
            request.isUserInRole("ADMIN")
        );

        final Car car = carService
            .getCarById(existingReview.getCarId())
            .orElseThrow(() ->
                new ResourceNotFoundException("El auto referenciado no existe.")
            );
        reviewForm.setCarId(existingReview.getCarId());

        final String defaultRedirect =
            "/reviews/car/" + existingReview.getCarId() + "#review-" + reviewId;
        final String safeRedirect = LoginRedirectUtils.safeRedirect(redirect, request.getContextPath()).orElse(defaultRedirect);

        final List<MultipartFile> uploadedFiles = nonEmptyFiles(reviewForm.getFiles());
        final List<ImagePayload> retainedPayloads = reviewService.collectRetainedReviewImagePayloads(
                reviewId, reviewForm.getRetainedImageIds());
        validateReviewImageUploads(uploadedFiles, retainedPayloads.size(), errors);

        if (errors.hasErrors()) {
            model.addAttribute("selectedCar", car);
            model.addAttribute("editMode", true);
            model.addAttribute("reviewId", reviewId);
            model.addAttribute("editRedirect", safeRedirect);
            model.addAttribute("existingReviewImageIds", reviewImageIdsCsv(reviewId));
            return "review-form.jsp";
        }

        final List<ImagePayload> finalImages = new ArrayList<>(retainedPayloads);
        try {
            finalImages.addAll(toImagePayloads(uploadedFiles));
        } catch (final IOException e) {
            LOGGER.error(
                "failed to read uploaded image during review update reviewId={} userId={}",
                reviewId,
                currentUser.getId(),
                e
            );
            throw new UploadedImageReadException(
                "updating review " + reviewId + " by user " + currentUser.getId(), e);
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
                reviewForm.getTagIds(),
                finalImages
            );
            LOGGER.info(
                "updated review id={} userId={}",
                reviewId,
                currentUser.getId()
            );
        } catch (final InvalidReviewTagSelectionException e) {
            LOGGER.warn(
                "update review rejected: invalid tag selection reviewId={} userId={}",
                reviewId,
                currentUser.getId()
            );
            errors.rejectValue("tagIds", "tagIds.invalid", e.getMessage());
            model.addAttribute("selectedCar", car);
            model.addAttribute("editMode", true);
            model.addAttribute("reviewId", reviewId);
            model.addAttribute("editRedirect", safeRedirect);
            return "review-form.jsp";
        }
        redirectAttributes.addFlashAttribute(ACTION_TOAST_ATTRIBUTE, "review.update.toast.success");
        return "redirect:" + safeRedirect;
    }

    @RequestMapping(
        value = "/reviews/{reviewId}/delete",
        method = RequestMethod.POST
    )
    public ModelAndView deleteReview(
        @PathVariable("reviewId") final long reviewId,
        @RequestParam(value = "redirect", required = false) final String redirect,
        final HttpServletRequest request,
        @AuthenticationPrincipal final AuthenticatedUser currentUser,
        final RedirectAttributes redirectAttributes
    ) {
        final Review existingReview = reviewService.getReviewAndCheckAccess(
            reviewId,
            currentUser.getId(),
            request.isUserInRole("ADMIN")
        );
        reviewService.deleteReview(reviewId);
        LOGGER.info(
            "user id={} deleted review id={}",
            currentUser.getId(),
            reviewId
        );
        final String defaultRedirect =
            "/reviews/car/" + existingReview.getCarId() + "#reviewsFeed";
        final String safeRedirect = LoginRedirectUtils.safeRedirect(redirect, request.getContextPath()).orElse(defaultRedirect);
        redirectAttributes.addFlashAttribute(ACTION_TOAST_ATTRIBUTE, "review.delete.toast.success");
        return new ModelAndView("redirect:" + safeRedirect);
    }

    @RequestMapping(
        value = "/reviews/{reviewId}/hide",
        method = RequestMethod.POST
    )
    public ModelAndView hideReview(
        @PathVariable("reviewId") final long reviewId,
        @Valid @ModelAttribute("reviewHideForm") final ReviewHideForm reviewHideForm,
        final BindingResult errors,
        @RequestParam(value = "redirect", required = false) final String redirect,
        final HttpServletRequest request,
        @AuthenticationPrincipal final AuthenticatedUser currentUser,
        final RedirectAttributes redirectAttributes
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        final Review review = reviewService
            .getReviewById(reviewId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Review", reviewId)
            );
        final long carId = review.getCarId();
        final String defaultRedirect =
            "/reviews/car/" + carId + "#review-" + reviewId;
        final String safeRedirect = LoginRedirectUtils
            .safeRedirect(redirect, request.getContextPath())
            .orElse(defaultRedirect);
        final String feedRedirect = "redirect:" + safeRedirect;

        if (errors.hasErrors()) {
            return new ModelAndView(feedRedirect);
        }

        if (!reviewService.hideReview(reviewId, reviewHideForm.getReason())) {
            LOGGER.warn("hide review failed reviewId={}", reviewId);
            return new ModelAndView(feedRedirect);
        }
        LOGGER.info(
            "admin id={} hid review id={}",
            currentUser.getId(),
            reviewId
        );
        redirectAttributes.addFlashAttribute(ACTION_TOAST_ATTRIBUTE, "review.hide.toast.success");
        return new ModelAndView(feedRedirect);
    }

    @RequestMapping(
        value = "/reviews/{reviewId}/replies",
        method = RequestMethod.POST
    )
    public ModelAndView createReply(
        @PathVariable("reviewId") final long reviewId,
        @Valid @ModelAttribute("reviewReplyForm") final ReviewReplyForm reviewReplyForm,
        final BindingResult errors,
        @RequestParam(value = "page", required = false) final Integer page,
        @RequestParam(value = "sort", required = false) final String sort,
        @AuthenticationPrincipal final AuthenticatedUser currentUser,
        final RedirectAttributes redirectAttributes
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }

        final Review review = reviewService
            .getReviewById(reviewId)
            .orElse(null);
        if (review == null) {
            throw new ResourceNotFoundException("Review", reviewId);
        }

        if (errors.hasErrors()) {
            final FieldError bodyError = errors.getFieldError("body");
            return carReviewPageWithReplyError(
                review.getCarId(),
                sort,
                page,
                reviewId,
                reviewReplyForm.getBody(),
                bodyError == null ? null : bodyError.getDefaultMessage(),
                currentUser
            );
        }

        final ReviewReply createdReply = reviewReplyService.createReply(reviewId, currentUser.getId(), reviewReplyForm.getBody());
        LOGGER.info(
            "user id={} replied to review id={}",
            currentUser.getId(),
            reviewId
        );
        final long totalReplies = reviewReplyService.countRepliesByReviewIds(List.of(reviewId))
            .getOrDefault(reviewId, 0L);
        final int lastPage = Math.max(1, Pagination.totalPages(totalReplies, Pagination.REPLIES_PAGE_SIZE));
        final StringBuilder target = new StringBuilder("redirect:/reviews/").append(reviewId);
        if (lastPage > 1) {
            target.append("?repliesPage=").append(lastPage);
        }
        target.append("#reply-").append(createdReply.getId());
        redirectAttributes.addFlashAttribute(ACTION_TOAST_ATTRIBUTE, "review.reply.create.toast.success");
        return new ModelAndView(target.toString());
    }

    @RequestMapping(
        value = "/reviews/replies/{replyId}/update",
        method = RequestMethod.POST
    )
    public ModelAndView updateReply(
        @PathVariable("replyId") final long replyId,
        @Valid @ModelAttribute("reviewReplyForm") final ReviewReplyForm reviewReplyForm,
        final BindingResult errors,
        @RequestParam(value = "redirect", required = false) final String redirect,
        final HttpServletRequest request,
        @AuthenticationPrincipal final AuthenticatedUser currentUser,
        final RedirectAttributes redirectAttributes
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        final ReviewReply reply = reviewReplyService
            .getReplyById(replyId)
            .orElseThrow(() -> new ResourceNotFoundException("Review reply", replyId));
        final Review review = reviewService
            .getReviewById(reply.getReviewId())
            .orElseThrow(() -> new ResourceNotFoundException("Review", reply.getReviewId()));
        final String defaultRedirect =
            "/reviews/car/" + review.getCarId() + "#reply-" + replyId;
        final String safeRedirect = LoginRedirectUtils
            .safeRedirect(redirect, request.getContextPath())
            .orElse(defaultRedirect);
        final String feedRedirect = "redirect:" + safeRedirect;

        if (errors.hasErrors()) {
            return new ModelAndView(feedRedirect);
        }

        reviewReplyService.updateReply(replyId, currentUser.getId(), reviewReplyForm.getBody());
        LOGGER.info("user id={} updated reply id={}", currentUser.getId(), replyId);
        redirectAttributes.addFlashAttribute(ACTION_TOAST_ATTRIBUTE, "review.reply.update.toast.success");
        return new ModelAndView(feedRedirect);
    }

    @RequestMapping(
        value = "/reviews/replies/{replyId}/delete",
        method = RequestMethod.POST
    )
    public ModelAndView deleteReply(
        @PathVariable("replyId") final long replyId,
        @RequestParam(value = "redirect", required = false) final String redirect,
        final HttpServletRequest request,
        @AuthenticationPrincipal final AuthenticatedUser currentUser,
        final RedirectAttributes redirectAttributes
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        final ReviewReply reply = reviewReplyService
            .getReplyById(replyId)
            .orElseThrow(() -> new ResourceNotFoundException("Review reply", replyId));
        final Review review = reviewService
            .getReviewById(reply.getReviewId())
            .orElseThrow(() -> new ResourceNotFoundException("Review", reply.getReviewId()));
        final String defaultRedirect =
            "/reviews/car/" + review.getCarId() + "#review-" + review.getId();
        final String safeRedirect = LoginRedirectUtils
            .safeRedirect(redirect, request.getContextPath())
            .orElse(defaultRedirect);

        reviewReplyService.deleteReply(replyId, currentUser.getId());
        LOGGER.info("user id={} deleted reply id={}", currentUser.getId(), replyId);
        redirectAttributes.addFlashAttribute(ACTION_TOAST_ATTRIBUTE, "review.reply.delete.toast.success");
        return new ModelAndView("redirect:" + safeRedirect);
    }

    @RequestMapping(
        value = "/reviews/replies/{replyId}/hide",
        method = RequestMethod.POST
    )
    public ModelAndView hideReply(
        @PathVariable("replyId") final long replyId,
        @Valid @ModelAttribute("reviewHideForm") final ReviewHideForm reviewHideForm,
        final BindingResult errors,
        @RequestParam(value = "redirect", required = false) final String redirect,
        final HttpServletRequest request,
        @AuthenticationPrincipal final AuthenticatedUser currentUser,
        final RedirectAttributes redirectAttributes
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        final ReviewReply reply = reviewReplyService
            .getReplyById(replyId)
            .orElseThrow(() -> new ResourceNotFoundException("Review reply", replyId));
        final Review review = reviewService
            .getReviewById(reply.getReviewId())
            .orElseThrow(() -> new ResourceNotFoundException("Review", reply.getReviewId()));
        final String defaultRedirect =
            "/reviews/car/" + review.getCarId() + "#review-" + review.getId();
        final String safeRedirect = LoginRedirectUtils
            .safeRedirect(redirect, request.getContextPath())
            .orElse(defaultRedirect);
        final String feedRedirect = "redirect:" + safeRedirect;

        if (errors.hasErrors()) {
            return new ModelAndView(feedRedirect);
        }

        if (!reviewReplyService.hideReply(replyId, reviewHideForm.getReason())) {
            LOGGER.warn("hide reply failed replyId={}", replyId);
            return new ModelAndView(feedRedirect);
        }
        LOGGER.info("admin id={} hid reply id={}", currentUser.getId(), replyId);
        redirectAttributes.addFlashAttribute(ACTION_TOAST_ATTRIBUTE, "review.reply.hide.toast.success");
        return new ModelAndView(feedRedirect);
    }

    @RequestMapping(
        value = "/reviews/{reviewId}/like",
        method = RequestMethod.POST
    )
    public ModelAndView toggleReviewLike(
        @PathVariable("reviewId") final long reviewId,
        @RequestParam(value = "redirect", required = false) final String redirect,
        final HttpServletRequest request,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }

        final Review review = reviewService
            .getReviewById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        final boolean liked = reviewLikeService.toggleReviewLike(
            reviewId,
            currentUser.getId()
        );
        LOGGER.info(
            "user id={} toggled review like reviewId={} liked={}",
            currentUser.getId(),
            reviewId,
            liked
        );
        final String defaultRedirect = "/reviews/car/" + review.getCarId() + "#review-" + reviewId;
        final String safeRedirect = LoginRedirectUtils
            .safeRedirect(redirect, request.getContextPath())
            .orElse(defaultRedirect);
        return new ModelAndView("redirect:" + safeRedirect);
    }

    @RequestMapping(
        value = "/reviews/replies/{replyId}/like",
        method = RequestMethod.POST
    )
    public ModelAndView toggleReplyLike(
        @PathVariable("replyId") final long replyId,
        @RequestParam(value = "redirect", required = false) final String redirect,
        final HttpServletRequest request,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }

        final ReviewReply reply = reviewReplyService
            .getReplyById(replyId)
            .orElseThrow(() -> new ResourceNotFoundException("Review reply", replyId));
        final Review review = reviewService
            .getReviewById(reply.getReviewId())
            .orElseThrow(() ->
                new ResourceNotFoundException("Review", reply.getReviewId())
            );

        final boolean liked = reviewLikeService.toggleReplyLike(
            replyId,
            currentUser.getId()
        );
        LOGGER.info(
            "user id={} toggled reply like replyId={} liked={}",
            currentUser.getId(),
            replyId,
            liked
        );
        final String defaultRedirect =
            "/reviews/car/" + review.getCarId() + "#review-" + review.getId();
        final String safeRedirect = LoginRedirectUtils
            .safeRedirect(redirect, request.getContextPath())
            .orElse(defaultRedirect);
        return new ModelAndView("redirect:" + safeRedirect);
    }

    private String normalizeOwnershipStatus(final String ownershipStatus) {
        return ReviewOwnershipStatus.normalize(ownershipStatus);
    }

    private ReviewForm toReviewForm(final Review review) {
        final ReviewForm form = new ReviewForm();
        form.setCarId(review.getCarId());
        form.setRating(review.getRating());
        form.setTitle(review.getTitle());
        form.setBody(review.getBody());
        form.setOwnershipStatus(ReviewOwnershipStatus.normalize(review.getOwnershipStatus()));
        form.setMileageKm(review.getMileageKm());
        form.setWouldRecommend(review.getWouldRecommend());
        form.setTagIds(
            review
                .getTags()
                .stream()
                .map(tag -> tag.getId())
                .collect(Collectors.toSet())
        );
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
        private final List<ImageMetadata> carImages;
        private final int currentPage;
        private final int totalPages;
        private final long totalItems;
        private final BigDecimal averageRating;

        private ReviewPageData(
            final Car selectedCar,
            final List<Review> reviews,
            final List<ReviewThread> reviewThreads,
            final String currentSort,
            final List<ImageMetadata> carImages,
            final int currentPage,
            final int totalPages,
            final long totalItems,
            final BigDecimal averageRating
        ) {
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
        private final long totalReplyCount;

        private ReviewThread(
            final Review review,
            final long likeCount,
            final boolean liked,
            final List<ReviewReplyCard> replies,
            final long totalReplyCount
        ) {
            this.review = review;
            this.likeCount = likeCount;
            this.liked = liked;
            this.replies = replies;
            this.totalReplyCount = totalReplyCount;
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

        public long getTotalReplyCount() {
            return totalReplyCount;
        }

        public boolean getHasMoreReplies() {
            return totalReplyCount > replies.size();
        }
    }

    public static final class ReviewReplyCard {

        private final ReviewReply reply;
        private final long likeCount;
        private final boolean liked;
        private final boolean ownedByCurrentUser;

        private ReviewReplyCard(
            final ReviewReply reply,
            final long likeCount,
            final boolean liked,
            final boolean ownedByCurrentUser
        ) {
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
