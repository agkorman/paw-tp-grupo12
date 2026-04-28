package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.BodyTypeRequest;
import ar.edu.itba.paw.model.Page;

import java.util.List;
import java.util.Optional;

public interface BodyTypeRequestService {
    String STATUS_PENDING = "pending";
    String STATUS_APPROVED = "approved";
    String STATUS_REJECTED = "rejected";

    Optional<BodyTypeRequest> getBodyTypeRequestById(long id);

    List<BodyTypeRequest> getBodyTypeRequestsByStatus(String status);

    Page<BodyTypeRequest> getBodyTypeRequestsByStatus(String status, int page);

    long countBodyTypeRequestsByStatus(String status);

    BodyTypeRequest createPendingRequest(Long submittedByUserId, String submitterEmail, String name, String comments);

    boolean approvePendingRequest(long id);

    boolean approvePendingRequest(long id, String overrideName);

    boolean rejectPendingRequest(long id);
}
