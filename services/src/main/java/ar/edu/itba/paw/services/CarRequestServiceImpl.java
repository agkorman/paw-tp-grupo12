package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarRequestImage;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarImageDao;
import ar.edu.itba.paw.persistence.CarRequestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
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
    public List<CarRequest> getAllCarRequests() {
        return carRequestDao.findAll();
    }

    @Override
    public List<CarRequest> getCarRequestsByStatus(final String status) {
        final String normalizedStatus = normalize(status);
        if (normalizedStatus == null) {
            return Collections.emptyList();
        }
        return carRequestDao.findByStatus(normalizedStatus);
    }

    @Override
    @Transactional
    public CarRequest createPendingRequest(final long submittedByUserId, final String submitterEmail,
                                           final long brandId, final long bodyTypeId, final String model,
                                           final String description, final Optional<String> imageContentType,
                                           final Optional<byte[]> imageData, final String fuelType,
                                           final Integer horsepower, final Integer airbagCount,
                                           final String transmission, final BigDecimal fuelConsumption,
                                           final Integer maxSpeedKmh) {
        final boolean hasImageContentType = imageContentType.isPresent();
        final boolean hasImageData = imageData.isPresent();
        if (hasImageContentType != hasImageData) {
            throw new IllegalArgumentException("Image metadata and payload must be provided together.");
        }
        final List<CarImagePayload> images = hasImageContentType
                ? List.of(new CarImagePayload(imageContentType.orElseThrow(), imageData.orElseThrow()))
                : Collections.emptyList();
        return createPendingRequest(
                submittedByUserId,
                submitterEmail,
                brandId,
                bodyTypeId,
                model,
                description,
                images,
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
    public CarRequest createPendingRequest(final long submittedByUserId, final String submitterEmail,
                                           final long brandId, final long bodyTypeId, final String model,
                                           final String description, final List<CarImagePayload> images,
                                           final String fuelType, final Integer horsepower,
                                           final Integer airbagCount, final String transmission,
                                           final BigDecimal fuelConsumption, final Integer maxSpeedKmh) {
        final String normalizedModel = normalize(model);
        if (normalizedModel == null) {
            throw new IllegalArgumentException("Model is required for car requests.");
        }

        final String normalizedDescription = normalize(description);
        if (normalizedDescription == null) {
            throw new IllegalArgumentException("Description is required for car requests.");
        }
        final List<CarImagePayload> normalizedImages = normalizeImages(images);
        final CarImagePayload coverImage = normalizedImages.isEmpty() ? null : normalizedImages.get(0);

        try {
            final CarRequest request = carRequestDao.create(
                    submittedByUserId,
                    submitterEmail,
                    brandId,
                    bodyTypeId,
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
                    maxSpeedKmh
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
                request.getDescription(),
                Optional.empty(),
                Optional.empty(),
                request.getFuelType(),
                request.getHorsepower(),
                request.getAirbagCount(),
                request.getTransmission(),
                request.getFuelConsumption(),
                request.getMaxSpeedKmh()
        );
    }

    @Override
    @Transactional
    public boolean approvePendingRequest(final long id, final long brandId, final String model,
                                         final long bodyTypeId, final String description,
                                         final Optional<String> imageContentType,
                                         final Optional<byte[]> imageData,
                                         final String fuelType, final Integer horsepower,
                                         final Integer airbagCount, final String transmission,
                                         final BigDecimal fuelConsumption, final Integer maxSpeedKmh) {
        final CarRequest request = carRequestDao.findById(id).orElse(null);
        if (request == null || !STATUS_PENDING.equals(request.getStatus())) {
            return false;
        }

        final String normalizedModel = normalize(model);
        if (normalizedModel == null) {
            throw new IllegalArgumentException("Model is required to approve car requests.");
        }

        final String normalizedDescription = normalize(description);
        if (normalizedDescription == null) {
            throw new IllegalArgumentException("Description is required to approve car requests.");
        }

        final boolean hasImageContentType = imageContentType.isPresent();
        final boolean hasImageData = imageData.isPresent();
        if (hasImageContentType != hasImageData) {
            throw new IllegalArgumentException("Image metadata and payload must be provided together.");
        }

        final boolean statusUpdated = carRequestDao.updateStatus(id, STATUS_PENDING, STATUS_APPROVED);
        if (!statusUpdated) {
            return false;
        }

        final Car createdCar = carDao.create(
                brandId,
                normalizedModel,
                bodyTypeId,
                normalizedDescription,
                fuelType,
                horsepower,
                airbagCount,
                transmission,
                fuelConsumption,
                maxSpeedKmh
        );
        final String contentTypeToPersist = imageContentType.orElse(request.getImageContentType());
        final byte[] imageDataToPersist = imageData.orElse(request.getImageData());
        if (imageContentType.isPresent() && imageData.isPresent()) {
            carImageDao.saveOrReplace(createdCar.getId(), contentTypeToPersist, imageDataToPersist);
        } else {
            final List<CarImagePayload> requestGallery = carRequestDao.findImagesByRequestId(id)
                    .stream()
                    .map(image -> carRequestDao.findImageByRequestIdAndImageId(id, image.getImageId()).orElse(null))
                    .filter(image -> image != null && image.getImageData() != null)
                    .map(image -> new CarImagePayload(image.getContentType(), image.getImageData()))
                    .toList();
            if (!requestGallery.isEmpty()) {
                carImageDao.replaceAll(createdCar.getId(), requestGallery);
            } else if (contentTypeToPersist != null && imageDataToPersist != null) {
                carImageDao.saveOrReplace(createdCar.getId(), contentTypeToPersist, imageDataToPersist);
            }
        }
        return true;
    }

    @Override
    public boolean rejectPendingRequest(final long id) {
        return carRequestDao.updateStatus(id, STATUS_PENDING, STATUS_REJECTED);
    }

    private String normalize(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<CarImagePayload> normalizeImages(final List<CarImagePayload> images) {
        if (images == null) {
            return Collections.emptyList();
        }
        for (final CarImagePayload image : images) {
            if (image == null || image.getContentType() == null || image.getContentType().isBlank()
                    || image.getImageData() == null || image.getImageData().length == 0) {
                throw new IllegalArgumentException("Image metadata and payload must be provided together.");
            }
        }
        return List.copyOf(images);
    }
}
