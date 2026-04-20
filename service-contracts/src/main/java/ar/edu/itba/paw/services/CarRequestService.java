package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequestImage;

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
                                   Optional<byte[]> imageData);

    CarRequest createPendingRequest(long submittedByUserId, long brandId, long bodyTypeId,
                                    String model, String description, List<CarImagePayload> images);

    List<CarRequestImage> getCarRequestImages(long requestId);

    Optional<CarRequestImage> getCarRequestImageById(long requestId, long imageId);

    boolean approvePendingRequest(long id);

    boolean rejectPendingRequest(long id);
}
