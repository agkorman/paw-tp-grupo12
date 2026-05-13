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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Transient
    private String brandName;

    @Column(name = "model")
    private String model;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "body_type_id")
    private BodyType bodyTypeEntity;

    @Column(name = "year")
    private Integer year;

    @Transient
    private String bodyType;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private boolean hasImage;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "horsepower")
    private Integer horsepower;

    @Column(name = "airbag_count")
    private Integer airbagCount;

    @Column(name = "transmission")
    private String transmission;

    @Column(name = "fuel_consumption")
    private BigDecimal fuelConsumption;

    @Column(name = "max_speed_kmh")
    private Integer maxSpeedKmh;

    @Column(name = "price_usd")
    private BigDecimal priceUsd;

    public Car() {}

    public Car(final long id, final long brandId, final String brandName, final String model, final long bodyTypeId,
               final String bodyType, final String description, final LocalDateTime createdAt) {
        this(id, brandId, brandName, model, bodyTypeId, bodyType, description, createdAt, false);
    }

    public Car(final long id, final long brandId, final String brandName, final String model, final long bodyTypeId,
               final String bodyType, final String description,
               final LocalDateTime createdAt, final boolean hasImage) {
        this(id, brandId, brandName, model, bodyTypeId, null, bodyType, description, createdAt, hasImage,
                null, null, null, null, null, null, null);
    }

    public Car(final long id, final long brandId, final String brandName, final String model, final long bodyTypeId,
               final String bodyType, final String description, final LocalDateTime createdAt, final boolean hasImage,
               final String fuelType, final Integer horsepower, final Integer airbagCount, final String transmission,
               final BigDecimal fuelConsumption, final Integer maxSpeedKmh, final BigDecimal priceUsd) {
        this(id, brandId, brandName, model, bodyTypeId, null, bodyType, description, createdAt, hasImage,
                fuelType, horsepower, airbagCount, transmission, fuelConsumption, maxSpeedKmh, priceUsd);
    }

    public Car(final long id, final long brandId, final String brandName, final String model, final long bodyTypeId,
               final Integer year, final String bodyType, final String description, final LocalDateTime createdAt,
               final boolean hasImage, final String fuelType, final Integer horsepower,
               final Integer airbagCount, final String transmission, final BigDecimal fuelConsumption,
               final Integer maxSpeedKmh, final BigDecimal priceUsd) {
        this.id = id;
        this.brand = brandReference(brandId, brandName);
        this.model = model;
        this.bodyTypeEntity = bodyTypeReference(bodyTypeId, bodyType);
        this.year = year;
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

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(final Brand brand) {
        this.brand = brand;
    }

    public long getBrandId() {
        return brand != null ? brand.getId() : 0;
    }

    public void setBrandId(final long brandId) {
        this.brand = brandReference(brandId, null);
    }

    public String getBrandName() {
        return brand != null ? brand.getName() : brandName;
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

    public BodyType getBodyTypeEntity() {
        return bodyTypeEntity;
    }

    public void setBodyTypeEntity(final BodyType bodyTypeEntity) {
        this.bodyTypeEntity = bodyTypeEntity;
    }

    public long getBodyTypeId() {
        return bodyTypeEntity != null ? bodyTypeEntity.getId() : 0;
    }

    public void setBodyTypeId(final long bodyTypeId) {
        this.bodyTypeEntity = bodyTypeReference(bodyTypeId, null);
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(final Integer year) {
        this.year = year;
    }

    public String getBodyType() {
        return bodyTypeEntity != null ? bodyTypeEntity.getName() : bodyType;
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

    private static Brand brandReference(final long id, final String name) {
        final Brand brand = new Brand();
        brand.setId(id);
        brand.setName(name);
        return brand;
    }

    private static BodyType bodyTypeReference(final long id, final String name) {
        final BodyType bodyType = new BodyType();
        bodyType.setId(id);
        bodyType.setName(name);
        return bodyType;
    }
}
