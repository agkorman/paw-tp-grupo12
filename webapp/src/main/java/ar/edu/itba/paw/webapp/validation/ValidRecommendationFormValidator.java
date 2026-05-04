package ar.edu.itba.paw.webapp.validation;

import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.webapp.form.RecommendationForm;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidRecommendationFormValidator implements ConstraintValidator<ValidRecommendationForm, RecommendationForm> {

    private final BodyTypeService bodyTypeService;

    @Autowired
    public ValidRecommendationFormValidator(final BodyTypeService bodyTypeService) {
        this.bodyTypeService = bodyTypeService;
    }

    @Override
    public boolean isValid(final RecommendationForm form, final ConstraintValidatorContext context) {
        if (form == null || form.getBodyType() == null || form.getBodyType().trim().isEmpty()
                || bodyTypeService.existsByName(form.getBodyType())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("{recommend.bodyType.invalid}")
                .addPropertyNode("bodyType")
                .addConstraintViolation();
        return false;
    }
}
