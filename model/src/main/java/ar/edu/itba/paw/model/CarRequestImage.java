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
@Table(name = "car_request_images")
public class CarRequestImage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private long imageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_request_id")
    private CarRequest request;

    @Column(name = "display_order")
    private int displayOrder;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "image_data")
    private byte[] imageData;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public CarRequestImage() {}

    public CarRequestImage(final long imageId, final long requestId, final int displayOrder,
                           final String contentType, final byte[] imageData,
                           final LocalDateTime updatedAt) {
        this.imageId = imageId;
        this.request = requestReference(requestId);
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

    public CarRequest getRequest() {
        return request;
    }

    public void setRequest(final CarRequest request) {
        this.request = request;
    }

    public long getRequestId() {
        return request != null ? request.getId() : 0;
    }

    public void setRequestId(final long requestId) {
        this.request = requestReference(requestId);
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

    private static CarRequest requestReference(final long id) {
        final CarRequest request = new CarRequest();
        request.setId(id);
        return request;
    }
}
