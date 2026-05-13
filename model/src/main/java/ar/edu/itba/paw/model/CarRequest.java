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

@Entity
@Table(name = "car_requests")
public class CarRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_request_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_user_id")
    private User submittedByUser;

    @Column(name = "submitter_email")
    private String submitterEmail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "body_type_id")
    private BodyType bodyType;

    @Column(name = "year")
    private Integer year;

    @Column(name = "model")
    private String model;

    @Column(name = "description")
    private String description;

    @Column(name = "image_content_type")
    private String imageContentType;

    @Column(name = "image_data")
    private byte[] imageData;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

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
        this.submittedByUser = userReference(submittedByUserId);
        this.submitterEmail = submitterEmail;
        this.brand = brandReference(brandId);
        this.bodyType = bodyTypeReference(bodyTypeId);
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
        return submittedByUser != null ? submittedByUser.getId() : null;
    }

    public void setSubmittedByUserId(final Long submittedByUserId) {
        this.submittedByUser = userReference(submittedByUserId);
    }

    public User getSubmittedByUser() {
        return submittedByUser;
    }

    public void setSubmittedByUser(final User submittedByUser) {
        this.submittedByUser = submittedByUser;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public void setSubmitterEmail(final String submitterEmail) {
        this.submitterEmail = submitterEmail;
    }

    public long getBrandId() {
        return brand != null ? brand.getId() : 0;
    }

    public void setBrandId(final long brandId) {
        this.brand = brandReference(brandId);
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(final Brand brand) {
        this.brand = brand;
    }

    public long getBodyTypeId() {
        return bodyType != null ? bodyType.getId() : 0;
    }

    public void setBodyTypeId(final long bodyTypeId) {
        this.bodyType = bodyTypeReference(bodyTypeId);
    }

    public BodyType getBodyType() {
        return bodyType;
    }

    public void setBodyType(final BodyType bodyType) {
        this.bodyType = bodyType;
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

    private static User userReference(final Long id) {
        if (id == null) {
            return null;
        }
        final User user = new User();
        user.setId(id);
        return user;
    }

    private static Brand brandReference(final long id) {
        final Brand brand = new Brand();
        brand.setId(id);
        return brand;
    }

    private static BodyType bodyTypeReference(final long id) {
        final BodyType bodyType = new BodyType();
        bodyType.setId(id);
        return bodyType;
    }
}
