package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "car_images")
public class CarImage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private long imageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id")
    private Car car;

    @Column(name = "car_id", insertable = false, updatable = false)
    private long carId;

    @Column(name = "display_order")
    private int displayOrder;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "image_data")
    private byte[] imageData;

    @Column(name = "updated_at", insertable = false, updatable = false)
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

    public Car getCar() {
        return car;
    }

    public void setCar(final Car car) {
        this.car = car;
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
