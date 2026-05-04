package ar.edu.itba.paw.webapp.validation;

import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.webapp.form.CarForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;

@Component
public class CarFormValidator implements Validator {

    @Override
    public boolean supports(final Class<?> clazz) {
        return CarForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        final CarForm carForm = (CarForm) target;
        if (!errors.hasFieldErrors("fuelType")
                && !CarSearchCriteria.ALLOWED_FUEL_TYPES.contains(normalizeSpecValue(carForm.getFuelType()))) {
            errors.rejectValue("fuelType", "fuelType.invalid");
        }
        if (!errors.hasFieldErrors("transmission")
                && !CarSearchCriteria.ALLOWED_TRANSMISSIONS.contains(normalizeSpecValue(carForm.getTransmission()))) {
            errors.rejectValue("transmission", "transmission.invalid");
        }
    }

    private String normalizeSpecValue(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT);
    }
}
