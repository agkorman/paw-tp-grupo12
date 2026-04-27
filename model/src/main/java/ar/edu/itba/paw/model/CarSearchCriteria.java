package ar.edu.itba.paw.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CarSearchCriteria {

    public static final Set<String> ALLOWED_FUEL_TYPES = Set.of("combustion", "hybrid", "electric");
    public static final Set<String> ALLOWED_TRANSMISSIONS = Set.of("manual", "automatic");
    public static final Set<String> ALLOWED_SORT_BY = Set.of("name_asc", "hp_desc", "hp_asc", "speed_desc", "consumption_asc", "price_asc", "price_desc");
    private static final Set<Integer> ALLOWED_AIRBAG_MIN_VALUES = Set.of(2, 4, 6, 8, 10);
    private static final int HORSEPOWER_MIN_BOUND = 0;
    private static final int HORSEPOWER_MAX_BOUND = 1500;
    private static final int MAX_SPEED_MIN_BOUND = 0;
    private static final int MAX_SPEED_MAX_BOUND = 500;
    private static final int YEAR_MIN_BOUND = 1886;
    private static final int YEAR_MAX_BOUND = 2100;
    private static final BigDecimal FUEL_CONSUMPTION_MIN_BOUND = BigDecimal.ZERO;
    private static final BigDecimal FUEL_CONSUMPTION_MAX_BOUND = BigDecimal.valueOf(30);
    private static final BigDecimal PRICE_MIN_BOUND = BigDecimal.ZERO;
    private static final BigDecimal PRICE_MAX_BOUND = BigDecimal.valueOf(5_000_000);

    private String q;
    private String brand;
    private String bodyType;
    private Integer year;
    private List<String> fuelTypes = new ArrayList<>();
    private Integer horsepowerMin;
    private Integer horsepowerMax;
    private Integer airbagMin;
    private String transmission;
    private BigDecimal fuelConsumptionMax;
    private Integer maxSpeedMin;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private String sortBy;
    private Integer page;

    public CarSearchCriteria() {}

    public boolean hasAdvancedFilters() {
        return !fuelTypes.isEmpty()
                || horsepowerMin != null
                || horsepowerMax != null
                || year != null
                || airbagMin != null
                || transmission != null && !transmission.isEmpty()
                || fuelConsumptionMax != null
                || maxSpeedMin != null
                || priceMin != null
                || priceMax != null;
    }

    public boolean isValid() {
        if (!ALLOWED_FUEL_TYPES.containsAll(fuelTypes)) {
            return false;
        }
        if (transmission != null && !ALLOWED_TRANSMISSIONS.contains(transmission)) {
            return false;
        }
        if (horsepowerMin != null && !isWithinBounds(horsepowerMin, HORSEPOWER_MIN_BOUND, HORSEPOWER_MAX_BOUND)) {
            return false;
        }
        if (horsepowerMax != null && !isWithinBounds(horsepowerMax, HORSEPOWER_MIN_BOUND, HORSEPOWER_MAX_BOUND)) {
            return false;
        }
        if (horsepowerMin != null && horsepowerMax != null && horsepowerMin > horsepowerMax) {
            return false;
        }
        if (year != null && !isWithinBounds(year, YEAR_MIN_BOUND, YEAR_MAX_BOUND)) {
            return false;
        }
        if (airbagMin != null && !ALLOWED_AIRBAG_MIN_VALUES.contains(airbagMin)) {
            return false;
        }
        if (fuelConsumptionMax != null && !isWithinBounds(fuelConsumptionMax, FUEL_CONSUMPTION_MIN_BOUND, FUEL_CONSUMPTION_MAX_BOUND)) {
            return false;
        }
        if (maxSpeedMin != null && !isWithinBounds(maxSpeedMin, MAX_SPEED_MIN_BOUND, MAX_SPEED_MAX_BOUND)) {
            return false;
        }
        if (priceMin != null && !isWithinBounds(priceMin, PRICE_MIN_BOUND, PRICE_MAX_BOUND)) {
            return false;
        }
        if (priceMax != null && !isWithinBounds(priceMax, PRICE_MIN_BOUND, PRICE_MAX_BOUND)) {
            return false;
        }
        if (priceMin != null && priceMax != null && priceMin.compareTo(priceMax) > 0) {
            return false;
        }
        return sortBy == null || ALLOWED_SORT_BY.contains(sortBy);
    }

    private boolean isWithinBounds(final int value, final int min, final int max) {
        return value >= min && value <= max;
    }

    private boolean isWithinBounds(final BigDecimal value, final BigDecimal min, final BigDecimal max) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    public String getQ() {
        return q;
    }

    public void setQ(final String q) {
        this.q = q == null || q.trim().isEmpty() ? null : q.trim();
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(final String brand) {
        this.brand = brand == null || brand.trim().isEmpty() ? null : brand.trim();
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(final String bodyType) {
        this.bodyType = bodyType == null || bodyType.trim().isEmpty() ? null : bodyType.trim();
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(final Integer year) {
        this.year = year;
    }

    public String getFuelType() {
        return fuelTypes.isEmpty() ? null : String.join(",", fuelTypes);
    }

    public void setFuelType(final String fuelType) {
        fuelTypes = new ArrayList<>();
        if (fuelType == null || fuelType.trim().isEmpty()) {
            return;
        }
        for (final String value : fuelType.split(",")) {
            final String normalized = normalizeFuelType(value);
            if (normalized != null && !fuelTypes.contains(normalized)) {
                fuelTypes.add(normalized);
            }
        }
    }

    public List<String> getFuelTypes() {
        return Collections.unmodifiableList(fuelTypes);
    }

    public void setFuelTypes(final List<String> fuelTypes) {
        this.fuelTypes = normalizeFuelTypes(fuelTypes);
    }

    public void setFuelTypes(final String[] fuelTypes) {
        this.fuelTypes = normalizeFuelTypes(fuelTypes == null ? null : Arrays.asList(fuelTypes));
    }

    private List<String> normalizeFuelTypes(final List<String> values) {
        final List<String> normalizedValues = new ArrayList<>();
        if (values == null) {
            return normalizedValues;
        }
        for (final String value : values) {
            if (value == null) {
                continue;
            }
            for (final String part : value.split(",")) {
                final String normalized = normalizeFuelType(part);
                if (normalized != null && !normalizedValues.contains(normalized)) {
                    normalizedValues.add(normalized);
                }
            }
        }
        return normalizedValues;
    }

    private String normalizeFuelType(final String fuelType) {
        return fuelType == null || fuelType.trim().isEmpty() ? null : fuelType.trim().toLowerCase(Locale.ROOT);
    }

    public Integer getHorsepowerMin() {
        return horsepowerMin;
    }

    public void setHorsepowerMin(final Integer horsepowerMin) {
        this.horsepowerMin = horsepowerMin;
    }

    public Integer getHorsepowerMax() {
        return horsepowerMax;
    }

    public void setHorsepowerMax(final Integer horsepowerMax) {
        this.horsepowerMax = horsepowerMax;
    }

    public Integer getAirbagMin() {
        return airbagMin;
    }

    public void setAirbagMin(final Integer airbagMin) {
        this.airbagMin = airbagMin;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(final String transmission) {
        this.transmission = transmission == null || transmission.trim().isEmpty() ? null : transmission.trim().toLowerCase(Locale.ROOT);
    }

    public BigDecimal getFuelConsumptionMax() {
        return fuelConsumptionMax;
    }

    public void setFuelConsumptionMax(final BigDecimal fuelConsumptionMax) {
        this.fuelConsumptionMax = fuelConsumptionMax;
    }

    public Integer getMaxSpeedMin() {
        return maxSpeedMin;
    }

    public void setMaxSpeedMin(final Integer maxSpeedMin) {
        this.maxSpeedMin = maxSpeedMin;
    }

    public BigDecimal getPriceMin() {
        return priceMin;
    }

    public void setPriceMin(final BigDecimal priceMin) {
        this.priceMin = priceMin;
    }

    public BigDecimal getPriceMax() {
        return priceMax;
    }

    public void setPriceMax(final BigDecimal priceMax) {
        this.priceMax = priceMax;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(final String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty() || "name_desc".equals(sortBy.trim())) {
            this.sortBy = null;
        } else {
            this.sortBy = sortBy.trim();
        }
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(final Integer page) {
        this.page = page;
    }
}
