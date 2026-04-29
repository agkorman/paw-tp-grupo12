package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.AdminRequest;
import ar.edu.itba.paw.model.Page;

import java.util.List;
import java.util.Optional;

public interface AdminRequestDao {
    Optional<AdminRequest> findById(long id);

    List<AdminRequest> findByStatus(String status);

    Page<AdminRequest> findByStatus(String status, int page);

    long countByStatus(String status);

    boolean existsPendingByUser(long userId);

    AdminRequest create(long submittedByUserId, String submitterEmail, String motivation,
                        String bio, String justification, String status);

    boolean updateStatus(long id, String expectedStatus, String newStatus);
}
