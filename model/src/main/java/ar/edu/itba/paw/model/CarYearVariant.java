package ar.edu.itba.paw.model;

public class CarYearVariant {
    private final long carId;
    private final Integer year;
    private final boolean selected;

    public CarYearVariant(final long carId, final Integer year, final boolean selected) {
        this.carId = carId;
        this.year = year;
        this.selected = selected;
    }

    public long getCarId() {
        return carId;
    }

    public Integer getYear() {
        return year;
    }

    public boolean isSelected() {
        return selected;
    }
}
