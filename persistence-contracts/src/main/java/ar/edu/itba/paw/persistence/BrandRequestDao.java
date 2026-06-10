package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.BrandRequest;
import ar.edu.itba.paw.model.Page;

import java.util.Optional;

public interface BrandRequestDao {
    Optional<BrandRequest> findById(long id);

    Page<BrandRequest> findByStatus(String status, int page);

    long countByStatus(String status);

    BrandRequest create(Long submittedByUserId, String submitterEmail, String name, String comments, String status);

    boolean updateStatus(long id, String expectedStatus, String newStatus);
}
