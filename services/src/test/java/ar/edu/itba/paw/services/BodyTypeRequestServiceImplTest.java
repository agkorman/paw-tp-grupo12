package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.BodyTypeRequest;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BodyTypeRequestDao;
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
public class BodyTypeRequestServiceImplTest {

    private static final long REQUEST_ID = 6L;
    private static final long USER_ID = 10L;
    private static final String EMAIL = "submitter@example.com";

    @Mock
    private BodyTypeRequestDao bodyTypeRequestDao;
    @Mock
    private BodyTypeDao bodyTypeDao;
    @Mock
    private UserService userService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private BodyTypeRequestServiceImpl bodyTypeRequestService;

    private static BodyTypeRequest pendingRequest(final String name) {
        return new BodyTypeRequest(REQUEST_ID, USER_ID, EMAIL, name, "comment",
                BodyTypeRequestService.STATUS_PENDING, LocalDateTime.now());
    }

    @Test
    public void shouldCreatePendingRequestWithNormalizedNameAndComments() {
        // Arrange
        final BodyTypeRequest created = pendingRequest("Roadster");
        when(bodyTypeRequestDao.create(USER_ID, EMAIL, "Roadster", "comment",
                BodyTypeRequestService.STATUS_PENDING)).thenReturn(created);

        // Exercise
        final BodyTypeRequest result = bodyTypeRequestService.createPendingRequest(USER_ID, EMAIL, " Roadster ", " comment ");

        // Assertions
        assertEquals(REQUEST_ID, result.getId());
        assertEquals("Roadster", result.getName());
    }

    @Test
    public void shouldRejectCreatePendingRequestWithBlankName() {
        // Arrange
        final String blankName = "  ";

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bodyTypeRequestService.createPendingRequest(USER_ID, EMAIL, blankName, "comment"));

        // Assertions
        assertEquals("Name is required for body type requests.", ex.getMessage());
    }

    @Test
    public void shouldRejectCreatePendingRequestWithoutSubmitterIdentity() {
        // Arrange
        final Long submittedByUserId = null;
        final String blankEmail = " ";

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bodyTypeRequestService.createPendingRequest(submittedByUserId, blankEmail, "Roadster", "comment"));

        // Assertions
        assertEquals("A submitter user id or email is required for body type requests.", ex.getMessage());
    }

    @Test
    public void shouldApprovePendingRequestUsingOverrideName() {
        // Arrange
        when(bodyTypeRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(pendingRequest("Existing Body")));
        when(bodyTypeDao.findByName("Override Body")).thenReturn(Optional.empty());
        when(bodyTypeRequestDao.updateStatus(REQUEST_ID, BodyTypeRequestService.STATUS_PENDING,
                BodyTypeRequestService.STATUS_APPROVED)).thenReturn(true);

        // Exercise
        final boolean result = bodyTypeRequestService.approvePendingRequest(REQUEST_ID, " Override Body ");

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldNotApprovePendingRequestWhenNameAlreadyExists() {
        // Arrange
        when(bodyTypeRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(pendingRequest("Sedan")));
        when(bodyTypeDao.findByName("Sedan")).thenReturn(Optional.of(new BodyType(1L, "Sedan", LocalDateTime.now())));

        // Exercise
        final boolean result = bodyTypeRequestService.approvePendingRequest(REQUEST_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldReturnEmptyPageWhenStatusFilterIsBlank() {
        // Arrange
        final String blankStatus = " ";
        final int requestedPage = -2;

        // Exercise
        final ar.edu.itba.paw.model.Page<BodyTypeRequest> result = bodyTypeRequestService.getBodyTypeRequestsByStatus(blankStatus, requestedPage);

        // Assertions
        assertTrue(result.getItems().isEmpty());
        assertEquals(1, result.getPageNumber());
    }
}
