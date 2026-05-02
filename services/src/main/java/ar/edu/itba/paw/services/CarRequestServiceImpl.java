package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarRequestImage;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarImageDao;
import ar.edu.itba.paw.persistence.CarRequestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CarRequestServiceImpl implements CarRequestService {

    private final CarRequestDao carRequestDao;
    private final CarDao carDao;
    private final CarImageDao carImageDao;

    @Autowired
    public CarRequestServiceImpl(final CarRequestDao carRequestDao, final CarDao carDao,
                                 final CarImageDao carImageDao) {
        this.carRequestDao = carRequestDao;
        this.carDao = carDao;
        this.carImageDao = carImageDao;
    }

    @Override
    public Optional<CarRequest> getCarRequestById(final long id) {
        return carRequestDao.findById(id);
    }

    @Override
    public List<CarRequest> getCarRequestsByStatus(final String status) {
        final String normalizedStatus = StringUtils.normalize(status);
        if (normalizedStatus == null) {
            return List.of();
        }
        return carRequestDao.findByStatus(normalizedStatus);
    }

    @Override
    public Page<CarRequest> getCarRequestsByStatus(final String status, final int page) {
        final String normalizedStatus = StringUtils.normalize(status);
        if (normalizedStatus == null) {
            return Page.empty(page < 1 ? 1 : page, 0);
        }
        return carRequestDao.findByStatus(normalizedStatus, page);
    }

    @Override
    public long countCarRequestsByStatus(final String status) {
        final String normalizedStatus = StringUtils.normalize(status);
        if (normalizedStatus == null) {
            return 0L;
        }
        return carRequestDao.countByStatus(normalizedStatus);
    }

    @Override
    @Transactional
    public CarRequest createPendingRequest(final long submittedByUserId, final String submitterEmail,
                                           final long brandId, final long bodyTypeId, final Integer year,
                                           final String model, final String description, final List<CarImagePayload> images,
                                           final String fuelType, final Integer horsepower,
                                           final Integer airbagCount, final String transmission,
                                           final BigDecimal fuelConsumption, final Integer maxSpeedKmh,
                                           final BigDecimal priceUsd) {
        final String normalizedModel = StringUtils.normalizeRequired(model, "Model is required for car requests.");
        final String normalizedDescription = StringUtils.normalizeRequired(description, "Description is required for car requests.");
        final List<CarImagePayload> normalizedImages = ImagePayloadUtils.normalizeImages(images);
        final CarImagePayload coverImage = normalizedImages.isEmpty() ? null : normalizedImages.get(0);

        try {
            final CarRequest request = carRequestDao.create(
                    submittedByUserId,
                    submitterEmail,
                    brandId,
                    bodyTypeId,
                    year,
                    normalizedModel,
                    normalizedDescription,
                    coverImage == null ? null : coverImage.getContentType(),
                    coverImage == null ? null : coverImage.getImageData(),
                    STATUS_PENDING,
                    fuelType,
                    horsepower,
                    airbagCount,
                    transmission,
                    fuelConsumption,
                    maxSpeedKmh,
                    priceUsd
            );
            if (!normalizedImages.isEmpty()) {
                carRequestDao.replaceImages(request.getId(), normalizedImages);
            }
            return request;
        } catch (final RuntimeException e) {
            throw new IllegalStateException("Failed to create pending car request with image gallery.", e);
        }
    }

    @Override
    public List<CarRequestImage> getCarRequestImages(final long requestId) {
        return carRequestDao.findImagesByRequestId(requestId);
    }

    @Override
    public Optional<CarRequestImage> getCarRequestImageById(final long requestId, final long imageId) {
        return carRequestDao.findImageByRequestIdAndImageId(requestId, imageId);
    }

    @Override
    @Transactional
    public boolean approvePendingRequest(final long id) {
        final CarRequest request = carRequestDao.findById(id).orElse(null);
        if (request == null || !STATUS_PENDING.equals(request.getStatus())) {
            return false;
        }

        return approvePendingRequest(
                id,
                request.getBrandId(),
                request.getModel(),
                request.getBodyTypeId(),
                request.getYear(),
                request.getDescription(),
                Optional.empty(),
                Optional.empty(),
                request.getFuelType(),
                request.getHorsepower(),
                request.getAirbagCount(),
                request.getTransmission(),
                request.getFuelConsumption(),
                request.getMaxSpeedKmh(),
                request.getPriceUsd()
        );
    }

    @Override
    @Transactional
    public boolean approvePendingRequest(final long id, final long brandId, final String model,
                                         final long bodyTypeId, final Integer year, final String description,
                                         final Optional<String> imageContentType,
                                         final Optional<byte[]> imageData,
                                         final String fuelType, final Integer horsepower,
                                         final Integer airbagCount, final String transmission,
                                         final BigDecimal fuelConsumption, final Integer maxSpeedKmh,
                                         final BigDecimal priceUsd) {
        final CarRequest request = carRequestDao.findById(id).orElse(null);
        if (request == null || !STATUS_PENDING.equals(request.getStatus())) {
            return false;
        }

        final String normalizedModel = StringUtils.normalizeRequired(model, "Model is required to approve car requests.");
        final String normalizedDescription = StringUtils.normalizeRequired(description, "Description is required to approve car requests.");

        final boolean hasImageContentType = imageContentType.isPresent();
        final boolean hasImageData = imageData.isPresent();
        if (hasImageContentType != hasImageData) {
            throw new IllegalArgumentException("Image metadata and payload must be provided together.");
        }

        final List<CarImagePayload> replacementImages = hasImageContentType
                ? List.of(new CarImagePayload(imageContentType.get(), imageData.orElseThrow()))
                : List.of();

        return approvePendingRequest(id, brandId, normalizedModel, bodyTypeId, year, normalizedDescription, replacementImages,
                fuelType, horsepower, airbagCount, transmission, fuelConsumption, maxSpeedKmh, priceUsd);
    }

    @Override
    @Transactional
    public boolean approvePendingRequest(final long id, final long brandId, final String model,
                                         final long bodyTypeId, final Integer year, final String description,
                                         final List<CarImagePayload> images,
                                         final String fuelType, final Integer horsepower,
                                         final Integer airbagCount, final String transmission,
                                         final BigDecimal fuelConsumption, final Integer maxSpeedKmh,
                                         final BigDecimal priceUsd) {
        final CarRequest request = carRequestDao.findById(id).orElse(null);
        if (request == null || !STATUS_PENDING.equals(request.getStatus())) {
            return false;
        }

        final String normalizedModel = StringUtils.normalizeRequired(model, "Model is required to approve car requests.");
        final String normalizedDescription = StringUtils.normalizeRequired(description, "Description is required to approve car requests.");
        final List<CarImagePayload> normalizedImages = ImagePayloadUtils.normalizeImages(images);

        final boolean statusUpdated = carRequestDao.updateStatus(id, STATUS_PENDING, STATUS_APPROVED);
        if (!statusUpdated) {
            return false;
        }

        final Car createdCar = carDao.create(
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
        final String contentTypeToPersist = request.getImageContentType();
        final byte[] imageDataToPersist = request.getImageData();
        if (!normalizedImages.isEmpty()) {
            final List<CarImagePayload> requestGallery = requestImagePayloads(request);
            final List<CarImagePayload> combinedImages = new ArrayList<>(requestGallery);
            combinedImages.addAll(normalizedImages);
            carImageDao.replaceAll(createdCar.getId(), combinedImages);
        } else {
            final List<CarImagePayload> requestGallery = requestImagePayloads(request);
            if (!requestGallery.isEmpty()) {
                carImageDao.replaceAll(createdCar.getId(), requestGallery);
            } else if (contentTypeToPersist != null && imageDataToPersist != null) {
                carImageDao.saveOrReplace(createdCar.getId(), contentTypeToPersist, imageDataToPersist);
            }
        }
        return true;
    }

    private List<CarImagePayload> requestImagePayloads(final CarRequest request) {
        final List<CarImagePayload> galleryPayloads = carRequestDao.findImagesByRequestId(request.getId())
                .stream()
                .map(image -> carRequestDao.findImageByRequestIdAndImageId(request.getId(), image.getImageId()).orElse(null))
                .filter(image -> image != null && image.getImageData() != null)
                .map(image -> new CarImagePayload(image.getContentType(), image.getImageData()))
                .toList();
        if (!galleryPayloads.isEmpty()) {
            return galleryPayloads;
        }
        if (request.getImageContentType() != null && request.getImageData() != null) {
            return List.of(new CarImagePayload(request.getImageContentType(), request.getImageData()));
        }
        return List.of();
    }

    @Override
    public boolean rejectPendingRequest(final long id) {
        return carRequestDao.updateStatus(id, STATUS_PENDING, STATUS_REJECTED);
    }

}
