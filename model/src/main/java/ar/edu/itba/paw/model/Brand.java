package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Brand implements Serializable {

    private long id;
    private String name;
    private String imageUrl;
    private LocalDateTime createdAt;

    public Brand() {}

    public Brand(long id, String name, String imageUrl, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
