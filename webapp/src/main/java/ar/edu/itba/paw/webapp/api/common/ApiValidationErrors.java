package ar.edu.itba.paw.webapp.api.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.constraints.*;

/** Explicit constraint/parameter allowlist; never serializes messages, invalid values or arbitrary attributes. */
final class ApiValidationErrors {

    private ApiValidationErrors() {
    }

    static List<ApiFieldError> fields(final Set<? extends ConstraintViolation<?>> violations, final Method method) {
        final List<ApiFieldError> fields = new ArrayList<>();
        for (final ConstraintViolation<?> violation : violations) {
            final String field = ApiJsonFields.validationField(violation.getPropertyPath(), method);
            if (field != null) {
                fields.add(fieldError(field, violation.getConstraintDescriptor().getAnnotation()));
            }
        }
        fields.sort(Comparator.comparing(ApiFieldError::getField).thenComparing(ApiFieldError::getCode));
        return fields;
    }

    private static ApiFieldError fieldError(final String field, final Annotation constraint) {
        final Map<String, Object> params = new LinkedHashMap<>();
        final String code;
        if (constraint instanceof NotNull || constraint instanceof NotBlank || constraint instanceof NotEmpty) {
            code = "REQUIRED";
        } else if (constraint instanceof Size size) {
            code = "SIZE";
            params.put("min", size.min());
            params.put("max", size.max());
        } else if (constraint instanceof Min min) {
            code = "OUT_OF_RANGE";
            params.put("min", min.value());
        } else if (constraint instanceof Max max) {
            code = "OUT_OF_RANGE";
            params.put("max", max.value());
        } else if (constraint instanceof DecimalMin min) {
            code = "OUT_OF_RANGE";
            params.put("min", new BigDecimal(min.value()));
            params.put("inclusive", min.inclusive());
        } else if (constraint instanceof DecimalMax max) {
            code = "OUT_OF_RANGE";
            params.put("max", new BigDecimal(max.value()));
            params.put("inclusive", max.inclusive());
        } else if (constraint instanceof Positive || constraint instanceof PositiveOrZero) {
            code = "OUT_OF_RANGE";
            params.put("min", 0);
            params.put("inclusive", constraint instanceof PositiveOrZero);
        } else if (constraint instanceof Negative || constraint instanceof NegativeOrZero) {
            code = "OUT_OF_RANGE";
            params.put("max", 0);
            params.put("inclusive", constraint instanceof NegativeOrZero);
        } else if (constraint instanceof Pattern || constraint instanceof Email || constraint instanceof Digits) {
            code = "INVALID_FORMAT";
        } else {
            code = "INVALID_VALUE";
        }
        return new ApiFieldError(field, code, params);
    }
}
