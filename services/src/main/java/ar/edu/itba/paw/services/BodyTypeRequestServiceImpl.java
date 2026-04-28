package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.BodyTypeRequest;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BodyTypeRequestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BodyTypeRequestServiceImpl implements BodyTypeRequestService {

    private final BodyTypeRequestDao bodyTypeRequestDao;
    private final BodyTypeDao bodyTypeDao;

    @Autowired
    public BodyTypeRequestServiceImpl(final BodyTypeRequestDao bodyTypeRequestDao, final BodyTypeDao bodyTypeDao) {
        this.bodyTypeRequestDao = bodyTypeRequestDao;
        this.bodyTypeDao = bodyTypeDao;
    }

    @Override
    public Optional<BodyTypeRequest> getBodyTypeRequestById(final long id) {
        return bodyTypeRequestDao.findById(id);
    }

    @Override
    public List<BodyTypeRequest> getBodyTypeRequestsByStatus(final String status) {
        final String normalizedStatus = StringUtils.normalize(status);
        if (normalizedStatus == null) {
            return List.of();
        }
        return bodyTypeRequestDao.findByStatus(normalizedStatus);
    }

    @Override
    public Page<BodyTypeRequest> getBodyTypeRequestsByStatus(final String status, final int page) {
        final String normalizedStatus = StringUtils.normalize(status);
        if (normalizedStatus == null) {
            return Page.empty(page < 1 ? 1 : page, 0);
        }
        return bodyTypeRequestDao.findByStatus(normalizedStatus, page);
    }

    @Override
    public long countBodyTypeRequestsByStatus(final String status) {
        final String normalizedStatus = StringUtils.normalize(status);
        if (normalizedStatus == null) {
            return 0L;
        }
        return bodyTypeRequestDao.countByStatus(normalizedStatus);
    }

    @Override
    @Transactional
    public BodyTypeRequest createPendingRequest(final Long submittedByUserId, final String submitterEmail,
                                                final String name, final String comments) {
        final String normalizedName = StringUtils.normalizeRequired(name, "Name is required for body type requests.");
        final String normalizedComments = StringUtils.normalize(comments);
        if (submittedByUserId == null && (submitterEmail == null || submitterEmail.isBlank())) {
            throw new IllegalArgumentException("A submitter user id or email is required for body type requests.");
        }
        return bodyTypeRequestDao.create(submittedByUserId, submitterEmail, normalizedName, normalizedComments, STATUS_PENDING);
    }

    @Override
    @Transactional
    public boolean approvePendingRequest(final long id) {
        return approvePendingRequest(id, null);
    }

    @Override
    @Transactional
    public boolean approvePendingRequest(final long id, final String overrideName) {
        final BodyTypeRequest request = bodyTypeRequestDao.findById(id).orElse(null);
        if (request == null || !STATUS_PENDING.equals(request.getStatus())) {
            return false;
        }

        final String resolvedName = StringUtils.normalize(overrideName);
        final String nameToCreate = resolvedName != null ? resolvedName : request.getName();
        if (nameToCreate == null || nameToCreate.isBlank()) {
            return false;
        }

        if (bodyTypeDao.findByName(nameToCreate).isPresent()) {
            return false;
        }

        final boolean statusUpdated = bodyTypeRequestDao.updateStatus(id, STATUS_PENDING, STATUS_APPROVED);
        if (!statusUpdated) {
            return false;
        }

        bodyTypeDao.create(nameToCreate);
        return true;
    }

    @Override
    public boolean rejectPendingRequest(final long id) {
        return bodyTypeRequestDao.updateStatus(id, STATUS_PENDING, STATUS_REJECTED);
    }
}
