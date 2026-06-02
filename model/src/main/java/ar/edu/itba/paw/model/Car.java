package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(name = "model", nullable = false, length = 120)
    private String model;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "body_type_id", nullable = false)
    private BodyType bodyTypeEntity;

    @Column(name = "year", nullable = true)
    private Integer year;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private boolean hasImage;

    @Column(name = "fuel_type", nullable = true, length = 20)
    private String fuelType;

    @Column(name = "horsepower", nullable = true)
    private Integer horsepower;

    @Column(name = "airbag_count", nullable = true)
    private Integer airbagCount;

    @Column(name = "transmission", nullable = true, length = 20)
    private String transmission;

    @Column(name = "fuel_consumption", nullable = true, precision = 4, scale = 1)
    private BigDecimal fuelConsumption;

    @Column(name = "max_speed_kmh", nullable = true)
    private Integer maxSpeedKmh;

    @Column(name = "price_usd", nullable = true, precision = 12, scale = 2)
    private BigDecimal priceUsd;

    Car() {}

    public Car(final Brand brand, final String model, final BodyType bodyTypeEntity) {
        this.brand = brand;
        this.model = model;
        this.bodyTypeEntity = bodyTypeEntity;
        this.createdAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(final Brand brand) {
        this.brand = brand;
    }

    public long getBrandId() {
        return brand != null ? brand.getId() : 0;
    }

    public String getBrandName() {
        return brand != null ? brand.getName() : null;
    }

    public String getModel() {
        return model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public BodyType getBodyTypeEntity() {
        return bodyTypeEntity;
    }

    public void setBodyTypeEntity(final BodyType bodyTypeEntity) {
        this.bodyTypeEntity = bodyTypeEntity;
    }

    public long getBodyTypeId() {
        return bodyTypeEntity != null ? bodyTypeEntity.getId() : 0;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(final Integer year) {
        this.year = year;
    }

    public String getBodyType() {
        return bodyTypeEntity != null ? bodyTypeEntity.getName() : null;
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
