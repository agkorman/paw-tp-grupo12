package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
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

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class CarController {

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

    @Autowired
    public CarController(final CarService carService, final BrandDao brandDao, final BodyTypeDao bodyTypeDao,
                         final ReviewService reviewService) {
        this.carService = carService;
        this.brandDao = brandDao;
        this.bodyTypeDao = bodyTypeDao;
        this.reviewService = reviewService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView home() {
        return new ModelAndView("redirect:/cars");
    }

    @RequestMapping(value = "/cars", method = RequestMethod.GET)
    public ModelAndView listCars(
            @RequestParam(value = "brand", required = false) final String brand,
            @RequestParam(value = "bodyType", required = false) final String bodyType) {
        final CarCatalogData catalogData = resolveCatalogData(brand, bodyType);

        final ModelAndView mav = new ModelAndView("cars.jsp");
        mav.addObject("cars", catalogData.cars);
        mav.addObject("reviewStatsByCarId", catalogData.reviewStatsByCarId);
        mav.addObject("brands", brandDao.findAll());
        mav.addObject("bodyTypes", bodyTypeDao.findAll());
        mav.addObject("selectedBrand", catalogData.brandFilter);
        mav.addObject("selectedBodyType", catalogData.bodyTypeFilter);
        return mav;
    }

    @RequestMapping(value = "/cars/content", method = RequestMethod.GET)
    public ModelAndView listCarsContent(
            @RequestParam(value = "brand", required = false) final String brand,
            @RequestParam(value = "bodyType", required = false) final String bodyType) {
        final CarCatalogData catalogData = resolveCatalogData(brand, bodyType);

        final ModelAndView mav = new ModelAndView("cars-content.jsp");
        mav.addObject("cars", catalogData.cars);
        mav.addObject("reviewStatsByCarId", catalogData.reviewStatsByCarId);
        return mav;
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
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Image file is required.");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            return ResponseEntity.badRequest().body("Image exceeds the 5 MB limit.");
        }

        final String contentType = normalizeContentType(file.getContentType());
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest().body("Unsupported image type. Use JPEG, PNG, or WEBP.");
        }

        try {
            carService.saveCarImage(carId, contentType, file.getBytes());
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read uploaded image.", e);
        }

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceeded(final MaxUploadSizeExceededException ignored) {
        return ResponseEntity.badRequest().body("Image exceeds the 5 MB limit.");
    }

    private CarCatalogData resolveCatalogData(final String brand, final String bodyType) {
        final String brandFilter = blankToNull(brand);
        final String bodyTypeFilter = blankToNull(bodyType);

        final List<Car> cars;
        if (brandFilter != null && bodyTypeFilter != null) {
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
