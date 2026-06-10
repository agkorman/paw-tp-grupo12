package ar.edu.itba.paw.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "car_images")
public class CarImage extends BaseImage {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    CarImage() {}

    public CarImage(final Car car, final int displayOrder, final String contentType, final byte[] imageData) {
        super(displayOrder, contentType, imageData);
        this.car = car;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(final Car car) {
        this.car = car;
    }

    public long getCarId() {
        return car.getId();
    }
}
