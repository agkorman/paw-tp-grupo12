package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ImageMetadata implements Serializable {

    private long imageId;
    private long ownerId;
    private int displayOrder;
    private String contentType;
    private LocalDateTime updatedAt;

    public ImageMetadata() {}

    public ImageMetadata(final long imageId, final long ownerId, final int displayOrder,
                         final String contentType, final LocalDateTime updatedAt) {
        this.imageId = imageId;
        this.ownerId = ownerId;
        this.displayOrder = displayOrder;
        this.contentType = contentType;
        this.updatedAt = updatedAt;
    }

    public long getImageId() {
        return imageId;
    }

    public void setImageId(final long imageId) {
        this.imageId = imageId;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(final long ownerId) {
        this.ownerId = ownerId;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
