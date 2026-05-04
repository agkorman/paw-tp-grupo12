package ar.edu.itba.paw.services.exception;

public class CarNotFoundException extends IllegalArgumentException {

    public CarNotFoundException(final long carId) {
        super("Car not found: " + carId);
    }
}
