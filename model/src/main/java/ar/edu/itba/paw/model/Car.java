package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Car implements Serializable {

    private long id;
    private long brandId;
    private String brandName;
    private String model;
    private long bodyTypeId;
    private String bodyType;
    private String description;
    private LocalDateTime createdAt;
    private boolean hasImage;
    private String fuelType;
    private Integer horsepower;
    private Integer airbagCount;
    private String transmission;
    private BigDecimal fuelConsumption;
    private Integer maxSpeedKmh;
    private BigDecimal priceUsd;

    public Car() {}

    public Car(final long id, final long brandId, final String brandName, final String model, final long bodyTypeId,
               final String bodyType, final String description, final LocalDateTime createdAt) {
        this(id, brandId, brandName, model, bodyTypeId, bodyType, description, createdAt, false);
    }

    public Car(final long id, final long brandId, final String brandName, final String model, final long bodyTypeId,
               final String bodyType, final String description,
               final LocalDateTime createdAt, final boolean hasImage) {
        this(id, brandId, brandName, model, bodyTypeId, bodyType, description, createdAt, hasImage,
                null, null, null, null, null, null, null);
    }

    public Car(final long id, final long brandId, final String brandName, final String model, final long bodyTypeId,
               final String bodyType, final String description, final LocalDateTime createdAt, final boolean hasImage,
               final String fuelType, final Integer horsepower, final Integer airbagCount, final String transmission,
               final BigDecimal fuelConsumption, final Integer maxSpeedKmh, final BigDecimal priceUsd) {
        this.id = id;
        this.brandId = brandId;
        this.brandName = brandName;
        this.model = model;
        this.bodyTypeId = bodyTypeId;
        this.bodyType = bodyType;
        this.description = description;
        this.createdAt = createdAt;
        this.hasImage = hasImage;
        this.fuelType = fuelType;
        this.horsepower = horsepower;
        this.airbagCount = airbagCount;
        this.transmission = transmission;
        this.fuelConsumption = fuelConsumption;
        this.maxSpeedKmh = maxSpeedKmh;
        this.priceUsd = priceUsd;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getBrandId() {
        return brandId;
    }

    public void setBrandId(final long brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(final String brandName) {
        this.brandName = brandName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public long getBodyTypeId() {
        return bodyTypeId;
    }

    public void setBodyTypeId(final long bodyTypeId) {
        this.bodyTypeId = bodyTypeId;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(final String bodyType) {
        this.bodyType = bodyType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean getHasImage() {
        return hasImage;
    }

    public void setHasImage(final boolean hasImage) {
        this.hasImage = hasImage;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(final String fuelType) {
        this.fuelType = fuelType;
    }

    public Integer getHorsepower() {
        return horsepower;
    }

    public void setHorsepower(final Integer horsepower) {
        this.horsepower = horsepower;
    }

    public Integer getAirbagCount() {
        return airbagCount;
    }

    public void setAirbagCount(final Integer airbagCount) {
        this.airbagCount = airbagCount;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(final String transmission) {
        this.transmission = transmission;
    }

    public BigDecimal getFuelConsumption() {
        return fuelConsumption;
    }

    public void setFuelConsumption(final BigDecimal fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }

    public Integer getMaxSpeedKmh() {
        return maxSpeedKmh;
    }

    public void setMaxSpeedKmh(final Integer maxSpeedKmh) {
        this.maxSpeedKmh = maxSpeedKmh;
    }

    public BigDecimal getPriceUsd() {
        return priceUsd;
    }

    public void setPriceUsd(final BigDecimal priceUsd) {
        this.priceUsd = priceUsd;
    }
}
