package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import java.util.List;

public interface CarService {

    List<Car> getAllCars();

    List<Car> getCarsByBodyType(String bodyType);

    List<Car> getCarsByBrand(String brand);

    List<Car> getCarsByBrandAndBodyType(String brand, String bodyType);
}
