package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.AdminRequest;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.AdminRequestDao;
import ar.edu.itba.paw.services.exception.PendingAdminRequestExistsException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminRequestServiceImpl implements AdminRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        AdminRequestServiceImpl.class
    );

    private final AdminRequestDao adminRequestDao;
    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public AdminRequestServiceImpl(final AdminRequestDao adminRequestDao, final UserService userService,
                                   final EmailService emailService) {
        this.adminRequestDao = adminRequestDao;
        this.userService = userService;
        this.emailService = emailService;
    }

    @Override
    public Optional<AdminRequest> getAdminRequestById(final long id) {
        return adminRequestDao.findById(id);
    }

    @Override
    public List<AdminRequest> getAdminRequestsByStatus(final String status) {
        final String normalizedStatus = StringUtils.normalize(status);
        if (normalizedStatus == null) {
            return List.of();
        }
        return adminRequestDao.findByStatus(normalizedStatus);
    }

    @Override
    public Page<AdminRequest> getAdminRequestsByStatus(
        final String status,
        final int page
    ) {
        final String normalizedStatus = StringUtils.normalize(status);
        if (normalizedStatus == null) {
            return Page.empty(page < 1 ? 1 : page, 0);
        }
        return adminRequestDao.findByStatus(normalizedStatus, page);
    }

    @Override
    public long countAdminRequestsByStatus(final String status) {
        final String normalizedStatus = StringUtils.normalize(status);
        if (normalizedStatus == null) {
            return 0L;
        }
        return adminRequestDao.countByStatus(normalizedStatus);
    }

    @Override
    public boolean hasPendingRequest(final long userId) {
        return adminRequestDao.existsPendingByUser(userId);
    }

    @Override
    public boolean isEligibleForModeratorRequest(final long userId) {
        final User user = userService.getUserById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        final String role = user.getRole();
        if (role != null && !"user".equalsIgnoreCase(role.trim())) {
            return false;
        }
        return !adminRequestDao.existsPendingByUser(userId);
    }

    @Override
    @Transactional
    public AdminRequest createPendingRequest(
        final long submittedByUserId,
        final String submitterEmail,
        final String motivation,
        final String bio,
        final String justification
    ) {
        final String normalizedMotivation = StringUtils.normalizeRequired(
            motivation,
            "Motivation is required for admin requests."
        );
        final String normalizedBio = StringUtils.normalizeRequired(
            bio,
            "Bio is required for admin requests."
        );
        final String normalizedJustification = StringUtils.normalizeRequired(
            justification,
            "Justification is required for admin requests."
        );
        if (adminRequestDao.existsPendingByUser(submittedByUserId)) {
            LOGGER.warn(
                "user id={} attempted to submit second pending admin request",
                submittedByUserId
            );
            throw new PendingAdminRequestExistsException(submittedByUserId);
        }
        LOGGER.info(
            "submitting admin request for user id={}",
            submittedByUserId
        );
        return adminRequestDao.create(
            submittedByUserId,
            submitterEmail,
            normalizedMotivation,
            normalizedBio,
            normalizedJustification,
            STATUS_PENDING
        );
    }

    @Override
    @Transactional
    public boolean approvePendingRequest(final long id) {
        final AdminRequest request = adminRequestDao.findById(id).orElse(null);
        if (request == null || !STATUS_PENDING.equals(request.getStatus())) {
            LOGGER.warn(
                "approve admin request rejected: not found or not pending id={}",
                id
            );
            return false;
        }

        final boolean statusUpdated = adminRequestDao.updateStatus(
            id,
            STATUS_PENDING,
            STATUS_APPROVED
        );
        if (!statusUpdated) {
            return false;
        }

        userService.updateRole(request.getSubmittedByUserId(), GRANTED_ROLE);
        sendRequestApprovedNotification(request);
        LOGGER.info(
            "approved admin request id={} userId={} grantedRole={}",
            id,
            request.getSubmittedByUserId(),
            GRANTED_ROLE
        );
        return true;
    }

    @Override
    @Transactional
    public boolean rejectPendingRequest(final long id) {
        final AdminRequest request = adminRequestDao.findById(id).orElse(null);
        if (request == null || !STATUS_PENDING.equals(request.getStatus())) {
            LOGGER.warn(
                "reject admin request rejected: not found or not pending id={}",
                id
            );
            return false;
        }

        final boolean statusUpdated = adminRequestDao.updateStatus(
            id,
            STATUS_PENDING,
            STATUS_REJECTED
        );
        if (statusUpdated) {
            sendRequestRejectedNotification(request);
            LOGGER.info(
                "rejected admin request id={} userId={}",
                id,
                request.getSubmittedByUserId()
            );
        }
        return statusUpdated;
    }

    private void sendRequestApprovedNotification(final AdminRequest request) {
        final String recipientEmail = resolveSubmitterEmail(request);
        if (recipientEmail != null) {
            emailService.sendAdminRequestApprovedNotification(recipientEmail);
        }
    }

    private void sendRequestRejectedNotification(final AdminRequest request) {
        final String recipientEmail = resolveSubmitterEmail(request);
        if (recipientEmail != null) {
            emailService.sendAdminRequestRejectedNotification(recipientEmail);
        }
    }

    private String resolveSubmitterEmail(final AdminRequest request) {
        if (request.getSubmitterEmail() != null && !request.getSubmitterEmail().isBlank()) {
            return request.getSubmitterEmail();
        }
        return userService.getUserById(request.getSubmittedByUserId())
                .map(User::getEmail)
                .filter(email -> !email.isBlank())
                .orElse(null);
    }
}
