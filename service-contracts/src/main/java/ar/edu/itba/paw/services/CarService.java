package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CarService {

    List<Car> getAllCars();

    Optional<Car> getCarById(long id);

    List<Car> getCarsByIds(Collection<Long> ids);

    List<Car> getCarsByBrandAndBodyType(String brand, String bodyType);

    Page<Car> searchCars(CarSearchCriteria criteria);

    Optional<CarImage> getCarImageByCarId(long carId);

    List<CarImage> getCarImagesByCarId(long carId);

    Optional<CarImage> getCarImageById(long carId, long imageId);

    void saveCarImages(long carId, List<CarImagePayload> images);

    CarRequest requestCarCreation(long brandId, String model, long bodyTypeId, long submittedByUserId,
                                  String submitterEmail, Optional<String> description,
                                  List<CarImagePayload> images,
                                  String fuelType, Integer horsepower, Integer airbagCount,
                                  String transmission, BigDecimal fuelConsumption, Integer maxSpeedKmh);

    Optional<Car> updateCar(long id, long brandId, String model, long bodyTypeId, String description,
                            Optional<String> imageContentType, Optional<byte[]> imageData,
                            String fuelType, Integer horsepower, Integer airbagCount,
                            String transmission, BigDecimal fuelConsumption, Integer maxSpeedKmh);

    boolean deleteCar(long id);
}
