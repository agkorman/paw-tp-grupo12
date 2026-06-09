package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.AdminRequest;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.AdminRequestDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminRequestServiceImplTest {

    private static final long REQUEST_ID = 5L;
    private static final long USER_ID = 42L;
    private static final String EMAIL = "u@x.com";
    private static final String MOTIVATION = "  motivation text  ";
    private static final String BIO = "  bio text  ";
    private static final String JUSTIFICATION = "  justification text  ";

    @Mock
    private AdminRequestDao adminRequestDao;
    @Mock
    private UserService userService;
    @Mock
    private EmailService emailService;
    @Mock
    private CarRequestService carRequestService;
    @Mock
    private BrandRequestService brandRequestService;
    @Mock
    private BodyTypeRequestService bodyTypeRequestService;
    @Mock
    private AuthenticatedSessionService authenticatedSessionService;

    @InjectMocks
    private AdminRequestServiceImpl adminRequestService;

    private static AdminRequest pendingRequest() {
        return TestModels.adminRequest(REQUEST_ID, USER_ID, EMAIL, "motivation", "bio", "justification",
                AdminRequestService.STATUS_PENDING, LocalDateTime.now());
    }

    @Test
    public void shouldCreatePendingRequestWithNormalizedFields() {
        // Arrange
        final AdminRequest created = TestModels.adminRequest(REQUEST_ID, USER_ID, EMAIL, "motivation text",
                "bio text", "justification text", AdminRequestService.STATUS_PENDING, LocalDateTime.now());
        when(adminRequestDao.existsPendingByUser(USER_ID)).thenReturn(false);
        when(adminRequestDao.create(USER_ID, EMAIL, "motivation text", "bio text", "justification text",
                AdminRequestService.STATUS_PENDING)).thenReturn(created);

        // Exercise
        final AdminRequest result = adminRequestService.createPendingRequest(USER_ID, EMAIL, MOTIVATION, BIO, JUSTIFICATION);

        // Assertions
        assertEquals(REQUEST_ID, result.getId());
        assertEquals("motivation text", result.getMotivation());
        assertEquals("bio text", result.getBio());
        assertEquals("justification text", result.getJustification());
        assertEquals(AdminRequestService.STATUS_PENDING, result.getStatus());
    }

    @Test
    public void shouldRejectCreateWhenMotivationIsBlank() {
        // Arrange
        final String blankMotivation = "  ";

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminRequestService.createPendingRequest(USER_ID, EMAIL, blankMotivation, BIO, JUSTIFICATION));

        // Assertions
        assertEquals("Motivation is required for admin requests.", ex.getMessage());
    }

    @Test
    public void shouldRejectCreateWhenBioIsBlank() {
        // Arrange
        final String blankBio = "  ";

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminRequestService.createPendingRequest(USER_ID, EMAIL, MOTIVATION, blankBio, JUSTIFICATION));

        // Assertions
        assertEquals("Bio is required for admin requests.", ex.getMessage());
    }

    @Test
    public void shouldRejectCreateWhenJustificationIsBlank() {
        // Arrange
        final String blankJustification = "  ";

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminRequestService.createPendingRequest(USER_ID, EMAIL, MOTIVATION, BIO, blankJustification));

        // Assertions
        assertEquals("Justification is required for admin requests.", ex.getMessage());
    }

    @Test
    public void shouldRejectCreateWhenUserAlreadyHasPending() {
        // Arrange
        when(adminRequestDao.existsPendingByUser(USER_ID)).thenReturn(true);

        // Exercise
        final IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adminRequestService.createPendingRequest(USER_ID, EMAIL, MOTIVATION, BIO, JUSTIFICATION));

        // Assertions
        assertEquals("User already has a pending admin request: 42", ex.getMessage());
    }

    @Test
    public void shouldApprovePendingRequestAndPromoteUserToAdmin() {
        // Arrange
        when(adminRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(pendingRequest()));
        when(adminRequestDao.updateStatus(REQUEST_ID, AdminRequestService.STATUS_PENDING,
                AdminRequestService.STATUS_APPROVED)).thenReturn(true);
        when(userService.updateRole(USER_ID, AdminRequestService.GRANTED_ROLE)).thenReturn(true);

        // Exercise
        final boolean result = adminRequestService.approvePendingRequest(REQUEST_ID);

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldNotApproveWhenRequestDoesNotExist() {
        // Arrange
        when(adminRequestDao.findById(REQUEST_ID)).thenReturn(Optional.empty());

        // Exercise
        final boolean result = adminRequestService.approvePendingRequest(REQUEST_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldNotApproveWhenRequestIsNotPending() {
        // Arrange
        final AdminRequest approved = TestModels.adminRequest(REQUEST_ID, USER_ID, EMAIL, "m", "b", "j",
                AdminRequestService.STATUS_APPROVED, LocalDateTime.now());
        when(adminRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(approved));

        // Exercise
        final boolean result = adminRequestService.approvePendingRequest(REQUEST_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldNotApproveWhenStatusUpdateFails() {
        // Arrange
        when(adminRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(pendingRequest()));
        when(adminRequestDao.updateStatus(REQUEST_ID, AdminRequestService.STATUS_PENDING,
                AdminRequestService.STATUS_APPROVED)).thenReturn(false);

        // Exercise
        final boolean result = adminRequestService.approvePendingRequest(REQUEST_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldRejectPendingRequestThroughDao() {
        // Arrange
        when(adminRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(pendingRequest()));
        when(adminRequestDao.updateStatus(REQUEST_ID, AdminRequestService.STATUS_PENDING,
                AdminRequestService.STATUS_REJECTED)).thenReturn(true);

        // Exercise
        final boolean result = adminRequestService.rejectPendingRequest(REQUEST_ID);

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldNotRejectWhenStatusUpdateFails() {
        // Arrange
        when(adminRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(pendingRequest()));
        when(adminRequestDao.updateStatus(REQUEST_ID, AdminRequestService.STATUS_PENDING,
                AdminRequestService.STATUS_REJECTED)).thenReturn(false);

        // Exercise
        final boolean result = adminRequestService.rejectPendingRequest(REQUEST_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldReturnEmptyPageWhenStatusFilterIsBlankForPaged() {
        // Arrange
        final String blankStatus = "  ";
        final int requestedPage = 3;

        // Exercise
        final Page<AdminRequest> result = adminRequestService.getAdminRequestsByStatus(blankStatus, requestedPage);

        // Assertions
        assertTrue(result.getItems().isEmpty());
        assertEquals(3, result.getPageNumber());
    }

    @Test
    public void shouldClampNegativePageToOneWhenStatusIsBlank() {
        // Arrange
        final String nullStatus = null;
        final int requestedPage = -5;

        // Exercise
        final Page<AdminRequest> result = adminRequestService.getAdminRequestsByStatus(nullStatus, requestedPage);

        // Assertions
        assertEquals(1, result.getPageNumber());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    public void shouldReturnZeroCountWhenStatusFilterIsBlank() {
        // Arrange
        final String nullStatus = null;

        // Exercise
        final long result = adminRequestService.countAdminRequestsByStatus(nullStatus);

        // Assertions
        assertEquals(0L, result);
    }

    @Test
    public void shouldDelegateCountToDaoWhenStatusIsValid() {
        // Arrange
        when(adminRequestDao.countByStatus(AdminRequestService.STATUS_PENDING)).thenReturn(7L);

        // Exercise
        final long result = adminRequestService.countAdminRequestsByStatus("  pending  ");

        // Assertions
        assertEquals(7L, result);
    }

    @Test
    public void shouldDelegateGetByIdToDao() {
        // Arrange
        when(adminRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(pendingRequest()));

        // Exercise
        final Optional<AdminRequest> result = adminRequestService.getAdminRequestById(REQUEST_ID);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(REQUEST_ID, result.get().getId());
    }

    @Test
    public void shouldAggregateTotalPendingItemsFromAllRequestTypes() {
        // Arrange
        when(carRequestService.countCarRequestsByStatus(CarRequestService.STATUS_PENDING))
            .thenReturn(3L);
        when(brandRequestService.countBrandRequestsByStatus(BrandRequestService.STATUS_PENDING))
            .thenReturn(5L);
        when(bodyTypeRequestService.countBodyTypeRequestsByStatus(BodyTypeRequestService.STATUS_PENDING))
            .thenReturn(2L);
        when(adminRequestDao.countByStatus(AdminRequestService.STATUS_PENDING))
            .thenReturn(4L);

        // Exercise
        final long result = adminRequestService.getTotalPendingItems();

        // Assertions
        assertEquals(14L, result);
    }

    @Test
    public void shouldResolveSubmitterEmailFromProvidedEmail() {
        // Arrange
        final String email = "submitter@example.com";
        final Long userId = 123L;

        // Exercise
        final String result = adminRequestService.resolveSubmitterEmail(email, userId);

        // Assertions
        assertEquals("submitter@example.com", result);
    }

    @Test
    public void shouldResolveSubmitterEmailFromUserIdWhenEmailIsBlank() {
        // Arrange
        final Long userId = 123L;
        final User user = TestModels.user(userId, "username", "email@example.com", "password", "user", LocalDateTime.now());
        when(userService.getUserById(userId)).thenReturn(java.util.Optional.of(user));

        // Exercise
        final String result = adminRequestService.resolveSubmitterEmail("  ", userId);

        // Assertions
        assertEquals("email@example.com", result);
    }

    @Test
    public void shouldReturnNullWhenCannotResolveSubmitterEmail() {
        // Arrange
        when(userService.getUserById(USER_ID)).thenReturn(java.util.Optional.empty());

        // Exercise
        final String result = adminRequestService.resolveSubmitterEmail(null, USER_ID);

        // Assertions
        assertEquals(null, result);
    }

    @Test
    public void shouldGetSubmitterLabelFromResolvedEmail() {
        // Arrange
        final String email = "submitter@example.com";
        final Long userId = 123L;

        // Exercise
        final String result = adminRequestService.getSubmitterLabel(email, userId);

        // Assertions
        assertEquals("submitter@example.com", result);
    }

    @Test
    public void shouldGetSubmitterLabelAsUserIdWhenEmailCannotBeResolved() {
        // Arrange
        when(userService.getUsersByIds(java.util.List.of(USER_ID)))
            .thenReturn(java.util.Collections.emptyList());

        // Exercise
        final String result = adminRequestService.getSubmitterLabel(null, USER_ID);

        // Assertions
        assertEquals("Usuario #42", result);
    }

    @Test
    public void shouldGetSubmitterLabelAsUnidentifiedWhenNothingCanBeResolved() {
        // Arrange

        // Exercise
        final String result = adminRequestService.getSubmitterLabel(null, null);

        // Assertions
        assertEquals("Usuario sin identificar", result);
    }
}
