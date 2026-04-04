package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Review implements Serializable {

    private long id;
    private Long userId;
    private String reviewerEmail;
    private long carId;
    private BigDecimal rating;
    private String title;
    private String body;
    private String ownershipStatus;
    private Integer modelYear;
    private Integer mileageKm;
    private Boolean wouldRecommend;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Review() {}

    public Review(long id, Long userId, String reviewerEmail, long carId, BigDecimal rating, String title, String body,
                  String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.reviewerEmail = reviewerEmail;
        this.carId = carId;
        this.rating = rating;
        this.title = title;
        this.body = body;
        this.ownershipStatus = ownershipStatus;
        this.modelYear = modelYear;
        this.mileageKm = mileageKm;
        this.wouldRecommend = wouldRecommend;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getReviewerEmail() { return reviewerEmail; }
    public void setReviewerEmail(String reviewerEmail) { this.reviewerEmail = reviewerEmail; }

    public long getCarId() { return carId; }
    public void setCarId(long carId) { this.carId = carId; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getOwnershipStatus() { return ownershipStatus; }
    public void setOwnershipStatus(String ownershipStatus) { this.ownershipStatus = ownershipStatus; }

    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }

    public Integer getMileageKm() { return mileageKm; }
    public void setMileageKm(Integer mileageKm) { this.mileageKm = mileageKm; }

    public Boolean getWouldRecommend() { return wouldRecommend; }
    public void setWouldRecommend(Boolean wouldRecommend) { this.wouldRecommend = wouldRecommend; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
