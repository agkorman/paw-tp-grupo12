package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.AdminRequest;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.AdminRequestDao;
import ar.edu.itba.paw.services.exception.PendingAdminRequestExistsException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
    private final CarRequestService carRequestService;
    private final BrandRequestService brandRequestService;
    private final BodyTypeRequestService bodyTypeRequestService;
    private final AuthenticatedSessionService authenticatedSessionService;

    @Autowired
    public AdminRequestServiceImpl(final AdminRequestDao adminRequestDao, final UserService userService,
                                   final EmailService emailService, final CarRequestService carRequestService,
                                   final BrandRequestService brandRequestService,
                                   final BodyTypeRequestService bodyTypeRequestService,
                                   final AuthenticatedSessionService authenticatedSessionService) {
        this.adminRequestDao = adminRequestDao;
        this.userService = userService;
        this.emailService = emailService;
        this.carRequestService = carRequestService;
        this.brandRequestService = brandRequestService;
        this.bodyTypeRequestService = bodyTypeRequestService;
        this.authenticatedSessionService = authenticatedSessionService;
    }

    @Override
    public Optional<AdminRequest> getAdminRequestById(final long id) {
        return adminRequestDao.findById(id);
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

        final long promotedUserId = request.getSubmittedByUserId();
        userService.updateRole(promotedUserId, GRANTED_ROLE);
        sendRequestApprovedNotification(request);
        authenticatedSessionService.promoteUserToAdmin(promotedUserId);
        LOGGER.info(
            "approved admin request id={} userId={} grantedRole={}",
            id,
            promotedUserId,
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
        final User submitter = request.getSubmittedByUser();
        if (submitter == null || submitter.getEmail() == null || submitter.getEmail().isBlank()) {
            return null;
        }
        return submitter.getEmail();
    }

    @Override
    public long getTotalPendingItems() {
        final long carRequestCount = carRequestService.countCarRequestsByStatus(
            CarRequestService.STATUS_PENDING
        );
        final long brandRequestCount = brandRequestService.countBrandRequestsByStatus(
            BrandRequestService.STATUS_PENDING
        );
        final long bodyTypeRequestCount = bodyTypeRequestService.countBodyTypeRequestsByStatus(
            BodyTypeRequestService.STATUS_PENDING
        );
        final long adminRequestCount = countAdminRequestsByStatus(STATUS_PENDING);
        return carRequestCount + brandRequestCount + bodyTypeRequestCount + adminRequestCount;
    }

    @Override
    public String resolveSubmitterEmail(final String submitterEmail, final Long submittedByUserId) {
        if (submitterEmail != null && !submitterEmail.isBlank()) {
            return submitterEmail;
        }
        if (submittedByUserId != null) {
            return userService.getUserById(submittedByUserId)
                .map(User::getEmail)
                .filter(email -> !email.isBlank())
                .orElse(null);
        }
        return null;
    }

    @Override
    public String getSubmitterLabel(final String submitterEmail, final Long submittedByUserId) {
        if (submitterEmail != null && !submitterEmail.isBlank()) {
            return submitterEmail;
        }
        final Map<Long, User> usersById = submittedByUserId == null
            ? Collections.emptyMap()
            : userService.getUsersByIds(List.of(submittedByUserId)).stream()
                .collect(Collectors.toMap(User::getId, user -> user, (existing, duplicate) -> existing));
        return getSubmitterLabel(submitterEmail, submittedByUserId, usersById);
    }

    @Override
    public String getSubmitterLabel(final String submitterEmail, final Long submittedByUserId,
                                    final Map<Long, User> usersById) {
        if (submitterEmail != null && !submitterEmail.isBlank()) {
            return submitterEmail;
        }
        if (submittedByUserId != null) {
            final User user = usersById == null ? null : usersById.get(submittedByUserId);
            if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
                return user.getEmail();
            }
            return "Usuario #" + submittedByUserId;
        }
        return "Usuario sin identificar";
    }
}
