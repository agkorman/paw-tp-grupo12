package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.CarYearVariant;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.StoredImagePayload;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarImageDao;
import ar.edu.itba.paw.services.exception.DuplicateCarException;
import ar.edu.itba.paw.services.exception.InvalidImagePayloadException;
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
    private final CarRequestService carRequestService;
    private final BrandDao brandDao;
    private final BodyTypeDao bodyTypeDao;
    private final EmailService emailService;

    @Autowired
    public CarServiceImpl(
        final CarDao carDao,
        final CarImageDao carImageDao,
        final CarRequestService carRequestService,
        final BrandDao brandDao,
        final BodyTypeDao bodyTypeDao,
        final EmailService emailService
    ) {
        this.carDao = carDao;
        this.carImageDao = carImageDao;
        this.carRequestService = carRequestService;
        this.brandDao = brandDao;
        this.bodyTypeDao = bodyTypeDao;
        this.emailService = emailService;
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
    public List<Car> getCarsByBrandAndBodyType(
        final String brand,
        final String bodyType
    ) {
        return brandDao
            .findByName(brand)
            .flatMap(b ->
                bodyTypeDao
                    .findByName(bodyType)
                    .map(bt ->
                        carDao.findByBrandIdAndBodyTypeId(b.getId(), bt.getId())
                    )
            )
            .orElse(Collections.emptyList());
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
        carImageDao.replaceAll(
            carId,
            ImagePayloadUtils.normalizeImages(images)
        );
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
    public CarRequest requestCarCreation(
        final long brandId,
        final String model,
        final long bodyTypeId,
        final Integer year,
        final long submittedByUserId,
        final String submitterEmail,
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
        final String normalizedDescription = StringUtils.normalizeRequired(
            description,
            "Description is required for car creation."
        );
        final List<ImagePayload> normalizedImages =
            ImagePayloadUtils.normalizeImages(images);
        if (normalizedImages.isEmpty()) {
            throw new InvalidImagePayloadException(
                "At least one image is required for car creation."
            );
        }

        validateYear(year);

        if (existsDuplicateCarByIds(brandId, bodyTypeId, model, year, -1L)) {
            throw new DuplicateCarException();
        }

        final CarRequest carRequest = carRequestService.createPendingRequest(
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

        final String brandName = brandDao
            .findById(brandId)
            .map(Brand::getName)
            .orElse("-");
        final String bodyTypeName = bodyTypeDao
            .findById(bodyTypeId)
            .map(BodyType::getName)
            .orElse("-");
        emailService.sendNewCarRequestNotification(
            carRequest,
            brandName,
            bodyTypeName,
            !normalizedImages.isEmpty()
        );

        return carRequest;
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
        final String lowerModel = normalizedModel.toLowerCase(Locale.ROOT);
        return brandDao
            .findByName(brandName)
            .flatMap(brand ->
                bodyTypeDao
                    .findByName(bodyTypeName)
                    .map(bodyType ->
                        carDao.existsByBrandIdAndBodyTypeIdAndModelAndYearExcludingId(
                            brand.getId(),
                            bodyType.getId(),
                            lowerModel,
                            year,
                            ignoredCarId
                        )
                    )
            )
            .orElse(false);
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

    private boolean existsDuplicateCarByIds(
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
