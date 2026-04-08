package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CarRequest implements Serializable {

    private long id;
    private Long submittedByUserId;
    private String submitterEmail;
    private long brandId;
    private long bodyTypeId;
    private String model;
    private String description;
    private String imageContentType;
    private byte[] imageData;
    private String status;
    private LocalDateTime createdAt;

    public CarRequest() {}

    public CarRequest(final long id, final Long submittedByUserId, final String submitterEmail, final long brandId,
                      final long bodyTypeId, final String model, final String description,
                      final String imageContentType, final byte[] imageData, final String status,
                      final LocalDateTime createdAt) {
        this.id = id;
        this.submittedByUserId = submittedByUserId;
        this.submitterEmail = submitterEmail;
        this.brandId = brandId;
        this.bodyTypeId = bodyTypeId;
        this.model = model;
        this.description = description;
        this.imageContentType = imageContentType;
        this.imageData = imageData;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Long getSubmittedByUserId() {
        return submittedByUserId;
    }

    public void setSubmittedByUserId(final Long submittedByUserId) {
        this.submittedByUserId = submittedByUserId;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public void setSubmitterEmail(final String submitterEmail) {
        this.submitterEmail = submitterEmail;
    }

    public long getBrandId() {
        return brandId;
    }

    public void setBrandId(final long brandId) {
        this.brandId = brandId;
    }

    public long getBodyTypeId() {
        return bodyTypeId;
    }

    public void setBodyTypeId(final long bodyTypeId) {
        this.bodyTypeId = bodyTypeId;
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
}
