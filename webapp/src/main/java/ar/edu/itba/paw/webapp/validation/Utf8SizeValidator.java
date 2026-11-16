package ar.edu.itba.paw.webapp.validation;

import java.nio.charset.StandardCharsets;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/** BCrypt limits input in bytes, not Java characters. */
public class Utf8SizeValidator implements ConstraintValidator<Utf8Size, String> {
    private int max;

    @Override public void initialize(final Utf8Size constraint) { max = constraint.max(); }
    @Override public boolean isValid(final String value, final ConstraintValidatorContext context) {
        return value == null || value.getBytes(StandardCharsets.UTF_8).length <= max;
    }
}
