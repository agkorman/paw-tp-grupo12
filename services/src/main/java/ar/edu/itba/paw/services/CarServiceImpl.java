package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarImageDao;
import ar.edu.itba.paw.persistence.ReviewDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CarServiceImpl implements CarService {

    private final CarDao carDao;
    private final CarImageDao carImageDao;
    private final ReviewDao reviewDao;
    private final CarRequestService carRequestService;
    private final BrandDao brandDao;
    private final BodyTypeDao bodyTypeDao;

    @Autowired
    public CarServiceImpl(final CarDao carDao, final CarImageDao carImageDao,
                          final ReviewDao reviewDao,
                          final CarRequestService carRequestService,
                          final BrandDao brandDao, final BodyTypeDao bodyTypeDao) {
        this.carDao = carDao;
        this.carImageDao = carImageDao;
        this.reviewDao = reviewDao;
        this.carRequestService = carRequestService;
        this.brandDao = brandDao;
        this.bodyTypeDao = bodyTypeDao;
    }

    @Override
    public List<Car> getAllCars() {
        return carDao.findAll();
    }

    @Override
    public Optional<Car> getCarById(final long id) {
        return carDao.findById(id);
    }

    @Override
    public List<Car> getCarsByIds(final Collection<Long> ids) {
        return carDao.findByIds(ids);
    }

    @Override
    public List<Car> getCarsByBodyType(final String bodyType) {
        return bodyTypeDao.findByName(bodyType)
                .map(bt -> carDao.findByBodyTypeId(bt.getId()))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Car> getCarsByBrand(final String brand) {
        return brandDao.findByName(brand)
                .map(b -> carDao.findByBrandId(b.getId()))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Car> getCarsByBrandAndBodyType(final String brand, final String bodyType) {
        return brandDao.findByName(brand)
                .flatMap(b -> bodyTypeDao.findByName(bodyType)
                        .map(bt -> carDao.findByBrandIdAndBodyTypeId(b.getId(), bt.getId())))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Car> searchCars(final CarSearchCriteria criteria) {
        return carDao.findByCriteria(criteria);
    }

    @Override
    public Optional<CarImage> getCarImageByCarId(final long carId) {
        return carImageDao.findByCarId(carId);
    }

    @Override
    public void saveCarImage(final long carId, final String contentType, final byte[] imageData) {
        carImageDao.saveOrReplace(carId, contentType, imageData);
    }

    @Override
    @Transactional
    public CarRequest requestCarCreation(final long brandId, final String model, final long bodyTypeId,
                                         final long submittedByUserId, final String submitterEmail,
                                         final Optional<String> description,
                                         final Optional<String> imageContentType,
                                         final Optional<byte[]> imageData,
                                         final String fuelType, final Integer horsepower,
                                         final Integer airbagCount, final String transmission,
                                         final BigDecimal fuelConsumption, final Integer maxSpeedKmh) {
        final String normalizedDescription = description
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Description is required for car creation."));

        final boolean hasImageContentType = imageContentType.isPresent();
        final boolean hasImageData = imageData.isPresent();
        if (hasImageContentType != hasImageData) {
            throw new IllegalArgumentException("Image metadata and payload must be provided together.");
        }

        return carRequestService.createPendingRequest(
                submittedByUserId,
                submitterEmail,
                brandId,
                bodyTypeId,
                model,
                normalizedDescription,
                imageContentType,
                imageData,
                fuelType,
                horsepower,
                airbagCount,
                transmission,
                fuelConsumption,
                maxSpeedKmh
        );
    }

    @Override
    @Transactional
    public Optional<Car> updateCar(final long id, final long brandId, final String model,
                                   final long bodyTypeId, final String description,
                                   final Optional<String> imageContentType,
                                   final Optional<byte[]> imageData,
                                   final String fuelType, final Integer horsepower,
                                   final Integer airbagCount, final String transmission,
                                   final BigDecimal fuelConsumption, final Integer maxSpeedKmh) {
        final String normalizedModel = normalizeRequired(model, "Model is required for car update.");
        final String normalizedDescription = normalizeRequired(description, "Description is required for car update.");
        validateImagePair(imageContentType, imageData);

        final Optional<Car> updated = carDao.update(id, brandId, normalizedModel, bodyTypeId, normalizedDescription,
                fuelType, horsepower, airbagCount, transmission, fuelConsumption, maxSpeedKmh);
        if (updated.isPresent() && imageContentType.isPresent()) {
            carImageDao.saveOrReplace(id, imageContentType.get(), imageData.orElseThrow());
        }
        return updated;
    }

    @Override
    @Transactional
    public boolean deleteCar(final long id) {
        if (carDao.findById(id).isEmpty()) {
            return false;
        }
        reviewDao.deleteByCarId(id);
        return carDao.delete(id);
    }

    private String normalizeRequired(final String value, final String errorMessage) {
        if (value == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return trimmed;
    }

    private void validateImagePair(final Optional<String> imageContentType, final Optional<byte[]> imageData) {
        final boolean hasImageContentType = imageContentType.isPresent();
        final boolean hasImageData = imageData.isPresent();
        if (hasImageContentType != hasImageData) {
            throw new IllegalArgumentException("Image metadata and payload must be provided together.");
        }
    }
}
