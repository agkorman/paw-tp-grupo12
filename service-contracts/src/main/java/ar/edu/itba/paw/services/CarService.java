package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarRequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CarService {

    List<Car> getAllCars();

    Optional<Car> getCarById(long id);

    List<Car> getCarsByIds(Collection<Long> ids);

    List<Car> getCarsByBodyType(String bodyType);

    List<Car> getCarsByBrand(String brand);

    List<Car> getCarsByBrandAndBodyType(String brand, String bodyType);

    List<Car> searchCars(String query, String brand, String bodyType);
    Optional<CarImage> getCarImageByCarId(long carId);

    void saveCarImage(long carId, String contentType, byte[] imageData);

    CarRequest requestCarCreation(long brandId, String model, long bodyTypeId, long submittedByUserId,
                                  Optional<String> description, Optional<String> imageContentType,
                                  Optional<byte[]> imageData);

    Optional<Car> updateCar(long id, long brandId, String model, long bodyTypeId, String description,
                            Optional<String> imageContentType, Optional<byte[]> imageData);

    boolean deleteCar(long id);
}
