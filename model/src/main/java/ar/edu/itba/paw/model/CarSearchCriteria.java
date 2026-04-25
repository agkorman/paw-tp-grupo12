package ar.edu.itba.paw.model;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;

public class CarSearchCriteria {

    public static final Set<String> ALLOWED_FUEL_TYPES = Set.of("combustion", "hybrid", "electric");
    public static final Set<String> ALLOWED_TRANSMISSIONS = Set.of("manual", "automatic");
    public static final Set<String> ALLOWED_SORT_BY = Set.of("name_asc", "hp_desc", "hp_asc", "speed_desc", "consumption_asc");
    private static final Set<Integer> ALLOWED_AIRBAG_MIN_VALUES = Set.of(2, 4, 6, 8, 10);
    private static final int HORSEPOWER_MIN_BOUND = 0;
    private static final int HORSEPOWER_MAX_BOUND = 1500;
    private static final int MAX_SPEED_MIN_BOUND = 0;
    private static final int MAX_SPEED_MAX_BOUND = 500;
    private static final BigDecimal FUEL_CONSUMPTION_MIN_BOUND = BigDecimal.ZERO;
    private static final BigDecimal FUEL_CONSUMPTION_MAX_BOUND = BigDecimal.valueOf(30);

    private String q;
    private String brand;
    private String bodyType;
    private String fuelType;
    private Integer horsepowerMin;
    private Integer horsepowerMax;
    private Integer airbagMin;
    private String transmission;
    private BigDecimal fuelConsumptionMax;
    private Integer maxSpeedMin;
    private String sortBy;
    private Integer page;

    public CarSearchCriteria() {}

    public boolean hasAdvancedFilters() {
        return fuelType != null && !fuelType.isEmpty()
                || horsepowerMin != null
                || horsepowerMax != null
                || airbagMin != null
                || transmission != null && !transmission.isEmpty()
                || fuelConsumptionMax != null
                || maxSpeedMin != null;
    }

    public boolean isValid() {
        if (fuelType != null && !ALLOWED_FUEL_TYPES.contains(fuelType)) {
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
        if (airbagMin != null && !ALLOWED_AIRBAG_MIN_VALUES.contains(airbagMin)) {
            return false;
        }
        if (fuelConsumptionMax != null && !isWithinBounds(fuelConsumptionMax, FUEL_CONSUMPTION_MIN_BOUND, FUEL_CONSUMPTION_MAX_BOUND)) {
            return false;
        }
        if (maxSpeedMin != null && !isWithinBounds(maxSpeedMin, MAX_SPEED_MIN_BOUND, MAX_SPEED_MAX_BOUND)) {
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

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(final String fuelType) {
        this.fuelType = fuelType == null || fuelType.trim().isEmpty() ? null : fuelType.trim().toLowerCase(Locale.ROOT);
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
