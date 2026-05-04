package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.BrandRequest;
import ar.edu.itba.paw.model.Page;

import java.util.List;
import java.util.Optional;

public interface BrandRequestDao {
    Optional<BrandRequest> findById(long id);

    List<BrandRequest> findByStatus(String status);

    Page<BrandRequest> findByStatus(String status, int page);

    long countByStatus(String status);

    BrandRequest insertAndFetch(Long submittedByUserId, String submitterEmail, String name, String comments, String status);

    boolean updateStatus(long id, String expectedStatus, String newStatus);
}
