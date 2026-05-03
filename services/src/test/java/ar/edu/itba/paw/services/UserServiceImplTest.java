package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.CarRequestDao;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.UserDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    private static final long USER_ID = 42L;
    private static final String RAW_USERNAME = "  Joaco  ";
    private static final String NORMALIZED_USERNAME = "Joaco";
    private static final String RAW_EMAIL = "  Joaco@Example.COM ";
    private static final String NORMALIZED_EMAIL = "joaco@example.com";
    private static final String RAW_PASSWORD = "secret-password";
    private static final String ENCODED_PASSWORD = "encoded-secret";

    @Mock
    private UserDao userDao;
    @Mock
    private ReviewDao reviewDao;
    @Mock
    private CarRequestDao carRequestDao;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void shouldReturnUserWhenDaoFindsExistingId() {
        // Arrange
        final User stored = new User(USER_ID, "Joaco", NORMALIZED_EMAIL, ENCODED_PASSWORD, "user", LocalDateTime.now());
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(stored));

        // Exercise
        final Optional<User> result = userService.getUserById(USER_ID);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(USER_ID, result.get().getId());
        assertEquals(NORMALIZED_EMAIL, result.get().getEmail());
    }

    @Test
    public void shouldReturnEmptyWhenFindByEmailReceivesBlank() {
        // Arrange
        final String blankEmail = "   ";

        // Exercise
        final Optional<User> result = userService.findByEmail(blankEmail);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyWhenFindByEmailReceivesNull() {
        // Arrange
        final String nullEmail = null;

        // Exercise
        final Optional<User> result = userService.findByEmail(nullEmail);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldNormalizeEmailBeforeQueryingDao() {
        // Arrange
        final User stored = new User(USER_ID, "Joaco", NORMALIZED_EMAIL, ENCODED_PASSWORD, "user", LocalDateTime.now());
        when(userDao.findByEmail(NORMALIZED_EMAIL)).thenReturn(Optional.of(stored));

        // Exercise
        final Optional<User> result = userService.findByEmail(RAW_EMAIL);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(NORMALIZED_EMAIL, result.get().getEmail());
    }

    @Test
    public void shouldReturnEmptyWhenFindByUsernameReceivesBlank() {
        // Arrange
        final String blankUsername = "   ";

        // Exercise
        final Optional<User> result = userService.findByUsername(blankUsername);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldCreateUserWithNormalizedFieldsAndEncodedPassword() {
        // Arrange
        final User created = new User(USER_ID, NORMALIZED_USERNAME, NORMALIZED_EMAIL, ENCODED_PASSWORD, "user",
                LocalDateTime.now());
        when(userDao.findByUsername(NORMALIZED_USERNAME)).thenReturn(Optional.empty());
        when(userDao.findByEmail(NORMALIZED_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userDao.create(NORMALIZED_USERNAME, NORMALIZED_EMAIL, ENCODED_PASSWORD, "user")).thenReturn(created);

        // Exercise
        final User result = userService.createUser(RAW_USERNAME, RAW_EMAIL, RAW_PASSWORD);

        // Assertions
        assertEquals(USER_ID, result.getId());
        assertEquals(NORMALIZED_USERNAME, result.getUsername());
        assertEquals(NORMALIZED_EMAIL, result.getEmail());
        assertEquals(ENCODED_PASSWORD, result.getPassword());
        assertEquals("user", result.getRole());
    }

    @Test
    public void shouldRejectCreateUserWhenUsernameIsBlank() {
        // Arrange
        final String blankUsername = "   ";

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(blankUsername, RAW_EMAIL, RAW_PASSWORD));

        // Assertions
        assertEquals("Username is required.", ex.getMessage());
    }

    @Test
    public void shouldRejectCreateUserWhenEmailIsBlank() {
        // Arrange
        final String blankEmail = "   ";

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(RAW_USERNAME, blankEmail, RAW_PASSWORD));

        // Assertions
        assertEquals("Email is required.", ex.getMessage());
    }

    @Test
    public void shouldRejectCreateUserWhenPasswordIsEmpty() {
        // Arrange
        final String emptyPassword = "";

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(RAW_USERNAME, RAW_EMAIL, emptyPassword));

        // Assertions
        assertEquals("Password is required.", ex.getMessage());
    }

    @Test
    public void shouldRejectCreateUserWhenPasswordIsNull() {
        // Arrange
        final String nullPassword = null;

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(RAW_USERNAME, RAW_EMAIL, nullPassword));

        // Assertions
        assertEquals("Password is required.", ex.getMessage());
    }

    @Test
    public void shouldRejectCreateUserWhenUsernameAlreadyExists() {
        // Arrange
        final User existing = new User(99L, NORMALIZED_USERNAME, "other@example.com", "x", "user", LocalDateTime.now());
        when(userDao.findByUsername(NORMALIZED_USERNAME)).thenReturn(Optional.of(existing));

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(RAW_USERNAME, RAW_EMAIL, RAW_PASSWORD));

        // Assertions
        assertEquals("Username is already registered.", ex.getMessage());
    }

    @Test
    public void shouldRejectCreateUserWhenEmailAlreadyExists() {
        // Arrange
        final User existing = new User(99L, "other", NORMALIZED_EMAIL, "x", "user", LocalDateTime.now());
        when(userDao.findByUsername(NORMALIZED_USERNAME)).thenReturn(Optional.empty());
        when(userDao.findByEmail(NORMALIZED_EMAIL)).thenReturn(Optional.of(existing));

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(RAW_USERNAME, RAW_EMAIL, RAW_PASSWORD));

        // Assertions
        assertEquals("Email is already registered.", ex.getMessage());
    }

    @Test
    public void shouldUpdateRoleNormalizedToLowerCase() {
        // Arrange
        when(userDao.updateRole(USER_ID, "admin")).thenReturn(true);

        // Exercise
        final boolean result = userService.updateRole(USER_ID, "  ADMIN ");

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldRejectUpdateRoleWhenRoleIsBlank() {
        // Arrange
        final String blankRole = "   ";

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateRole(USER_ID, blankRole));

        // Assertions
        assertEquals("Role is required.", ex.getMessage());
    }

    @Test
    public void shouldReturnModeratorEmailsFromDao() {
        // Arrange
        final List<String> emails = List.of("a@x.com", "b@x.com");
        when(userDao.findEmailsByRoles(List.of("moderator", "admin"))).thenReturn(emails);

        // Exercise
        final List<String> result = userService.getModeratorsEmails();

        // Assertions
        assertEquals(emails, result);
    }

    @Test
    public void shouldReturnAllUsersFromDao() {
        // Arrange
        final List<User> users = List.of(
                new User(1L, "a", "a@x.com", "p", "user", LocalDateTime.now()),
                new User(2L, "b", "b@x.com", "p", "user", LocalDateTime.now())
        );
        when(userDao.findAll()).thenReturn(users);

        // Exercise
        final List<User> result = userService.getAllUsers();

        // Assertions
        assertEquals(2, result.size());
        assertSame(users, result);
    }

    @Test
    public void shouldReturnEmptyWhenFindByIdReceivesUnknown() {
        // Arrange
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        // Exercise
        final Optional<User> result = userService.getUserById(999L);

        // Assertions
        assertTrue(result.isEmpty());
    }
}
