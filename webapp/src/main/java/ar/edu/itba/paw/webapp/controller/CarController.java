package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.services.BrandService;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.EmailService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.form.CarForm;
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

import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class CarController {

    private static final int FEATURED_REVIEW_COUNT = 3;
    private static final int MAX_IMAGE_COUNT = 5;

    private final CarService carService;
    private final CarFavoriteService carFavoriteService;
    private final BrandService brandService;
    private final BodyTypeService bodyTypeService;
    private final ReviewService reviewService;
    private final EmailService emailService;

    @Autowired
    public CarController(final CarService carService, final CarFavoriteService carFavoriteService,
                         final BrandService brandService, final BodyTypeService bodyTypeService,
                         final ReviewService reviewService, final EmailService emailService) {
        this.carService = carService;
        this.carFavoriteService = carFavoriteService;
        this.brandService = brandService;
        this.bodyTypeService = bodyTypeService;
        this.reviewService = reviewService;
        this.emailService = emailService;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView home(@AuthenticationPrincipal final AuthenticatedUser currentUser) {
        return landingPage(currentUser);
    }

    private ModelAndView landingPage(final AuthenticatedUser currentUser) {
        final List<Car> allCars = carService.getAllCars();
        final Map<Long, ReviewStats> reviewStatsByCarId = getReviewStatsByCarId(allCars);
        final List<Car> featuredCars = selectFeaturedCars(allCars, reviewStatsByCarId);
        final Car heroCar = featuredCars.isEmpty() ? getFallbackHeroCar(allCars) : featuredCars.get(0);
        final Review heroReview = heroCar == null
                ? null
                : reviewService.getTopRatedLatestReviewByCar(heroCar.getId()).orElse(null);

        final ModelAndView mav = new ModelAndView("landing.jsp");
        mav.addObject("featuredCars", featuredCars);
        mav.addObject("reviewStatsByCarId", reviewStatsByCarId);
        mav.addObject("favoritedCarIds", favoritedCarIdsById(featuredCars, currentUser));
        mav.addObject("heroCar", heroCar);
        mav.addObject("heroReview", heroReview);
        return mav;
    }

    @RequestMapping(value = "/cars", method = RequestMethod.GET)
    public String listCars(@ModelAttribute final CarSearchCriteria criteria,
                           @RequestParam(value = "createCar", required = false) final String createCar,
                           @RequestParam(value = "submitted", required = false) final String submitted,
                           @AuthenticationPrincipal final AuthenticatedUser currentUser,
                           final Model model) {
        populateCarsPageModel(model, criteria, currentUser);
        if ("true".equalsIgnoreCase(createCar)) {
            model.addAttribute("openCarModal", true);
            model.addAttribute("openCreateCarModal", true);
        }
        if ("true".equalsIgnoreCase(submitted)) {
            model.addAttribute("showSubmittedToast", true);
        }
        return "cars.jsp";
    }

    @RequestMapping(value = "/cars/content", method = RequestMethod.GET)
    public ModelAndView listCarsContent(@ModelAttribute final CarSearchCriteria criteria,
                                        @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        final CarCatalogData catalogData = resolveCatalogData(criteria);

        final ModelAndView mav = new ModelAndView("cars-content.jsp");
        mav.addObject("cars", catalogData.cars);
        mav.addObject("reviewStatsByCarId", catalogData.reviewStatsByCarId);
        mav.addObject("favoritedCarIds", favoritedCarIdsById(catalogData.cars, currentUser));
        addShowSpecFlags(mav, criteria);
        return mav;
    }

    @RequestMapping(value = "/cars/new", method = RequestMethod.GET)
    public ModelAndView newCarRequest() {
        return new ModelAndView("redirect:/cars?createCar=true");
    }

    @RequestMapping(value = "/cars", method = RequestMethod.POST, consumes = "multipart/form-data")
    public String createCar(@Valid @ModelAttribute("carForm") final CarForm carForm,
                            final BindingResult errors,
                            final Model model,
                            @AuthenticationPrincipal final AuthenticatedUser currentUser) {
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
            resolvedBrand = brandService.findByName(carForm.getBrand()).orElse(null);
            if (resolvedBrand == null) {
                errors.rejectValue("brand", "brand.invalid", "Marca no válida.");
            }
        }

        BodyType resolvedBodyType = null;
        if (!errors.hasFieldErrors("bodyType")) {
            resolvedBodyType = bodyTypeService.findByName(carForm.getBodyType()).orElse(null);
            if (resolvedBodyType == null) {
                errors.rejectValue("bodyType", "bodyType.invalid", "Tipo de carrocería no válido.");
            }
        }

        if (!errors.hasErrors() && resolvedBrand != null && resolvedBodyType != null) {
            final boolean duplicate = carService
                    .getCarsByBrandAndBodyType(resolvedBrand.getName(), resolvedBodyType.getName())
                    .stream()
                    .anyMatch(car -> car.getModel().equalsIgnoreCase(carForm.getModel()));
            if (duplicate) {
                errors.reject("car.duplicate",
                        "Ya existe un auto con esa marca, modelo y tipo de carrocería.");
            }
        }

        if (errors.hasErrors()) {
            populateCarsPageModel(model, new CarSearchCriteria(), currentUser);
            model.addAttribute("openCarModal", true);
            model.addAttribute("openCreateCarModal", true);
            return "cars.jsp";
        }

        final List<CarImagePayload> imagePayloads;
        try {
            imagePayloads = toImagePayloads(files);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read uploaded image.", e);
        }

        final CarRequest carRequest = carService.requestCarCreation(
                resolvedBrand.getId(),
                carForm.getModel(),
                resolvedBodyType.getId(),
                currentUser.getId(),
                currentUser.getEmail(),
                Optional.ofNullable(carForm.getDescription()).filter(value -> !value.isEmpty()),
                imagePayloads,
                ControllerUtils.normalizeSpecValue(carForm.getFuelType()),
                carForm.getHorsepower(),
                carForm.getAirbagCount(),
                ControllerUtils.normalizeSpecValue(carForm.getTransmission()),
                carForm.getFuelConsumption(),
                carForm.getMaxSpeedKmh()
        );
        emailService.sendNewCarRequestNotification(carRequest, resolvedBrand.getName(), resolvedBodyType.getName());

        return "redirect:/cars?submitted=true";
    }

    @RequestMapping(value = "/cars/{carId}/favorite", method = RequestMethod.POST)
    public Object updateFavorite(@PathVariable("carId") final long carId,
                                 @RequestParam("favorite") final boolean favorite,
                                 @RequestHeader(value = "X-Requested-With", required = false) final String requestedWith,
                                 @RequestHeader(value = "Referer", required = false) final String referer,
                                 @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            if (ControllerUtils.isAjaxRequest(requestedWith)) {
                return new ResponseEntity<String>("/login", HttpStatus.UNAUTHORIZED);
            }
            return new ModelAndView("redirect:/login");
        }
        if (carService.getCarById(carId).isEmpty()) {
            if (ControllerUtils.isAjaxRequest(requestedWith)) {
                return new ResponseEntity<String>("Auto no encontrado.", HttpStatus.NOT_FOUND);
            }
            return new ModelAndView("redirect:/cars");
        }

        carFavoriteService.setFavorite(currentUser.getId(), carId, favorite);
        final boolean favorited = carFavoriteService.isFavorited(currentUser.getId(), carId);
        if (ControllerUtils.isAjaxRequest(requestedWith)) {
            return new ResponseEntity<String>(Boolean.toString(favorited), HttpStatus.OK);
        }
        return new ModelAndView("redirect:" + safeRedirectPath(referer));
    }

    private void populateCarsPageModel(final Model model, final CarSearchCriteria criteria,
                                       final AuthenticatedUser currentUser) {
        final CarCatalogData catalogData = resolveCatalogData(criteria);

        model.addAttribute("cars", catalogData.cars);
        model.addAttribute("reviewStatsByCarId", catalogData.reviewStatsByCarId);
        model.addAttribute("favoritedCarIds", favoritedCarIdsById(catalogData.cars, currentUser));
        model.addAttribute("selectedBrand", criteria.getBrand());
        model.addAttribute("selectedBodyType", criteria.getBodyType());
        model.addAttribute("searchQuery", criteria.getQ());
        model.addAttribute("criteria", criteria);
        model.addAttribute("hasAdvancedFilters", criteria.hasAdvancedFilters());
        model.addAttribute("showHp", criteria.getHorsepowerMin() != null || criteria.getHorsepowerMax() != null);
        model.addAttribute("showSpeed", criteria.getMaxSpeedMin() != null);
        model.addAttribute("showConsumption", criteria.getFuelConsumptionMax() != null);
        model.addAttribute("showAirbags", criteria.getAirbagMin() != null);
        model.addAttribute("showTransmission", criteria.getTransmission() != null);
        model.addAttribute("showFuelType", criteria.getFuelType() != null);
    }

    private void addShowSpecFlags(final ModelAndView mav, final CarSearchCriteria criteria) {
        mav.addObject("showHp", criteria.getHorsepowerMin() != null || criteria.getHorsepowerMax() != null);
        mav.addObject("showSpeed", criteria.getMaxSpeedMin() != null);
        mav.addObject("showConsumption", criteria.getFuelConsumptionMax() != null);
        mav.addObject("showAirbags", criteria.getAirbagMin() != null);
        mav.addObject("showTransmission", criteria.getTransmission() != null);
        mav.addObject("showFuelType", criteria.getFuelType() != null);
    }

    private Map<Long, Boolean> favoritedCarIdsById(final List<Car> cars, final AuthenticatedUser currentUser) {
        if (currentUser == null || cars.isEmpty()) {
            return Collections.emptyMap();
        }
        return carFavoriteService.getFavoritedCarIds(
                        currentUser.getId(),
                        cars.stream().map(Car::getId).collect(Collectors.toList())
                )
                .stream()
                .collect(Collectors.toMap(Function.identity(), ignored -> Boolean.TRUE));
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
            if ("/".equals(path) || "/cars".equals(path) || "/reviews".equals(path) || "/profile".equals(path)
                    || path.matches("/profiles/\\d+")) {
                final String query = uri.getRawQuery();
                return path + (query == null ? "" : "?" + query);
            }
        } catch (final IllegalArgumentException ignored) {
            return "/cars";
        }
        return "/cars";
    }

    @RequestMapping(value = "/cars/{carId}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getCarImage(
            @PathVariable("carId") final long carId,
            @RequestHeader(value = "If-None-Match", required = false) final String ifNoneMatch) {
        return getCarImageResponse(carId, ifNoneMatch);
    }

    @RequestMapping(value = "/car-image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getCarImageByQueryParam(
            @RequestParam("carId") final long carId,
            @RequestHeader(value = "If-None-Match", required = false) final String ifNoneMatch) {
        return getCarImageResponse(carId, ifNoneMatch);
    }

    private ResponseEntity<byte[]> getCarImageResponse(final long carId, final String ifNoneMatch) {
        final CarImage carImage = carService.getCarImageByCarId(carId).orElse(null);
        return getCarImageResponse(carImage, ifNoneMatch);
    }

    @RequestMapping(value = "/cars/{carId}/images/{imageId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getCarImageById(
            @PathVariable("carId") final long carId,
            @PathVariable("imageId") final long imageId,
            @RequestHeader(value = "If-None-Match", required = false) final String ifNoneMatch) {
        final CarImage carImage = carService.getCarImageById(carId, imageId).orElse(null);
        return getCarImageResponse(carImage, ifNoneMatch);
    }

    private ResponseEntity<byte[]> getCarImageResponse(final CarImage carImage, final String ifNoneMatch) {
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
                    .lastModified(Timestamp.valueOf(carImage.getUpdatedAt()).getTime())
                    .build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(carImage.getContentType()))
                .contentLength(carImage.getImageData().length)
                .cacheControl(cacheControl)
                .eTag(eTag)
                .lastModified(Timestamp.valueOf(carImage.getUpdatedAt()).getTime())
                .body(carImage.getImageData());
    }

    @RequestMapping(value = "/cars/{carId}/image", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<?> uploadCarImage(
            @PathVariable("carId") final long carId,
            @RequestParam("file") final List<MultipartFile> files) {
        return uploadCarImageResponse(carId, files);
    }

    @RequestMapping(value = "/car-image", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<?> uploadCarImageByQueryParam(
            @RequestParam("carId") final long carId,
            @RequestParam("file") final List<MultipartFile> files) {
        return uploadCarImageResponse(carId, files);
    }

    private ResponseEntity<?> uploadCarImageResponse(final long carId, final List<MultipartFile> files) {
        if (carService.getCarById(carId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Auto no encontrado.");
        }
        final List<MultipartFile> selectedFiles = selectedImageFiles(files);
        final String imageValidationError = validateUploadedImages(selectedFiles, true);
        if (imageValidationError != null) {
            return ResponseEntity.badRequest().body(imageValidationError);
        }

        try {
            carService.saveCarImages(carId, toImagePayloads(selectedFiles));
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read uploaded image.", e);
        }

        return ResponseEntity.noContent().build();
    }

    private String validateUploadedImages(final List<MultipartFile> files, final boolean required) {
        if (files.isEmpty()) {
            return required ? "La imagen es obligatoria." : null;
        }
        if (files.size() > MAX_IMAGE_COUNT) {
            return "Podés cargar hasta " + MAX_IMAGE_COUNT + " imágenes.";
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
        return ControllerUtils.validateUploadedImage(file, required);
    }

    private String resolveImageContentType(final MultipartFile file) {
        return ControllerUtils.normalizeContentType(file == null ? null : file.getContentType());
    }

    private List<MultipartFile> selectedImageFiles(final List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .collect(Collectors.toList());
    }

    private List<CarImagePayload> toImagePayloads(final List<MultipartFile> files) throws IOException {
        final List<CarImagePayload> payloads = new ArrayList<>();
        for (final MultipartFile file : files) {
            payloads.add(new CarImagePayload(resolveImageContentType(file), file.getBytes()));
        }
        return payloads;
    }

    private CarCatalogData resolveCatalogData(final CarSearchCriteria criteria) {
        if (!criteria.isValid()) {
            return new CarCatalogData(Collections.emptyList(), Collections.emptyMap());
        }

        final List<Car> cars = carService.searchCars(criteria);

        final Map<Long, ReviewStats> reviewStatsByCarId;
        if (cars.isEmpty()) {
            reviewStatsByCarId = Collections.emptyMap();
        } else {
            reviewStatsByCarId = reviewService.getReviewStatsByCarIds(
                            cars.stream().map(Car::getId).collect(Collectors.toList()))
                    .stream()
                    .collect(Collectors.toMap(ReviewStats::getCarId, Function.identity()));
        }
        return new CarCatalogData(cars, reviewStatsByCarId);
    }

    private Map<Long, ReviewStats> getReviewStatsByCarId(final List<Car> cars) {
        if (cars.isEmpty()) {
            return Collections.emptyMap();
        }
        return reviewService.getReviewStatsByCarIds(cars.stream().map(Car::getId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(ReviewStats::getCarId, Function.identity()));
    }

    private List<Car> selectFeaturedCars(final List<Car> allCars, final Map<Long, ReviewStats> reviewStatsByCarId) {
        if (allCars.isEmpty()) {
            return Collections.emptyList();
        }

        final Comparator<Car> featuredComparator = Comparator
                .comparing((Car car) -> getAverageRating(reviewStatsByCarId.get(car.getId())), Comparator.reverseOrder())
                .thenComparing((Car car) -> getReviewCount(reviewStatsByCarId.get(car.getId())), Comparator.reverseOrder())
                .thenComparingLong(Car::getId);

        final List<Car> featuredCars = allCars.stream()
                .filter(car -> getReviewCount(reviewStatsByCarId.get(car.getId())) > 0)
                .sorted(featuredComparator)
                .limit(FEATURED_REVIEW_COUNT)
                .collect(Collectors.toCollection(ArrayList::new));

        if (featuredCars.size() == FEATURED_REVIEW_COUNT) {
            return featuredCars;
        }

        final Set<Long> selectedIds = featuredCars.stream()
                .map(Car::getId)
                .collect(Collectors.toCollection(HashSet::new));

        allCars.stream()
                .filter(car -> !selectedIds.contains(car.getId()))
                .sorted(Comparator.comparing(Car::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparingLong(Car::getId))
                .limit(FEATURED_REVIEW_COUNT - featuredCars.size())
                .forEach(featuredCars::add);

        return featuredCars;
    }

    private Car getFallbackHeroCar(final List<Car> allCars) {
        return allCars.isEmpty() ? null : allCars.get(0);
    }

    private BigDecimal getAverageRating(final ReviewStats stats) {
        return stats == null || stats.getAverageRating() == null ? BigDecimal.ZERO : stats.getAverageRating();
    }

    private long getReviewCount(final ReviewStats stats) {
        return stats == null ? 0 : stats.getReviewCount();
    }

    private void rejectInvalidSpecFields(final BindingResult errors, final CarForm carForm) {
        if (!errors.hasFieldErrors("fuelType")
                && !CarSearchCriteria.ALLOWED_FUEL_TYPES.contains(
                        ControllerUtils.normalizeSpecValue(carForm.getFuelType()))) {
            errors.rejectValue("fuelType", "fuelType.invalid", "Tipo de motorización no válido.");
        }
        if (!errors.hasFieldErrors("transmission")
                && !CarSearchCriteria.ALLOWED_TRANSMISSIONS.contains(
                        ControllerUtils.normalizeSpecValue(carForm.getTransmission()))) {
            errors.rejectValue("transmission", "transmission.invalid", "Transmisión no válida.");
        }
    }

    private static String buildImageEtag(final CarImage carImage) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(carImage.getContentType().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            digest.update(carImage.getImageData());
            digest.update(carImage.getUpdatedAt().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return "\"" + HexFormat.of().formatHex(digest.digest()) + "\"";
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }

    private static final class CarCatalogData {
        private final List<Car> cars;
        private final Map<Long, ReviewStats> reviewStatsByCarId;

        private CarCatalogData(final List<Car> cars, final Map<Long, ReviewStats> reviewStatsByCarId) {
            this.cars = cars;
            this.reviewStatsByCarId = reviewStatsByCarId;
        }
    }
}
