package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.StoredImagePayload;
import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.services.BrandService;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.exception.DuplicateCarException;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.auth.LoginRedirectUtils;
import ar.edu.itba.paw.webapp.exception.UploadedImageReadException;
import ar.edu.itba.paw.webapp.form.CarForm;
import ar.edu.itba.paw.webapp.util.ImageValidationService;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CarController {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        CarController.class
    );

    private static final int FEATURED_REVIEW_COUNT = 3;
    private static final int MAX_IMAGE_COUNT = 5;
    private final CarService carService;
    private final CarFavoriteService carFavoriteService;
    private final BrandService brandService;
    private final BodyTypeService bodyTypeService;
    private final ReviewService reviewService;
    private final ImageValidationService imageValidationService;

    @Autowired
    public CarController(
        final CarService carService,
        final CarFavoriteService carFavoriteService,
        final BrandService brandService,
        final BodyTypeService bodyTypeService,
        final ReviewService reviewService,
        final ImageValidationService imageValidationService
    ) {
        this.carService = carService;
        this.carFavoriteService = carFavoriteService;
        this.brandService = brandService;
        this.bodyTypeService = bodyTypeService;
        this.reviewService = reviewService;
        this.imageValidationService = imageValidationService;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(
            String.class,
            new StringTrimmerEditor(true)
        );
        binder.setDisallowedFields(
            "formMode",
            "carId",
            "requestId",
            "retainedImageIds"
        );
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView home(
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        return landingPage(currentUser);
    }

    private ModelAndView landingPage(final AuthenticatedUser currentUser) {
        final List<Car> featuredCars = carService.getFeaturedCars(
            FEATURED_REVIEW_COUNT
        );
        final Car heroCar = featuredCars.isEmpty() ? null : featuredCars.get(0);
        final Review heroReview =
            heroCar == null
                ? null
                : reviewService
                      .getTopRatedLatestReviewByCar(heroCar.getId())
                      .orElse(null);
        final Map<Long, ReviewStats> reviewStatsByCarId = getReviewStatsByCarId(
            featuredCars
        );

        final ModelAndView mav = new ModelAndView("landing.jsp");
        mav.addObject("featuredCars", featuredCars);
        mav.addObject("reviewStatsByCarId", reviewStatsByCarId);
        mav.addObject(
            "favoritedCarIds",
            favoritedCarIdsById(featuredCars, currentUser)
        );
        mav.addObject("heroCar", heroCar);
        mav.addObject("heroReview", heroReview);
        return mav;
    }

    @RequestMapping(value = "/cars", method = RequestMethod.GET)
    public String listCars(
        @ModelAttribute final CarSearchCriteria criteria,
        @RequestParam(
            value = "createCar",
            required = false
        ) final String createCar,
        @RequestParam(
            value = "submitted",
            required = false
        ) final String submitted,
        @AuthenticationPrincipal final AuthenticatedUser currentUser,
        final Model model
    ) {
        if ("true".equalsIgnoreCase(createCar)) {
            return "redirect:/cars/new";
        }
        populateCarsPageModel(model, criteria, currentUser);
        final String submittedToastMessageCode =
            ControllerUtils.submittedToastMessageCode(submitted);
        if (submittedToastMessageCode != null) {
            model.addAttribute("showSubmittedToast", true);
            model.addAttribute(
                "submittedToastMessageCode",
                submittedToastMessageCode
            );
        }
        return "cars.jsp";
    }

    @RequestMapping(value = "/cars/new", method = RequestMethod.GET)
    public ModelAndView newCarRequest(
        @RequestParam(
            value = "submitted",
            required = false
        ) final String submitted
    ) {
        final ModelAndView mav = new ModelAndView("car-form.jsp");
        addSubmittedToast(mav, submitted);
        return mav;
    }

    @RequestMapping(
        value = "/cars",
        method = RequestMethod.POST,
        consumes = "multipart/form-data"
    )
    public String createCar(
        @Valid @ModelAttribute("carForm") final CarForm carForm,
        final BindingResult errors,
        final Model model,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        final List<MultipartFile> files = selectedImageFiles(carForm.getFiles());
        carForm.setFormMode("create");
        carForm.setCarId(null);
        carForm.setRequestId(null);
        carForm.setRetainedImageIds(Collections.emptyList());
        final String imageError = validateUploadedImages(files, true);
        if (imageError != null) {
            errors.rejectValue(
                "files",
                imageError,
                new Object[] { MAX_IMAGE_COUNT },
                null
            );
        }
        final ControllerUtils.ResolvedCarCatalog resolvedCatalog =
            ControllerUtils.resolveCarCatalog(carForm, errors, brandService, bodyTypeService);
        final Brand resolvedBrand = resolvedCatalog.getBrand();
        final BodyType resolvedBodyType = resolvedCatalog.getBodyType();

        if (errors.hasErrors()) {
            LOGGER.warn(
                "car request submission rejected: validation errors userId={} errorCount={}",
                currentUser.getId(),
                errors.getErrorCount()
            );
            return "car-form.jsp";
        }

        final List<ImagePayload> imagePayloads;
        try {
            imagePayloads = toImagePayloads(files);
        } catch (final IOException e) {
            LOGGER.error("failed to read uploaded image during car request creation userId={}", currentUser.getId(), e);
            throw new UploadedImageReadException("creating car request for user " + currentUser.getId(), e);
        }

        final CarRequest carRequest;
        try {
            carRequest = carService.requestCarCreation(
                resolvedBrand.getId(),
                carForm.getModel(),
                resolvedBodyType.getId(),
                carForm.getYear(),
                currentUser.getId(),
                currentUser.getEmail(),
                carForm.getDescription(),
                imagePayloads,
                ControllerUtils.normalizeSpecValue(carForm.getFuelType()),
                carForm.getHorsepower(),
                carForm.getAirbagCount(),
                ControllerUtils.normalizeSpecValue(carForm.getTransmission()),
                carForm.getFuelConsumption(),
                carForm.getMaxSpeedKmh(),
                carForm.getPriceUsd()
            );
        } catch (final DuplicateCarException e) {
            errors.reject("validation.car.duplicate");
            LOGGER.warn(
                "car request submission rejected: duplicate car userId={}",
                currentUser.getId()
            );
            return "car-form.jsp";
        }
        LOGGER.info(
            "submitted car request id={} userId={} brandId={} bodyTypeId={}",
            carRequest.getId(),
            currentUser.getId(),
            resolvedBrand.getId(),
            resolvedBodyType.getId()
        );

        return "redirect:/cars?submitted=true";
    }

    private void addSubmittedToast(
        final ModelAndView mav,
        final String submitted
    ) {
        final String submittedToastMessageCode =
            ControllerUtils.submittedToastMessageCode(submitted);
        if (submittedToastMessageCode != null) {
            mav.addObject("showSubmittedToast", true);
            mav.addObject(
                "submittedToastMessageCode",
                submittedToastMessageCode
            );
        }
    }

    @RequestMapping(
        value = "/cars/{carId}/favorite",
        method = RequestMethod.POST
    )
    public ModelAndView updateFavorite(
        @PathVariable("carId") final long carId,
        @RequestParam("favorite") final boolean favorite,
        @RequestHeader(
            value = "Referer",
            required = false
        ) final String referer,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (carService.getCarById(carId).isEmpty()) {
            return new ModelAndView("redirect:/cars");
        }

        carFavoriteService.setFavorite(currentUser.getId(), carId, favorite);
        LOGGER.info(
            "user id={} set favorite carId={} favorited={}",
            currentUser.getId(),
            carId,
            favorite
        );
        return new ModelAndView("redirect:" + safeRedirectPath(referer));
    }

    private void populateCarsPageModel(
        final Model model,
        final CarSearchCriteria criteria,
        final AuthenticatedUser currentUser
    ) {
        final CarCatalogData catalogData = resolveCatalogData(criteria);

        model.addAttribute("cars", catalogData.cars);
        model.addAttribute(
            "reviewStatsByCarId",
            catalogData.reviewStatsByCarId
        );
        model.addAttribute(
            "favoritedCarIds",
            favoritedCarIdsById(catalogData.cars, currentUser)
        );
        model.addAttribute("selectedBrand", criteria.getBrand());
        model.addAttribute("selectedBodyType", criteria.getBodyType());
        model.addAttribute("searchQuery", criteria.getQ());
        model.addAttribute("criteria", criteria);
        model.addAttribute("hasAdvancedFilters", criteria.hasAdvancedFilters());
        model.addAttribute("currentPage", catalogData.page.getPageNumber());
        model.addAttribute("totalPages", catalogData.page.getTotalPages());
        model.addAttribute("totalItems", catalogData.page.getTotalItems());
        model.addAttribute(
            "showHp",
            criteria.getHorsepowerMin() != null ||
                criteria.getHorsepowerMax() != null
        );
        model.addAttribute("showSpeed", criteria.getMaxSpeedMin() != null);
        model.addAttribute(
            "showConsumption",
            criteria.getFuelConsumptionMax() != null
        );
        model.addAttribute("showAirbags", criteria.getAirbagMin() != null);
        model.addAttribute(
            "showTransmission",
            criteria.getTransmission() != null
        );
        model.addAttribute("showFuelType", criteria.getFuelTypes().size() > 1);
        model.addAttribute(
            "showPrice",
            criteria.getPriceMin() != null || criteria.getPriceMax() != null
        );
        model.addAttribute(
            "showYear",
            criteria.getYearMin() != null || criteria.getYearMax() != null
        );
    }

    private Map<Long, Boolean> favoritedCarIdsById(
        final List<Car> cars,
        final AuthenticatedUser currentUser
    ) {
        if (currentUser == null || cars.isEmpty()) {
            return Collections.emptyMap();
        }
        return carFavoriteService
            .getFavoritedCarIds(
                currentUser.getId(),
                cars.stream().map(Car::getId).collect(Collectors.toList())
            )
            .stream()
            .collect(
                Collectors.toMap(Function.identity(), ignored -> Boolean.TRUE)
            );
    }

    private String safeRedirectPath(final String referer) {
        return LoginRedirectUtils
            .safeRefererPath(referer, ControllerUtils.currentContextPath())
            .filter(CarController::isAllowedFavoriteReturnPath)
            .orElse("/cars");
    }

    private static boolean isAllowedFavoriteReturnPath(final String target) {
        final String path = ControllerUtils.pathWithoutQuery(target);
        return "/".equals(path)
            || "/cars".equals(path)
            || path.matches("/reviews/car/\\d+")
            || "/user".equals(path)
            || path.matches("/users/\\d+");
    }

    @RequestMapping(value = "/cars/{carId}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getCarImage(
        @PathVariable("carId") final long carId,
        @RequestHeader(
            value = "If-None-Match",
            required = false
        ) final String ifNoneMatch
    ) {
        return getCarImageResponse(carId, ifNoneMatch);
    }

    @RequestMapping(value = "/car-image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getCarImageByQueryParam(
        @RequestParam("carId") final long carId,
        @RequestHeader(
            value = "If-None-Match",
            required = false
        ) final String ifNoneMatch
    ) {
        return getCarImageResponse(carId, ifNoneMatch);
    }

    private ResponseEntity<byte[]> getCarImageResponse(
        final long carId,
        final String ifNoneMatch
    ) {
        final ImageMetadata metadata = carService
            .getCarImageMetadataByCarId(carId)
            .orElse(null);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        return respondWithCachedImage(
            metadata,
            ifNoneMatch,
            () -> carService.getCarImageByCarId(carId).orElse(null)
        );
    }

    @RequestMapping(
        value = "/cars/{carId}/images/{imageId}",
        method = RequestMethod.GET
    )
    public ResponseEntity<byte[]> getCarImageById(
        @PathVariable("carId") final long carId,
        @PathVariable("imageId") final long imageId,
        @RequestHeader(
            value = "If-None-Match",
            required = false
        ) final String ifNoneMatch
    ) {
        final ImageMetadata metadata = carService
            .getCarImageMetadataById(carId, imageId)
            .orElse(null);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        return respondWithCachedImage(
            metadata,
            ifNoneMatch,
            () -> carService.getCarImageById(carId, imageId).orElse(null)
        );
    }

    private ResponseEntity<byte[]> respondWithCachedImage(
        final ImageMetadata metadata,
        final String ifNoneMatch,
        final java.util.function.Supplier<StoredImagePayload> imageSupplier
    ) {
        final String eTag = buildImageEtag(metadata);
        final CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.HOURS)
            .cachePublic()
            .mustRevalidate();
        final long lastModified = imageLastModified(metadata);

        if (eTag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                .eTag(eTag)
                .cacheControl(cacheControl)
                .lastModified(lastModified)
                .build();
        }

        final StoredImagePayload carImage = imageSupplier.get();
        if (carImage == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType carImageMediaType;
        try {
            carImageMediaType = carImage.getContentType() == null
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(carImage.getContentType());
        } catch (final IllegalArgumentException e) {
            carImageMediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
            .contentType(carImageMediaType)
            .contentLength(carImage.getImageData().length)
            .cacheControl(cacheControl)
            .eTag(eTag)
            .lastModified(lastModified)
            .body(carImage.getImageData());
    }

    @RequestMapping(
        value = "/cars/{carId}/image",
        method = RequestMethod.POST,
        consumes = "multipart/form-data"
    )
    public ResponseEntity<?> uploadCarImage(
        @PathVariable("carId") final long carId,
        @RequestParam("file") final List<MultipartFile> files
    ) {
        return uploadCarImageResponse(carId, files);
    }

    @RequestMapping(
        value = "/car-image",
        method = RequestMethod.POST,
        consumes = "multipart/form-data"
    )
    public ResponseEntity<?> uploadCarImageByQueryParam(
        @RequestParam("carId") final long carId,
        @RequestParam("file") final List<MultipartFile> files
    ) {
        return uploadCarImageResponse(carId, files);
    }

    private ResponseEntity<?> uploadCarImageResponse(
        final long carId,
        final List<MultipartFile> files
    ) {
        if (carService.getCarById(carId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                "error.car.notFound"
            );
        }
        final List<MultipartFile> selectedFiles = selectedImageFiles(files);
        final String imageValidationError = validateUploadedImages(selectedFiles, true);
        if (imageValidationError != null) {
            return ResponseEntity.badRequest().body(imageValidationError);
        }

        try {
            carService.saveCarImages(carId, toImagePayloads(selectedFiles));
            LOGGER.info("uploaded {} image(s) for car id={}", selectedFiles.size(), carId);
        } catch (final IOException e) {
            LOGGER.error("failed to read uploaded image for car id={}", carId, e);
            throw new UploadedImageReadException("updating car " + carId, e);
        }

        return ResponseEntity.noContent().build();
    }

    private String validateUploadedImages(final List<MultipartFile> files, final boolean required) {
        if (files.isEmpty()) {
            return required ? "validation.car.image.required" : null;
        }
        if (files.size() > MAX_IMAGE_COUNT) {
            return "validation.car.files.maxCount";
        }
        for (final MultipartFile file : files) {
            final String imageError = validateUploadedImage(file, true);
            if (imageError != null) {
                return imageError;
            }
        }
        return null;
    }

    private String validateUploadedImage(final MultipartFile file, final boolean required) {
        return imageValidationService.validateUploadedImage(file, required);
    }

    private String resolveImageContentType(final MultipartFile file) {
        return ControllerUtils.normalizeContentType(
            file == null ? null : file.getContentType()
        );
    }

    private List<MultipartFile> selectedImageFiles(
        final List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        return files
            .stream()
            .filter(file -> file != null && !file.isEmpty())
            .collect(Collectors.toList());
    }

    private List<ImagePayload> toImagePayloads(
        final List<MultipartFile> files
    ) throws IOException {
        final List<ImagePayload> payloads = new ArrayList<>();
        for (final MultipartFile file : files) {
            payloads.add(
                new ImagePayload(
                    resolveImageContentType(file),
                    file.getBytes()
                )
            );
        }
        return payloads;
    }

    private CarCatalogData resolveCatalogData(final CarSearchCriteria criteria) {
        if (!criteria.isValid()) {
            return new CarCatalogData(Page.empty(1, 0), Collections.emptyMap());
        }

        final Page<Car> carPage = carService.searchCars(criteria);
        final List<Car> cars = carPage.getItems();

        final Map<Long, ReviewStats> reviewStatsByCarId;
        if (cars.isEmpty()) {
            reviewStatsByCarId = Collections.emptyMap();
        } else {
            reviewStatsByCarId = reviewService
                .getReviewStatsByCarIds(
                    cars.stream().map(Car::getId).collect(Collectors.toList())
                )
                .stream()
                .collect(
                    Collectors.toMap(ReviewStats::getCarId, Function.identity())
                );
        }
        return new CarCatalogData(carPage, reviewStatsByCarId);
    }

    private Map<Long, ReviewStats> getReviewStatsByCarId(final List<Car> cars) {
        if (cars.isEmpty()) {
            return Collections.emptyMap();
        }
        return reviewService
            .getReviewStatsByCarIds(
                cars.stream().map(Car::getId).collect(Collectors.toList())
            )
            .stream()
            .collect(
                Collectors.toMap(ReviewStats::getCarId, Function.identity())
            );
    }

    private static String buildImageEtag(final ImageMetadata metadata) {
        final Object stamp = metadata.getUpdatedAt() == null ? "0" : metadata.getUpdatedAt();
        return "\"c" + metadata.getOwnerId() + "-i" + metadata.getImageId()
                + "-" + stamp + "\"";
    }

    private static long imageLastModified(final ImageMetadata metadata) {
        if (metadata.getUpdatedAt() == null) {
            return 0L;
        }
        return metadata.getUpdatedAt()
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();
    }

    private static final class CarCatalogData {

        private final Page<Car> page;
        private final List<Car> cars;
        private final Map<Long, ReviewStats> reviewStatsByCarId;

        private CarCatalogData(
            final Page<Car> page,
            final Map<Long, ReviewStats> reviewStatsByCarId
        ) {
            this.page = page;
            this.cars = page.getItems();
            this.reviewStatsByCarId = reviewStatsByCarId;
        }
    }
}
