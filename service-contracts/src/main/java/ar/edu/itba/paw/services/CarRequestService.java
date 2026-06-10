package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.StoredImagePayload;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CarRequestService {
    String STATUS_PENDING = "pending";
    String STATUS_APPROVED = "approved";
    String STATUS_REJECTED = "rejected";

    Optional<CarRequest> getCarRequestById(long id);

    Page<CarRequest> getCarRequestsByStatus(String status, int page);

    long countCarRequestsByStatus(String status);

    CarRequest createPendingRequest(long submittedByUserId, String submitterEmail, long brandId,
                                    long bodyTypeId, Integer year, String model, String description,
                                    List<ImagePayload> images,
                                    String fuelType, Integer horsepower, Integer airbagCount,
                                    String transmission, BigDecimal fuelConsumption, Integer maxSpeedKmh,
                                    BigDecimal priceUsd);

    List<ImageMetadata> getCarRequestImages(long requestId);

    List<ImageMetadata> getCarRequestImagesByRequestIds(Collection<Long> requestIds);

    Optional<StoredImagePayload> getPrimaryCarRequestImage(long requestId);

    Optional<ImageMetadata> getPrimaryCarRequestImageMetadata(long requestId);

    Optional<StoredImagePayload> getCarRequestImageById(long requestId, long imageId);

    Optional<ImageMetadata> getCarRequestImageMetadataById(long requestId, long imageId);

    boolean approvePendingRequest(long id);

    boolean approvePendingRequest(long id, long brandId, String model, long bodyTypeId, Integer year, String description,
                                  String imageContentType, byte[] imageData,
                                  String fuelType, Integer horsepower, Integer airbagCount,
                                  String transmission, BigDecimal fuelConsumption, Integer maxSpeedKmh,
                                  BigDecimal priceUsd);

    boolean approvePendingRequest(long id, long brandId, String model, long bodyTypeId, Integer year, String description,
                                  List<ImagePayload> images,
                                  String fuelType, Integer horsepower, Integer airbagCount,
                                  String transmission, BigDecimal fuelConsumption, Integer maxSpeedKmh,
                                  BigDecimal priceUsd);

    boolean rejectPendingRequest(long id);

    List<ImagePayload> collectRetainedImagePayloads(long requestId, List<Long> retainedImageIds);
}
