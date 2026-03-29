package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Car implements Serializable {

    private long id;
    private long brandId;
    private String model;
    private long bodyTypeId;
    private String bodyType;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;

    public Car() {}

    public Car(final long id, final long brandId, final String model, final long bodyTypeId,
               final String bodyType, final String description, final String imageUrl, final LocalDateTime createdAt) {
        this.id = id;
        this.brandId = brandId;
        this.model = model;
        this.bodyTypeId = bodyTypeId;
        this.bodyType = bodyType;
        this.description = description;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getBrandId() {
        return brandId;
    }

    public void setBrandId(final long brandId) {
        this.brandId = brandId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public long getBodyTypeId() {
        return bodyTypeId;
    }

    public void setBodyTypeId(final long bodyTypeId) {
        this.bodyTypeId = bodyTypeId;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(final String bodyType) {
        this.bodyType = bodyType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
