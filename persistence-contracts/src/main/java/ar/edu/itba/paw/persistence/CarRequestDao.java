package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.StoredImagePayload;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CarRequestDao {
    Optional<CarRequest> findById(long id);

    Page<CarRequest> findByStatus(String status, int page);

    long countByStatus(String status);

    CarRequest create(Long submittedByUserId, String submitterEmail, long brandId, long bodyTypeId, Integer year,
                      String model, String description, String status,
                      String fuelType, Integer horsepower, Integer airbagCount,
                      String transmission, BigDecimal fuelConsumption, Integer maxSpeedKmh, BigDecimal priceUsd);

    List<ImageMetadata> findImagesByRequestId(long requestId);

    List<ImageMetadata> findImagesByRequestIds(Collection<Long> requestIds);

    List<ImageMetadata> findLegacyImagesByRequestIds(Collection<Long> requestIds);

    List<StoredImagePayload> findImagesByRequestIdWithData(long requestId);

    List<StoredImagePayload> findImagesByRequestIdAndImageIdsWithData(long requestId, Collection<Long> imageIds);

    Optional<StoredImagePayload> findImageByRequestIdAndImageId(long requestId, long imageId);

    Optional<ImageMetadata> findImageMetadataByRequestIdAndImageId(long requestId, long imageId);

    Optional<ImageMetadata> findFirstImageMetadataByRequestId(long requestId);

    Optional<StoredImagePayload> findLegacyImageByRequestId(long requestId);

    Optional<ImageMetadata> findLegacyImageMetadataByRequestId(long requestId);

    void replaceImages(long requestId, List<ImagePayload> images);

    boolean updateStatus(long id, String expectedStatus, String newStatus);

    int bindRequestsToUserByEmail(long userId, String email);
}
