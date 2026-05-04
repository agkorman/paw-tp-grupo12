package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequestImage;
import ar.edu.itba.paw.model.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CarRequestDao {
    Optional<CarRequest> findById(long id);

    List<CarRequest> findByStatus(String status);

    Page<CarRequest> findByStatus(String status, int page);

    long countByStatus(String status);

    CarRequest insertAndFetch(long submittedByUserId, String submitterEmail, long brandId, long bodyTypeId, Integer year,
                      String model, String description, String imageContentType, byte[] imageData,
                      String status, String fuelType, Integer horsepower, Integer airbagCount,
                      String transmission, BigDecimal fuelConsumption, Integer maxSpeedKmh, BigDecimal priceUsd);

    List<CarRequestImage> findImagesByRequestId(long requestId);

    Optional<CarRequestImage> findImageByRequestIdAndImageId(long requestId, long imageId);

    void replaceImages(long requestId, List<CarImagePayload> images);

    boolean updateStatus(long id, String expectedStatus, String newStatus);

    int bindRequestsToUserByEmail(long userId, String email);
}
