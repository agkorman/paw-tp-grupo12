package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.services.CarRequestService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.form.CarForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp"
    );

    private final CarRequestService carRequestService;
    private final BrandDao brandDao;
    private final BodyTypeDao bodyTypeDao;
    private final UserService userService;

    @Autowired
    public AdminController(final CarRequestService carRequestService, final BrandDao brandDao,
                           final BodyTypeDao bodyTypeDao, final UserService userService) {
        this.carRequestService = carRequestService;
        this.brandDao = brandDao;
        this.bodyTypeDao = bodyTypeDao;
        this.userService = userService;
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

    @RequestMapping(value = "/requests/{requestId}/accept", method = RequestMethod.POST,
            consumes = "multipart/form-data")
    public ModelAndView acceptRequest(@PathVariable("requestId") final long requestId,
                                      @Valid @ModelAttribute("carForm") final CarForm carForm,
                                      final BindingResult errors) {
        if (errors.hasErrors()) {
            return new ModelAndView("redirect:/admin");
        }

        final Brand resolvedBrand = brandDao.findByName(carForm.getBrand()).orElse(null);
        final BodyType resolvedBodyType = bodyTypeDao.findByName(carForm.getBodyType()).orElse(null);
        if (resolvedBrand == null || resolvedBodyType == null) {
            return new ModelAndView("redirect:/admin");
        }

        final MultipartFile file = carForm.getFile();
        final String imageError = validateUploadedImage(file, false);
        if (imageError != null) {
            return new ModelAndView("redirect:/admin");
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

        carRequestService.approvePendingRequest(
                requestId,
                resolvedBrand.getId(),
                carForm.getModel(),
                resolvedBodyType.getId(),
                carForm.getDescription(),
                imageContentType,
                imageData
        );
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
                request.getImageData() == null ? null : "/admin/requests/" + request.getId() + "/image"
        );
    }

    private String submitterLabel(final CarRequest request) {
        if (request.getSubmitterEmail() != null && !request.getSubmitterEmail().isBlank()) {
            return request.getSubmitterEmail();
        }
        if (request.getSubmittedByUserId() != null) {
            return userService.getUserById(request.getSubmittedByUserId())
                    .map(User::getEmail)
                    .filter(email -> !email.isBlank())
                    .orElse("Usuario #" + request.getSubmittedByUserId());
        }
        return "Usuario sin identificar";
    }

    private String validateUploadedImage(final MultipartFile file, final boolean required) {
        if (file == null || file.isEmpty()) {
            return required ? "La imagen es obligatoria." : null;
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            return "La imagen no debe superar los 10 MB.";
        }
        final String contentType = resolveImageContentType(file);
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            return "Tipo de imagen no soportado. Usá JPEG, PNG o WEBP.";
        }
        return null;
    }

    private String resolveImageContentType(final MultipartFile file) {
        return normalizeContentType(file == null ? null : file.getContentType());
    }

    private static String normalizeContentType(final String contentType) {
        if (contentType == null) {
            return null;
        }
        final String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
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

        private AdminCarRequestCard(final long id, final String brandName, final String model,
                                    final String bodyTypeName, final String description, final String submitter,
                                    final boolean hasImage, final String imageUrl) {
            this.id = id;
            this.brandName = brandName;
            this.model = model;
            this.bodyTypeName = bodyTypeName;
            this.description = description;
            this.submitter = submitter;
            this.hasImage = hasImage;
            this.imageUrl = imageUrl;
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
    }
}
