package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.persistence.CarRequestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CarRequestServiceImpl implements CarRequestService {

    private static final String PENDING_STATUS = "pending";

    private final CarRequestDao carRequestDao;

    @Autowired
    public CarRequestServiceImpl(final CarRequestDao carRequestDao) {
        this.carRequestDao = carRequestDao;
    }

    @Override
    public Optional<CarRequest> getCarRequestById(final long id) {
        return carRequestDao.findById(id);
    }

    @Override
    public List<CarRequest> getAllCarRequests() {
        return carRequestDao.findAll();
    }

    @Override
    public List<CarRequest> getCarRequestsByStatus(final String status) {
        final String normalizedStatus = normalize(status);
        if (normalizedStatus == null) {
            return Collections.emptyList();
        }
        return carRequestDao.findByStatus(normalizedStatus);
    }

    @Override
    public CarRequest createPendingRequest(final Long submittedByUserId, final String submitterEmail,
                                           final long brandId, final long bodyTypeId, final String model,
                                           final String description, final Optional<String> imageContentType,
                                           final Optional<byte[]> imageData) {
        final boolean hasImageContentType = imageContentType.isPresent();
        final boolean hasImageData = imageData.isPresent();
        if (hasImageContentType != hasImageData) {
            throw new IllegalArgumentException("Image metadata and payload must be provided together.");
        }

        final String normalizedModel = normalize(model);
        if (normalizedModel == null) {
            throw new IllegalArgumentException("Model is required for car requests.");
        }

        final String normalizedDescription = normalize(description);
        if (normalizedDescription == null) {
            throw new IllegalArgumentException("Description is required for car requests.");
        }

        return carRequestDao.create(
                submittedByUserId,
                normalize(submitterEmail),
                brandId,
                bodyTypeId,
                normalizedModel,
                normalizedDescription,
                imageContentType.orElse(null),
                imageData.orElse(null),
                PENDING_STATUS
        );
    }

    private String normalize(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
