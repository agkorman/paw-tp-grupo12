package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.BodyTypeRequest;
import ar.edu.itba.paw.model.Page;

import java.util.List;
import java.util.Optional;

public interface BodyTypeRequestDao {
    Optional<BodyTypeRequest> findById(long id);

    List<BodyTypeRequest> findByStatus(String status);

    Page<BodyTypeRequest> findByStatus(String status, int page);

    long countByStatus(String status);

    BodyTypeRequest create(Long submittedByUserId, String submitterEmail, String name, String status);

    boolean updateStatus(long id, String expectedStatus, String newStatus);
}
