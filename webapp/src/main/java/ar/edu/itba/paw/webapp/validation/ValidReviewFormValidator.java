package ar.edu.itba.paw.webapp.validation;

import ar.edu.itba.paw.services.ReviewTagService;
import ar.edu.itba.paw.services.exception.InvalidReviewTagSelectionException;
import ar.edu.itba.paw.webapp.form.ReviewForm;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.util.Set;

public class ValidReviewFormValidator implements ConstraintValidator<ValidReviewForm, ReviewForm> {

    private static final BigDecimal RATING_STEP_DOUBLED = BigDecimal.valueOf(2);
    private static final Set<String> ALLOWED_OWNERSHIP_STATUSES =
            Set.of("", "Propietario actual", "Ex propietario");

    private final ReviewTagService reviewTagService;

    @Autowired
    public ValidReviewFormValidator(final ReviewTagService reviewTagService) {
        this.reviewTagService = reviewTagService;
    }

    @Override
    public boolean isValid(final ReviewForm form, final ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }
        boolean valid = true;
        context.disableDefaultConstraintViolation();

        final BigDecimal rating = form.getRating();
        if (rating != null && rating.multiply(RATING_STEP_DOUBLED).remainder(BigDecimal.ONE).signum() != 0) {
            valid = violation(context, "rating", "{rating.step}");
        }
        final String ownership = form.getOwnershipStatus() == null ? "" : form.getOwnershipStatus();
        if (!ALLOWED_OWNERSHIP_STATUSES.contains(ownership)) {
            valid = violation(context, "ownershipStatus", "{ownership.invalid}");
        }
        try {
            reviewTagService.validateSelection(form.getTagIds());
        } catch (final InvalidReviewTagSelectionException e) {
            valid = violation(context, "tagIds", "{tagIds.invalid}");
        }
        return valid;
    }

    private boolean violation(final ConstraintValidatorContext context, final String field, final String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
        return false;
    }
}
