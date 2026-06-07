package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseImage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", nullable = false)
    private long imageId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "image_data", nullable = false)
    private byte[] imageData;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected BaseImage() {}

    protected BaseImage(final int displayOrder, final String contentType, final byte[] imageData) {
        this.displayOrder = displayOrder;
        this.contentType = contentType;
        this.imageData = imageData;
        this.updatedAt = LocalDateTime.now();
    }

    public long getImageId() {
        return imageId;
    }

    public void setImageId(final long imageId) {
        this.imageId = imageId;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(final int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(final byte[] imageData) {
        this.imageData = imageData;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
