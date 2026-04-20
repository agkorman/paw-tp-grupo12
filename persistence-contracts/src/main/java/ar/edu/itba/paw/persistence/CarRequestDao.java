package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequestImage;

import java.util.List;
import java.util.Optional;

public interface CarRequestDao {
    Optional<CarRequest> findById(long id);

    List<CarRequest> findAll();

    List<CarRequest> findByStatus(String status);

    CarRequest create(long submittedByUserId, long brandId, long bodyTypeId, String model,
                      String description, String imageContentType, byte[] imageData, String status);

    List<CarRequestImage> findImagesByRequestId(long requestId);

    Optional<CarRequestImage> findImageByRequestIdAndImageId(long requestId, long imageId);

    void replaceImages(long requestId, List<CarImagePayload> images);

    boolean updateStatus(long id, String expectedStatus, String newStatus);

    int bindRequestsToUserByEmail(long userId, String email);
}
