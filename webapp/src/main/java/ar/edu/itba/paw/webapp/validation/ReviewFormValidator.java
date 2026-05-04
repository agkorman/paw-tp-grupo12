package ar.edu.itba.paw.webapp.validation;

import ar.edu.itba.paw.webapp.form.ReviewForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class ReviewFormValidator implements Validator {

    private static final BigDecimal RATING_STEP_DOUBLED = BigDecimal.valueOf(2);
    private static final Set<String> ALLOWED_OWNERSHIP_STATUSES =
            Set.of("", "Propietario actual", "Ex propietario");

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
        if (!ALLOWED_OWNERSHIP_STATUSES.contains(ownership)) {
            errors.rejectValue("ownershipStatus", "ownership.invalid");
        }
    }
}
