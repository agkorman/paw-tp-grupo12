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

public class CarForm {

    @NotBlank(message = "La marca es obligatoria.")
    private String brand;

    @NotBlank(message = "El tipo de carrocería es obligatorio.")
    private String bodyType;

    @NotBlank(message = "El modelo es obligatorio.")
    @Size(max = 120, message = "El modelo debe tener como máximo 120 caracteres.")
    private String model;

    @Email(message = "Ingresá un email válido.")
    @Size(max = 100, message = "El email debe tener como máximo 100 caracteres.")
    private String submitterEmail;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 1500, message = "La descripción debe tener como máximo 1500 caracteres.")
    private String description;

    private MultipartFile file;

    @NotBlank(message = "El tipo de motorización es obligatorio.")
    private String fuelType;

    @NotNull(message = "La potencia es obligatoria.")
    @Min(value = 1, message = "La potencia debe ser mayor a 0.")
    @Max(value = 2000, message = "La potencia no puede superar los 2000 HP.")
    private Integer horsepower;

    @NotNull(message = "El número de airbags es obligatorio.")
    @Min(value = 0, message = "El número de airbags no puede ser negativo.")
    @Max(value = 30, message = "El número de airbags no puede superar 30.")
    private Integer airbagCount;

    @NotBlank(message = "La transmisión es obligatoria.")
    private String transmission;

    @NotNull(message = "El consumo de nafta es obligatorio.")
    @DecimalMin(value = "0.0", message = "El consumo debe ser mayor o igual a 0.")
    @DecimalMax(value = "99.9", message = "El consumo no puede superar 99.9 L/100km.")
    private BigDecimal fuelConsumption;

    @NotNull(message = "La velocidad máxima es obligatoria.")
    @Min(value = 1, message = "La velocidad debe ser mayor a 0.")
    @Max(value = 600, message = "La velocidad no puede superar los 600 km/h.")
    private Integer maxSpeedKmh;

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
        return file;
    }

    public void setFile(final MultipartFile file) {
        this.file = file;
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
}
