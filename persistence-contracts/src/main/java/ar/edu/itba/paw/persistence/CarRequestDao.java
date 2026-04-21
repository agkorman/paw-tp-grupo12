package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.CarRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CarRequestDao {
    Optional<CarRequest> findById(long id);

    List<CarRequest> findAll();

    List<CarRequest> findByStatus(String status);

    CarRequest create(long submittedByUserId, long brandId, long bodyTypeId, String model,
                      String description, String imageContentType, byte[] imageData, String status,
                      String fuelType, Integer horsepower, Integer airbagCount,
                      String transmission, BigDecimal fuelConsumption, Integer maxSpeedKmh);

    boolean updateStatus(long id, String expectedStatus, String newStatus);

    int bindRequestsToUserByEmail(long userId, String email);
}
