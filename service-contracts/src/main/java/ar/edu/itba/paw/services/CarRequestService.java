package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.CarRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CarRequestService {
    String STATUS_PENDING = "pending";
    String STATUS_APPROVED = "approved";
    String STATUS_REJECTED = "rejected";

    Optional<CarRequest> getCarRequestById(long id);

    List<CarRequest> getAllCarRequests();

    List<CarRequest> getCarRequestsByStatus(String status);

    CarRequest createPendingRequest(long submittedByUserId, long brandId, long bodyTypeId,
                                   String model, String description, Optional<String> imageContentType,
                                   Optional<byte[]> imageData, String fuelType, Integer horsepower,
                                   Integer airbagCount, String transmission, BigDecimal fuelConsumption,
                                   Integer maxSpeedKmh);

    boolean approvePendingRequest(long id);

    boolean rejectPendingRequest(long id);
}
