package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.EmailService;
import ar.edu.itba.paw.services.ReviewService;
import org.springframework.mail.MailException;
import java.util.logging.Logger;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class CarController {

    private static final Logger LOGGER = Logger.getLogger(CarController.class.getName());
    private static final int FEATURED_REVIEW_COUNT = 3;
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp"
    );

    private final CarService carService;
    private final BrandDao brandDao;
    private final BodyTypeDao bodyTypeDao;
    private final ReviewService reviewService;
    private final EmailService emailService;

    @Autowired
    public CarController(final CarService carService, final BrandDao brandDao, final BodyTypeDao bodyTypeDao,
                         final ReviewService reviewService, final EmailService emailService) {
        this.carService = carService;
        this.brandDao = brandDao;
        this.bodyTypeDao = bodyTypeDao;
        this.reviewService = reviewService;
        this.emailService = emailService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView home() {
        return landingPage(null);
    }

    private ModelAndView landingPage(final String error) {
        final List<Car> allCars = carService.getAllCars();
        final Map<Long, ReviewStats> reviewStatsByCarId = getReviewStatsByCarId(allCars);
        final List<Car> featuredCars = selectFeaturedCars(allCars, reviewStatsByCarId);

        final ModelAndView mav = new ModelAndView("landing.jsp");
        mav.addObject("featuredCars", featuredCars);
        mav.addObject("reviewStatsByCarId", reviewStatsByCarId);
        mav.addObject("heroCar", featuredCars.isEmpty() ? getFallbackHeroCar(allCars) : featuredCars.get(0));
        mav.addObject("brands", brandDao.findAll());
        mav.addObject("bodyTypes", bodyTypeDao.findAll());
        if (error != null) {
            mav.addObject("carFormError", error);
        }
        return mav;
    }

    @RequestMapping(value = "/cars", method = RequestMethod.GET)
    public ModelAndView listCars(
            @RequestParam(value = "q", required = false) final String q,
            @RequestParam(value = "brand", required = false) final String brand,
            @RequestParam(value = "bodyType", required = false) final String bodyType) {
        final String searchQuery = blankToNull(q);
        final CarCatalogData catalogData = resolveCatalogData(searchQuery, brand, bodyType);

        final ModelAndView mav = new ModelAndView("cars.jsp");
        mav.addObject("cars", catalogData.cars);
        mav.addObject("reviewStatsByCarId", catalogData.reviewStatsByCarId);
        mav.addObject("brands", brandDao.findAll());
        mav.addObject("bodyTypes", bodyTypeDao.findAll());
        mav.addObject("selectedBrand", catalogData.brandFilter);
        mav.addObject("selectedBodyType", catalogData.bodyTypeFilter);
        mav.addObject("searchQuery", searchQuery);
        return mav;
    }

    @RequestMapping(value = "/cars/content", method = RequestMethod.GET)
    public ModelAndView listCarsContent(
            @RequestParam(value = "q", required = false) final String q,
            @RequestParam(value = "brand", required = false) final String brand,
            @RequestParam(value = "bodyType", required = false) final String bodyType) {
        final CarCatalogData catalogData = resolveCatalogData(blankToNull(q), brand, bodyType);

        final ModelAndView mav = new ModelAndView("cars-content.jsp");
        mav.addObject("cars", catalogData.cars);
        mav.addObject("reviewStatsByCarId", catalogData.reviewStatsByCarId);
        return mav;
    }

    @RequestMapping(value = "/cars", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ModelAndView createCar(@RequestParam("brand") final String brand,
                                  @RequestParam("bodyType") final String bodyType,
                                  @RequestParam("model") final String model,
                                  @RequestParam(value = "description", required = false) final String description,
                                  @RequestParam(value = "file", required = false) final MultipartFile file) {

        final String trimmedBrand = brand.trim();
        final String trimmedBodyType = bodyType.trim();
        final String trimmedModel = model.trim();
        final String trimmedDescription = description == null ? null : description.trim();

        if (trimmedBrand.isEmpty() || trimmedBodyType.isEmpty()) {
            return landingPage("Marca y tipo de carrocería son obligatorios.");
        }
        if (trimmedModel.isEmpty() || trimmedModel.length() > 120) {
            return landingPage("El modelo es obligatorio y debe tener como máximo 120 caracteres.");
        }
        if (trimmedDescription != null && trimmedDescription.length() > 1500) {
            return landingPage("La descripción debe tener como máximo 1500 caracteres.");
        }

        final String imageValidationError = validateUploadedImage(file, false);
        if (imageValidationError != null) {
            return landingPage(imageValidationError);
        }

        final Brand resolvedBrand = brandDao.findByName(trimmedBrand).orElse(null);
        final BodyType resolvedBodyType = bodyTypeDao.findByName(trimmedBodyType).orElse(null);
        if (resolvedBrand == null || resolvedBodyType == null) {
            return landingPage("Marca o tipo de carrocería no válidos.");
        }

        final Optional<String> imageContentType;
        final Optional<byte[]> imageData;
        if (file == null || file.isEmpty()) {
            imageContentType = Optional.empty();
            imageData = Optional.empty();
        } else {
            imageContentType = Optional.ofNullable(resolveImageContentType(file));
            try {
                imageData = Optional.of(file.getBytes());
            } catch (final IOException e) {
                throw new IllegalStateException("Failed to read uploaded image.", e);
            }
        }

        final Car createdCar = carService.createCar(
                resolvedBrand.getId(),
                trimmedModel,
                resolvedBodyType.getId(),
                Optional.ofNullable(trimmedDescription).filter(value -> !value.isEmpty()),
                imageContentType,
                imageData
        );

        try {
            emailService.sendCarCreatedNotification(createdCar);
        } catch (final MailException e) {
            LOGGER.warning("Failed to send car creation notification for car " + createdCar.getId() + ": " + e.getMessage());
        }

        return new ModelAndView("redirect:/cars");
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
            @RequestParam("file") final MultipartFile file) {
        return uploadCarImageResponse(carId, file);
    }

    @RequestMapping(value = "/car-image", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<?> uploadCarImageByQueryParam(
            @RequestParam("carId") final long carId,
            @RequestParam("file") final MultipartFile file) {
        return uploadCarImageResponse(carId, file);
    }

    private ResponseEntity<?> uploadCarImageResponse(final long carId, final MultipartFile file) {
        if (carService.getCarById(carId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Car not found.");
        }
        final String imageValidationError = validateUploadedImage(file, true);
        if (imageValidationError != null) {
            return ResponseEntity.badRequest().body(imageValidationError);
        }

        final String contentType = resolveImageContentType(file);
        try {
            carService.saveCarImage(carId, contentType, file.getBytes());
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read uploaded image.", e);
        }

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceeded(final MaxUploadSizeExceededException ignored) {
        return ResponseEntity.badRequest().body("La imagen no debe superar los 5 MB.");
    }

    private String validateUploadedImage(final MultipartFile file, final boolean required) {
        if (file == null || file.isEmpty()) {
            return required ? "La imagen es obligatoria." : null;
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            return "La imagen no debe superar los 5 MB.";
        }
        final String contentType = resolveImageContentType(file);
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            return "Tipo de imagen no soportado. Use JPEG, PNG o WEBP.";
        }
        return null;
    }

    private String resolveImageContentType(final MultipartFile file) {
        return normalizeContentType(file == null ? null : file.getContentType());
    }

    private CarCatalogData resolveCatalogData(final String searchQuery, final String brand, final String bodyType) {
        final String brandFilter = blankToNull(brand);
        final String bodyTypeFilter = blankToNull(bodyType);

        final List<Car> cars;
        if (searchQuery != null) {
            cars = filterCars(carService.searchCars(searchQuery), brandFilter, bodyTypeFilter);
        } else if (brandFilter != null && bodyTypeFilter != null) {
            cars = carService.getCarsByBrandAndBodyType(brandFilter, bodyTypeFilter);
        } else if (brandFilter != null) {
            cars = carService.getCarsByBrand(brandFilter);
        } else if (bodyTypeFilter != null) {
            cars = carService.getCarsByBodyType(bodyTypeFilter);
        } else {
            cars = carService.getAllCars();
        }

        final Map<Long, ReviewStats> reviewStatsByCarId;
        if (cars.isEmpty()) {
            reviewStatsByCarId = Collections.emptyMap();
        } else {
            reviewStatsByCarId = reviewService.getReviewStatsByCarIds(
                            cars.stream().map(Car::getId).collect(Collectors.toList()))
                    .stream()
                    .collect(Collectors.toMap(ReviewStats::getCarId, Function.identity()));
        }
        return new CarCatalogData(cars, reviewStatsByCarId, brandFilter, bodyTypeFilter);
    }

    private Map<Long, ReviewStats> getReviewStatsByCarId(final List<Car> cars) {
        if (cars.isEmpty()) {
            return Collections.emptyMap();
        }
        return reviewService.getReviewStatsByCarIds(cars.stream().map(Car::getId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(ReviewStats::getCarId, Function.identity()));
    }

    private List<Car> filterCars(final List<Car> cars, final String brandFilter, final String bodyTypeFilter) {
        return cars.stream()
                .filter(car -> brandFilter == null || brandFilter.equalsIgnoreCase(car.getBrandName()))
                .filter(car -> bodyTypeFilter == null || bodyTypeFilter.equalsIgnoreCase(car.getBodyType()))
                .collect(Collectors.toList());
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

    private static String blankToNull(final String s) {
        if (s == null) {
            return null;
        }
        final String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String normalizeContentType(final String contentType) {
        if (contentType == null) {
            return null;
        }
        final String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
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
        private final String brandFilter;
        private final String bodyTypeFilter;

        private CarCatalogData(final List<Car> cars, final Map<Long, ReviewStats> reviewStatsByCarId,
                               final String brandFilter, final String bodyTypeFilter) {
            this.cars = cars;
            this.reviewStatsByCarId = reviewStatsByCarId;
            this.brandFilter = brandFilter;
            this.bodyTypeFilter = bodyTypeFilter;
        }
    }
}
