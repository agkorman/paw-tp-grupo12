package ar.edu.itba.paw.webapp.form;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CarForm {

    @NotBlank(message = "{validation.car.brand.required}")
    private String brand;

    @NotBlank(message = "{validation.car.bodyType.required}")
    private String bodyType;

    @NotBlank(message = "{validation.car.model.required}")
    @Size(max = 120, message = "{validation.car.model.max}")
    private String model;

    @Min(value = 1886, message = "{validation.car.year.min}")
    @Max(value = 2100, message = "{validation.car.year.max}")
    private Integer year;

    @Email(message = "{validation.email.invalid}")
    @Size(max = 100, message = "{validation.email.max}")
    private String submitterEmail;

    @NotBlank(message = "{validation.car.description.required}")
    @Size(max = 1500, message = "{validation.car.description.max}")
    private String description;

    private List<MultipartFile> files = new ArrayList<>();

    private List<Long> retainedImageIds = new ArrayList<>();

    @NotBlank(message = "{validation.car.fuelType.required}")
    private String fuelType;

    @NotNull(message = "{validation.car.horsepower.required}")
    @Min(value = 1, message = "{validation.car.horsepower.min}")
    @Max(value = 2000, message = "{validation.car.horsepower.max}")
    private Integer horsepower;

    @NotNull(message = "{validation.car.airbagCount.required}")
    @Min(value = 0, message = "{validation.car.airbagCount.min}")
    @Max(value = 30, message = "{validation.car.airbagCount.max}")
    private Integer airbagCount;

    @NotBlank(message = "{validation.car.transmission.required}")
    private String transmission;

    @NotNull(message = "{validation.car.fuelConsumption.required}")
    @DecimalMin(value = "0.0", message = "{validation.car.fuelConsumption.min}")
    @DecimalMax(value = "99.9", message = "{validation.car.fuelConsumption.max}")
    private BigDecimal fuelConsumption;

    @NotNull(message = "{validation.car.maxSpeed.required}")
    @Min(value = 1, message = "{validation.car.maxSpeed.min}")
    @Max(value = 600, message = "{validation.car.maxSpeed.max}")
    private Integer maxSpeedKmh;

    @DecimalMin(value = "0.01", message = "{validation.car.price.min}")
    @DecimalMax(value = "5000000.00", message = "{validation.car.price.max}")
    private BigDecimal priceUsd;

    public String getBrand() {
        return brand;
    }

    public void setBrand(final String brand) {
        this.brand = brand;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(final String bodyType) {
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

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public void setSubmitterEmail(final String submitterEmail) {
        this.submitterEmail = submitterEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public MultipartFile getFile() {
        return files == null || files.isEmpty() ? null : files.get(0);
    }

    public void setFile(final MultipartFile file) {
        this.files = new ArrayList<>();
        if (file != null) {
            this.files.add(file);
        }
    }

    public List<MultipartFile> getFiles() {
        return files;
    }

    public void setFiles(final List<MultipartFile> files) {
        this.files = files == null ? new ArrayList<>() : files;
    }

    public List<Long> getRetainedImageIds() {
        return retainedImageIds;
    }

    public void setRetainedImageIds(final List<Long> retainedImageIds) {
        this.retainedImageIds = retainedImageIds == null ? new ArrayList<>() : retainedImageIds;
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
