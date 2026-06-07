package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class CarSpec implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "body_type_id", nullable = false)
    private BodyType bodyType;

    @Column(name = "model", nullable = false, length = 120)
    private String model;

    @Column(name = "year", nullable = true)
    private Integer year;

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

    CarSpec() {}

    public CarSpec(final Brand brand, final BodyType bodyType, final String model) {
        this.brand = brand;
        this.bodyType = bodyType;
        this.model = model;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(final Brand brand) {
        this.brand = brand;
    }

    public BodyType getBodyType() {
        return bodyType;
    }

    public void setBodyType(final BodyType bodyType) {
        this.bodyType = bodyType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(final Integer year) {
        this.year = year;
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
