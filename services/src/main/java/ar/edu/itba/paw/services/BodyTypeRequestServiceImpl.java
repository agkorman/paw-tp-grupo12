package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.BodyTypeRequest;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BodyTypeRequestDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BodyTypeRequestServiceImpl implements BodyTypeRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BodyTypeRequestServiceImpl.class);

    private final BodyTypeRequestDao bodyTypeRequestDao;
    private final BodyTypeDao bodyTypeDao;
    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public BodyTypeRequestServiceImpl(final BodyTypeRequestDao bodyTypeRequestDao, final BodyTypeDao bodyTypeDao,
                                      final UserService userService, final EmailService emailService) {
        this.bodyTypeRequestDao = bodyTypeRequestDao;
        this.bodyTypeDao = bodyTypeDao;
        this.userService = userService;
        this.emailService = emailService;
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
        LOGGER.info("submitting body type request name={} userId={}", normalizedName, submittedByUserId);
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
            LOGGER.warn("approve body type request rejected: not found or not pending id={}", id);
            return false;
        }

        final String resolvedName = StringUtils.normalize(overrideName);
        final String nameToCreate = resolvedName != null ? resolvedName : request.getName();
        if (nameToCreate == null || nameToCreate.isBlank()) {
            LOGGER.warn("approve body type request rejected: blank name id={}", id);
            return false;
        }

        if (bodyTypeDao.findByName(nameToCreate).isPresent()) {
            LOGGER.warn("approve body type request rejected: name already exists id={} name={}", id, nameToCreate);
            return false;
        }

        final boolean statusUpdated = bodyTypeRequestDao.updateStatus(id, STATUS_PENDING, STATUS_APPROVED);
        if (!statusUpdated) {
            return false;
        }

        bodyTypeDao.create(nameToCreate);
        sendRequestApprovedNotification(request, nameToCreate);
        LOGGER.info("approved body type request id={} createdName={}", id, nameToCreate);
        return true;
    }

    @Override
    @Transactional
    public boolean rejectPendingRequest(final long id) {
        final BodyTypeRequest request = bodyTypeRequestDao.findById(id).orElse(null);
        if (request == null || !STATUS_PENDING.equals(request.getStatus())) {
            LOGGER.warn("reject body type request rejected: not found or not pending id={}", id);
            return false;
        }

        final boolean statusUpdated = bodyTypeRequestDao.updateStatus(id, STATUS_PENDING, STATUS_REJECTED);
        if (statusUpdated) {
            sendRequestRejectedNotification(request);
            LOGGER.info("rejected body type request id={}", id);
        }
        return statusUpdated;
    }

    private void sendRequestApprovedNotification(final BodyTypeRequest request, final String approvedName) {
        final String recipientEmail = resolveSubmitterEmail(request);
        if (recipientEmail != null) {
            emailService.sendCatalogRequestApprovedNotification(recipientEmail, "tipo de carrocería",
                    approvedName);
        }
    }

    private void sendRequestRejectedNotification(final BodyTypeRequest request) {
        final String recipientEmail = resolveSubmitterEmail(request);
        if (recipientEmail != null) {
            emailService.sendCatalogRequestRejectedNotification(recipientEmail, "tipo de carrocería",
                    request.getName());
        }
    }

    private String resolveSubmitterEmail(final BodyTypeRequest request) {
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
