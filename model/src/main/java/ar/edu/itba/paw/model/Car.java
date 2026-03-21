package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Car implements Serializable {

    private long id;
    private long brandId;
    private String model;
    private String generation;
    private String bodyType;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;

    public Car() {}

    public Car(long id, long brandId, String model, String generation,
               String bodyType, String description, String imageUrl, LocalDateTime createdAt) {
        this.id = id;
        this.brandId = brandId;
        this.model = model;
        this.generation = generation;
        this.bodyType = bodyType;
        this.description = description;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getBrandId() { return brandId; }
    public void setBrandId(long brandId) { this.brandId = brandId; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getGeneration() { return generation; }
    public void setGeneration(String generation) { this.generation = generation; }

    public String getBodyType() { return bodyType; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
