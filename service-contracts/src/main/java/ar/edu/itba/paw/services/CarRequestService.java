package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequestImage;
import ar.edu.itba.paw.model.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CarRequestService {
    String STATUS_PENDING = "pending";
    String STATUS_APPROVED = "approved";
    String STATUS_REJECTED = "rejected";

    Optional<CarRequest> getCarRequestById(long id);

    List<CarRequest> getCarRequestsByStatus(String status);

    Page<CarRequest> getCarRequestsByStatus(String status, int page);

    long countCarRequestsByStatus(String status);

    CarRequest createPendingRequest(long submittedByUserId, String submitterEmail, long brandId,
                                    long bodyTypeId, Integer year, String model, String description,
                                    List<CarImagePayload> images,
                                    String fuelType, Integer horsepower, Integer airbagCount,
                                    String transmission, BigDecimal fuelConsumption, Integer maxSpeedKmh,
                                    BigDecimal priceUsd);

    List<CarRequestImage> getCarRequestImages(long requestId);

    Optional<CarRequestImage> getCarRequestImageById(long requestId, long imageId);

    boolean approvePendingRequest(long id);

    boolean approvePendingRequest(long id, long brandId, String model, long bodyTypeId, Integer year, String description,
                                  Optional<String> imageContentType, Optional<byte[]> imageData,
                                  String fuelType, Integer horsepower, Integer airbagCount,
                                  String transmission, BigDecimal fuelConsumption, Integer maxSpeedKmh,
                                  BigDecimal priceUsd);

    boolean approvePendingRequest(long id, long brandId, String model, long bodyTypeId, Integer year, String description,
                                  List<CarImagePayload> images,
                                  String fuelType, Integer horsepower, Integer airbagCount,
                                  String transmission, BigDecimal fuelConsumption, Integer maxSpeedKmh,
                                  BigDecimal priceUsd);

    boolean rejectPendingRequest(long id);
}
