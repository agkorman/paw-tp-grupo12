package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class ReviewForm {

    @NotNull(message = "El auto es obligatorio.")
    private Long carId;

    @Email(message = "Ingresá un email válido.")
    @Size(max = 100, message = "El email debe tener como máximo 100 caracteres.")
    private String reviewerEmail;

    @NotNull(message = "La puntuación es obligatoria.")
    @DecimalMin(value = "0.0", message = "La puntuación debe estar entre 0 y 5.")
    @DecimalMax(value = "5.0", message = "La puntuación debe estar entre 0 y 5.")
    private BigDecimal rating;

    @NotBlank(message = "El título es obligatorio.")
    @Size(max = 200, message = "El título debe tener como máximo 200 caracteres.")
    private String title;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 2000, message = "La descripción debe tener como máximo 2000 caracteres.")
    private String body;

    @Size(max = 20, message = "Estado de propiedad no válido.")
    private String ownershipStatus;

    @Min(value = 1886, message = "Ingresá un año válido.")
    @Max(value = 2100, message = "Ingresá un año válido.")
    private Integer modelYear;

    @Min(value = 0, message = "Ingresá un kilometraje entre 0 y 2.000.000 km.")
    @Max(value = 2_000_000, message = "Ingresá un kilometraje entre 0 y 2.000.000 km.")
    private Integer mileageKm;

    private Boolean wouldRecommend;

    public Long getCarId() {
        return carId;
    }

    public void setCarId(final Long carId) {
        this.carId = carId;
    }

    public String getReviewerEmail() {
        return reviewerEmail;
    }

    public void setReviewerEmail(final String reviewerEmail) {
        this.reviewerEmail = reviewerEmail;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(final BigDecimal rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }

    public String getOwnershipStatus() {
        return ownershipStatus;
    }

    public void setOwnershipStatus(final String ownershipStatus) {
        this.ownershipStatus = ownershipStatus;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(final Integer modelYear) {
        this.modelYear = modelYear;
    }

    public Integer getMileageKm() {
        return mileageKm;
    }

    public void setMileageKm(final Integer mileageKm) {
        this.mileageKm = mileageKm;
    }

    public Boolean getWouldRecommend() {
        return wouldRecommend;
    }

    public void setWouldRecommend(final Boolean wouldRecommend) {
        this.wouldRecommend = wouldRecommend;
    }
}
