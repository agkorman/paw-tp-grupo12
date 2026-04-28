package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.BrandRequest;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.persistence.BrandRequestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BrandRequestServiceImpl implements BrandRequestService {

    private final BrandRequestDao brandRequestDao;

    @Autowired
    public BrandRequestServiceImpl(final BrandRequestDao brandRequestDao) {
        this.brandRequestDao = brandRequestDao;
    }

    @Override
    public Optional<BrandRequest> getBrandRequestById(final long id) {
        return brandRequestDao.findById(id);
    }

    @Override
    public List<BrandRequest> getBrandRequestsByStatus(final String status) {
        final String normalizedStatus = StringUtils.normalize(status);
        if (normalizedStatus == null) {
            return List.of();
        }
        return brandRequestDao.findByStatus(normalizedStatus);
    }

    @Override
    public Page<BrandRequest> getBrandRequestsByStatus(final String status, final int page) {
        final String normalizedStatus = StringUtils.normalize(status);
        if (normalizedStatus == null) {
            return Page.empty(page < 1 ? 1 : page, 0);
        }
        return brandRequestDao.findByStatus(normalizedStatus, page);
    }

    @Override
    public long countBrandRequestsByStatus(final String status) {
        final String normalizedStatus = StringUtils.normalize(status);
        if (normalizedStatus == null) {
            return 0L;
        }
        return brandRequestDao.countByStatus(normalizedStatus);
    }

    @Override
    @Transactional
    public BrandRequest createPendingRequest(final Long submittedByUserId, final String submitterEmail,
                                             final String name) {
        final String normalizedName = StringUtils.normalizeRequired(name, "Name is required for brand requests.");
        if (submittedByUserId == null && (submitterEmail == null || submitterEmail.isBlank())) {
            throw new IllegalArgumentException("A submitter user id or email is required for brand requests.");
        }
        return brandRequestDao.create(submittedByUserId, submitterEmail, normalizedName, STATUS_PENDING);
    }

    @Override
    public boolean rejectPendingRequest(final long id) {
        return brandRequestDao.updateStatus(id, STATUS_PENDING, STATUS_REJECTED);
    }
}
