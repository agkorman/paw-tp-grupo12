package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CarRequest implements Serializable {

    private long id;
    private Long submittedByUserId;
    private String submitterEmail;
    private long brandId;
    private long bodyTypeId;
    private Integer year;
    private String model;
    private String description;
    private String imageContentType;
    private byte[] imageData;
    private String status;
    private LocalDateTime createdAt;
    private String fuelType;
    private Integer horsepower;
    private Integer airbagCount;
    private String transmission;
    private BigDecimal fuelConsumption;
    private Integer maxSpeedKmh;
    private BigDecimal priceUsd;

    public CarRequest() {}

    public CarRequest(final long id, final Long submittedByUserId, final String submitterEmail, final long brandId,
                      final long bodyTypeId, final String model, final String description,
                      final String imageContentType, final byte[] imageData, final String status,
                      final LocalDateTime createdAt) {
        this(id, submittedByUserId, submitterEmail, brandId, bodyTypeId, null, model, description,
                imageContentType, imageData, status, createdAt, null, null, null, null, null, null, null);
    }

    public CarRequest(final long id, final Long submittedByUserId, final String submitterEmail, final long brandId,
                      final long bodyTypeId, final String model, final String description,
                      final String imageContentType, final byte[] imageData, final String status,
                      final LocalDateTime createdAt, final String fuelType, final Integer horsepower,
                      final Integer airbagCount, final String transmission, final BigDecimal fuelConsumption,
                      final Integer maxSpeedKmh, final BigDecimal priceUsd) {
        this(id, submittedByUserId, submitterEmail, brandId, bodyTypeId, null, model, description,
                imageContentType, imageData, status, createdAt, fuelType, horsepower, airbagCount, transmission,
                fuelConsumption, maxSpeedKmh, priceUsd);
    }

    public CarRequest(final long id, final Long submittedByUserId, final String submitterEmail, final long brandId,
                      final long bodyTypeId, final Integer year, final String model, final String description,
                      final String imageContentType, final byte[] imageData, final String status,
                      final LocalDateTime createdAt, final String fuelType, final Integer horsepower,
                      final Integer airbagCount, final String transmission, final BigDecimal fuelConsumption,
                      final Integer maxSpeedKmh, final BigDecimal priceUsd) {
        this.id = id;
        this.submittedByUserId = submittedByUserId;
        this.submitterEmail = submitterEmail;
        this.brandId = brandId;
        this.bodyTypeId = bodyTypeId;
        this.year = year;
        this.model = model;
        this.description = description;
        this.imageContentType = imageContentType;
        this.imageData = imageData;
        this.status = status;
        this.createdAt = createdAt;
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

    public Long getSubmittedByUserId() {
        return submittedByUserId;
    }

    public void setSubmittedByUserId(final Long submittedByUserId) {
        this.submittedByUserId = submittedByUserId;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public void setSubmitterEmail(final String submitterEmail) {
        this.submitterEmail = submitterEmail;
    }

    public long getBrandId() {
        return brandId;
    }

    public void setBrandId(final long brandId) {
        this.brandId = brandId;
    }

    public long getBodyTypeId() {
        return bodyTypeId;
    }

    public void setBodyTypeId(final long bodyTypeId) {
        this.bodyTypeId = bodyTypeId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(final Integer year) {
        this.year = year;
    }

    public String getModel() {
        return model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getImageContentType() {
        return imageContentType;
    }

    public void setImageContentType(final String imageContentType) {
        this.imageContentType = imageContentType;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(final byte[] imageData) {
        this.imageData = imageData;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
