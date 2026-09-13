package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.CarYearVariant;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.StoredImagePayload;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarImageDao;
import ar.edu.itba.paw.services.exception.DuplicateCarException;
import ar.edu.itba.paw.services.exception.InvalidServiceInputException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CarServiceImpl implements CarService {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        CarServiceImpl.class
    );

    private final CarDao carDao;
    private final CarImageDao carImageDao;

    @Autowired
    public CarServiceImpl(
        final CarDao carDao,
        final CarImageDao carImageDao
    ) {
        this.carDao = carDao;
        this.carImageDao = carImageDao;
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
    public Page<Car> searchCars(final CarSearchCriteria criteria) {
        normalizeAndValidateSearchCriteria(criteria);
        return carDao.findByCriteria(criteria);
    }

    private void normalizeAndValidateSearchCriteria(final CarSearchCriteria criteria) {
        // Ignore fuel consumption filter for electric-only searches
        if (criteria.isElectricOnly()) {
            criteria.setFuelConsumptionMax(null);
        }
    }

    @Override
    public Optional<StoredImagePayload> getCarImageByCarId(final long carId) {
        return carImageDao.findFirstByCarIdWithData(carId);
    }

    @Override
    public Optional<ImageMetadata> getCarImageMetadataByCarId(final long carId) {
        return carImageDao.findFirstMetadataByCarId(carId);
    }

    @Override
    public List<ImageMetadata> getCarImagesByCarId(final long carId) {
        return carImageDao.findAllByCarId(carId);
    }

    @Override
    public Optional<StoredImagePayload> getCarImageById(
        final long carId,
        final long imageId
    ) {
        return carImageDao.findByCarIdAndImageId(carId, imageId);
    }

    @Override
    public Optional<ImageMetadata> getCarImageMetadataById(
        final long carId,
        final long imageId
    ) {
        return carImageDao.findMetadataByCarIdAndImageId(carId, imageId);
    }

    @Override
    @Transactional
    public void saveCarImages(
        final long carId,
        final List<ImagePayload> images
    ) {
        final List<ImagePayload> normalizedImages = ImagePayloadUtils.normalizeImages(images);
        carImageDao.replaceAll(carId, normalizedImages);
        LOGGER.info("saved car images carId={} imageCount={}", carId, normalizedImages.size());
    }

    @Override
    @Transactional
    public void appendCarImages(
        final long carId,
        final List<ImagePayload> images
    ) {
        final List<ImagePayload> normalizedImages =
            ImagePayloadUtils.normalizeImages(images);
        if (normalizedImages.isEmpty()) {
            return;
        }
        carImageDao.appendAll(carId, normalizedImages);
    }

    @Override
    @Transactional
    public Car createCar(
        final long brandId,
        final String model,
        final long bodyTypeId,
        final Integer year,
        final String description,
        final String fuelType,
        final Integer horsepower,
        final Integer airbagCount,
        final String transmission,
        final BigDecimal fuelConsumption,
        final Integer maxSpeedKmh,
        final BigDecimal priceUsd
    ) {
        final Car created = carDao.create(
            brandId,
            model,
            bodyTypeId,
            year,
            description,
            fuelType,
            horsepower,
            airbagCount,
            transmission,
            fuelConsumption,
            maxSpeedKmh,
            priceUsd
        );
        LOGGER.info("created car id={} model={}", created.getId(), model);
        return created;
    }

    @Override
    @Transactional
    public Optional<Car> updateCar(
        final long id,
        final long brandId,
        final String model,
        final long bodyTypeId,
        final Integer year,
        final String description,
        final List<ImagePayload> images,
        final String fuelType,
        final Integer horsepower,
        final Integer airbagCount,
        final String transmission,
        final BigDecimal fuelConsumption,
        final Integer maxSpeedKmh,
        final BigDecimal priceUsd
    ) {
        final String normalizedModel = StringUtils.normalizeRequired(
            model,
            "Model is required for car update."
        );
        final String normalizedDescription = StringUtils.normalizeRequired(
            description,
            "Description is required for car update."
        );
        validateYear(year);

        if (
            existsDuplicateCarByIds(
                brandId,
                bodyTypeId,
                normalizedModel,
                year,
                id
            )
        ) {
            LOGGER.warn("update car rejected: duplicate car id={} brandId={} bodyTypeId={}", id, brandId, bodyTypeId);
            throw new DuplicateCarException();
        }

        final Optional<Car> updated = carDao.update(
            id,
            brandId,
            normalizedModel,
            bodyTypeId,
            year,
            normalizedDescription,
            fuelType,
            horsepower,
            airbagCount,
            transmission,
            fuelConsumption,
            maxSpeedKmh,
            priceUsd
        );
        if (updated.isPresent() && images != null) {
            carImageDao.replaceAll(
                id,
                ImagePayloadUtils.normalizeImages(images)
            );
        }
        if (updated.isPresent()) {
            LOGGER.info(
                "updated car id={} brandId={} model={}",
                id,
                brandId,
                normalizedModel
            );
        }
        return updated;
    }

    @Override
    public List<Long> searchCarIds(final CarSearchCriteria criteria) {
        normalizeAndValidateSearchCriteria(criteria);
        return carDao.findIdsByCriteria(criteria);
    }

    @Override
    public long countCarsByBrandId(final long brandId) {
        return carDao.countByBrandId(brandId);
    }

    @Override
    public long countCarsByBodyTypeId(final long bodyTypeId) {
        return carDao.countByBodyTypeId(bodyTypeId);
    }

    @Override
    public List<Car> getFeaturedCars(final int limit) {
        final List<Car> topRated = carDao.findTopRated(limit);
        if (topRated.size() >= limit) {
            return topRated;
        }
        final List<Long> excludedIds = topRated
            .stream()
            .map(Car::getId)
            .toList();
        final List<Car> result = new java.util.ArrayList<>(topRated);
        result.addAll(
            carDao.findRecentlyAdded(limit - topRated.size(), excludedIds)
        );
        return result;
    }

    @Override
    public boolean existsDuplicateCar(
        final String brandName,
        final String bodyTypeName,
        final String model,
        final Integer year,
        final long ignoredCarId
    ) {
        final String normalizedModel = StringUtils.normalize(model);
        if (normalizedModel == null) {
            return false;
        }
        return carDao.existsByBrandNameAndBodyTypeNameAndModelAndYearExcludingId(
            brandName,
            bodyTypeName,
            normalizedModel.toLowerCase(Locale.ROOT),
            year,
            ignoredCarId
        );
    }

    @Override
    public List<ImagePayload> collectRetainedImagePayloads(
        final long carId,
        final List<Long> retainedImageIds
    ) {
        final List<ImagePayload> payloads = new ArrayList<>();
        if (retainedImageIds == null) {
            return payloads;
        }
        final List<Long> nonLegacyIds = retainedImageIds.stream()
            .filter(Objects::nonNull)
            .filter(imageId -> imageId != LEGACY_IMAGE_ID)
            .collect(Collectors.toList());
        final Map<Long, StoredImagePayload> imagesById = carImageDao
            .findByCarIdAndImageIdsWithData(carId, nonLegacyIds)
            .stream()
            .collect(
                Collectors.toMap(
                    StoredImagePayload::getImageId,
                    carImage -> carImage,
                    (existing, duplicate) -> existing,
                    LinkedHashMap::new
                )
            );
        for (final Long imageId : retainedImageIds) {
            if (imageId == null) {
                continue;
            }
            final StoredImagePayload image =
                imageId == LEGACY_IMAGE_ID
                    ? carImageDao.findFirstByCarIdWithData(carId).orElse(null)
                    : imagesById.get(imageId);
            if (image != null && image.getImageData() != null) {
                payloads.add(
                    new ImagePayload(
                        image.getContentType(),
                        image.getImageData()
                    )
                );
            }
        }
        return payloads;
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

    @Override
    public boolean existsDuplicateCarByIds(
        final long brandId,
        final long bodyTypeId,
        final String model,
        final Integer year,
        final long ignoredCarId
    ) {
        final String normalizedModel = StringUtils.normalize(model);
        if (normalizedModel == null) {
            return false;
        }
        return carDao.existsByBrandIdAndBodyTypeIdAndModelAndYearExcludingId(
            brandId,
            bodyTypeId,
            normalizedModel.toLowerCase(Locale.ROOT),
            year,
            ignoredCarId
        );
    }

    private void validateYear(final Integer year) {
        if (year == null) {
            return;
        }
        if (year < Car.MIN_YEAR || year > Car.MAX_YEAR) {
            throw new InvalidServiceInputException(
                "Year must be between " + Car.MIN_YEAR + " and " + Car.MAX_YEAR + "."
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarYearVariant> getYearVariants(final long carId) {
        final Optional<Car> selectedCarOptional = carDao.findById(carId);
        if (selectedCarOptional.isEmpty() || selectedCarOptional.get().getModel() == null) {
            return Collections.emptyList();
        }
        final Car selectedCar = selectedCarOptional.get();
        final String selectedModel = selectedCar.getModel().trim().toLowerCase(Locale.ROOT);
        return carDao.findByBrandIdAndBodyTypeIdAndModel(
            selectedCar.getBrandId(),
            selectedCar.getBodyTypeId(),
            selectedModel
        )
            .stream()
            .map(car -> new CarYearVariant(car.getId(), car.getYear(), car.getId() == selectedCar.getId()))
            .collect(Collectors.toList());
    }
}
