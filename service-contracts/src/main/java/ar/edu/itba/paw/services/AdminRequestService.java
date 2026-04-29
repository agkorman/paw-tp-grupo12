package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.AdminRequest;
import ar.edu.itba.paw.model.Page;

import java.util.List;
import java.util.Optional;

public interface AdminRequestService {
    String STATUS_PENDING = "pending";
    String STATUS_APPROVED = "approved";
    String STATUS_REJECTED = "rejected";

    String GRANTED_ROLE = "admin";

    Optional<AdminRequest> getAdminRequestById(long id);

    List<AdminRequest> getAdminRequestsByStatus(String status);

    Page<AdminRequest> getAdminRequestsByStatus(String status, int page);

    long countAdminRequestsByStatus(String status);

    boolean hasPendingRequest(long userId);

    AdminRequest createPendingRequest(long submittedByUserId, String submitterEmail,
                                      String motivation, String bio, String justification);

    boolean approvePendingRequest(long id);

    boolean rejectPendingRequest(long id);
}
