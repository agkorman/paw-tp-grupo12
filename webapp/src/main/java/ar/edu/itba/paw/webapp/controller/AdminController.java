package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.AdminRequest;
import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.BodyTypeRequest;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandRequest;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.CarRequestImage;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.AdminRequestService;
import ar.edu.itba.paw.services.BodyTypeRequestService;
import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.services.BrandRequestService;
import ar.edu.itba.paw.services.BrandService;
import ar.edu.itba.paw.services.CarRequestService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.EmailService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.WeeklyDigestService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final String TAB_CARS = "cars";
    private static final String TAB_BRANDS = "brands";
    private static final String TAB_BODY_TYPES = "body-types";
    private static final String TAB_MODERATORS = "moderators";
    private static final int MAX_IMAGE_COUNT = 5;

    private final CarRequestService carRequestService;
    private final CarService carService;
    private final BrandService brandService;
    private final BodyTypeService bodyTypeService;
    private final BrandRequestService brandRequestService;
    private final BodyTypeRequestService bodyTypeRequestService;
    private final AdminRequestService adminRequestService;
    private final UserService userService;
    private final EmailService emailService;
    private final WeeklyDigestService weeklyDigestService;

    @Autowired
    public AdminController(final CarRequestService carRequestService, final CarService carService,
                           final BrandService brandService,
                           final BodyTypeService bodyTypeService,
                           final BrandRequestService brandRequestService,
                           final BodyTypeRequestService bodyTypeRequestService,
                           final AdminRequestService adminRequestService,
                           final UserService userService,
                           final EmailService emailService,
                           final WeeklyDigestService weeklyDigestService) {
        this.carRequestService = carRequestService;
        this.carService = carService;
        this.brandService = brandService;
        this.bodyTypeService = bodyTypeService;
        this.brandRequestService = brandRequestService;
        this.bodyTypeRequestService = bodyTypeRequestService;
        this.adminRequestService = adminRequestService;
        this.userService = userService;
        this.emailService = emailService;
        this.weeklyDigestService = weeklyDigestService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView admin(@ModelAttribute("brands") final List<Brand> brands,
                              @ModelAttribute("bodyTypes") final List<BodyType> bodyTypes,
                              @RequestParam(value = "tab", required = false) final String tab,
                              @RequestParam(value = "page", defaultValue = "1") final int page) {
        final String activeTab = normalizeAdminTab(tab);
        final long carRequestCount = carRequestService.countCarRequestsByStatus(CarRequestService.STATUS_PENDING);
        final long brandRequestCount = brandRequestService.countBrandRequestsByStatus(BrandRequestService.STATUS_PENDING);
        final long bodyTypeRequestCount = bodyTypeRequestService
                .countBodyTypeRequestsByStatus(BodyTypeRequestService.STATUS_PENDING);
        final long adminRequestCount = adminRequestService.countAdminRequestsByStatus(AdminRequestService.STATUS_PENDING);

        int currentPage = 1;
        int totalPages = 0;
        long totalItems = 0L;

        List<AdminCarRequestCard> pendingRequests = List.of();
        List<AdminCatalogRequestCard> pendingBrandRequests = List.of();
        List<AdminCatalogRequestCard> pendingBodyTypeRequests = List.of();
        List<AdminAdminRequestCard> pendingAdminRequests = List.of();

        switch (activeTab) {
            case TAB_BRANDS: {
                final Page<BrandRequest> requestPage = brandRequestService
                        .getBrandRequestsByStatus(BrandRequestService.STATUS_PENDING, page);
                pendingBrandRequests = requestPage.getItems().stream()
                        .map(this::toBrandCard)
                        .toList();
                currentPage = requestPage.getPageNumber();
                totalPages = requestPage.getTotalPages();
                totalItems = requestPage.getTotalItems();
                break;
            }
            case TAB_BODY_TYPES: {
                final Page<BodyTypeRequest> requestPage = bodyTypeRequestService
                        .getBodyTypeRequestsByStatus(BodyTypeRequestService.STATUS_PENDING, page);
                pendingBodyTypeRequests = requestPage.getItems().stream()
                        .map(this::toBodyTypeCard)
                        .toList();
                currentPage = requestPage.getPageNumber();
                totalPages = requestPage.getTotalPages();
                totalItems = requestPage.getTotalItems();
                break;
            }
            case TAB_MODERATORS: {
                final Page<AdminRequest> requestPage = adminRequestService
                        .getAdminRequestsByStatus(AdminRequestService.STATUS_PENDING, page);
                pendingAdminRequests = requestPage.getItems().stream()
                        .map(this::toAdminRequestCard)
                        .toList();
                currentPage = requestPage.getPageNumber();
                totalPages = requestPage.getTotalPages();
                totalItems = requestPage.getTotalItems();
                break;
            }
            case TAB_CARS:
            default: {
                final Map<Long, Brand> brandsById = brands.stream()
                        .collect(Collectors.toMap(Brand::getId, Function.identity()));
                final Map<Long, BodyType> bodyTypesById = bodyTypes.stream()
                        .collect(Collectors.toMap(BodyType::getId, Function.identity()));
                final Page<CarRequest> requestPage = carRequestService
                        .getCarRequestsByStatus(CarRequestService.STATUS_PENDING, page);
                pendingRequests = requestPage.getItems().stream()
                        .map(request -> toCard(request, brandsById, bodyTypesById))
                        .toList();
                currentPage = requestPage.getPageNumber();
                totalPages = requestPage.getTotalPages();
                totalItems = requestPage.getTotalItems();
                break;
            }
        }

        final ModelAndView mav = new ModelAndView("admin.jsp");
        mav.addObject("activeTab", activeTab);
        mav.addObject("carRequestCount", carRequestCount);
        mav.addObject("brandRequestCount", brandRequestCount);
        mav.addObject("bodyTypeRequestCount", bodyTypeRequestCount);
        mav.addObject("adminRequestCount", adminRequestCount);
        mav.addObject("totalPendingItems",
                carRequestCount + brandRequestCount + bodyTypeRequestCount + adminRequestCount);
        mav.addObject("pendingRequests", pendingRequests);
        mav.addObject("pendingBrandRequests", pendingBrandRequests);
        mav.addObject("pendingBodyTypeRequests", pendingBodyTypeRequests);
        mav.addObject("pendingAdminRequests", pendingAdminRequests);
        mav.addObject("currentPage", currentPage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("totalItems", totalItems);
        return mav;
    }

    @RequestMapping(value = "/requests/{requestId}/review", method = RequestMethod.GET)
    public ModelAndView reviewCarRequest(@PathVariable("requestId") final long requestId) {
        final CarRequest request = carRequestService.getCarRequestById(requestId).orElse(null);
        if (request == null || !CarRequestService.STATUS_PENDING.equals(request.getStatus())) {
            return new ModelAndView("redirect:/admin");
        }
        return carRequestFormPage(request, toForm(request), null);
    }

    @RequestMapping(value = "/cars/{carId}/edit", method = RequestMethod.GET)
    public ModelAndView editCar(@PathVariable("carId") final long carId,
                                @RequestHeader(value = "Referer", required = false) final String referer) {
        final Car car = carService.getCarById(carId).orElse(null);
        if (car == null) {
            return redirectBackToCatalog(referer);
        }
        return carEditFormPage(car, toForm(car), null);
    }

    @RequestMapping(value = "/requests/{requestId}/accept", method = RequestMethod.POST,
            consumes = "multipart/form-data")
    public ModelAndView acceptRequest(@PathVariable("requestId") final long requestId,
                                      @Valid @ModelAttribute("carForm") final CarForm carForm,
                                      final BindingResult errors,
                                      @RequestHeader(value = "Referer", required = false) final String referer) {
        final CarRequest pendingRequest = carRequestService.getCarRequestById(requestId).orElse(null);
        if (pendingRequest == null || !CarRequestService.STATUS_PENDING.equals(pendingRequest.getStatus())) {
            return new ModelAndView("redirect:/admin");
        }

        rejectInvalidSpecFields(errors, carForm);
        final List<MultipartFile> files = selectedImageFiles(carForm.getFiles());
        final int existingImageCount = buildRequestImageUrls(pendingRequest).size();
        final String imageError = validateUploadedImages(files, false, existingImageCount);
        if (imageError != null) {
            errors.rejectValue("files", "image.invalid", imageError);
        }

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

        if (!errors.hasErrors() && resolvedBrand != null && resolvedBodyType != null
                && isDuplicateCar(resolvedBrand, resolvedBodyType, carForm.getModel(), carForm.getYear(), -1L)) {
            errors.reject("car.duplicate", "Ya existe un auto con esa marca, modelo, carrocería y año.");
        }

        if (errors.hasErrors()) {
            return carRequestFormPage(pendingRequest, carForm, errors);
        }

        final List<CarImagePayload> imagePayloads;
        try {
            imagePayloads = toImagePayloads(files);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read uploaded image.", e);
        }

        final boolean approved = carRequestService.approvePendingRequest(
                requestId,
                resolvedBrand.getId(),
                carForm.getModel(),
                resolvedBodyType.getId(),
                carForm.getYear(),
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

        if (approved && pendingRequest != null) {
            final String submitterEmail = resolveSubmitterEmail(pendingRequest);
            if (submitterEmail != null) {
                emailService.sendCarApprovedNotification(submitterEmail, resolvedBrand.getName(), carForm.getModel());
            }
        }

        return redirectBackToAdmin(referer);
    }

    @RequestMapping(value = "/cars/{carId}", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ModelAndView updateCar(@PathVariable("carId") final long carId,
                                  @Valid @ModelAttribute("carForm") final CarForm carForm,
                                  final BindingResult errors,
                                  @RequestHeader(value = "Referer", required = false) final String referer) {
        final Car existingCar = carService.getCarById(carId).orElse(null);
        if (existingCar == null) {
            return redirectBackToCatalog(referer);
        }

        rejectInvalidSpecFields(errors, carForm);
        final List<MultipartFile> files = selectedImageFiles(carForm.getFiles());
        final int existingImageCount = buildCarImageUrls(existingCar).size();
        final String imageError = validateUploadedImages(files, false, existingImageCount);
        if (imageError != null) {
            errors.rejectValue("files", "image.invalid", imageError);
        }

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

        if (!errors.hasErrors() && resolvedBrand != null && resolvedBodyType != null
                && isDuplicateCar(resolvedBrand, resolvedBodyType, carForm.getModel(), carForm.getYear(), carId)) {
            errors.reject("car.duplicate", "Ya existe un auto con esa marca, modelo, carrocería y año.");
        }

        if (errors.hasErrors()) {
            return carEditFormPage(existingCar, carForm, errors);
        }

        final List<CarImagePayload> imagePayloads;
        try {
            imagePayloads = toImagePayloads(files);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read uploaded image.", e);
        }

        final Optional<Car> updated = carService.updateCar(
                carId,
                resolvedBrand.getId(),
                carForm.getModel(),
                resolvedBodyType.getId(),
                carForm.getYear(),
                carForm.getDescription(),
                Optional.empty(),
                Optional.empty(),
                ControllerUtils.normalizeSpecValue(carForm.getFuelType()),
                carForm.getHorsepower(),
                carForm.getAirbagCount(),
                ControllerUtils.normalizeSpecValue(carForm.getTransmission()),
                carForm.getFuelConsumption(),
                carForm.getMaxSpeedKmh(),
                carForm.getPriceUsd()
        );
        if (updated.isPresent() && !imagePayloads.isEmpty()) {
            carService.appendCarImages(carId, imagePayloads);
        }
        return new ModelAndView("redirect:/reviews?carId=" + carId);
    }

    @RequestMapping(value = "/cars/{carId}/delete", method = RequestMethod.POST)
    public ModelAndView deleteCar(@PathVariable("carId") final long carId,
                                  @RequestHeader(value = "Referer", required = false) final String referer) {
        carService.deleteCar(carId);
        return redirectBackAfterDelete(referer);
    }

    @RequestMapping(value = "/digest/preview", method = RequestMethod.POST)
    public ModelAndView previewDigest() {
        weeklyDigestService.sendWeeklyDigest();
        return new ModelAndView("redirect:/admin");
    }

    @RequestMapping(value = "/requests/{requestId}/reject", method = RequestMethod.POST)
    public ModelAndView rejectRequest(@PathVariable("requestId") final long requestId,
                                      @RequestHeader(value = "Referer", required = false) final String referer) {
        carRequestService.rejectPendingRequest(requestId);
        return redirectBackToAdmin(referer);
    }

    @RequestMapping(value = "/brand-requests/{requestId}/accept", method = RequestMethod.POST)
    public ModelAndView acceptBrandRequest(@PathVariable("requestId") final long requestId,
                                           @RequestParam(value = "name", required = false) final String name,
                                           @RequestHeader(value = "Referer", required = false) final String referer) {
        brandRequestService.approvePendingRequest(requestId, name);
        return redirectBackToAdmin(referer);
    }

    @RequestMapping(value = "/brand-requests/{requestId}/reject", method = RequestMethod.POST)
    public ModelAndView rejectBrandRequest(@PathVariable("requestId") final long requestId,
                                           @RequestHeader(value = "Referer", required = false) final String referer) {
        brandRequestService.rejectPendingRequest(requestId);
        return redirectBackToAdmin(referer);
    }

    @RequestMapping(value = "/brands/{brandId}", method = RequestMethod.POST)
    public ModelAndView updateBrand(@PathVariable("brandId") final long brandId,
                                    @RequestParam("name") final String name,
                                    @RequestHeader(value = "Referer", required = false) final String referer) {
        brandService.updateBrand(brandId, name);
        return redirectBackToCatalog(referer);
    }

    @RequestMapping(value = "/brands/{brandId}/delete", method = RequestMethod.POST)
    public ModelAndView deleteBrand(@PathVariable("brandId") final long brandId,
                                    @RequestHeader(value = "Referer", required = false) final String referer) {
        brandService.deleteBrand(brandId);
        return redirectBackAfterDelete(referer);
    }

    @RequestMapping(value = "/body-type-requests/{requestId}/accept", method = RequestMethod.POST)
    public ModelAndView acceptBodyTypeRequest(@PathVariable("requestId") final long requestId,
                                              @RequestParam(value = "name", required = false) final String name,
                                              @RequestHeader(value = "Referer", required = false) final String referer) {
        bodyTypeRequestService.approvePendingRequest(requestId, name);
        return redirectBackToAdmin(referer);
    }

    @RequestMapping(value = "/body-type-requests/{requestId}/reject", method = RequestMethod.POST)
    public ModelAndView rejectBodyTypeRequest(@PathVariable("requestId") final long requestId,
                                              @RequestHeader(value = "Referer", required = false) final String referer) {
        bodyTypeRequestService.rejectPendingRequest(requestId);
        return redirectBackToAdmin(referer);
    }

    @RequestMapping(value = "/body-types/{bodyTypeId}", method = RequestMethod.POST)
    public ModelAndView updateBodyType(@PathVariable("bodyTypeId") final long bodyTypeId,
                                       @RequestParam("name") final String name,
                                       @RequestHeader(value = "Referer", required = false) final String referer) {
        bodyTypeService.updateBodyType(bodyTypeId, name);
        return redirectBackToCatalog(referer);
    }

    @RequestMapping(value = "/body-types/{bodyTypeId}/delete", method = RequestMethod.POST)
    public ModelAndView deleteBodyType(@PathVariable("bodyTypeId") final long bodyTypeId,
                                       @RequestHeader(value = "Referer", required = false) final String referer) {
        bodyTypeService.deleteBodyType(bodyTypeId);
        return redirectBackAfterDelete(referer);
    }

    @RequestMapping(value = "/admin-requests/{requestId}/accept", method = RequestMethod.POST)
    public ModelAndView acceptAdminRequest(@PathVariable("requestId") final long requestId,
                                           @RequestHeader(value = "Referer", required = false) final String referer) {
        adminRequestService.approvePendingRequest(requestId);
        return redirectBackToAdmin(referer);
    }

    @RequestMapping(value = "/admin-requests/{requestId}/reject", method = RequestMethod.POST)
    public ModelAndView rejectAdminRequest(@PathVariable("requestId") final long requestId,
                                           @RequestHeader(value = "Referer", required = false) final String referer) {
        adminRequestService.rejectPendingRequest(requestId);
        return redirectBackToAdmin(referer);
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

    private ModelAndView carRequestFormPage(final CarRequest request, final CarForm carForm,
                                            final BindingResult errors) {
        final ModelAndView mav = new ModelAndView("car-form.jsp");
        addCarFormBinding(mav, carForm, errors);
        mav.addObject("carFormMode", "review-request");
        mav.addObject("formKicker", "Solicitud pendiente");
        mav.addObject("formTitle", "Revisar formulario");
        mav.addObject("formSubtitle", "Corregí los datos que haga falta antes de aprobar o rechazar la solicitud.");
        mav.addObject("formAction", "/admin/requests/" + request.getId() + "/accept");
        mav.addObject("cancelUrl", "/admin?tab=cars");
        mav.addObject("submitLabel", "Confirmar auto");
        mav.addObject("rejectAction", "/admin/requests/" + request.getId() + "/reject");
        mav.addObject("rejectLabel", "Rechazar");
        mav.addObject("showCatalogRequestLinks", false);
        mav.addObject("existingImageUrls", buildRequestImageUrls(request));
        mav.addObject("existingImageStatus", requestImageStatus(request));
        return mav;
    }

    private ModelAndView carEditFormPage(final Car car, final CarForm carForm, final BindingResult errors) {
        final ModelAndView mav = new ModelAndView("car-form.jsp");
        addCarFormBinding(mav, carForm, errors);
        mav.addObject("carFormMode", "edit-car");
        mav.addObject("formKicker", "Catálogo");
        mav.addObject("formTitle", "Editar auto");
        mav.addObject("formSubtitle", "Modificá los datos del auto publicado. Si cargás imágenes nuevas, se agregan a la galería actual.");
        mav.addObject("formAction", "/admin/cars/" + car.getId());
        mav.addObject("cancelUrl", "/reviews?carId=" + car.getId());
        mav.addObject("submitLabel", "Guardar cambios");
        mav.addObject("showCatalogRequestLinks", false);
        mav.addObject("existingImageUrls", buildCarImageUrls(car));
        mav.addObject("existingImageStatus", carImageStatus(car));
        return mav;
    }

    private void addCarFormBinding(final ModelAndView mav, final CarForm carForm, final BindingResult errors) {
        mav.addObject("carForm", carForm);
        if (errors != null) {
            mav.addObject(BindingResult.MODEL_KEY_PREFIX + "carForm", errors);
        }
    }

    private CarForm toForm(final CarRequest request) {
        final CarForm form = new CarForm();
        brandService.findById(request.getBrandId()).map(Brand::getName).ifPresent(form::setBrand);
        bodyTypeService.findById(request.getBodyTypeId()).map(BodyType::getName).ifPresent(form::setBodyType);
        form.setModel(request.getModel());
        form.setYear(request.getYear());
        form.setSubmitterEmail(resolveSubmitterEmail(request));
        form.setDescription(request.getDescription());
        form.setFuelType(request.getFuelType());
        form.setHorsepower(request.getHorsepower());
        form.setAirbagCount(request.getAirbagCount());
        form.setTransmission(request.getTransmission());
        form.setFuelConsumption(request.getFuelConsumption());
        form.setMaxSpeedKmh(request.getMaxSpeedKmh());
        form.setPriceUsd(request.getPriceUsd());
        return form;
    }

    private CarForm toForm(final Car car) {
        final CarForm form = new CarForm();
        form.setBrand(car.getBrandName());
        form.setBodyType(car.getBodyType());
        form.setModel(car.getModel());
        form.setYear(car.getYear());
        form.setDescription(car.getDescription());
        form.setFuelType(car.getFuelType());
        form.setHorsepower(car.getHorsepower());
        form.setAirbagCount(car.getAirbagCount());
        form.setTransmission(car.getTransmission());
        form.setFuelConsumption(car.getFuelConsumption());
        form.setMaxSpeedKmh(car.getMaxSpeedKmh());
        form.setPriceUsd(car.getPriceUsd());
        return form;
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
                request.getYear(),
                valueOrFallback(bodyTypeName, "Carrocería pendiente"),
                request.getDescription(),
                submitterLabel(request),
                !imageUrls.isEmpty(),
                imageUrls.isEmpty() ? null : imageUrls.get(0),
                String.join("|", imageUrls),
                request.getFuelType(),
                request.getHorsepower(),
                request.getAirbagCount(),
                request.getTransmission(),
                request.getFuelConsumption(),
                request.getMaxSpeedKmh(),
                request.getPriceUsd()
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

    private List<String> buildCarImageUrls(final Car car) {
        final List<CarImage> carImages = carService.getCarImagesByCarId(car.getId());
        if (!carImages.isEmpty()) {
            return carImages.stream()
                    .map(image -> "/cars/" + car.getId() + "/images/" + image.getImageId())
                    .collect(Collectors.toList());
        }
        if (!car.getHasImage()) {
            return List.of();
        }
        return List.of("/cars/" + car.getId() + "/image");
    }

    private String requestImageStatus(final CarRequest request) {
        final List<String> imageUrls = buildRequestImageUrls(request);
        if (imageUrls.isEmpty()) {
            return "Sin imágenes cargadas";
        }
        return imageUrls.size() + " imagen" + (imageUrls.size() == 1 ? "" : "es") + " cargada"
                + (imageUrls.size() == 1 ? "" : "s") + " en la solicitud #" + request.getId();
    }

    private String carImageStatus(final Car car) {
        final List<String> imageUrls = buildCarImageUrls(car);
        if (imageUrls.isEmpty()) {
            return "Sin imágenes cargadas";
        }
        return imageUrls.size() + " imagen" + (imageUrls.size() == 1 ? "" : "es") + " actual"
                + (imageUrls.size() == 1 ? "" : "es") + " del auto #" + car.getId();
    }

    private String submitterLabel(final CarRequest request) {
        final String submitterEmail = resolveSubmitterEmail(request);
        if (submitterEmail != null) {
            return submitterEmail;
        }
        if (request.getSubmittedByUserId() != null) {
            return "Usuario #" + request.getSubmittedByUserId();
        }
        return "Usuario sin identificar";
    }

    private String submitterLabel(final String submitterEmail, final Long submittedByUserId) {
        if (submitterEmail != null && !submitterEmail.isBlank()) {
            return submitterEmail;
        }
        if (submittedByUserId != null) {
            final String resolvedEmail = userService.getUserById(submittedByUserId)
                    .map(User::getEmail)
                    .filter(email -> !email.isBlank())
                    .orElse(null);
            if (resolvedEmail != null) {
                return resolvedEmail;
            }
            return "Usuario #" + submittedByUserId;
        }
        return "Usuario sin identificar";
    }

    private AdminCatalogRequestCard toBrandCard(final BrandRequest request) {
        return new AdminCatalogRequestCard(
                request.getId(),
                request.getName(),
                submitterLabel(request.getSubmitterEmail(), request.getSubmittedByUserId()),
                request.getComments()
        );
    }

    private AdminCatalogRequestCard toBodyTypeCard(final BodyTypeRequest request) {
        return new AdminCatalogRequestCard(
                request.getId(),
                request.getName(),
                submitterLabel(request.getSubmitterEmail(), request.getSubmittedByUserId()),
                request.getComments()
        );
    }

    private AdminAdminRequestCard toAdminRequestCard(final AdminRequest request) {
        final User submitter = userService.getUserById(request.getSubmittedByUserId()).orElse(null);
        final String username = submitter != null && submitter.getUsername() != null
                && !submitter.getUsername().isBlank()
                ? submitter.getUsername()
                : "Usuario sin identificar";
        final String label = username + " · #" + request.getSubmittedByUserId();
        return new AdminAdminRequestCard(
                request.getId(),
                request.getSubmittedByUserId(),
                username,
                label,
                request.getMotivation(),
                request.getBio(),
                request.getJustification()
        );
    }

    private String resolveSubmitterEmail(final CarRequest request) {
        if (request.getSubmitterEmail() != null && !request.getSubmitterEmail().isBlank()) {
            return request.getSubmitterEmail();
        }
        if (request.getSubmittedByUserId() != null) {
            return userService.getUserById(request.getSubmittedByUserId())
                    .map(User::getEmail)
                    .filter(email -> !email.isBlank())
                    .orElse(null);
        }
        return null;
    }

    private String validateUploadedImage(final MultipartFile file, final boolean required) {
        return ControllerUtils.validateUploadedImage(file, required);
    }

    private String validateUploadedImages(final List<MultipartFile> files, final boolean required) {
        return validateUploadedImages(files, required, 0);
    }

    private String validateUploadedImages(final List<MultipartFile> files, final boolean required,
                                          final int existingImageCount) {
        if (files.isEmpty()) {
            return required ? "La imagen es obligatoria." : null;
        }
        if (existingImageCount + files.size() > MAX_IMAGE_COUNT) {
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

    private boolean isDuplicateCar(final Brand brand, final BodyType bodyType, final String model, final Integer year,
                                   final long ignoredCarId) {
        final String normalizedModel = ControllerUtils.normalize(model);
        if (normalizedModel == null) {
            return false;
        }
        return carService
                .getCarsByBrandAndBodyType(brand.getName(), bodyType.getName())
                .stream()
                .anyMatch(car -> car.getId() != ignoredCarId
                        && sameModel(car.getModel(), normalizedModel)
                        && Objects.equals(car.getYear(), year));
    }

    private void rejectInvalidSpecFields(final BindingResult errors, final CarForm carForm) {
        if (carForm.getFuelType() != null
                && !CarSearchCriteria.ALLOWED_FUEL_TYPES.contains(
                        ControllerUtils.normalizeSpecValue(carForm.getFuelType()))) {
            errors.rejectValue("fuelType", "fuelType.invalid", "Tipo de motorización no válido.");
        }
        if (carForm.getTransmission() != null
                && !CarSearchCriteria.ALLOWED_TRANSMISSIONS.contains(
                        ControllerUtils.normalizeSpecValue(carForm.getTransmission()))) {
            errors.rejectValue("transmission", "transmission.invalid", "Transmisión no válida.");
        }
    }

    private ModelAndView redirectBackToCatalog(final String referer) {
        return redirectBackToCatalog(referer, true);
    }

    private ModelAndView redirectBackAfterDelete(final String referer) {
        return redirectBackToCatalog(referer, false);
    }

    private ModelAndView redirectBackToAdmin(final String referer) {
        final String fallback = "redirect:/admin";
        if (referer == null || referer.isBlank()) {
            return new ModelAndView(fallback);
        }
        try {
            final URI uri = URI.create(referer);
            if (!"/admin".equals(uri.getRawPath())) {
                return new ModelAndView(fallback);
            }
            final String query = uri.getRawQuery();
            return new ModelAndView("redirect:/admin" + (query == null ? "" : "?" + query));
        } catch (final IllegalArgumentException ignored) {
            return new ModelAndView(fallback);
        }
    }

    private ModelAndView redirectBackToCatalog(final String referer, final boolean allowReviewsPage) {
        final String fallback = "redirect:/cars";
        if (referer == null || referer.isBlank()) {
            return new ModelAndView(fallback);
        }
        try {
            final URI uri = URI.create(referer);
            final String path = uri.getRawPath();
            if (path == null || path.isBlank()) {
                return new ModelAndView(fallback);
            }
            if ("/".equals(path) || "/cars".equals(path) || (allowReviewsPage && "/reviews".equals(path))) {
                final String query = uri.getRawQuery();
                return new ModelAndView("redirect:" + path + (query == null ? "" : "?" + query));
            }
        } catch (final IllegalArgumentException ignored) {
            return new ModelAndView(fallback);
        }
        return new ModelAndView(fallback);
    }

    private String valueOrFallback(final String value, final String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String normalizeAdminTab(final String tab) {
        if (tab == null || tab.isBlank()) {
            return TAB_CARS;
        }
        final String normalizedTab = tab.trim().toLowerCase(Locale.ROOT);
        switch (normalizedTab) {
            case TAB_BRANDS:
                return TAB_BRANDS;
            case TAB_BODY_TYPES:
                return TAB_BODY_TYPES;
            case TAB_MODERATORS:
                return TAB_MODERATORS;
            case "moderator":
            case "admin-requests":
                return TAB_MODERATORS;
            case TAB_CARS:
            default:
                return TAB_CARS;
        }
    }

    private boolean sameModel(final String existingModel, final String submittedModel) {
        final String normalizedExistingModel = ControllerUtils.normalize(existingModel);
        final String normalizedSubmittedModel = ControllerUtils.normalize(submittedModel);
        return normalizedExistingModel != null && normalizedSubmittedModel != null
                && normalizedExistingModel.toLowerCase(Locale.ROOT)
                .equals(normalizedSubmittedModel.toLowerCase(Locale.ROOT));
    }

    public static final class AdminCarRequestCard {
        private final long id;
        private final String brandName;
        private final String model;
        private final Integer year;
        private final String bodyTypeName;
        private final String description;
        private final String submitter;
        private final boolean hasImage;
        private final String imageUrl;
        private final String imageUrls;
        private final String fuelType;
        private final Integer horsepower;
        private final Integer airbagCount;
        private final String transmission;
        private final BigDecimal fuelConsumption;
        private final Integer maxSpeedKmh;
        private final BigDecimal priceUsd;

        private AdminCarRequestCard(final long id, final String brandName, final String model,
                                    final Integer year, final String bodyTypeName, final String description, final String submitter,
                                    final boolean hasImage, final String imageUrl, final String imageUrls,
                                    final String fuelType, final Integer horsepower,
                                    final Integer airbagCount, final String transmission,
                                    final BigDecimal fuelConsumption, final Integer maxSpeedKmh,
                                    final BigDecimal priceUsd) {
            this.id = id;
            this.brandName = brandName;
            this.model = model;
            this.year = year;
            this.bodyTypeName = bodyTypeName;
            this.description = description;
            this.submitter = submitter;
            this.hasImage = hasImage;
            this.imageUrl = imageUrl;
            this.imageUrls = imageUrls;
            this.fuelType = fuelType;
            this.horsepower = horsepower;
            this.airbagCount = airbagCount;
            this.transmission = transmission;
            this.fuelConsumption = fuelConsumption;
            this.maxSpeedKmh = maxSpeedKmh;
            this.priceUsd = priceUsd;
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

        public Integer getYear() {
            return year;
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

        public BigDecimal getPriceUsd() {
            return priceUsd;
        }
    }

    public static final class AdminCatalogRequestCard {
        private final long id;
        private final String name;
        private final String submitter;
        private final String comments;

        private AdminCatalogRequestCard(final long id, final String name, final String submitter,
                                        final String comments) {
            this.id = id;
            this.name = name;
            this.submitter = submitter;
            this.comments = comments;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSubmitter() {
            return submitter;
        }

        public String getComments() {
            return comments;
        }
    }

    public static final class AdminAdminRequestCard {
        private final long id;
        private final long userId;
        private final String username;
        private final String label;
        private final String motivation;
        private final String bio;
        private final String justification;

        private AdminAdminRequestCard(final long id, final long userId, final String username,
                                      final String label, final String motivation,
                                      final String bio, final String justification) {
            this.id = id;
            this.userId = userId;
            this.username = username;
            this.label = label;
            this.motivation = motivation;
            this.bio = bio;
            this.justification = justification;
        }

        public long getId() {
            return id;
        }

        public long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getLabel() {
            return label;
        }

        public String getMotivation() {
            return motivation;
        }

        public String getBio() {
            return bio;
        }

        public String getJustification() {
            return justification;
        }
    }
}
