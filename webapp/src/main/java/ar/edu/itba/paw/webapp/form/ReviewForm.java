package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.webapp.validation.ValidReviewForm;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@ValidReviewForm
public class ReviewForm {

    @NotNull(message = "{validation.review.car.required}")
    private Long carId;

    @Email(message = "{validation.email.invalid}")
    @Size(max = 100, message = "{validation.email.max}")
    private String reviewerEmail;

    @NotNull(message = "{validation.review.rating.required}")
    @DecimalMin(value = "0.0", message = "{validation.review.rating.range}")
    @DecimalMax(value = "5.0", message = "{validation.review.rating.range}")
    private BigDecimal rating;

    @NotBlank(message = "{validation.review.title.required}")
    @Size(max = 200, message = "{validation.review.title.max}")
    private String title;

    @NotBlank(message = "{validation.review.body.required}")
    @Size(max = 2000, message = "{validation.review.body.max}")
    private String body;

    @Size(max = 20, message = "{validation.review.ownershipStatus.max}")
    private String ownershipStatus;

    @NotNull(message = "{validation.review.modelYear.required}")
    @Min(value = 1886, message = "{validation.review.modelYear.range}")
    @Max(value = 2100, message = "{validation.review.modelYear.range}")
    private Integer modelYear;

    @NotNull(message = "{validation.review.mileage.required}")
    @Min(value = 0, message = "{validation.review.mileage.range}")
    @Max(value = 2_000_000, message = "{validation.review.mileage.range}")
    private Integer mileageKm;

    private Boolean wouldRecommend;

    @Size(max = 6, message = "{validation.review.tags.max}")
    private Set<Short> tagIds = new LinkedHashSet<>();

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

    public Set<Short> getTagIds() {
        return tagIds;
    }

    public void setTagIds(final Set<Short> tagIds) {
        this.tagIds = tagIds == null ? new LinkedHashSet<>() : tagIds;
    }
}
