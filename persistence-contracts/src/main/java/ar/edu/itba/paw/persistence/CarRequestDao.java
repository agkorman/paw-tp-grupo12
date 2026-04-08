package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.CarRequest;

import java.util.List;
import java.util.Optional;

public interface CarRequestDao {
    Optional<CarRequest> findById(long id);

    List<CarRequest> findAll();

    List<CarRequest> findByStatus(String status);

    CarRequest create(Long submittedByUserId, String submitterEmail, long brandId, long bodyTypeId, String model,
                      String description, String imageContentType, byte[] imageData, String status);
}
