package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;

public class StoredImagePayload implements Serializable {

    private long imageId;
    private long ownerId;
    private int displayOrder;
    private String contentType;
    private byte[] imageData;
    private LocalDateTime updatedAt;

    public StoredImagePayload() {}

    public StoredImagePayload(final long imageId, final long ownerId, final int displayOrder,
                              final String contentType, final byte[] imageData,
                              final LocalDateTime updatedAt) {
        this.imageId = imageId;
        this.ownerId = ownerId;
        this.displayOrder = displayOrder;
        this.contentType = contentType;
        this.imageData = imageData == null ? null : Arrays.copyOf(imageData, imageData.length);
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

    public byte[] getImageData() {
        return imageData == null ? null : Arrays.copyOf(imageData, imageData.length);
    }

    public void setImageData(final byte[] imageData) {
        this.imageData = imageData == null ? null : Arrays.copyOf(imageData, imageData.length);
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
