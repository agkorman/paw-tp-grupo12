package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.AdminRequest;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.AdminRequestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdminRequestServiceImpl implements AdminRequestService {

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
    public Page<AdminRequest> getAdminRequestsByStatus(final String status, final int page) {
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
    @Transactional
    public AdminRequest createPendingRequest(final long submittedByUserId, final String submitterEmail,
                                             final String motivation, final String bio,
                                             final String justification) {
        final String normalizedMotivation = StringUtils.normalizeRequired(motivation,
                "Motivation is required for admin requests.");
        final String normalizedBio = StringUtils.normalizeRequired(bio,
                "Bio is required for admin requests.");
        final String normalizedJustification = StringUtils.normalizeRequired(justification,
                "Justification is required for admin requests.");
        if (adminRequestDao.existsPendingByUser(submittedByUserId)) {
            throw new IllegalStateException("User already has a pending admin request.");
        }
        return adminRequestDao.create(submittedByUserId, submitterEmail, normalizedMotivation,
                normalizedBio, normalizedJustification, STATUS_PENDING);
    }

    @Override
    @Transactional
    public boolean approvePendingRequest(final long id) {
        final AdminRequest request = adminRequestDao.findById(id).orElse(null);
        if (request == null || !STATUS_PENDING.equals(request.getStatus())) {
            return false;
        }

        final boolean statusUpdated = adminRequestDao.updateStatus(id, STATUS_PENDING, STATUS_APPROVED);
        if (!statusUpdated) {
            return false;
        }

        userService.updateRole(request.getSubmittedByUserId(), GRANTED_ROLE);
        sendRequestApprovedNotification(request);
        return true;
    }

    @Override
    @Transactional
    public boolean rejectPendingRequest(final long id) {
        final AdminRequest request = adminRequestDao.findById(id).orElse(null);
        if (request == null || !STATUS_PENDING.equals(request.getStatus())) {
            return false;
        }

        final boolean statusUpdated = adminRequestDao.updateStatus(id, STATUS_PENDING, STATUS_REJECTED);
        if (statusUpdated) {
            sendRequestRejectedNotification(request);
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
