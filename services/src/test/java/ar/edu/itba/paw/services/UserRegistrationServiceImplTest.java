package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.exception.ServiceOperationException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserRegistrationServiceImplTest {

    private static final long USER_ID = 42L;
    private static final String USERNAME = "joaco";
    private static final String EMAIL = "joaco@example.com";
    private static final String PASSWORD = "secret-password";

    @Mock
    private UserService userService;
    @Mock
    private ReviewService reviewService;
    @Mock
    private CarRequestService carRequestService;

    @InjectMocks
    private UserRegistrationServiceImpl userRegistrationService;

    @Test
    public void shouldReturnCreatedUserWhenRegistrationCompletes() {
        // Arrange
        final User created = TestModels.user(USER_ID, USERNAME, EMAIL, "encoded", "user", LocalDateTime.now());
        when(userService.createUser(USERNAME, EMAIL, PASSWORD)).thenReturn(created);

        // Exercise
        final User result = userRegistrationService.register(USERNAME, EMAIL, PASSWORD);

        // Assertions
        assertEquals(USER_ID, result.getId());
        assertEquals(EMAIL, result.getEmail());
    }

    @Test
    public void shouldPropagateUserCreationFailure() {
        // Arrange
        final ServiceOperationException failure = new ServiceOperationException("user creation failed", new RuntimeException());
        when(userService.createUser(USERNAME, EMAIL, PASSWORD)).thenThrow(failure);

        // Exercise
        final ServiceOperationException result = assertThrows(ServiceOperationException.class,
                () -> userRegistrationService.register(USERNAME, EMAIL, PASSWORD));

        // Assertions
        assertSame(failure, result);
    }

    @Test
    public void shouldPropagateReviewClaimFailure() {
        // Arrange
        final User created = TestModels.user(USER_ID, USERNAME, EMAIL, "encoded", "user", LocalDateTime.now());
        final ServiceOperationException failure = new ServiceOperationException("review claim failed", new RuntimeException());
        when(userService.createUser(USERNAME, EMAIL, PASSWORD)).thenReturn(created);
        doThrow(failure).when(reviewService).claimPreRegistrationReviews(USER_ID, EMAIL);

        // Exercise
        final ServiceOperationException result = assertThrows(ServiceOperationException.class,
                () -> userRegistrationService.register(USERNAME, EMAIL, PASSWORD));

        // Assertions
        assertSame(failure, result);
    }

    @Test
    public void shouldPropagateCarRequestClaimFailure() {
        // Arrange
        final User created = TestModels.user(USER_ID, USERNAME, EMAIL, "encoded", "user", LocalDateTime.now());
        final ServiceOperationException failure = new ServiceOperationException("request claim failed", new RuntimeException());
        when(userService.createUser(USERNAME, EMAIL, PASSWORD)).thenReturn(created);
        doThrow(failure).when(carRequestService).claimPreRegistrationRequests(USER_ID, EMAIL);

        // Exercise
        final ServiceOperationException result = assertThrows(ServiceOperationException.class,
                () -> userRegistrationService.register(USERNAME, EMAIL, PASSWORD));

        // Assertions
        assertSame(failure, result);
    }
}
