package ar.edu.itba.paw.webapp.validation;

import ar.edu.itba.paw.model.ReviewOwnershipStatus;
import ar.edu.itba.paw.webapp.form.ReviewForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

@Component
public class ReviewFormValidator implements Validator {

    private static final BigDecimal RATING_STEP_DOUBLED = BigDecimal.valueOf(2);
    @Override
    public boolean supports(final Class<?> clazz) {
        return ReviewForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        final ReviewForm form = (ReviewForm) target;
        final BigDecimal rating = form.getRating();
        if (rating != null && rating.multiply(RATING_STEP_DOUBLED).remainder(BigDecimal.ONE).signum() != 0) {
            errors.rejectValue("rating", "rating.step");
        }

        final String ownership = form.getOwnershipStatus() == null ? "" : form.getOwnershipStatus();
        if (!ReviewOwnershipStatus.isValid(ownership)) {
            errors.rejectValue("ownershipStatus", "ownership.invalid");
        }
    }
}
