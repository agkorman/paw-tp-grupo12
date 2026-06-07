package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "car_requests")
public class CarRequestLegacyImage implements Serializable {

    @Id
    @Column(name = "car_request_id")
    private long id;

    @Column(name = "image_content_type")
    private String imageContentType;

    @Column(name = "image_data")
    private byte[] imageData;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    CarRequestLegacyImage() {}

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
