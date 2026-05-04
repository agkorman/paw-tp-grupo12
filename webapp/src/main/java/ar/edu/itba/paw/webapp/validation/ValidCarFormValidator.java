package ar.edu.itba.paw.webapp.validation;

import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.services.BrandService;
import ar.edu.itba.paw.services.CarRequestService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.webapp.form.CarForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidCarFormValidator implements ConstraintValidator<ValidCarForm, CarForm> {

    private static final String MODE_EDIT_CAR = "edit-car";
    private static final String MODE_REVIEW_REQUEST = "review-request";

    private final BrandService brandService;
    private final BodyTypeService bodyTypeService;
    private final CarService carService;
    private final CarRequestService carRequestService;

    @Autowired
    public ValidCarFormValidator(final BrandService brandService, final BodyTypeService bodyTypeService,
                                 final CarService carService, final CarRequestService carRequestService) {
        this.brandService = brandService;
        this.bodyTypeService = bodyTypeService;
        this.carService = carService;
        this.carRequestService = carRequestService;
    }

    @Override
    public boolean isValid(final CarForm form, final ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }
        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (form.getFuelType() != null
                && !CarSearchCriteria.ALLOWED_FUEL_TYPES.contains(normalize(form.getFuelType()))) {
            valid = violation(context, "fuelType", "{fuelType.invalid}");
        }
        if (form.getTransmission() != null
                && !CarSearchCriteria.ALLOWED_TRANSMISSIONS.contains(normalize(form.getTransmission()))) {
            valid = violation(context, "transmission", "{transmission.invalid}");
        }

        final boolean brandValid = form.getBrand() == null || brandService.findByName(form.getBrand()).isPresent();
        if (!brandValid) {
            valid = violation(context, "brand", "{brand.invalid}");
        }
        final boolean bodyTypeValid = form.getBodyType() == null || bodyTypeService.findByName(form.getBodyType()).isPresent();
        if (!bodyTypeValid) {
            valid = violation(context, "bodyType", "{bodyType.invalid}");
        }

        final FormContext formContext = currentFormContext(form);
        if (brandValid && bodyTypeValid && form.getBrand() != null && form.getBodyType() != null
                && form.getModel() != null && carService.existsDuplicateCar(
                form.getBrand(), form.getBodyType(), form.getModel(), form.getYear(), formContext.duplicateExcludedCarId())) {
            valid = violation(context, null, "{car.duplicate}");
        }

        final List<MultipartFile> files = selectedImageFiles(form.getFiles());
        final List<Long> availableImageIds = availableImageIds(formContext);
        final int retainedCount = retainedImageIds(form, availableImageIds).size();
        if (retainedCount + files.size() == 0) {
            valid = violation(context, "files", "{validation.image.required}");
        }
        if (retainedCount + files.size() > UploadedImageValidation.MAX_IMAGE_COUNT) {
            valid = violation(context, "files", "{validation.image.maxCount}");
        }
        if (hasUnknownRetainedImageIds(form.getRetainedImageIds(), availableImageIds)) {
            valid = violation(context, "files", "{validation.image.retained.invalid}");
        }
        for (final MultipartFile file : files) {
            final String error = UploadedImageValidation.validate(file);
            if (error != null) {
                valid = violation(context, "files", error);
                break;
            }
        }
        return valid;
    }

    private List<Long> availableImageIds(final FormContext formContext) {
        if (MODE_REVIEW_REQUEST.equals(formContext.mode) && formContext.id != null) {
            return carRequestService.getCarRequestById(formContext.id)
                    .filter(request -> CarRequestService.STATUS_PENDING.equals(request.getStatus()))
                    .map(this::requestImageIds)
                    .orElseGet(Collections::emptyList);
        }
        if (MODE_EDIT_CAR.equals(formContext.mode) && formContext.id != null) {
            return carService.getCarById(formContext.id)
                    .map(car -> carService.getCarImagesByCarId(car.getId()).stream()
                            .map(image -> image.getImageId())
                            .collect(Collectors.toList()))
                    .orElseGet(Collections::emptyList);
        }
        return Collections.emptyList();
    }

    private FormContext currentFormContext(final CarForm form) {
        final String rawMode = form.getFormMode();
        final String mode = rawMode == null ? ""
                : rawMode.trim().toLowerCase(Locale.ROOT);
        if (MODE_REVIEW_REQUEST.equals(mode) && form.getRequestId() != null) {
            return new FormContext(MODE_REVIEW_REQUEST, form.getRequestId());
        }
        if (MODE_EDIT_CAR.equals(mode) && form.getCarId() != null) {
            return new FormContext(MODE_EDIT_CAR, form.getCarId());
        }
        return new FormContext("create", null);
    }

    private List<Long> requestImageIds(final CarRequest request) {
        return carRequestService.getCarRequestImages(request.getId()).stream()
                .map(image -> image.getImageId())
                .collect(Collectors.toList());
    }

    private List<Long> retainedImageIds(final CarForm form, final List<Long> availableImageIds) {
        if (form.getRetainedImageIds() == null || availableImageIds.isEmpty()) {
            return Collections.emptyList();
        }
        final Set<Long> available = new LinkedHashSet<>(availableImageIds);
        return form.getRetainedImageIds().stream()
                .filter(Objects::nonNull)
                .filter(available::contains)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean hasUnknownRetainedImageIds(final List<Long> submittedImageIds, final List<Long> availableImageIds) {
        if (submittedImageIds == null || submittedImageIds.isEmpty()) {
            return false;
        }
        final Set<Long> available = new LinkedHashSet<>(availableImageIds);
        return submittedImageIds.stream().filter(Objects::nonNull).anyMatch(imageId -> !available.contains(imageId));
    }

    private List<MultipartFile> selectedImageFiles(final List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        return files.stream().filter(file -> file != null && !file.isEmpty()).collect(Collectors.toList());
    }

    private String normalize(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT);
    }

    private boolean violation(final ConstraintValidatorContext context, final String field, final String message) {
        final ConstraintValidatorContext.ConstraintViolationBuilder builder =
                context.buildConstraintViolationWithTemplate(message);
        if (field == null) {
            builder.addConstraintViolation();
        } else {
            builder.addPropertyNode(field).addConstraintViolation();
        }
        return false;
    }

    private static final class FormContext {
        private final String mode;
        private final Long id;

        private FormContext(final String mode, final Long id) {
            this.mode = mode;
            this.id = id;
        }

        private long duplicateExcludedCarId() {
            return MODE_EDIT_CAR.equals(mode) && id != null ? id : -1L;
        }
    }
}
