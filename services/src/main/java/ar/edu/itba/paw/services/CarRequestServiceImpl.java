package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarImageDao;
import ar.edu.itba.paw.persistence.CarRequestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CarRequestServiceImpl implements CarRequestService {

    private final CarRequestDao carRequestDao;
    private final CarDao carDao;
    private final CarImageDao carImageDao;

    @Autowired
    public CarRequestServiceImpl(final CarRequestDao carRequestDao, final CarDao carDao,
                                 final CarImageDao carImageDao) {
        this.carRequestDao = carRequestDao;
        this.carDao = carDao;
        this.carImageDao = carImageDao;
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
    public CarRequest createPendingRequest(final long submittedByUserId, final long brandId,
                                           final long bodyTypeId, final String model,
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
                brandId,
                bodyTypeId,
                normalizedModel,
                normalizedDescription,
                imageContentType.orElse(null),
                imageData.orElse(null),
                STATUS_PENDING
        );
    }

    @Override
    @Transactional
    public boolean approvePendingRequest(final long id) {
        final CarRequest request = carRequestDao.findById(id).orElse(null);
        if (request == null || !STATUS_PENDING.equals(request.getStatus())) {
            return false;
        }

        final boolean statusUpdated = carRequestDao.updateStatus(id, STATUS_PENDING, STATUS_APPROVED);
        if (!statusUpdated) {
            return false;
        }

        final Car createdCar = carDao.create(
                request.getBrandId(),
                request.getModel(),
                request.getBodyTypeId(),
                request.getDescription()
        );
        if (request.getImageContentType() != null && request.getImageData() != null) {
            carImageDao.saveOrReplace(createdCar.getId(), request.getImageContentType(), request.getImageData());
        }
        return true;
    }

    @Override
    public boolean rejectPendingRequest(final long id) {
        return carRequestDao.updateStatus(id, STATUS_PENDING, STATUS_REJECTED);
    }

    private String normalize(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
