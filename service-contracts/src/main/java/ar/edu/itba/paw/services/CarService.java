package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;

import java.util.List;
import java.util.Optional;

public interface CarService {

    List<Car> getAllCars();

    Optional<Car> getCarById(long id);

    List<Car> getCarsByBodyType(String bodyType);

    List<Car> getCarsByBrand(String brand);

    List<Car> getCarsByBrandAndBodyType(String brand, String bodyType);

    List<Car> searchCars(String query);
    Optional<CarImage> getCarImageByCarId(long carId);

    void saveCarImage(long carId, String contentType, byte[] imageData);

    Car createCar(long brandId, String model, long bodyTypeId, Optional<String> description,
                  Optional<String> imageContentType, Optional<byte[]> imageData);
}
