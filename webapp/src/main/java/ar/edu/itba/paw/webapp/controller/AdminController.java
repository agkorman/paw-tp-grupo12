package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.AdminRequest;
import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.BodyTypeRequest;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandRequest;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.StoredImagePayload;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.AdminRequestService;
import ar.edu.itba.paw.services.BodyTypeRequestService;
import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.services.BrandRequestService;
import ar.edu.itba.paw.services.BrandService;
import ar.edu.itba.paw.services.CarRequestService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.exception.DuplicateCarException;
import ar.edu.itba.paw.webapp.auth.LoginRedirectUtils;
import ar.edu.itba.paw.webapp.exception.UploadedImageReadException;
import ar.edu.itba.paw.webapp.form.CarForm;
import ar.edu.itba.paw.webapp.util.ImageValidationService;
import ar.edu.itba.paw.webapp.util.LogSanitizer;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        AdminController.class
    );

    private static final String TAB_CARS = "cars";
    private static final String TAB_BRANDS = "brands";
    private static final String TAB_BODY_TYPES = "body-types";
    private static final String TAB_MODERATORS = "moderators";
    private static final int MAX_IMAGE_COUNT = 5;
    private static final String ACTION_TOAST_ATTRIBUTE = "actionToastCode";
    private static final String ACTION_TOAST_TYPE_ATTRIBUTE = "actionToastType";
    private static final String TOAST_TYPE_ERROR = "error";

    /**
     * Maps an action outcome key to the i18n bundle key rendered by the toast on the
     * redirected dashboard. The code (not resolved text) travels as a flash attribute,
     * matching the {@code actionToastCode} pattern used by the other controllers.
     */
    private static final Map<String, String> ADMIN_TOAST_CODES = Map.ofEntries(
        Map.entry("carAccepted", "admin.carRequest.accept.toast.success"),
        Map.entry("carRejected", "admin.carRequest.reject.toast.success"),
        Map.entry("catalogAccepted", "admin.catalogRequest.accept.toast.success"),
        Map.entry("catalogRejected", "admin.catalogRequest.reject.toast.success"),
        Map.entry("catalogAcceptError", "admin.catalogRequest.accept.toast.error"),
        Map.entry("catalogError", "admin.catalogRequest.toast.error"),
        Map.entry("requestAccepted", "admin.request.accept.toast.success"),
        Map.entry("requestRejected", "admin.request.reject.toast.success"),
        Map.entry("requestError", "admin.request.toast.error")
    );
    private static final Set<String> ADMIN_TOAST_ERROR_KEYS = Set.of(
        "catalogAcceptError",
        "catalogError",
        "requestError"
    );

    private final CarRequestService carRequestService;
    private final CarService carService;
    private final BrandService brandService;
    private final BodyTypeService bodyTypeService;
    private final BrandRequestService brandRequestService;
    private final BodyTypeRequestService bodyTypeRequestService;
    private final AdminRequestService adminRequestService;
    private final UserService userService;
    private final ImageValidationService imageValidationService;

    @Autowired
    public AdminController(
        final CarRequestService carRequestService,
        final CarService carService,
        final BrandService brandService,
        final BodyTypeService bodyTypeService,
        final BrandRequestService brandRequestService,
        final BodyTypeRequestService bodyTypeRequestService,
        final AdminRequestService adminRequestService,
        final UserService userService,
        final ImageValidationService imageValidationService
    ) {
        this.carRequestService = carRequestService;
        this.carService = carService;
        this.brandService = brandService;
        this.bodyTypeService = bodyTypeService;
        this.brandRequestService = brandRequestService;
        this.bodyTypeRequestService = bodyTypeRequestService;
        this.adminRequestService = adminRequestService;
        this.userService = userService;
        this.imageValidationService = imageValidationService;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(
            String.class,
            new StringTrimmerEditor(true)
        );
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView admin(
        @ModelAttribute("brands") final List<Brand> brands,
        @ModelAttribute("bodyTypes") final List<BodyType> bodyTypes,
        @RequestParam(value = "tab", required = false) final String tab,
        @RequestParam(value = "page", defaultValue = "1") final int page
    ) {
        final String activeTab = normalizeAdminTab(tab);
        final long carRequestCount = carRequestService.countCarRequestsByStatus(
            CarRequestService.STATUS_PENDING
        );
        final long brandRequestCount =
            brandRequestService.countBrandRequestsByStatus(
                BrandRequestService.STATUS_PENDING
            );
        final long bodyTypeRequestCount =
            bodyTypeRequestService.countBodyTypeRequestsByStatus(
                BodyTypeRequestService.STATUS_PENDING
            );
        final long adminRequestCount =
            adminRequestService.countAdminRequestsByStatus(
                AdminRequestService.STATUS_PENDING
            );

        int currentPage = 1;
        int totalPages = 0;
        long totalItems = 0L;

        List<AdminCarRequestCard> pendingRequests = List.of();
        List<AdminCatalogRequestCard> pendingBrandRequests = List.of();
        List<AdminCatalogRequestCard> pendingBodyTypeRequests = List.of();
        List<AdminAdminRequestCard> pendingAdminRequests = List.of();

        switch (activeTab) {
            case TAB_BRANDS: {
                final Page<BrandRequest> requestPage =
                    brandRequestService.getBrandRequestsByStatus(
                        BrandRequestService.STATUS_PENDING,
                        page
                    );
                final Map<Long, User> brandSubmitters = fetchSubmitters(
                    requestPage.getItems(),
                    BrandRequest::getSubmittedByUserId
                );
                pendingBrandRequests = requestPage
                    .getItems()
                    .stream()
                    .map(request -> toBrandCard(request, brandSubmitters))
                    .toList();
                currentPage = requestPage.getPageNumber();
                totalPages = requestPage.getTotalPages();
                totalItems = requestPage.getTotalItems();
                break;
            }
            case TAB_BODY_TYPES: {
                final Page<BodyTypeRequest> requestPage =
                    bodyTypeRequestService.getBodyTypeRequestsByStatus(
                        BodyTypeRequestService.STATUS_PENDING,
                        page
                    );
                final Map<Long, User> bodyTypeSubmitters = fetchSubmitters(
                    requestPage.getItems(),
                    BodyTypeRequest::getSubmittedByUserId
                );
                pendingBodyTypeRequests = requestPage
                    .getItems()
                    .stream()
                    .map(request -> toBodyTypeCard(request, bodyTypeSubmitters))
                    .toList();
                currentPage = requestPage.getPageNumber();
                totalPages = requestPage.getTotalPages();
                totalItems = requestPage.getTotalItems();
                break;
            }
            case TAB_MODERATORS: {
                final Page<AdminRequest> requestPage =
                    adminRequestService.getAdminRequestsByStatus(
                        AdminRequestService.STATUS_PENDING,
                        page
                    );
                final Map<Long, User> adminSubmitters = fetchSubmitters(
                    requestPage.getItems(),
                    AdminRequest::getSubmittedByUserId
                );
                pendingAdminRequests = requestPage
                    .getItems()
                    .stream()
                    .map(request -> toAdminRequestCard(request, adminSubmitters))
                    .toList();
                currentPage = requestPage.getPageNumber();
                totalPages = requestPage.getTotalPages();
                totalItems = requestPage.getTotalItems();
                break;
            }
            case TAB_CARS:
            default: {
                final Map<Long, Brand> brandsById = brands
                    .stream()
                    .collect(
                        Collectors.toMap(Brand::getId, Function.identity())
                    );
                final Map<Long, BodyType> bodyTypesById = bodyTypes
                    .stream()
                    .collect(
                        Collectors.toMap(BodyType::getId, Function.identity())
                    );
                final Page<CarRequest> requestPage =
                    carRequestService.getCarRequestsByStatus(
                        CarRequestService.STATUS_PENDING,
                        page
                    );
                final Map<Long, User> carSubmitters = fetchSubmitters(
                    requestPage.getItems(),
                    CarRequest::getSubmittedByUserId
                );
                final Map<Long, List<ImageMetadata>> requestImagesById =
                    fetchRequestImagesById(requestPage.getItems());
                pendingRequests = requestPage
                    .getItems()
                    .stream()
                    .map(request ->
                        toCard(
                            request,
                            brandsById,
                            bodyTypesById,
                            carSubmitters,
                            requestImagesById
                        )
                    )
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
        mav.addObject("totalPendingItems", adminRequestService.getTotalPendingItems());
        mav.addObject("pendingRequests", pendingRequests);
        mav.addObject("pendingBrandRequests", pendingBrandRequests);
        mav.addObject("pendingBodyTypeRequests", pendingBodyTypeRequests);
        mav.addObject("pendingAdminRequests", pendingAdminRequests);
        mav.addObject("currentPage", currentPage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("totalItems", totalItems);
        return mav;
    }

    @RequestMapping(
        value = "/requests/{requestId}/review",
        method = RequestMethod.GET
    )
    public ModelAndView reviewCarRequest(
        @PathVariable("requestId") final long requestId,
        @RequestParam(value = "redirect", required = false) final String redirect,
        final HttpServletRequest request
    ) {
        final CarRequest carRequest = carRequestService
            .getCarRequestById(requestId)
            .orElse(null);
        if (
            carRequest == null ||
            !CarRequestService.STATUS_PENDING.equals(carRequest.getStatus())
        ) {
            return new ModelAndView("redirect:/admin");
        }
        final String adminRedirect = LoginRedirectUtils
            .safeRedirect(redirect, request.getContextPath())
            .orElse(null);
        final List<ImageMetadata> requestImages =
            carRequestService.getCarRequestImages(carRequest.getId());
        return carRequestFormPage(
            carRequest,
            toForm(carRequest, requestImages),
            null,
            requestImages,
            adminRedirect
        );
    }

    @RequestMapping(value = "/cars/{carId}/edit", method = RequestMethod.GET)
    public ModelAndView editCar(
        @PathVariable("carId") final long carId,
        @RequestHeader(value = "Referer", required = false) final String referer
    ) {
        final Car car = carService.getCarById(carId).orElse(null);
        if (car == null) {
            return redirectBackToCatalog(referer);
        }
        return carEditFormPage(car, toForm(car), null);
    }

    @RequestMapping(
        value = "/requests/{requestId}/accept",
        method = RequestMethod.POST,
        consumes = "multipart/form-data"
    )
    public ModelAndView acceptRequest(
        @PathVariable("requestId") final long requestId,
        @Valid @ModelAttribute("carForm") final CarForm carForm,
        final BindingResult errors,
        @RequestParam(value = "redirect", required = false) final String redirect,
        final HttpServletRequest request,
        final RedirectAttributes redirectAttributes
    ) {
        final CarRequest pendingRequest = carRequestService
            .getCarRequestById(requestId)
            .orElse(null);
        if (
            pendingRequest == null ||
            !CarRequestService.STATUS_PENDING.equals(pendingRequest.getStatus())
        ) {
            return new ModelAndView("redirect:/admin");
        }
        final String adminRedirect = LoginRedirectUtils
            .safeRedirect(redirect, request.getContextPath())
            .orElse(null);

        rejectInvalidSpecFields(errors, carForm);
        final List<MultipartFile> files = selectedImageFiles(
            carForm.getFiles()
        );
        final List<Long> retainedImageIds = resolveRetainedRequestImageIds(
            pendingRequest,
            carForm,
            errors
        );
        final String imageError = validateUploadedImages(
            files,
            true,
            retainedImageIds.size()
        );
        if (imageError != null) {
            errors.rejectValue(
                "files",
                imageError,
                new Object[] { MAX_IMAGE_COUNT },
                null
            );
        }

        Brand resolvedBrand = null;
        if (!errors.hasFieldErrors("brand")) {
            resolvedBrand = brandService
                .findByName(carForm.getBrand())
                .orElse(null);
            if (resolvedBrand == null) {
                errors.rejectValue(
                    "brand",
                    "validation.car.brand.invalid"
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
                    "validation.car.bodyType.invalid"
                );
            }
        }

        if (errors.hasErrors()) {
            LOGGER.warn(
                "car request approval rejected: validation errors requestId={} errorCount={}",
                requestId,
                errors.getErrorCount()
            );
            return carRequestFormPage(pendingRequest, carForm, errors, adminRedirect);
        }

        final List<ImagePayload> imagePayloads;
        try {
            imagePayloads = carRequestService.collectRetainedImagePayloads(
                requestId,
                retainedImageIds
            );
            imagePayloads.addAll(toImagePayloads(files));
        } catch (final IOException e) {
            LOGGER.error(
                "failed to read uploaded image for car request id={}",
                requestId,
                e
            );
            throw new UploadedImageReadException(
                "approving car request " + requestId,
                e
            );
        }

        try {
            LOGGER.info(
                "approving car request id={} brandId={} bodyTypeId={}",
                requestId,
                resolvedBrand.getId(),
                resolvedBodyType.getId()
            );
            carRequestService.approvePendingRequest(
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
        } catch (final DuplicateCarException e) {
            errors.reject("validation.car.duplicate");
            LOGGER.warn(
                "car request approval rejected: duplicate car requestId={}",
                requestId
            );
            return carRequestFormPage(pendingRequest, carForm, errors, adminRedirect);
        }

        addAdminToast(redirectAttributes, "carAccepted");
        return new ModelAndView("redirect:" + (adminRedirect != null ? adminRedirect : "/admin"));
    }

    @RequestMapping(
        value = "/cars/{carId}",
        method = RequestMethod.POST,
        consumes = "multipart/form-data"
    )
    public ModelAndView updateCar(
        @PathVariable("carId") final long carId,
        @Valid @ModelAttribute("carForm") final CarForm carForm,
        final BindingResult errors,
        @RequestHeader(value = "Referer", required = false) final String referer
    ) {
        final Car existingCar = carService.getCarById(carId).orElse(null);
        if (existingCar == null) {
            return redirectBackToCatalog(referer);
        }

        rejectInvalidSpecFields(errors, carForm);
        final List<MultipartFile> files = selectedImageFiles(
            carForm.getFiles()
        );
        final List<Long> retainedImageIds = resolveRetainedCarImageIds(
            existingCar,
            carForm,
            errors
        );
        final String imageError = validateUploadedImages(
            files,
            true,
            retainedImageIds.size()
        );
        if (imageError != null) {
            errors.rejectValue(
                "files",
                imageError,
                new Object[] { MAX_IMAGE_COUNT },
                null
            );
        }

        Brand resolvedBrand = null;
        if (!errors.hasFieldErrors("brand")) {
            resolvedBrand = brandService
                .findByName(carForm.getBrand())
                .orElse(null);
            if (resolvedBrand == null) {
                errors.rejectValue(
                    "brand",
                    "validation.car.brand.invalid"
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
                    "validation.car.bodyType.invalid"
                );
            }
        }

        if (errors.hasErrors()) {
            LOGGER.warn(
                "car update rejected: validation errors carId={} errorCount={}",
                carId,
                errors.getErrorCount()
            );
            return carEditFormPage(existingCar, carForm, errors);
        }

        final List<ImagePayload> imagePayloads;
        try {
            imagePayloads = carService.collectRetainedImagePayloads(
                carId,
                retainedImageIds
            );
            imagePayloads.addAll(toImagePayloads(files));
        } catch (final IOException e) {
            LOGGER.error(
                "failed to read uploaded image for car id={}",
                carId,
                e
            );
            throw new UploadedImageReadException("editing car " + carId, e);
        }

        final Optional<Car> updated;
        try {
            updated = carService.updateCar(
                carId,
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
        } catch (final DuplicateCarException e) {
            errors.reject("validation.car.duplicate");
            LOGGER.warn("car update rejected: duplicate car carId={}", carId);
            return carEditFormPage(existingCar, carForm, errors);
        }
        if (updated.isPresent()) {
            LOGGER.info("admin updated car id={}", carId);
        }
        return new ModelAndView("redirect:/reviews/car/" + carId);
    }

    @RequestMapping(value = "/cars/{carId}/delete", method = RequestMethod.POST)
    public ModelAndView deleteCar(
        @PathVariable("carId") final long carId,
        @RequestHeader(value = "Referer", required = false) final String referer
    ) {
        LOGGER.info("admin delete car id={}", carId);
        carService.deleteCar(carId);
        return redirectBackAfterDelete(referer);
    }

    @RequestMapping(
        value = "/requests/{requestId}/reject",
        method = RequestMethod.POST
    )
    public ModelAndView rejectRequest(
        @PathVariable("requestId") final long requestId,
        @RequestParam(value = "redirect", required = false) final String redirect,
        final HttpServletRequest request,
        final RedirectAttributes redirectAttributes
    ) {
        LOGGER.info("admin reject car request id={}", requestId);
        carRequestService.rejectPendingRequest(requestId);
        final String adminRedirect = LoginRedirectUtils
            .safeRedirect(redirect, request.getContextPath())
            .orElse(null);
        addAdminToast(redirectAttributes, "carRejected");
        return new ModelAndView("redirect:" + (adminRedirect != null ? adminRedirect : "/admin"));
    }

    @RequestMapping(
        value = "/brand-requests/{requestId}/accept",
        method = RequestMethod.POST
    )
    public ModelAndView acceptBrandRequest(
        @PathVariable("requestId") final long requestId,
        @RequestParam(value = "name", required = false) final String name,
        @RequestHeader(value = "Referer", required = false) final String referer,
        final RedirectAttributes redirectAttributes
    ) {
        LOGGER.info(
            "admin accept brand request id={} overrideName={}",
            requestId,
            LogSanitizer.forLog(name, LogSanitizer.MAX_LOG_NAME_CODE_POINTS)
        );
        final boolean accepted = brandRequestService.approvePendingRequest(
            requestId,
            name
        );
        return redirectBackToAdmin(
            referer,
            accepted ? "catalogAccepted" : "catalogAcceptError",
            redirectAttributes
        );
    }

    @RequestMapping(
        value = "/brand-requests/{requestId}/reject",
        method = RequestMethod.POST
    )
    public ModelAndView rejectBrandRequest(
        @PathVariable("requestId") final long requestId,
        @RequestHeader(value = "Referer", required = false) final String referer,
        final RedirectAttributes redirectAttributes
    ) {
        LOGGER.info("admin reject brand request id={}", requestId);
        final boolean rejected = brandRequestService.rejectPendingRequest(
            requestId
        );
        return redirectBackToAdmin(
            referer,
            rejected ? "catalogRejected" : "catalogError",
            redirectAttributes
        );
    }

    @RequestMapping(value = "/brands/{brandId}", method = RequestMethod.POST)
    public ModelAndView updateBrand(
        @PathVariable("brandId") final long brandId,
        @RequestParam("name") final String name,
        @RequestHeader(value = "Referer", required = false) final String referer
    ) {
        LOGGER.info(
            "admin update brand id={} name={}",
            brandId,
            LogSanitizer.forLog(name, LogSanitizer.MAX_LOG_NAME_CODE_POINTS)
        );
        brandService.updateBrand(brandId, name);
        return redirectBackToCatalog(referer);
    }

    @RequestMapping(
        value = "/brands/{brandId}/delete",
        method = RequestMethod.POST
    )
    public ModelAndView deleteBrand(
        @PathVariable("brandId") final long brandId,
        @RequestHeader(value = "Referer", required = false) final String referer
    ) {
        LOGGER.info("admin delete brand id={}", brandId);
        brandService.deleteBrand(brandId);
        return redirectBackAfterDelete(referer);
    }

    @RequestMapping(
        value = "/body-type-requests/{requestId}/accept",
        method = RequestMethod.POST
    )
    public ModelAndView acceptBodyTypeRequest(
        @PathVariable("requestId") final long requestId,
        @RequestParam(value = "name", required = false) final String name,
        @RequestHeader(value = "Referer", required = false) final String referer,
        final RedirectAttributes redirectAttributes
    ) {
        LOGGER.info(
            "admin accept body type request id={} overrideName={}",
            requestId,
            LogSanitizer.forLog(name, LogSanitizer.MAX_LOG_NAME_CODE_POINTS)
        );
        final boolean accepted = bodyTypeRequestService.approvePendingRequest(
            requestId,
            name
        );
        return redirectBackToAdmin(
            referer,
            accepted ? "catalogAccepted" : "catalogAcceptError",
            redirectAttributes
        );
    }

    @RequestMapping(
        value = "/body-type-requests/{requestId}/reject",
        method = RequestMethod.POST
    )
    public ModelAndView rejectBodyTypeRequest(
        @PathVariable("requestId") final long requestId,
        @RequestHeader(value = "Referer", required = false) final String referer,
        final RedirectAttributes redirectAttributes
    ) {
        LOGGER.info("admin reject body type request id={}", requestId);
        final boolean rejected = bodyTypeRequestService.rejectPendingRequest(
            requestId
        );
        return redirectBackToAdmin(
            referer,
            rejected ? "catalogRejected" : "catalogError",
            redirectAttributes
        );
    }

    @RequestMapping(
        value = "/body-types/{bodyTypeId}",
        method = RequestMethod.POST
    )
    public ModelAndView updateBodyType(
        @PathVariable("bodyTypeId") final long bodyTypeId,
        @RequestParam("name") final String name,
        @RequestHeader(value = "Referer", required = false) final String referer
    ) {
        LOGGER.info(
            "admin update body type id={} name={}",
            bodyTypeId,
            LogSanitizer.forLog(name, LogSanitizer.MAX_LOG_NAME_CODE_POINTS)
        );
        bodyTypeService.updateBodyType(bodyTypeId, name);
        return redirectBackToCatalog(referer);
    }

    @RequestMapping(
        value = "/body-types/{bodyTypeId}/delete",
        method = RequestMethod.POST
    )
    public ModelAndView deleteBodyType(
        @PathVariable("bodyTypeId") final long bodyTypeId,
        @RequestHeader(value = "Referer", required = false) final String referer
    ) {
        LOGGER.info("admin delete body type id={}", bodyTypeId);
        bodyTypeService.deleteBodyType(bodyTypeId);
        return redirectBackAfterDelete(referer);
    }

    @RequestMapping(
        value = "/admin-requests/{requestId}/accept",
        method = RequestMethod.POST
    )
    public ModelAndView acceptAdminRequest(
        @PathVariable("requestId") final long requestId,
        @RequestHeader(value = "Referer", required = false) final String referer,
        final RedirectAttributes redirectAttributes
    ) {
        LOGGER.info("admin accept admin-role request id={}", requestId);
        final boolean accepted = adminRequestService.approvePendingRequest(
            requestId
        );
        return redirectBackToAdmin(
            referer,
            accepted ? "requestAccepted" : "requestError",
            redirectAttributes
        );
    }

    @RequestMapping(
        value = "/admin-requests/{requestId}/reject",
        method = RequestMethod.POST
    )
    public ModelAndView rejectAdminRequest(
        @PathVariable("requestId") final long requestId,
        @RequestHeader(value = "Referer", required = false) final String referer,
        final RedirectAttributes redirectAttributes
    ) {
        LOGGER.info("admin reject admin-role request id={}", requestId);
        final boolean rejected = adminRequestService.rejectPendingRequest(
            requestId
        );
        return redirectBackToAdmin(
            referer,
            rejected ? "requestRejected" : "requestError",
            redirectAttributes
        );
    }

    @RequestMapping(
        value = "/requests/{requestId}/image",
        method = RequestMethod.GET
    )
    public ResponseEntity<byte[]> getRequestImage(
        @PathVariable("requestId") final long requestId,
        @RequestHeader(
            value = "If-None-Match",
            required = false
        ) final String ifNoneMatch
    ) {
        final ImageMetadata metadata = carRequestService
            .getPrimaryCarRequestImageMetadata(requestId)
            .orElse(null);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        return getRequestImageResponse(
            requestId, metadata.getImageId(), metadata, ifNoneMatch
        );
    }

    @RequestMapping(
        value = "/requests/{requestId}/images/{imageId}",
        method = RequestMethod.GET
    )
    public ResponseEntity<byte[]> getRequestImageById(
        @PathVariable("requestId") final long requestId,
        @PathVariable("imageId") final long imageId,
        @RequestHeader(
            value = "If-None-Match",
            required = false
        ) final String ifNoneMatch
    ) {
        final ImageMetadata metadata = carRequestService
            .getCarRequestImageMetadataById(requestId, imageId)
            .orElse(null);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        return getRequestImageResponse(requestId, imageId, metadata, ifNoneMatch);
    }

    private ResponseEntity<byte[]> getRequestImageResponse(
        final long requestId,
        final long imageId,
        final ImageMetadata metadata,
        final String ifNoneMatch
    ) {
        final Object eTagStamp = metadata.getUpdatedAt() == null ? "0" : metadata.getUpdatedAt();
        final String eTag =
            "\"" + metadata.getImageId() + "-" + eTagStamp + "\"";
        final CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.HOURS)
            .cachePrivate()
            .mustRevalidate();
        final long lastModified = metadata.getUpdatedAt() == null
            ? 0L
            : metadata.getUpdatedAt()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        if (eTag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                .eTag(eTag)
                .cacheControl(cacheControl)
                .lastModified(lastModified)
                .build();
        }

        final StoredImagePayload requestImage = carRequestService
            .getCarRequestImageById(requestId, imageId)
            .orElse(null);
        if (requestImage == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
            .contentType(
                MediaType.parseMediaType(requestImage.getContentType())
            )
            .contentLength(requestImage.getImageData().length)
            .eTag(eTag)
            .cacheControl(cacheControl)
            .lastModified(lastModified)
            .body(requestImage.getImageData());
    }

    private ModelAndView carRequestFormPage(
        final CarRequest request,
        final CarForm carForm,
        final BindingResult errors,
        final String adminRedirect
    ) {
        return carRequestFormPage(
            request,
            carForm,
            errors,
            carRequestService.getCarRequestImages(request.getId()),
            adminRedirect
        );
    }

    private ModelAndView carRequestFormPage(
        final CarRequest request,
        final CarForm carForm,
        final BindingResult errors,
        final List<ImageMetadata> requestImages,
        final String adminRedirect
    ) {
        prepareCarFormContext(carForm, "review-request", null, request.getId());
        final ModelAndView mav = new ModelAndView("car-form.jsp");
        addCarFormBinding(mav, carForm, errors);
        mav.addObject("carFormMode", "review-request");
        mav.addObject("showCatalogRequestLinks", false);
        if (adminRedirect != null) {
            mav.addObject("adminRedirect", adminRedirect);
        }
        final List<Long> retainedImageIds = retainedImageIds(
            carForm.getRetainedImageIds(),
            imageIdsFrom(requestImages)
        );
        mav.addObject("existingImageIds", retainedImageIds);
        return mav;
    }

    private ModelAndView carEditFormPage(
        final Car car,
        final CarForm carForm,
        final BindingResult errors
    ) {
        prepareCarFormContext(carForm, "edit-car", car.getId(), null);
        final ModelAndView mav = new ModelAndView("car-form.jsp");
        addCarFormBinding(mav, carForm, errors);
        mav.addObject("carFormMode", "edit-car");
        mav.addObject("showCatalogRequestLinks", false);
        final List<Long> retainedImageIds = retainedImageIds(
            carForm.getRetainedImageIds(),
            buildCarImageIds(car)
        );
        mav.addObject("existingImageIds", retainedImageIds);
        return mav;
    }

    private void addCarFormBinding(
        final ModelAndView mav,
        final CarForm carForm,
        final BindingResult errors
    ) {
        mav.addObject("carForm", carForm);
        if (errors != null) {
            mav.addObject(BindingResult.MODEL_KEY_PREFIX + "carForm", errors);
        }
    }

    private void prepareCarFormContext(
        final CarForm carForm,
        final String formMode,
        final Long carId,
        final Long requestId
    ) {
        carForm.setFormMode(formMode);
        carForm.setCarId(carId);
        carForm.setRequestId(requestId);
    }

    private CarForm toForm(
        final CarRequest request,
        final List<ImageMetadata> requestImages
    ) {
        final CarForm form = new CarForm();
        brandService
            .findById(request.getBrandId())
            .map(Brand::getName)
            .ifPresent(form::setBrand);
        bodyTypeService
            .findById(request.getBodyTypeId())
            .map(BodyType::getName)
            .ifPresent(form::setBodyType);
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
        form.setRetainedImageIds(imageIdsFrom(requestImages));
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
        form.setRetainedImageIds(buildCarImageIds(car));
        return form;
    }

    private AdminCarRequestCard toCard(
        final CarRequest request,
        final Map<Long, Brand> brandsById,
        final Map<Long, BodyType> bodyTypesById,
        final Map<Long, User> usersById,
        final Map<Long, List<ImageMetadata>> requestImagesById
    ) {
        final Brand brand = brandsById.get(request.getBrandId());
        final BodyType bodyType = bodyTypesById.get(request.getBodyTypeId());
        final String brandName = brand == null ? null : brand.getName();
        final String bodyTypeName = bodyType == null ? null : bodyType.getName();
        final List<ImageMetadata> requestImages =
            requestImagesById.getOrDefault(request.getId(), List.of());
        final List<Long> imageIds = imageIdsFrom(requestImages);
        return new AdminCarRequestCard(
                request.getId(),
                brandName,
                request.getModel(),
                request.getYear(),
                bodyTypeName,
                request.getDescription(),
                submitterLabel(request, usersById),
                !imageIds.isEmpty(),
                imageIds.isEmpty() ? null : imageIds.get(0),
                request.getFuelType(),
                request.getHorsepower(),
                request.getAirbagCount(),
                request.getTransmission(),
                request.getFuelConsumption(),
                request.getMaxSpeedKmh(),
                request.getPriceUsd()
        );
    }

    private Map<Long, List<ImageMetadata>> fetchRequestImagesById(
        final List<CarRequest> requests
    ) {
        final List<Long> requestIds = requests
            .stream()
            .map(CarRequest::getId)
            .distinct()
            .toList();
        if (requestIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return carRequestService
            .getCarRequestImagesByRequestIds(requestIds)
            .stream()
            .collect(Collectors.groupingBy(ImageMetadata::getOwnerId));
    }

    private List<Long> buildRequestImageIds(final CarRequest request) {
        return imageIdsFrom(
            carRequestService.getCarRequestImages(request.getId())
        );
    }

    private List<Long> imageIdsFrom(final List<ImageMetadata> requestImages) {
        if (requestImages.isEmpty()) {
            return List.of();
        }
        return requestImages
            .stream()
            .map(ImageMetadata::getImageId)
            .collect(Collectors.toList());
    }

    private List<Long> buildCarImageIds(final Car car) {
        final List<ImageMetadata> carImages = carService.getCarImagesByCarId(
            car.getId()
        );
        if (!carImages.isEmpty()) {
            return carImages
                .stream()
                .map(ImageMetadata::getImageId)
                .collect(Collectors.toList());
        }
        if (!car.getHasImage()) {
            return List.of();
        }
        return List.of(CarService.LEGACY_IMAGE_ID);
    }

    private <T> Map<Long, User> fetchSubmitters(
        final List<T> requests,
        final Function<T, Long> userIdExtractor
    ) {
        final List<Long> userIds = requests
            .stream()
            .map(userIdExtractor)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userService
            .getUsersByIds(userIds)
            .stream()
            .collect(
                Collectors.toMap(
                    User::getId,
                    user -> user,
                    (existing, duplicate) -> existing
                )
            );
    }

    private String submitterLabel(
        final CarRequest request,
        final Map<Long, User> usersById
    ) {
        return adminRequestService.getSubmitterLabel(
            request.getSubmitterEmail(),
            request.getSubmittedByUserId(),
            usersById
        );
    }

    private String submitterLabel(
        final String submitterEmail,
        final Long submittedByUserId,
        final Map<Long, User> usersById
    ) {
        return adminRequestService.getSubmitterLabel(submitterEmail, submittedByUserId, usersById);
    }

    private AdminCatalogRequestCard toBrandCard(
        final BrandRequest request,
        final Map<Long, User> usersById
    ) {
        return new AdminCatalogRequestCard(
            request.getId(),
            request.getName(),
            submitterLabel(
                request.getSubmitterEmail(),
                request.getSubmittedByUserId(),
                usersById
            ),
            request.getComments()
        );
    }

    private AdminCatalogRequestCard toBodyTypeCard(
        final BodyTypeRequest request,
        final Map<Long, User> usersById
    ) {
        return new AdminCatalogRequestCard(
            request.getId(),
            request.getName(),
            submitterLabel(
                request.getSubmitterEmail(),
                request.getSubmittedByUserId(),
                usersById
            ),
            request.getComments()
        );
    }

    private AdminAdminRequestCard toAdminRequestCard(
        final AdminRequest request,
        final Map<Long, User> usersById
    ) {
        final User submitter = usersById.get(request.getSubmittedByUserId());
        final String username =
            submitter != null &&
            submitter.getUsername() != null &&
            !submitter.getUsername().isBlank()
                ? submitter.getUsername()
                : "Usuario sin identificar";
        final String label = username;
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
        return adminRequestService.resolveSubmitterEmail(
            request.getSubmitterEmail(),
            request.getSubmittedByUserId()
        );
    }

    private String validateUploadedImages(
        final List<MultipartFile> files,
        final boolean required,
        final int existingImageCount
    ) {
        final int totalImageCount = existingImageCount + files.size();
        if (totalImageCount == 0) {
            return required ? "validation.car.image.required" : null;
        }
        if (totalImageCount > MAX_IMAGE_COUNT) {
            return "validation.car.files.maxCount";
        }
        for (final MultipartFile file : files) {
            final String imageError =
                imageValidationService.validateUploadedImage(file, true);
            if (imageError != null) {
                return imageError;
            }
        }
        return null;
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

    private List<Long> resolveRetainedRequestImageIds(
        final CarRequest request,
        final CarForm carForm,
        final BindingResult errors
    ) {
        return resolveRetainedImageIds(
            carForm,
            buildRequestImageIds(request),
            errors
        );
    }

    private List<Long> resolveRetainedCarImageIds(
        final Car car,
        final CarForm carForm,
        final BindingResult errors
    ) {
        return resolveRetainedImageIds(carForm, buildCarImageIds(car), errors);
    }

    private List<Long> resolveRetainedImageIds(
        final CarForm carForm,
        final List<Long> availableImageIds,
        final BindingResult errors
    ) {
        final List<Long> submittedImageIds = carForm.getRetainedImageIds();
        final List<Long> retainedImageIds = retainedImageIds(
            submittedImageIds,
            availableImageIds
        );
        carForm.setRetainedImageIds(retainedImageIds);
        if (hasUnknownRetainedImageIds(submittedImageIds, availableImageIds)) {
            errors.rejectValue(
                "files",
                "validation.car.image.preloaded.invalid"
            );
        }
        return retainedImageIds;
    }

    private List<Long> retainedImageIds(
        final List<Long> submittedImageIds,
        final List<Long> availableImageIds
    ) {
        if (
            submittedImageIds == null ||
            submittedImageIds.isEmpty() ||
            availableImageIds.isEmpty()
        ) {
            return List.of();
        }
        final Set<Long> available = new LinkedHashSet<>(availableImageIds);
        final Set<Long> retained = new LinkedHashSet<>();
        for (final Long imageId : submittedImageIds) {
            if (imageId != null && available.contains(imageId)) {
                retained.add(imageId);
            }
        }
        return new ArrayList<>(retained);
    }

    private boolean hasUnknownRetainedImageIds(
        final List<Long> submittedImageIds,
        final List<Long> availableImageIds
    ) {
        if (submittedImageIds == null || submittedImageIds.isEmpty()) {
            return false;
        }
        final Set<Long> available = new LinkedHashSet<>(availableImageIds);
        return submittedImageIds
            .stream()
            .filter(Objects::nonNull)
            .anyMatch(imageId -> !available.contains(imageId));
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

    private void rejectInvalidSpecFields(
        final BindingResult errors,
        final CarForm carForm
    ) {
        if (
            carForm.getFuelType() != null &&
            !CarSearchCriteria.ALLOWED_FUEL_TYPES.contains(
                ControllerUtils.normalizeSpecValue(carForm.getFuelType())
            )
        ) {
            errors.rejectValue(
                "fuelType",
                "validation.car.fuelType.invalid"
            );
        }
        if (
            carForm.getTransmission() != null &&
            !CarSearchCriteria.ALLOWED_TRANSMISSIONS.contains(
                ControllerUtils.normalizeSpecValue(carForm.getTransmission())
            )
        ) {
            errors.rejectValue(
                "transmission",
                "validation.car.transmission.invalid"
            );
        }
    }

    private ModelAndView redirectBackToCatalog(final String referer) {
        return redirectBackToCatalog(referer, true);
    }

    private ModelAndView redirectBackAfterDelete(final String referer) {
        return redirectBackToCatalog(referer, false);
    }

    private ModelAndView redirectBackToAdmin(
        final String referer,
        final String outcomeKey,
        final RedirectAttributes redirectAttributes
    ) {
        addAdminToast(redirectAttributes, outcomeKey);
        final String target = LoginRedirectUtils
            .safeRefererPath(referer, ControllerUtils.currentContextPath())
            .filter(path -> "/admin".equals(ControllerUtils.pathWithoutQuery(path)))
            .orElse("/admin");
        return new ModelAndView("redirect:" + target);
    }

    private void addAdminToast(final RedirectAttributes redirectAttributes, final String outcomeKey) {
        final String code = ADMIN_TOAST_CODES.get(outcomeKey);
        if (code == null) {
            return;
        }
        redirectAttributes.addFlashAttribute(ACTION_TOAST_ATTRIBUTE, code);
        if (ADMIN_TOAST_ERROR_KEYS.contains(outcomeKey)) {
            redirectAttributes.addFlashAttribute(ACTION_TOAST_TYPE_ATTRIBUTE, TOAST_TYPE_ERROR);
        }
    }

    private ModelAndView redirectBackToCatalog(
        final String referer,
        final boolean allowReviewsPage
    ) {
        final String fallback = "redirect:/cars";
        final String target = LoginRedirectUtils
            .safeRefererPath(referer, ControllerUtils.currentContextPath())
            .orElse(null);
        if (target == null) {
            return new ModelAndView(fallback);
        }
        final String path = ControllerUtils.pathWithoutQuery(target);
        if (
            "/".equals(path) ||
            "/cars".equals(path) ||
            (allowReviewsPage && path.matches("/reviews/car/\\d+"))
        ) {
            return new ModelAndView("redirect:" + target);
        }
        return new ModelAndView(fallback);
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

    public static final class AdminCarRequestCard {

        private final long id;
        private final String brandName;
        private final String model;
        private final Integer year;
        private final String bodyTypeName;
        private final String description;
        private final String submitter;
        private final boolean hasImage;
        private final Long heroImageId;
        private final String fuelType;
        private final Integer horsepower;
        private final Integer airbagCount;
        private final String transmission;
        private final BigDecimal fuelConsumption;
        private final Integer maxSpeedKmh;
        private final BigDecimal priceUsd;

        private AdminCarRequestCard(
            final long id,
            final String brandName,
            final String model,
            final Integer year,
            final String bodyTypeName,
            final String description,
            final String submitter,
            final boolean hasImage,
            final Long heroImageId,
            final String fuelType,
            final Integer horsepower,
            final Integer airbagCount,
            final String transmission,
            final BigDecimal fuelConsumption,
            final Integer maxSpeedKmh,
            final BigDecimal priceUsd
        ) {
            this.id = id;
            this.brandName = brandName;
            this.model = model;
            this.year = year;
            this.bodyTypeName = bodyTypeName;
            this.description = description;
            this.submitter = submitter;
            this.hasImage = hasImage;
            this.heroImageId = heroImageId;
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

        public Long getHeroImageId() {
            return heroImageId;
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

        private AdminCatalogRequestCard(
            final long id,
            final String name,
            final String submitter,
            final String comments
        ) {
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

        private AdminAdminRequestCard(
            final long id,
            final long userId,
            final String username,
            final String label,
            final String motivation,
            final String bio,
            final String justification
        ) {
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
