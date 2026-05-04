package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.services.BrandService;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.exception.DuplicateCarException;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.exception.ETagGenerationException;
import ar.edu.itba.paw.webapp.exception.UploadedImageReadException;
import ar.edu.itba.paw.webapp.form.CarForm;
import ar.edu.itba.paw.webapp.util.ImageValidationService;
import ar.edu.itba.paw.webapp.util.LogSanitizer;
import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private final MessageSource messageSource;
    private final ImageValidationService imageValidationService;

    @Autowired
    public CarController(
        final CarService carService,
        final CarFavoriteService carFavoriteService,
        final BrandService brandService,
        final BodyTypeService bodyTypeService,
        final ReviewService reviewService,
        final MessageSource messageSource,
        final ImageValidationService imageValidationService
    ) {
        this.carService = carService;
        this.carFavoriteService = carFavoriteService;
        this.brandService = brandService;
        this.bodyTypeService = bodyTypeService;
        this.reviewService = reviewService;
        this.messageSource = messageSource;
        this.imageValidationService = imageValidationService;
    }

    private String message(final String code, final Object... args) {
        return messageSource.getMessage(
            code,
            args,
            LocaleContextHolder.getLocale()
        );
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(
            String.class,
            new StringTrimmerEditor(true)
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

    @RequestMapping(value = "/cars/content", method = RequestMethod.GET)
    public ModelAndView listCarsContent(
        @ModelAttribute final CarSearchCriteria criteria,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        final CarCatalogData catalogData = resolveCatalogData(criteria);

        final ModelAndView mav = new ModelAndView("cars-content.jsp");
        mav.addObject("cars", catalogData.cars);
        mav.addObject("reviewStatsByCarId", catalogData.reviewStatsByCarId);
        mav.addObject(
            "favoritedCarIds",
            favoritedCarIdsById(catalogData.cars, currentUser)
        );
        mav.addObject("criteria", criteria);
        mav.addObject("currentPage", catalogData.page.getPageNumber());
        mav.addObject("totalPages", catalogData.page.getTotalPages());
        mav.addObject("totalItems", catalogData.page.getTotalItems());
        addShowSpecFlags(mav, criteria);
        return mav;
    }

    @RequestMapping(value = "/cars/new", method = RequestMethod.GET)
    public ModelAndView newCarRequest(
        @RequestParam(
            value = "submitted",
            required = false
        ) final String submitted
    ) {
        final ModelAndView mav = new ModelAndView("car-form.jsp");
        mav.addObject("carForm", new CarForm());
        populateCarFormPageModel(mav);
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
        if (currentUser == null) {
            return "redirect:/cars/new";
        }

        final List<MultipartFile> files = selectedImageFiles(carForm.getFiles());
        final String imageError = validateUploadedImages(files, true);
        if (imageError != null) {
            errors.rejectValue("files", "image.invalid", imageError);
        }
        rejectInvalidSpecFields(errors, carForm);

        Brand resolvedBrand = null;
        if (!errors.hasFieldErrors("brand")) {
            resolvedBrand = brandService
                .findByName(carForm.getBrand())
                .orElse(null);
            if (resolvedBrand == null) {
                errors.rejectValue(
                    "brand",
                    "validation.car.brand.invalid",
                    message("validation.car.brand.invalid")
                );
            }
        }

        BodyType resolvedBodyType = null;
        if (!errors.hasFieldErrors("bodyType")) {
            resolvedBodyType = bodyTypeService
                .findByName(carForm.getBodyType())
                .orElse(null);
            if (resolvedBodyType == null) {
                errors.rejectValue(
                    "bodyType",
                    "validation.car.bodyType.invalid",
                    message("validation.car.bodyType.invalid")
                );
            }
        }

        if (errors.hasErrors()) {
            LOGGER.warn(
                "car request submission rejected: validation errors userId={} errorCount={}",
                currentUser.getId(),
                errors.getErrorCount()
            );
            return "car-form.jsp";
        }

        final List<CarImagePayload> imagePayloads;
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
                Optional.ofNullable(carForm.getDescription()).filter(value ->
                    !value.isEmpty()
                ),
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
            errors.reject(
                "validation.car.duplicate",
                message("validation.car.duplicate")
            );
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

    private void populateCarFormPageModel(final ModelAndView mav) {
        mav.addObject("brands", brandService.findAll());
        mav.addObject("bodyTypes", bodyTypeService.findAll());
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
    public Object updateFavorite(
        @PathVariable("carId") final long carId,
        @RequestParam("favorite") final boolean favorite,
        @RequestHeader(
            value = "X-Requested-With",
            required = false
        ) final String requestedWith,
        @RequestHeader(
            value = "Referer",
            required = false
        ) final String referer,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            if (ControllerUtils.isAjaxRequest(requestedWith)) {
                return new ResponseEntity<String>(
                    "/login",
                    HttpStatus.UNAUTHORIZED
                );
            }
            return new ModelAndView("redirect:/login");
        }
        if (carService.getCarById(carId).isEmpty()) {
            if (ControllerUtils.isAjaxRequest(requestedWith)) {
                return new ResponseEntity<String>(
                    message("error.car.notFound"),
                    HttpStatus.NOT_FOUND
                );
            }
            return new ModelAndView("redirect:/cars");
        }

        carFavoriteService.setFavorite(currentUser.getId(), carId, favorite);
        final boolean favorited = carFavoriteService.isFavorited(
            currentUser.getId(),
            carId
        );
        LOGGER.info(
            "user id={} set favorite carId={} favorited={}",
            currentUser.getId(),
            carId,
            favorited
        );
        if (ControllerUtils.isAjaxRequest(requestedWith)) {
            return new ResponseEntity<String>(
                Boolean.toString(favorited),
                HttpStatus.OK
            );
        }
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

    private void addShowSpecFlags(
        final ModelAndView mav,
        final CarSearchCriteria criteria
    ) {
        mav.addObject(
            "showHp",
            criteria.getHorsepowerMin() != null ||
                criteria.getHorsepowerMax() != null
        );
        mav.addObject("showSpeed", criteria.getMaxSpeedMin() != null);
        mav.addObject(
            "showConsumption",
            criteria.getFuelConsumptionMax() != null
        );
        mav.addObject("showAirbags", criteria.getAirbagMin() != null);
        mav.addObject("showTransmission", criteria.getTransmission() != null);
        mav.addObject("showFuelType", criteria.getFuelTypes().size() > 1);
        mav.addObject(
            "showPrice",
            criteria.getPriceMin() != null || criteria.getPriceMax() != null
        );
        mav.addObject(
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
        if (referer == null || referer.isBlank()) {
            return "/cars";
        }
        try {
            final URI uri = URI.create(referer);
            final String path = uri.getRawPath();
            if (path == null || path.isBlank()) {
                return "/cars";
            }
            if (
                "/".equals(path) ||
                "/cars".equals(path) ||
                "/reviews".equals(path) ||
                "/profile".equals(path) ||
                path.matches("/profiles/\\d+")
            ) {
                final String query = uri.getRawQuery();
                return path + (query == null ? "" : "?" + query);
            }
        } catch (final IllegalArgumentException e) {
            LOGGER.warn(
                "invalid referer URI for redirect, falling back to /cars referer={}",
                LogSanitizer.forLog(
                    referer,
                    LogSanitizer.MAX_LOG_URL_CODE_POINTS
                ),
                e
            );
            return "/cars";
        }
        return "/cars";
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
        final CarImage carImage = carService
            .getCarImageByCarId(carId)
            .orElse(null);
        return getCarImageResponse(carImage, ifNoneMatch);
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
        final CarImage carImage = carService
            .getCarImageById(carId, imageId)
            .orElse(null);
        return getCarImageResponse(carImage, ifNoneMatch);
    }

    private ResponseEntity<byte[]> getCarImageResponse(
        final CarImage carImage,
        final String ifNoneMatch
    ) {
        if (carImage == null) {
            return ResponseEntity.notFound().build();
        }

        final String eTag = buildImageEtag(carImage);
        final CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.HOURS)
            .cachePublic()
            .mustRevalidate();

        if (eTag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                .eTag(eTag)
                .cacheControl(cacheControl)
                .lastModified(
                    carImage
                        .getUpdatedAt()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                )
                .build();
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(carImage.getContentType()))
            .contentLength(carImage.getImageData().length)
            .cacheControl(cacheControl)
            .eTag(eTag)
            .lastModified(
                carImage
                    .getUpdatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )
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
                message("error.car.notFound")
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
            return required ? message("validation.car.image.required") : null;
        }
        if (files.size() > MAX_IMAGE_COUNT) {
            return message("validation.car.files.maxCount", MAX_IMAGE_COUNT);
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
        final String key = imageValidationService.validateUploadedImage(file, required);
        return key == null ? null : message(key);
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

    private List<CarImagePayload> toImagePayloads(
        final List<MultipartFile> files
    ) throws IOException {
        final List<CarImagePayload> payloads = new ArrayList<>();
        for (final MultipartFile file : files) {
            payloads.add(
                new CarImagePayload(
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

    private void rejectInvalidSpecFields(final BindingResult errors, final CarForm carForm) {
        if (!errors.hasFieldErrors("fuelType")
                && !CarSearchCriteria.ALLOWED_FUEL_TYPES.contains(
                        ControllerUtils.normalizeSpecValue(carForm.getFuelType()))) {
            errors.rejectValue("fuelType", "validation.car.fuelType.invalid", message("validation.car.fuelType.invalid"));
        }
        if (!errors.hasFieldErrors("transmission")
                && !CarSearchCriteria.ALLOWED_TRANSMISSIONS.contains(
                        ControllerUtils.normalizeSpecValue(carForm.getTransmission()))) {
            errors.rejectValue("transmission", "validation.car.transmission.invalid", message("validation.car.transmission.invalid"));
        }
    }

    private static String buildImageEtag(final CarImage carImage) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(
                carImage
                    .getContentType()
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );
            digest.update(carImage.getImageData());
            digest.update(
                carImage
                    .getUpdatedAt()
                    .toString()
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );
            return "\"" + HexFormat.of().formatHex(digest.digest()) + "\"";
        } catch (final NoSuchAlgorithmException e) {
            throw new ETagGenerationException(
                "car image",
                carImage.getImageId(),
                e
            );
        }
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
