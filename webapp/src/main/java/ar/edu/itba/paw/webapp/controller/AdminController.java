package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarRequestImage;
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
        if (request == null) {
            return ResponseEntity.notFound().build();
        }
        final List<CarRequestImage> requestImages = carRequestService.getCarRequestImages(requestId);
        if (!requestImages.isEmpty()) {
            final CarRequestImage coverImage = carRequestService
                    .getCarRequestImageById(requestId, requestImages.get(0).getImageId())
                    .orElse(null);
            return getRequestImageResponse(coverImage, ifNoneMatch);
        }
        if (request.getImageData() == null || request.getImageContentType() == null) {
            return ResponseEntity.notFound().build();
        }
        return getLegacyRequestImageResponse(request, ifNoneMatch);
    }

    @RequestMapping(value = "/requests/{requestId}/images/{imageId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getRequestImageById(
            @PathVariable("requestId") final long requestId,
            @PathVariable("imageId") final long imageId,
            @RequestHeader(value = "If-None-Match", required = false) final String ifNoneMatch) {
        final CarRequestImage requestImage = carRequestService.getCarRequestImageById(requestId, imageId).orElse(null);
        return getRequestImageResponse(requestImage, ifNoneMatch);
    }

    private ResponseEntity<byte[]> getLegacyRequestImageResponse(final CarRequest request, final String ifNoneMatch) {
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

    private ResponseEntity<byte[]> getRequestImageResponse(final CarRequestImage requestImage,
                                                           final String ifNoneMatch) {
        if (requestImage == null) {
            return ResponseEntity.notFound().build();
        }

        final String eTag = "\"" + requestImage.getImageId() + "-" + requestImage.getImageData().length
                + "-" + requestImage.getUpdatedAt() + "\"";
        final CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.HOURS)
                .cachePrivate()
                .mustRevalidate();

        if (eTag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(eTag)
                    .cacheControl(cacheControl)
                    .lastModified(Timestamp.valueOf(requestImage.getUpdatedAt()).getTime())
                    .build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(requestImage.getContentType()))
                .contentLength(requestImage.getImageData().length)
                .eTag(eTag)
                .cacheControl(cacheControl)
                .lastModified(Timestamp.valueOf(requestImage.getUpdatedAt()).getTime())
                .body(requestImage.getImageData());
    }

    private AdminCarRequestCard toCard(final CarRequest request, final Map<Long, Brand> brandsById,
                                       final Map<Long, BodyType> bodyTypesById) {
        final String brandName = brandsById.getOrDefault(request.getBrandId(), new Brand()).getName();
        final String bodyTypeName = bodyTypesById.getOrDefault(request.getBodyTypeId(), new BodyType()).getName();
        final List<String> imageUrls = buildRequestImageUrls(request);
        return new AdminCarRequestCard(
                request.getId(),
                valueOrFallback(brandName, "Marca pendiente"),
                request.getModel(),
                valueOrFallback(bodyTypeName, "Carrocería pendiente"),
                request.getDescription(),
                submitterLabel(request),
                !imageUrls.isEmpty(),
                imageUrls.isEmpty() ? null : imageUrls.get(0),
                String.join("|", imageUrls)
        );
    }

    private List<String> buildRequestImageUrls(final CarRequest request) {
        final List<CarRequestImage> requestImages = carRequestService.getCarRequestImages(request.getId());
        if (!requestImages.isEmpty()) {
            return requestImages.stream()
                    .map(image -> "/admin/requests/" + request.getId() + "/images/" + image.getImageId())
                    .collect(Collectors.toList());
        }
        if (request.getImageData() == null) {
            return List.of();
        }
        return List.of("/admin/requests/" + request.getId() + "/image");
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
        private final String imageUrls;

        private AdminCarRequestCard(final long id, final String brandName, final String model,
                                    final String bodyTypeName, final String description, final String submitter,
                                    final boolean hasImage, final String imageUrl, final String imageUrls) {
            this.id = id;
            this.brandName = brandName;
            this.model = model;
            this.bodyTypeName = bodyTypeName;
            this.description = description;
            this.submitter = submitter;
            this.hasImage = hasImage;
            this.imageUrl = imageUrl;
            this.imageUrls = imageUrls;
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

        public String getImageUrls() {
            return imageUrls;
        }
    }
}
