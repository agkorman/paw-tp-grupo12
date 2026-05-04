package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.BrandRequest;
import ar.edu.itba.paw.model.Page;

import java.util.List;
import java.util.Optional;

public interface BrandRequestService {
    String STATUS_PENDING = "pending";
    String STATUS_APPROVED = "approved";
    String STATUS_REJECTED = "rejected";

    Optional<BrandRequest> getBrandRequestById(long id);

    List<BrandRequest> getBrandRequestsByStatus(String status);

    Page<BrandRequest> getBrandRequestsByStatus(String status, int page);

    long countBrandRequestsByStatus(String status);

    BrandRequest createPendingRequest(Long submittedByUserId, String submitterEmail, String name, String comments);

    boolean approvePendingRequest(long id);

    boolean approvePendingRequest(long id, String overrideName);

    boolean updateRequestStatus(long id, String oldStatus, String newStatus);

    boolean rejectPendingRequest(long id);
}
