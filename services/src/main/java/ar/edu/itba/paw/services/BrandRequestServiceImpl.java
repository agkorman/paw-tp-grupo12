package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.BrandRequest;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.BrandRequestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BrandRequestServiceImpl implements BrandRequestService {

    private final BrandRequestDao brandRequestDao;
    private final BrandDao brandDao;
    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public BrandRequestServiceImpl(final BrandRequestDao brandRequestDao, final BrandDao brandDao,
                                   final UserService userService, final EmailService emailService) {
        this.brandRequestDao = brandRequestDao;
        this.brandDao = brandDao;
        this.userService = userService;
        this.emailService = emailService;
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
                                             final String name, final String comments) {
        final String normalizedName = StringUtils.normalizeRequired(name, "Name is required for brand requests.");
        final String normalizedComments = StringUtils.normalize(comments);
        if (submittedByUserId == null && (submitterEmail == null || submitterEmail.isBlank())) {
            throw new IllegalArgumentException("A submitter user id or email is required for brand requests.");
        }
        return brandRequestDao.create(submittedByUserId, submitterEmail, normalizedName, normalizedComments, STATUS_PENDING);
    }

    @Override
    @Transactional
    public boolean approvePendingRequest(final long id) {
        return approvePendingRequest(id, null);
    }

    @Override
    @Transactional
    public boolean approvePendingRequest(final long id, final String overrideName) {
        final BrandRequest request = brandRequestDao.findById(id).orElse(null);
        if (request == null || !STATUS_PENDING.equals(request.getStatus())) {
            return false;
        }

        final String resolvedName = StringUtils.normalize(overrideName);
        final String nameToCreate = resolvedName != null ? resolvedName : request.getName();
        if (nameToCreate == null || nameToCreate.isBlank()) {
            return false;
        }

        if (brandDao.findByName(nameToCreate).isPresent()) {
            return false;
        }

        final boolean statusUpdated = brandRequestDao.updateStatus(id, STATUS_PENDING, STATUS_APPROVED);
        if (!statusUpdated) {
            return false;
        }

        brandDao.create(nameToCreate);
        sendRequestApprovedNotification(request, nameToCreate);
        return true;
    }

    @Override
    @Transactional
    public boolean rejectPendingRequest(final long id) {
        final BrandRequest request = brandRequestDao.findById(id).orElse(null);
        if (request == null || !STATUS_PENDING.equals(request.getStatus())) {
            return false;
        }

        final boolean statusUpdated = brandRequestDao.updateStatus(id, STATUS_PENDING, STATUS_REJECTED);
        if (statusUpdated) {
            sendRequestRejectedNotification(request);
        }
        return statusUpdated;
    }

    private void sendRequestApprovedNotification(final BrandRequest request, final String approvedName) {
        final String recipientEmail = resolveSubmitterEmail(request);
        if (recipientEmail != null) {
            emailService.sendCatalogRequestApprovedNotification(recipientEmail, "marca", approvedName);
        }
    }

    private void sendRequestRejectedNotification(final BrandRequest request) {
        final String recipientEmail = resolveSubmitterEmail(request);
        if (recipientEmail != null) {
            emailService.sendCatalogRequestRejectedNotification(recipientEmail, "marca", request.getName());
        }
    }

    private String resolveSubmitterEmail(final BrandRequest request) {
        if (request.getSubmitterEmail() != null && !request.getSubmitterEmail().isBlank()) {
            return request.getSubmitterEmail();
        }
        if (request.getSubmittedByUserId() == null) {
            return null;
        }
        return userService.getUserById(request.getSubmittedByUserId())
                .map(User::getEmail)
                .filter(email -> !email.isBlank())
                .orElse(null);
    }
}
