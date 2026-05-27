package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "reviews")
public class Review implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "reviewer_email")
    private String reviewerEmail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id")
    private Car car;

    @Column(name = "rating")
    private BigDecimal rating;

    @Column(name = "title")
    private String title;

    @Column(name = "body")
    private String body;

    @Column(name = "ownership_status")
    private String ownershipStatus;

    @Column(name = "model_year")
    private Integer modelYear;

    @Column(name = "mileage_km")
    private Integer mileageKm;

    @Column(name = "would_recommend")
    private Boolean wouldRecommend;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    @Transient
    private List<ReviewTag> tags = new ArrayList<>();

    @Transient
    private List<ReviewImage> images = new ArrayList<>();

    public Review() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    public String getReviewerEmail() { return reviewerEmail; }
    public void setReviewerEmail(String reviewerEmail) { this.reviewerEmail = reviewerEmail; }

    public String getReviewerUsername() {
        return user != null ? user.getUsername() : null;
    }

    public long getCarId() { return car != null ? car.getId() : 0; }

    public Car getCar() { return car; }
    public void setCar(Car car) { this.car = car; }

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

    public List<ReviewTag> getTags() { return tags; }
    public void setTags(List<ReviewTag> tags) { this.tags = tags == null ? new ArrayList<>() : tags; }

    public List<ReviewImage> getImages() { return images; }
    public void setImages(List<ReviewImage> images) { this.images = images == null ? new ArrayList<>() : images; }

    @PreUpdate
    private void touchUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

}
