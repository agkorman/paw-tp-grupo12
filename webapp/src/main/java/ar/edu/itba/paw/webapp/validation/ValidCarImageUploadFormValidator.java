package ar.edu.itba.paw.webapp.validation;

import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.webapp.form.CarImageUploadForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ValidCarImageUploadFormValidator implements ConstraintValidator<ValidCarImageUploadForm, CarImageUploadForm> {

    private final CarService carService;

    @Autowired
    public ValidCarImageUploadFormValidator(final CarService carService) {
        this.carService = carService;
    }

    @Override
    public boolean isValid(final CarImageUploadForm form, final ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }
        boolean valid = true;
        context.disableDefaultConstraintViolation();
        if (form.getCarId() == null || carService.getCarById(form.getCarId()).isEmpty()) {
            valid = violation(context, "carId", "{validation.car.notFound}");
        }
        final List<MultipartFile> files = selectedImageFiles(form.getFiles());
        if (files.isEmpty()) {
            valid = violation(context, "files", "{validation.image.required}");
        }
        if (files.size() > UploadedImageValidation.MAX_IMAGE_COUNT) {
            valid = violation(context, "files", "{validation.image.maxCount}");
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

    private List<MultipartFile> selectedImageFiles(final List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        return files.stream().filter(file -> file != null && !file.isEmpty()).collect(Collectors.toList());
    }

    private boolean violation(final ConstraintValidatorContext context, final String field, final String message) {
        context.buildConstraintViolationWithTemplate(message).addPropertyNode(field).addConstraintViolation();
        return false;
    }
}
