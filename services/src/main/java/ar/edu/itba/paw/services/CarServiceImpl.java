package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarImageDao;
import ar.edu.itba.paw.services.exception.InvalidImagePayloadException;
import ar.edu.itba.paw.services.exception.InvalidServiceInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CarServiceImpl implements CarService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarServiceImpl.class);

    private final CarDao carDao;
    private final CarImageDao carImageDao;
    private final CarRequestService carRequestService;
    private final BrandDao brandDao;
    private final BodyTypeDao bodyTypeDao;

    @Autowired
    public CarServiceImpl(final CarDao carDao, final CarImageDao carImageDao,
                          final CarRequestService carRequestService,
                          final BrandDao brandDao, final BodyTypeDao bodyTypeDao) {
        this.carDao = carDao;
        this.carImageDao = carImageDao;
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
    public List<Car> getCarsByBrandAndBodyType(final String brand, final String bodyType) {
        return brandDao.findByName(brand)
                .flatMap(b -> bodyTypeDao.findByName(bodyType)
                        .map(bt -> carDao.findByBrandIdAndBodyTypeId(b.getId(), bt.getId())))
                .orElse(Collections.emptyList());
    }

    @Override
    public Page<Car> searchCars(final CarSearchCriteria criteria) {
        return carDao.findByCriteria(criteria);
    }

    @Override
    public Optional<CarImage> getCarImageByCarId(final long carId) {
        return carImageDao.findByCarId(carId);
    }

    @Override
    public List<CarImage> getCarImagesByCarId(final long carId) {
        return carImageDao.findAllByCarId(carId);
    }

    @Override
    public Optional<CarImage> getCarImageById(final long carId, final long imageId) {
        return carImageDao.findByCarIdAndImageId(carId, imageId);
    }

    @Override
    @Transactional
    public void saveCarImages(final long carId, final List<CarImagePayload> images) {
        carImageDao.replaceAll(carId, ImagePayloadUtils.normalizeImages(images));
    }

    @Override
    @Transactional
    public void appendCarImages(final long carId, final List<CarImagePayload> images) {
        final List<CarImagePayload> normalizedImages = ImagePayloadUtils.normalizeImages(images);
        if (normalizedImages.isEmpty()) {
            return;
        }
        final List<CarImagePayload> existingImages = carImageDao.findAllByCarId(carId)
                .stream()
                .map(image -> carImageDao.findByCarIdAndImageId(carId, image.getImageId()).orElse(null))
                .filter(image -> image != null && image.getImageData() != null)
                .map(image -> new CarImagePayload(image.getContentType(), image.getImageData()))
                .toList();
        final List<CarImagePayload> combinedImages = new java.util.ArrayList<>(existingImages);
        combinedImages.addAll(normalizedImages);
        carImageDao.replaceAll(carId, combinedImages);
    }

    @Override
    @Transactional
    public CarRequest requestCarCreation(final long brandId, final String model, final long bodyTypeId,
                                         final Integer year, final long submittedByUserId, final String submitterEmail,
                                         final Optional<String> description,
                                         final List<CarImagePayload> images,
                                         final String fuelType, final Integer horsepower,
                                         final Integer airbagCount, final String transmission,
                                         final BigDecimal fuelConsumption, final Integer maxSpeedKmh,
                                         final BigDecimal priceUsd) {
        final String normalizedDescription = description
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElseThrow(() -> new InvalidServiceInputException("Description is required for car creation."));
        final List<CarImagePayload> normalizedImages = ImagePayloadUtils.normalizeImages(images);
        if (normalizedImages.isEmpty()) {
            throw new InvalidImagePayloadException("At least one image is required for car creation.");
        }

        return carRequestService.createPendingRequest(
                submittedByUserId,
                submitterEmail,
                brandId,
                bodyTypeId,
                year,
                model,
                normalizedDescription,
                normalizedImages,
                fuelType,
                horsepower,
                airbagCount,
                transmission,
                fuelConsumption,
                maxSpeedKmh,
                priceUsd
        );
    }

    @Override
    @Transactional
    public Optional<Car> updateCar(final long id, final long brandId, final String model,
                                   final long bodyTypeId, final Integer year, final String description,
                                   final Optional<String> imageContentType,
                                   final Optional<byte[]> imageData,
                                   final String fuelType, final Integer horsepower,
                                   final Integer airbagCount, final String transmission,
                                   final BigDecimal fuelConsumption, final Integer maxSpeedKmh,
                                   final BigDecimal priceUsd) {
        final String normalizedModel = StringUtils.normalizeRequired(model, "Model is required for car update.");
        final String normalizedDescription = StringUtils.normalizeRequired(description, "Description is required for car update.");
        validateImagePair(imageContentType, imageData);

        final Optional<Car> updated = carDao.update(id, brandId, normalizedModel, bodyTypeId, year, normalizedDescription,
                fuelType, horsepower, airbagCount, transmission, fuelConsumption, maxSpeedKmh, priceUsd);
        if (updated.isPresent() && imageContentType.isPresent()) {
            carImageDao.saveOrReplace(id, imageContentType.get(), imageData.orElseThrow());
        }
        if (updated.isPresent()) {
            LOGGER.info("updated car id={} brandId={} model={}", id, brandId, normalizedModel);
        }
        return updated;
    }

    @Override
    public List<Car> getFeaturedCars(final int limit) {
        final List<Car> topRated = carDao.findTopRated(limit);
        if (topRated.size() >= limit) {
            return topRated;
        }
        final List<Long> excludedIds = topRated.stream().map(Car::getId).toList();
        final List<Car> result = new java.util.ArrayList<>(topRated);
        result.addAll(carDao.findRecentlyAdded(limit - topRated.size(), excludedIds));
        return result;
    }

    @Override
    public boolean existsDuplicateCar(final String brandName, final String bodyTypeName,
                                      final String model, final Integer year, final long ignoredCarId) {
        final String normalizedModel = StringUtils.normalize(model);
        if (normalizedModel == null) {
            return false;
        }
        final String lowerModel = normalizedModel.toLowerCase(Locale.ROOT);
        return getCarsByBrandAndBodyType(brandName, bodyTypeName)
                .stream()
                .anyMatch(car -> {
                    final String existingModel = StringUtils.normalize(car.getModel());
                    return car.getId() != ignoredCarId
                            && existingModel != null
                            && lowerModel.equals(existingModel.toLowerCase(Locale.ROOT))
                            && Objects.equals(car.getYear(), year);
                });
    }

    @Override
    @Transactional
    public boolean deleteCar(final long id) {
        if (carDao.findById(id).isEmpty()) {
            LOGGER.warn("delete car rejected: not found id={}", id);
            return false;
        }
        final boolean deleted = carDao.delete(id);
        if (deleted) {
            LOGGER.info("deleted car id={}", id);
        }
        return deleted;
    }

    private void validateImagePair(final Optional<String> imageContentType, final Optional<byte[]> imageData) {
        final boolean hasImageContentType = imageContentType.isPresent();
        final boolean hasImageData = imageData.isPresent();
        if (hasImageContentType != hasImageData) {
            throw new InvalidImagePayloadException("Image metadata and payload must be provided together.");
        }
    }
}
