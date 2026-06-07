package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.CarYearVariant;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.StoredImagePayload;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CarService {
    long LEGACY_IMAGE_ID = 0L;

    List<Car> getAllCars();

    Optional<Car> getCarById(long id);

    List<Car> getCarsByIds(Collection<Long> ids);

    List<Car> getCarsByBrandAndBodyType(String brand, String bodyType);

    Page<Car> searchCars(CarSearchCriteria criteria);

    Optional<StoredImagePayload> getCarImageByCarId(long carId);

    List<ImageMetadata> getCarImagesByCarId(long carId);

    Optional<StoredImagePayload> getCarImageById(long carId, long imageId);

    void saveCarImages(long carId, List<ImagePayload> images);

    void appendCarImages(long carId, List<ImagePayload> images);

    CarRequest requestCarCreation(
        long brandId,
        String model,
        long bodyTypeId,
        Integer year,
        long submittedByUserId,
        String submitterEmail,
        Optional<String> description,
        List<ImagePayload> images,
        String fuelType,
        Integer horsepower,
        Integer airbagCount,
        String transmission,
        BigDecimal fuelConsumption,
        Integer maxSpeedKmh,
        BigDecimal priceUsd
    );

    Optional<Car> updateCar(
        long id,
        long brandId,
        String model,
        long bodyTypeId,
        Integer year,
        String description,
        Optional<String> imageContentType,
        Optional<byte[]> imageData,
        String fuelType,
        Integer horsepower,
        Integer airbagCount,
        String transmission,
        BigDecimal fuelConsumption,
        Integer maxSpeedKmh,
        BigDecimal priceUsd
    );

    boolean existsDuplicateCar(
        String brandName,
        String bodyTypeName,
        String model,
        Integer year,
        long ignoredCarId
    );

    List<Car> getFeaturedCars(int limit);

    boolean deleteCar(long id);

    List<ImagePayload> collectRetainedImagePayloads(
        long carId,
        List<Long> retainedImageIds
    );

    List<CarYearVariant> getYearVariants(long carId);
}
