package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.StoredImagePayload;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarImageDao;
import ar.edu.itba.paw.persistence.CarRequestDao;
import ar.edu.itba.paw.services.exception.DuplicateCarException;
import ar.edu.itba.paw.services.exception.InvalidImagePayloadException;
import ar.edu.itba.paw.services.exception.ServiceOperationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CarRequestServiceImpl implements CarRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        CarRequestServiceImpl.class
    );

    private final CarRequestDao carRequestDao;
    private final CarDao carDao;
    private final CarImageDao carImageDao;
    private final BrandDao brandDao;
    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public CarRequestServiceImpl(
        final CarRequestDao carRequestDao,
        final CarDao carDao,
        final CarImageDao carImageDao,
        final BrandDao brandDao,
        final UserService userService,
        final EmailService emailService
    ) {
        this.carRequestDao = carRequestDao;
        this.carDao = carDao;
        this.carImageDao = carImageDao;
        this.brandDao = brandDao;
        this.userService = userService;
        this.emailService = emailService;
    }

    public CarRequestServiceImpl(
        final CarRequestDao carRequestDao,
        final CarDao carDao,
        final CarImageDao carImageDao
    ) {
        this(carRequestDao, carDao, carImageDao, null, null, null);
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
    public Page<CarRequest> getCarRequestsByStatus(
        final String status,
        final int page
    ) {
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
    public CarRequest createPendingRequest(
        final long submittedByUserId,
        final String submitterEmail,
        final long brandId,
        final long bodyTypeId,
        final Integer year,
        final String model,
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
            "Model is required for car requests."
        );
        final String normalizedDescription = StringUtils.normalizeRequired(
            description,
            "Description is required for car requests."
        );
        final List<ImagePayload> normalizedImages =
            ImagePayloadUtils.normalizeImages(images);

        try {
            final CarRequest request = carRequestDao.create(
                submittedByUserId,
                submitterEmail,
                brandId,
                bodyTypeId,
                year,
                normalizedModel,
                normalizedDescription,
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
        } catch (final DataAccessException e) {
            throw new ServiceOperationException(
                "Failed to create pending car request with image gallery.",
                e
            );
        }
    }

    @Override
    public List<ImageMetadata> getCarRequestImages(final long requestId) {
        final List<ImageMetadata> images = carRequestDao.findImagesByRequestId(
            requestId
        );
        if (!images.isEmpty()) {
            return images;
        }
        return carRequestDao.findLegacyImagesByRequestIds(List.of(requestId));
    }

    @Override
    public List<ImageMetadata> getCarRequestImagesByRequestIds(
        final Collection<Long> requestIds
    ) {
        final List<ImageMetadata> images = carRequestDao.findImagesByRequestIds(
            requestIds
        );
        if (requestIds == null || requestIds.isEmpty()) {
            return images;
        }
        final java.util.Set<Long> requestsWithGallery = images
            .stream()
            .map(ImageMetadata::getOwnerId)
            .collect(java.util.stream.Collectors.toSet());
        final List<Long> legacyCandidateIds = requestIds
            .stream()
            .filter(java.util.Objects::nonNull)
            .distinct()
            .filter(id -> !requestsWithGallery.contains(id))
            .toList();
        if (legacyCandidateIds.isEmpty()) {
            return images;
        }
        final List<ImageMetadata> combined = new ArrayList<>(images);
        combined.addAll(carRequestDao.findLegacyImagesByRequestIds(legacyCandidateIds));
        return combined;
    }

    @Override
    public Optional<StoredImagePayload> getPrimaryCarRequestImage(
        final long requestId
    ) {
        final List<ImageMetadata> images = getCarRequestImages(requestId);
        if (images.isEmpty()) {
            return Optional.empty();
        }
        return getCarRequestImageById(requestId, images.get(0).getImageId());
    }

    @Override
    public Optional<ImageMetadata> getPrimaryCarRequestImageMetadata(
        final long requestId
    ) {
        final Optional<ImageMetadata> gallery =
            carRequestDao.findFirstImageMetadataByRequestId(requestId);
        if (gallery.isPresent()) {
            return gallery;
        }
        return carRequestDao.findLegacyImageMetadataByRequestId(requestId);
    }

    @Override
    public Optional<StoredImagePayload> getCarRequestImageById(
        final long requestId,
        final long imageId
    ) {
        if (imageId == CarService.LEGACY_IMAGE_ID) {
            return carRequestDao.findLegacyImageByRequestId(requestId);
        }
        return carRequestDao.findImageByRequestIdAndImageId(requestId, imageId);
    }

    @Override
    public Optional<ImageMetadata> getCarRequestImageMetadataById(
        final long requestId,
        final long imageId
    ) {
        if (imageId == CarService.LEGACY_IMAGE_ID) {
            return carRequestDao.findLegacyImageMetadataByRequestId(requestId);
        }
        return carRequestDao.findImageMetadataByRequestIdAndImageId(requestId, imageId);
    }

    @Override
    @Transactional
    public boolean approvePendingRequest(final long id) {
        final Optional<CarRequest> requestOptional = carRequestDao.findById(id);
        if (requestOptional.isEmpty() || !STATUS_PENDING.equals(requestOptional.get().getStatus())) {
            return false;
        }
        final CarRequest request = requestOptional.get();

        return approvePendingRequest(
            id,
            request.getBrandId(),
            request.getModel(),
            request.getBodyTypeId(),
            request.getYear(),
            request.getDescription(),
            null,
            null,
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
    public boolean approvePendingRequest(
        final long id,
        final long brandId,
        final String model,
        final long bodyTypeId,
        final Integer year,
        final String description,
        final String imageContentType,
        final byte[] imageData,
        final String fuelType,
        final Integer horsepower,
        final Integer airbagCount,
        final String transmission,
        final BigDecimal fuelConsumption,
        final Integer maxSpeedKmh,
        final BigDecimal priceUsd
    ) {
        final Optional<CarRequest> requestOptional = carRequestDao.findById(id);
        if (requestOptional.isEmpty() || !STATUS_PENDING.equals(requestOptional.get().getStatus())) {
            return false;
        }
        final CarRequest request = requestOptional.get();

        final String normalizedModel = StringUtils.normalizeRequired(
            model,
            "Model is required to approve car requests."
        );
        final String normalizedDescription = StringUtils.normalizeRequired(
            description,
            "Description is required to approve car requests."
        );

        final boolean hasImageContentType = imageContentType != null;
        final boolean hasImageData = imageData != null;
        if (hasImageContentType != hasImageData) {
            throw new InvalidImagePayloadException(
                "Image metadata and payload must be provided together."
            );
        }

        final List<ImagePayload> replacementImages = hasImageContentType
            ? List.of(
                  new ImagePayload(
                      imageContentType,
                      imageData
                  )
              )
            : List.of();
        final List<ImagePayload> approvalImages = new ArrayList<>(
            requestImagePayloads(request)
        );
        approvalImages.addAll(replacementImages);

        return approvePendingRequest(
            id,
            brandId,
            normalizedModel,
            bodyTypeId,
            year,
            normalizedDescription,
            approvalImages,
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
    public boolean approvePendingRequest(
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
        final Optional<CarRequest> requestOptional = carRequestDao.findById(id);
        if (requestOptional.isEmpty() || !STATUS_PENDING.equals(requestOptional.get().getStatus())) {
            return false;
        }
        final CarRequest request = requestOptional.get();

        final String normalizedModel = StringUtils.normalizeRequired(
            model,
            "Model is required to approve car requests."
        );
        final String normalizedDescription = StringUtils.normalizeRequired(
            description,
            "Description is required to approve car requests."
        );

        if (
            existsDuplicateCarByIds(
                brandId,
                bodyTypeId,
                normalizedModel,
                year,
                -1L
            )
        ) {
            throw new DuplicateCarException();
        }

        final List<ImagePayload> normalizedImages =
            ImagePayloadUtils.normalizeImages(images);

        final boolean statusUpdated = carRequestDao.updateStatus(
            id,
            STATUS_PENDING,
            STATUS_APPROVED
        );
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
        if (!normalizedImages.isEmpty()) {
            carImageDao.replaceAll(createdCar.getId(), normalizedImages);
        } else if (images == null) {
            final List<ImagePayload> requestGallery = requestImagePayloads(
                request
            );
            if (!requestGallery.isEmpty()) {
                carImageDao.replaceAll(createdCar.getId(), requestGallery);
            }
        }
        sendCarApprovedNotification(
            request,
            brandId,
            normalizedModel,
            createdCar.getId()
        );
        LOGGER.info(
            "Approved car request id={} -> car id={} model={}",
            id,
            createdCar.getId(),
            normalizedModel
        );
        return true;
    }

    @Override
    public List<ImagePayload> collectRetainedImagePayloads(
        final long requestId,
        final List<Long> retainedImageIds
    ) {
        final List<ImagePayload> payloads = new ArrayList<>();
        if (retainedImageIds == null) {
            return payloads;
        }
        final Optional<CarRequest> requestOptional = carRequestDao.findById(requestId);
        if (requestOptional.isEmpty()) {
            return payloads;
        }
        final CarRequest request = requestOptional.get();
        final List<Long> nonLegacyIds = new ArrayList<>();
        for (final Long imageId : retainedImageIds) {
            if (imageId != null && imageId != CarService.LEGACY_IMAGE_ID) {
                nonLegacyIds.add(imageId);
            }
        }
        final java.util.Map<Long, StoredImagePayload> imagesById =
            new java.util.LinkedHashMap<>();
        for (final StoredImagePayload image : carRequestDao
            .findImagesByRequestIdAndImageIdsWithData(requestId, nonLegacyIds)) {
            imagesById.putIfAbsent(image.getImageId(), image);
        }
        final Optional<StoredImagePayload> legacyImage =
            retainedImageIds.contains(CarService.LEGACY_IMAGE_ID)
                ? carRequestDao.findLegacyImageByRequestId(requestId)
                : Optional.empty();
        for (final Long imageId : retainedImageIds) {
            if (imageId == null) {
                continue;
            }
            if (imageId == CarService.LEGACY_IMAGE_ID) {
                if (legacyImage.isPresent()) {
                    final StoredImagePayload image = legacyImage.get();
                    payloads.add(
                        new ImagePayload(
                            image.getContentType(),
                            image.getImageData()
                        )
                    );
                }
            } else {
                final StoredImagePayload image = imagesById.get(imageId);
                if (image != null && image.getImageData() != null) {
                    payloads.add(
                        new ImagePayload(
                            image.getContentType(),
                            image.getImageData()
                        )
                    );
                }
            }
        }
        return payloads;
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

    private List<ImagePayload> requestImagePayloads(
        final CarRequest request
    ) {
        final List<ImagePayload> galleryPayloads = carRequestDao
            .findImagesByRequestIdWithData(request.getId())
            .stream()
            .filter(image -> image.getImageData() != null)
            .map(image ->
                new ImagePayload(
                    image.getContentType(),
                    image.getImageData()
                )
            )
            .toList();
        if (!galleryPayloads.isEmpty()) {
            return galleryPayloads;
        }
        return carRequestDao
            .findLegacyImageByRequestId(request.getId())
            .filter(image -> image.getImageData() != null)
            .map(image ->
                List.of(
                    new ImagePayload(
                        image.getContentType(),
                        image.getImageData()
                    )
                )
            )
            .orElseGet(List::of);
    }

    @Override
    @Transactional
    public boolean rejectPendingRequest(final long id) {
        final Optional<CarRequest> requestOptional = carRequestDao.findById(id);
        if (requestOptional.isEmpty() || !STATUS_PENDING.equals(requestOptional.get().getStatus())) {
            return false;
        }
        final CarRequest request = requestOptional.get();

        final boolean statusUpdated = carRequestDao.updateStatus(
            id,
            STATUS_PENDING,
            STATUS_REJECTED
        );
        if (statusUpdated) {
            sendCarRejectedNotification(request);
            LOGGER.info("Rejected car request id={}", id);
        }
        return statusUpdated;
    }

    private void sendCarApprovedNotification(
        final CarRequest request,
        final long brandId,
        final String model,
        final long carId
    ) {
        if (emailService == null) {
            return;
        }
        final String recipientEmail = resolveSubmitterEmail(request);
        if (recipientEmail == null) {
            return;
        }
        emailService.sendCarApprovedNotification(
            recipientEmail,
            resolveBrandName(brandId),
            model,
            carId
        );
    }

    private void sendCarRejectedNotification(final CarRequest request) {
        if (emailService == null) {
            return;
        }
        final String recipientEmail = resolveSubmitterEmail(request);
        if (recipientEmail == null) {
            return;
        }
        emailService.sendCarRejectedNotification(
            recipientEmail,
            resolveBrandName(request.getBrandId()),
            request.getModel()
        );
    }

    private String resolveSubmitterEmail(final CarRequest request) {
        if (
            request.getSubmitterEmail() != null &&
            !request.getSubmitterEmail().isBlank()
        ) {
            return request.getSubmitterEmail();
        }
        if (userService == null || request.getSubmittedByUserId() == null) {
            return null;
        }
        return userService
            .getUserById(request.getSubmittedByUserId())
            .map(User::getEmail)
            .filter(email -> !email.isBlank())
            .orElse(null);
    }

    private String resolveBrandName(final long brandId) {
        if (brandDao == null) {
            return "-";
        }
        return brandDao
            .findById(brandId)
            .map(brand -> brand.getName())
            .filter(name -> !name.isBlank())
            .orElse("-");
    }
}
