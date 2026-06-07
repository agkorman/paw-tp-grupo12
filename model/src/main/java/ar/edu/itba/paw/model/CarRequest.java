package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Embedded;
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
    @Column(name = "car_request_id", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_user_id", nullable = true)
    private User submittedByUser;

    @Column(name = "submitter_email", nullable = true, length = 100)
    private String submitterEmail;

    @Embedded
    private CarSpec spec = new CarSpec();

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    CarRequest() {}

    public CarRequest(final Brand brand, final BodyType bodyType, final String model,
                      final String description, final String status) {
        this.spec = new CarSpec(brand, bodyType, model);
        this.description = description;
        this.status = status;
        this.createdAt = LocalDateTime.now();
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

    public CarSpec getSpec() {
        return spec;
    }

    public void setSpec(final CarSpec spec) {
        this.spec = spec == null ? new CarSpec() : spec;
    }

    public long getBrandId() {
        return getBrand() != null ? getBrand().getId() : 0;
    }

    public Brand getBrand() {
        return spec.getBrand();
    }

    public void setBrand(final Brand brand) {
        spec.setBrand(brand);
    }

    public long getBodyTypeId() {
        return getBodyType() != null ? getBodyType().getId() : 0;
    }

    public BodyType getBodyType() {
        return spec.getBodyType();
    }

    public void setBodyType(final BodyType bodyType) {
        spec.setBodyType(bodyType);
    }

    public Integer getYear() {
        return spec.getYear();
    }

    public void setYear(final Integer year) {
        spec.setYear(year);
    }

    public String getModel() {
        return spec.getModel();
    }

    public void setModel(final String model) {
        spec.setModel(model);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
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
