package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CarImage implements Serializable {

    private long imageId;
    private long carId;
    private int displayOrder;
    private String contentType;
    private byte[] imageData;
    private LocalDateTime updatedAt;

    public CarImage() {}

    public CarImage(final long carId, final String contentType, final byte[] imageData,
                    final LocalDateTime updatedAt) {
        this(0, carId, 0, contentType, imageData, updatedAt);
    }

    public CarImage(final long imageId, final long carId, final int displayOrder,
                    final String contentType, final byte[] imageData,
                    final LocalDateTime updatedAt) {
        this.imageId = imageId;
        this.carId = carId;
        this.displayOrder = displayOrder;
        this.contentType = contentType;
        this.imageData = imageData;
        this.updatedAt = updatedAt;
    }

    public long getImageId() {
        return imageId;
    }

    public void setImageId(final long imageId) {
        this.imageId = imageId;
    }

    public long getCarId() {
        return carId;
    }

    public void setCarId(final long carId) {
        this.carId = carId;
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
