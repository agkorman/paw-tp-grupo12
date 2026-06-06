package ar.edu.itba.paw.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "car_request_images")
public class CarRequestImage extends BaseImage {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_request_id", nullable = false)
    private CarRequest request;

    CarRequestImage() {}

    public CarRequestImage(final CarRequest request, final int displayOrder, final String contentType,
                           final byte[] imageData) {
        super(displayOrder, contentType, imageData);
        this.request = request;
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
}
