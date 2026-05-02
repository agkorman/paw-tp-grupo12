package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.AdminRequest;
import ar.edu.itba.paw.model.Page;
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

    @InjectMocks
    private AdminRequestServiceImpl adminRequestService;

    private static AdminRequest pendingRequest() {
        return new AdminRequest(REQUEST_ID, USER_ID, EMAIL, "motivation", "bio", "justification",
                AdminRequestService.STATUS_PENDING, LocalDateTime.now());
    }

    @Test
    public void shouldCreatePendingRequestWithNormalizedFields() {
        // Arrange
        final AdminRequest created = new AdminRequest(REQUEST_ID, USER_ID, EMAIL, "motivation text",
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

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminRequestService.createPendingRequest(USER_ID, EMAIL, "  ", BIO, JUSTIFICATION));

        // Assertions
        assertEquals("Motivation is required for admin requests.", ex.getMessage());
    }

    @Test
    public void shouldRejectCreateWhenBioIsBlank() {
        // Arrange

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminRequestService.createPendingRequest(USER_ID, EMAIL, MOTIVATION, "  ", JUSTIFICATION));

        // Assertions
        assertEquals("Bio is required for admin requests.", ex.getMessage());
    }

    @Test
    public void shouldRejectCreateWhenJustificationIsBlank() {
        // Arrange

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminRequestService.createPendingRequest(USER_ID, EMAIL, MOTIVATION, BIO, "  "));

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
        assertEquals("User already has a pending admin request.", ex.getMessage());
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
        final AdminRequest approved = new AdminRequest(REQUEST_ID, USER_ID, EMAIL, "m", "b", "j",
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
    public void shouldReturnEmptyListWhenStatusFilterIsBlank() {
        // Arrange

        // Exercise
        final List<AdminRequest> result = adminRequestService.getAdminRequestsByStatus("   ");

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyPageWhenStatusFilterIsBlankForPaged() {
        // Arrange

        // Exercise
        final Page<AdminRequest> result = adminRequestService.getAdminRequestsByStatus("  ", 3);

        // Assertions
        assertTrue(result.getItems().isEmpty());
        assertEquals(3, result.getPageNumber());
    }

    @Test
    public void shouldClampNegativePageToOneWhenStatusIsBlank() {
        // Arrange

        // Exercise
        final Page<AdminRequest> result = adminRequestService.getAdminRequestsByStatus(null, -5);

        // Assertions
        assertEquals(1, result.getPageNumber());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    public void shouldReturnZeroCountWhenStatusFilterIsBlank() {
        // Arrange

        // Exercise
        final long result = adminRequestService.countAdminRequestsByStatus(null);

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
}
