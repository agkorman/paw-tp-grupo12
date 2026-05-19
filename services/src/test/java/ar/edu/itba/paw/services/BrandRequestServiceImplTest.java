package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandRequest;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.BrandRequestDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BrandRequestServiceImplTest {

    private static final long REQUEST_ID = 4L;
    private static final long USER_ID = 8L;
    private static final String EMAIL = "submitter@example.com";

    @Mock
    private BrandRequestDao brandRequestDao;
    @Mock
    private BrandDao brandDao;
    @Mock
    private UserService userService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private BrandRequestServiceImpl brandRequestService;

    private static BrandRequest pendingRequest(final String name) {
        return TestModels.brandRequest(REQUEST_ID, USER_ID, EMAIL, name, "comment",
                BrandRequestService.STATUS_PENDING, LocalDateTime.now());
    }

    @Test
    public void shouldCreatePendingRequestWithNormalizedNameAndComments() {
        // Arrange
        final BrandRequest created = pendingRequest("New Brand");
        when(brandRequestDao.create(USER_ID, EMAIL, "New Brand", "comment",
                BrandRequestService.STATUS_PENDING)).thenReturn(created);

        // Exercise
        final BrandRequest result = brandRequestService.createPendingRequest(USER_ID, EMAIL, "  New Brand  ", " comment ");

        // Assertions
        assertEquals(REQUEST_ID, result.getId());
        assertEquals("New Brand", result.getName());
    }

    @Test
    public void shouldRejectCreatePendingRequestWithoutSubmitterIdentity() {
        // Arrange
        final Long submittedByUserId = null;
        final String blankEmail = "   ";

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> brandRequestService.createPendingRequest(submittedByUserId, blankEmail, "Brand", "comment"));

        // Assertions
        assertEquals("A submitter user id or email is required for brand requests.", ex.getMessage());
    }

    @Test
    public void shouldApprovePendingRequestUsingOverrideNameWhenOriginalNameExists() {
        // Arrange
        final BrandRequest request = pendingRequest("Existing Brand");
        when(brandRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        when(brandDao.findByName("Renamed Brand")).thenReturn(Optional.empty());
        when(brandRequestDao.updateStatus(REQUEST_ID, BrandRequestService.STATUS_PENDING,
                BrandRequestService.STATUS_APPROVED)).thenReturn(true);

        // Exercise
        final boolean result = brandRequestService.approvePendingRequest(REQUEST_ID, "  Renamed Brand  ");

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldNotApprovePendingRequestWhenRequestedNameAlreadyExists() {
        // Arrange
        final BrandRequest request = pendingRequest("Existing Brand");
        when(brandRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        when(brandDao.findByName("Existing Brand")).thenReturn(Optional.of(TestModels.brand(1L, "Existing Brand", LocalDateTime.now())));

        // Exercise
        final boolean result = brandRequestService.approvePendingRequest(REQUEST_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldRejectPendingRequestWhenItIsPending() {
        // Arrange
        when(brandRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(pendingRequest("New Brand")));
        when(brandRequestDao.updateStatus(REQUEST_ID, BrandRequestService.STATUS_PENDING,
                BrandRequestService.STATUS_REJECTED)).thenReturn(true);

        // Exercise
        final boolean result = brandRequestService.rejectPendingRequest(REQUEST_ID);

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldResolveSubmitterEmailFromUserWhenRequestEmailIsBlank() {
        // Arrange
        final BrandRequest request = TestModels.brandRequest(REQUEST_ID, USER_ID, " ", "New Brand", "comment",
                BrandRequestService.STATUS_PENDING, LocalDateTime.now());
        final User user = TestModels.user(USER_ID, "user", "fallback@example.com", "p", "user", LocalDateTime.now());
        when(brandRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        when(brandDao.findByName("New Brand")).thenReturn(Optional.empty());
        when(brandRequestDao.updateStatus(REQUEST_ID, BrandRequestService.STATUS_PENDING,
                BrandRequestService.STATUS_APPROVED)).thenReturn(true);
        when(userService.getUserById(USER_ID)).thenReturn(Optional.of(user));

        // Exercise
        final boolean result = brandRequestService.approvePendingRequest(REQUEST_ID);

        // Assertions
        assertTrue(result);
    }
}
