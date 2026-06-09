package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.BodyTypeRequest;
import ar.edu.itba.paw.model.Page;

import java.util.Optional;

public interface BodyTypeRequestDao {
    Optional<BodyTypeRequest> findById(long id);

    Page<BodyTypeRequest> findByStatus(String status, int page);

    long countByStatus(String status);

    BodyTypeRequest create(Long submittedByUserId, String submitterEmail, String name, String comments, String status);

    boolean updateStatus(long id, String expectedStatus, String newStatus);
}
