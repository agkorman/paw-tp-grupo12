package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CarImage implements Serializable {

    private long carId;
    private String contentType;
    private byte[] imageData;
    private LocalDateTime updatedAt;

    public CarImage() {}

    public CarImage(final long carId, final String contentType, final byte[] imageData,
                    final LocalDateTime updatedAt) {
        this.carId = carId;
        this.contentType = contentType;
        this.imageData = imageData;
        this.updatedAt = updatedAt;
    }

    public long getCarId() {
        return carId;
    }

    public void setCarId(final long carId) {
        this.carId = carId;
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
