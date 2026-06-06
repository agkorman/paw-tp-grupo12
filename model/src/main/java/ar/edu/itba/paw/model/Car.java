package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "cars")
public class Car implements Serializable {

    public static final int MIN_YEAR = 1950;
    public static final int MAX_YEAR = 2026;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id", nullable = false)
    private long id;

    @Embedded
    private CarSpec spec = new CarSpec();

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private boolean hasImage;

    Car() {}

    public Car(final Brand brand, final String model, final BodyType bodyTypeEntity) {
        this.spec = new CarSpec(brand, bodyTypeEntity, model);
        this.createdAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public CarSpec getSpec() {
        return spec;
    }

    public void setSpec(final CarSpec spec) {
        this.spec = spec == null ? new CarSpec() : spec;
    }

    public Brand getBrand() {
        return spec.getBrand();
    }

    public void setBrand(final Brand brand) {
        spec.setBrand(brand);
    }

    public long getBrandId() {
        return getBrand() != null ? getBrand().getId() : 0;
    }

    public String getBrandName() {
        return getBrand() != null ? getBrand().getName() : null;
    }

    public String getModel() {
        return spec.getModel();
    }

    public void setModel(final String model) {
        spec.setModel(model);
    }

    public BodyType getBodyTypeEntity() {
        return spec.getBodyType();
    }

    public void setBodyTypeEntity(final BodyType bodyTypeEntity) {
        spec.setBodyType(bodyTypeEntity);
    }

    public long getBodyTypeId() {
        return getBodyTypeEntity() != null ? getBodyTypeEntity().getId() : 0;
    }

    public Integer getYear() {
        return spec.getYear();
    }

    public void setYear(final Integer year) {
        spec.setYear(year);
    }

    public String getBodyType() {
        return getBodyTypeEntity() != null ? getBodyTypeEntity().getName() : null;
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
        return spec.getFuelType();
    }

    public void setFuelType(final String fuelType) {
        spec.setFuelType(fuelType);
    }

    public Integer getHorsepower() {
        return spec.getHorsepower();
    }

    public void setHorsepower(final Integer horsepower) {
        spec.setHorsepower(horsepower);
    }

    public Integer getAirbagCount() {
        return spec.getAirbagCount();
    }

    public void setAirbagCount(final Integer airbagCount) {
        spec.setAirbagCount(airbagCount);
    }

    public String getTransmission() {
        return spec.getTransmission();
    }

    public void setTransmission(final String transmission) {
        spec.setTransmission(transmission);
    }

    public BigDecimal getFuelConsumption() {
        return spec.getFuelConsumption();
    }

    public void setFuelConsumption(final BigDecimal fuelConsumption) {
        spec.setFuelConsumption(fuelConsumption);
    }

    public Integer getMaxSpeedKmh() {
        return spec.getMaxSpeedKmh();
    }

    public void setMaxSpeedKmh(final Integer maxSpeedKmh) {
        spec.setMaxSpeedKmh(maxSpeedKmh);
    }

    public BigDecimal getPriceUsd() {
        return spec.getPriceUsd();
    }

    public void setPriceUsd(final BigDecimal priceUsd) {
        spec.setPriceUsd(priceUsd);
    }

}
