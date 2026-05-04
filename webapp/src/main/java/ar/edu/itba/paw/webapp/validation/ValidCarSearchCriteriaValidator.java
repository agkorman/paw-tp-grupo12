package ar.edu.itba.paw.webapp.validation;

import ar.edu.itba.paw.model.CarSearchCriteria;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.util.Set;

public class ValidCarSearchCriteriaValidator implements ConstraintValidator<ValidCarSearchCriteria, CarSearchCriteria> {

    private static final Set<Integer> ALLOWED_AIRBAG_MIN_VALUES = Set.of(2, 4, 6, 8, 10);
    private static final int HORSEPOWER_MIN_BOUND = 50;
    private static final int HORSEPOWER_MAX_BOUND = 800;
    private static final int MAX_SPEED_MIN_BOUND = 0;
    private static final int MAX_SPEED_MAX_BOUND = 500;
    private static final int YEAR_MIN_BOUND = 1950;
    private static final int YEAR_MAX_BOUND = 2100;
    private static final BigDecimal FUEL_CONSUMPTION_MIN_BOUND = BigDecimal.ZERO;
    private static final BigDecimal FUEL_CONSUMPTION_MAX_BOUND = BigDecimal.valueOf(30);
    private static final BigDecimal PRICE_MIN_BOUND = BigDecimal.valueOf(10_000);
    private static final BigDecimal PRICE_MAX_BOUND = BigDecimal.valueOf(5_000_000);

    @Override
    public boolean isValid(final CarSearchCriteria criteria, final ConstraintValidatorContext context) {
        if (criteria == null) {
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (!CarSearchCriteria.ALLOWED_FUEL_TYPES.containsAll(criteria.getFuelTypes())) {
            valid = violation(context, "fuelTypes", "{validation.carSearch.fuelType.invalid}");
        }
        if (criteria.getTransmission() != null
                && !CarSearchCriteria.ALLOWED_TRANSMISSIONS.contains(criteria.getTransmission())) {
            valid = violation(context, "transmission", "{validation.carSearch.transmission.invalid}");
        }
        if (!withinBounds(criteria.getHorsepowerMin(), HORSEPOWER_MIN_BOUND, HORSEPOWER_MAX_BOUND)) {
            valid = violation(context, "horsepowerMin", "{validation.carSearch.horsepower.range}");
        }
        if (!withinBounds(criteria.getHorsepowerMax(), HORSEPOWER_MIN_BOUND, HORSEPOWER_MAX_BOUND)) {
            valid = violation(context, "horsepowerMax", "{validation.carSearch.horsepower.range}");
        }
        if (criteria.getHorsepowerMin() != null && criteria.getHorsepowerMax() != null
                && criteria.getHorsepowerMin() > criteria.getHorsepowerMax()) {
            valid = violation(context, "horsepowerMin", "{validation.carSearch.horsepower.order}");
        }
        if (!withinBounds(criteria.getYearMin(), YEAR_MIN_BOUND, YEAR_MAX_BOUND)) {
            valid = violation(context, "yearMin", "{validation.carSearch.year.range}");
        }
        if (!withinBounds(criteria.getYearMax(), YEAR_MIN_BOUND, YEAR_MAX_BOUND)) {
            valid = violation(context, "yearMax", "{validation.carSearch.year.range}");
        }
        if (criteria.getYearMin() != null && criteria.getYearMax() != null
                && criteria.getYearMin() > criteria.getYearMax()) {
            valid = violation(context, "yearMin", "{validation.carSearch.year.order}");
        }
        if (criteria.getAirbagMin() != null && !ALLOWED_AIRBAG_MIN_VALUES.contains(criteria.getAirbagMin())) {
            valid = violation(context, "airbagMin", "{validation.carSearch.airbag.invalid}");
        }
        if (!withinBounds(criteria.getFuelConsumptionMax(), FUEL_CONSUMPTION_MIN_BOUND, FUEL_CONSUMPTION_MAX_BOUND)) {
            valid = violation(context, "fuelConsumptionMax", "{validation.carSearch.fuelConsumption.range}");
        }
        if (!withinBounds(criteria.getMaxSpeedMin(), MAX_SPEED_MIN_BOUND, MAX_SPEED_MAX_BOUND)) {
            valid = violation(context, "maxSpeedMin", "{validation.carSearch.maxSpeed.range}");
        }
        if (!withinBounds(criteria.getPriceMin(), PRICE_MIN_BOUND, PRICE_MAX_BOUND)) {
            valid = violation(context, "priceMin", "{validation.carSearch.price.range}");
        }
        if (!withinBounds(criteria.getPriceMax(), PRICE_MIN_BOUND, PRICE_MAX_BOUND)) {
            valid = violation(context, "priceMax", "{validation.carSearch.price.range}");
        }
        if (criteria.getPriceMin() != null && criteria.getPriceMax() != null
                && criteria.getPriceMin().compareTo(criteria.getPriceMax()) > 0) {
            valid = violation(context, "priceMin", "{validation.carSearch.price.order}");
        }
        if (criteria.getSortBy() != null && !CarSearchCriteria.ALLOWED_SORT_BY.contains(criteria.getSortBy())) {
            valid = violation(context, "sortBy", "{validation.carSearch.sort.invalid}");
        }

        return valid;
    }

    private boolean withinBounds(final Integer value, final int min, final int max) {
        return value == null || value >= min && value <= max;
    }

    private boolean withinBounds(final BigDecimal value, final BigDecimal min, final BigDecimal max) {
        return value == null || value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    private boolean violation(final ConstraintValidatorContext context, final String property, final String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(property)
                .addConstraintViolation();
        return false;
    }
}
