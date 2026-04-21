package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.services.CarRequestService;
import ar.edu.itba.paw.webapp.form.CarForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CarRequestService carRequestService;
    private final BrandDao brandDao;
    private final BodyTypeDao bodyTypeDao;

    @Autowired
    public AdminController(final CarRequestService carRequestService, final BrandDao brandDao,
                           final BodyTypeDao bodyTypeDao) {
        this.carRequestService = carRequestService;
        this.brandDao = brandDao;
        this.bodyTypeDao = bodyTypeDao;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView admin() {
        final List<Brand> brands = brandDao.findAll();
        final List<BodyType> bodyTypes = bodyTypeDao.findAll();
        final Map<Long, Brand> brandsById = brands.stream()
                .collect(Collectors.toMap(Brand::getId, Function.identity()));
        final Map<Long, BodyType> bodyTypesById = bodyTypes.stream()
                .collect(Collectors.toMap(BodyType::getId, Function.identity()));

        final List<AdminCarRequestCard> pendingRequests = carRequestService
                .getCarRequestsByStatus(CarRequestService.STATUS_PENDING)
                .stream()
                .map(request -> toCard(request, brandsById, bodyTypesById))
                .toList();

        final ModelAndView mav = new ModelAndView("admin.jsp");
        mav.addObject("pendingRequests", pendingRequests);
        mav.addObject("brands", brands);
        mav.addObject("bodyTypes", bodyTypes);
        mav.addObject("carForm", new CarForm());
        return mav;
    }

    @RequestMapping(value = "/requests/{requestId}/accept", method = RequestMethod.POST)
    public ModelAndView acceptRequest(@PathVariable("requestId") final long requestId) {
        carRequestService.approvePendingRequest(requestId);
        return new ModelAndView("redirect:/admin");
    }

    @RequestMapping(value = "/requests/{requestId}/reject", method = RequestMethod.POST)
    public ModelAndView rejectRequest(@PathVariable("requestId") final long requestId) {
        carRequestService.rejectPendingRequest(requestId);
        return new ModelAndView("redirect:/admin");
    }

    @RequestMapping(value = "/requests/{requestId}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getRequestImage(
            @PathVariable("requestId") final long requestId,
            @RequestHeader(value = "If-None-Match", required = false) final String ifNoneMatch) {
        final CarRequest request = carRequestService.getCarRequestById(requestId).orElse(null);
        if (request == null || request.getImageData() == null || request.getImageContentType() == null) {
            return ResponseEntity.notFound().build();
        }

        final String eTag = "\"" + request.getId() + "-" + request.getImageData().length + "\"";
        final CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.HOURS)
                .cachePrivate()
                .mustRevalidate();

        if (eTag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(eTag)
                    .cacheControl(cacheControl)
                    .lastModified(Timestamp.valueOf(request.getCreatedAt()).getTime())
                    .build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(request.getImageContentType()))
                .contentLength(request.getImageData().length)
                .eTag(eTag)
                .cacheControl(cacheControl)
                .lastModified(Timestamp.valueOf(request.getCreatedAt()).getTime())
                .body(request.getImageData());
    }

    private AdminCarRequestCard toCard(final CarRequest request, final Map<Long, Brand> brandsById,
                                       final Map<Long, BodyType> bodyTypesById) {
        final String brandName = brandsById.getOrDefault(request.getBrandId(), new Brand()).getName();
        final String bodyTypeName = bodyTypesById.getOrDefault(request.getBodyTypeId(), new BodyType()).getName();
        return new AdminCarRequestCard(
                request.getId(),
                valueOrFallback(brandName, "Marca pendiente"),
                request.getModel(),
                valueOrFallback(bodyTypeName, "Carrocería pendiente"),
                request.getDescription(),
                submitterLabel(request),
                request.getImageData() != null,
                request.getImageData() == null ? null : "/admin/requests/" + request.getId() + "/image",
                request.getFuelType(),
                request.getHorsepower(),
                request.getAirbagCount(),
                request.getTransmission(),
                request.getFuelConsumption(),
                request.getMaxSpeedKmh()
        );
    }

    private String submitterLabel(final CarRequest request) {
        if (request.getSubmitterEmail() != null && !request.getSubmitterEmail().isBlank()) {
            return request.getSubmitterEmail();
        }
        if (request.getSubmittedByUserId() != null) {
            return "Usuario #" + request.getSubmittedByUserId();
        }
        return "Usuario sin identificar";
    }

    private String valueOrFallback(final String value, final String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public static final class AdminCarRequestCard {
        private final long id;
        private final String brandName;
        private final String model;
        private final String bodyTypeName;
        private final String description;
        private final String submitter;
        private final boolean hasImage;
        private final String imageUrl;
        private final String fuelType;
        private final Integer horsepower;
        private final Integer airbagCount;
        private final String transmission;
        private final BigDecimal fuelConsumption;
        private final Integer maxSpeedKmh;

        private AdminCarRequestCard(final long id, final String brandName, final String model,
                                    final String bodyTypeName, final String description, final String submitter,
                                    final boolean hasImage, final String imageUrl, final String fuelType,
                                    final Integer horsepower, final Integer airbagCount, final String transmission,
                                    final BigDecimal fuelConsumption, final Integer maxSpeedKmh) {
            this.id = id;
            this.brandName = brandName;
            this.model = model;
            this.bodyTypeName = bodyTypeName;
            this.description = description;
            this.submitter = submitter;
            this.hasImage = hasImage;
            this.imageUrl = imageUrl;
            this.fuelType = fuelType;
            this.horsepower = horsepower;
            this.airbagCount = airbagCount;
            this.transmission = transmission;
            this.fuelConsumption = fuelConsumption;
            this.maxSpeedKmh = maxSpeedKmh;
        }

        public long getId() {
            return id;
        }

        public String getBrandName() {
            return brandName;
        }

        public String getModel() {
            return model;
        }

        public String getBodyTypeName() {
            return bodyTypeName;
        }

        public String getDescription() {
            return description;
        }

        public String getSubmitter() {
            return submitter;
        }

        public boolean isHasImage() {
            return hasImage;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getFuelType() {
            return fuelType;
        }

        public Integer getHorsepower() {
            return horsepower;
        }

        public Integer getAirbagCount() {
            return airbagCount;
        }

        public String getTransmission() {
            return transmission;
        }

        public BigDecimal getFuelConsumption() {
            return fuelConsumption;
        }

        public Integer getMaxSpeedKmh() {
            return maxSpeedKmh;
        }
    }
}
