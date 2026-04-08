package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.CarRequest;

import java.util.List;
import java.util.Optional;

public interface CarRequestService {
    Optional<CarRequest> getCarRequestById(long id);

    List<CarRequest> getAllCarRequests();

    List<CarRequest> getCarRequestsByStatus(String status);

    CarRequest createPendingRequest(Long submittedByUserId, String submitterEmail, long brandId, long bodyTypeId,
                                   String model, String description, Optional<String> imageContentType,
                                   Optional<byte[]> imageData);
}
