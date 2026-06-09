package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.EmailRecipient;
import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserDaoTest extends AbstractPersistenceTest {

    @Test
    public void shouldCreateUserAndPersistFields() {
        // Arrange
        final String username = "created-user";
        final String email = "created@example.com";
        final String role = "admin";

        // Exercise
        final User result = userDao.create(username, email, "password", role);
        flushAndClear();

        // Assertions
        assertEquals(username, result.getUsername());
        assertEquals(1, countRows("SELECT COUNT(*) FROM users WHERE user_id = ?", result.getId()));
        assertEquals(email, jdbcTemplate.queryForObject(
                "SELECT email FROM users WHERE user_id = ?", String.class, result.getId()
        ));
        assertEquals(role, jdbcTemplate.queryForObject(
                "SELECT role FROM users WHERE user_id = ?", String.class, result.getId()
        ));
        assertEquals("es", jdbcTemplate.queryForObject(
                "SELECT preferred_locale FROM users WHERE user_id = ?", String.class, result.getId()
        ));
    }

    @Test
    public void shouldFindUserByEmailIgnoringCaseWhenUserExists() {
        // Arrange
        final User created = insertUser("alice", "Alice@Example.com", "password", "user");

        // Exercise
        final Optional<User> result = userDao.findByEmail("alice@example.com");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(created.getId(), result.get().getId());
        assertEquals("Alice@Example.com", result.get().getEmail());
    }

    @Test
    public void shouldUpdatePersistedRoleWhenUserExists() {
        // Arrange
        final User created = insertUser("admin-candidate", "admin-candidate@example.com", "password", "user");

        // Exercise
        final boolean result = userDao.updateRole(created.getId(), "admin");
        flushAndClear();

        // Assertions
        assertTrue(result);
        assertEquals("admin", jdbcTemplate.queryForObject(
                "SELECT role FROM users WHERE user_id = ?", String.class, created.getId()
        ));
    }

    @Test
    public void shouldUpdatePersistedUsernameWhenUserExists() {
        // Arrange
        final User created = userDao.create("old-name", "rename@example.com", "password", "user");

        // Exercise
        final boolean result = userDao.updateUsername(created.getId(), "new-name");
        flushAndClear();

        // Assertions
        assertTrue(result);
        assertEquals("new-name", jdbcTemplate.queryForObject(
                "SELECT username FROM users WHERE user_id = ?", String.class, created.getId()
        ));
        assertEquals("rename@example.com", jdbcTemplate.queryForObject(
                "SELECT email FROM users WHERE user_id = ?", String.class, created.getId()
        ));
    }

    @Test
    public void shouldUpdatePersistedPreferredLocaleWhenUserExists() {
        // Arrange
        final User created = insertUser("language-user", "language@example.com", "password", "user");

        // Exercise
        final boolean result = userDao.updatePreferredLocale(created.getId(), "en");
        flushAndClear();

        // Assertions
        assertTrue(result);
        assertEquals("en", userDao.findById(created.getId()).orElseThrow().getPreferredLocale());
        assertEquals("language@example.com", jdbcTemplate.queryForObject(
                "SELECT email FROM users WHERE user_id = ?", String.class, created.getId()
        ));
    }

    @Test
    public void shouldRejectUsernameUpdateWhenNormalizedUsernameAlreadyExists() {
        // Arrange
        userDao.create("Nica", "nica@example.com", "password", "user");
        final User created = userDao.create("other", "other@example.com", "password", "user");

        // Exercise
        assertThrows(DataIntegrityViolationException.class, () -> {
            userDao.updateUsername(created.getId(), "nica");
            flushAndClear();
        });

        // Assertions
        assertEquals("other", userDao.findById(created.getId()).orElseThrow().getUsername());
    }

    @Test
    public void shouldFindEmailRecipientsByNormalizedRolesOnly() {
        // Arrange
        insertUser("regular", "regular@example.com", "password", "user");
        final User admin = insertUser("admin", "admin@example.com", "password", "admin");
        jdbcTemplate.update("UPDATE users SET preferred_locale = ? WHERE user_id = ?", "en", admin.getId());

        // Exercise
        final List<EmailRecipient> result = userDao.findEmailRecipientsByRoles(List.of(" ADMIN ", "missing"));

        // Assertions
        assertEquals(1, result.size());
        assertEquals("admin@example.com", result.get(0).getEmail());
        assertEquals("en", result.get(0).getPreferredLocale());
    }

    @Test
    public void shouldReturnFalseWhenUpdatingMissingUserRole() {
        // Arrange
        insertUser("existing", "existing@example.com", "password", "user");

        // Exercise
        final boolean result = userDao.updateRole(9999L, "admin");

        // Assertions
        assertFalse(result);
        assertEquals(1, countRows("SELECT COUNT(*) FROM users"));
        assertEquals("user", jdbcTemplate.queryForObject(
                "SELECT role FROM users WHERE email = ?", String.class, "existing@example.com"
        ));
    }

    @Test
    public void shouldReturnEmptyWhenFindByEmailHasNoMatch() {
        // Arrange
        insertUser("ghost-email", "ghost@example.com", "password", "user");

        // Exercise
        final Optional<User> result = userDao.findByEmail("nobody@example.com");

        // Assertions
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldReturnEmptyWhenFindByIdHasNoMatch() {
        // Arrange
        final long missingId = 9999L;

        // Exercise
        final Optional<User> result = userDao.findById(missingId);

        // Assertions
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldReturnFalseWhenUpdatingMissingUserUsername() {
        // Arrange
        insertUser("real-user", "real@example.com", "password", "user");

        // Exercise
        final boolean result = userDao.updateUsername(9999L, "ghost-name");

        // Assertions
        assertFalse(result);
        assertEquals(0, countRows("SELECT COUNT(*) FROM users WHERE username = ?", "ghost-name"));
    }

    @Test
    public void shouldRejectDuplicateEmailOnCreate() {
        // Arrange
        insertUser("first-user", "duplicate@example.com", "password", "user");

        // Exercise
        assertThrows(DataIntegrityViolationException.class,
                () -> userDao.create("second-user", "duplicate@example.com", "password", "user"));

        // Assertions
        assertEquals(1, countRows("SELECT COUNT(*) FROM users WHERE email = ?", "duplicate@example.com"));
    }

    @Test
    public void shouldFindOnlyRequestedUsersByIds() {
        // Arrange
        final User first = insertUser("batch-first", "batch-first@example.com", "password", "user");
        final User second = insertUser("batch-second", "batch-second@example.com", "password", "user");
        final User excluded = insertUser("batch-excluded", "batch-excluded@example.com", "password", "user");

        // Exercise
        final List<User> result = userDao.findByIds(List.of(first.getId(), second.getId()));

        // Assertions
        assertEquals(2, result.size());
        assertEquals(first.getId(), result.get(0).getId());
        assertEquals(second.getId(), result.get(1).getId());
        assertFalse(result.stream().anyMatch(user -> user.getId() == excluded.getId()));
    }

    @Test
    public void shouldReturnEmptyListWhenFindingByEmptyIds() {
        // Arrange
        final List<Long> ids = List.of();

        // Exercise
        final List<User> result = userDao.findByIds(ids);

        // Assertions
        assertTrue(result.isEmpty());
    }
}
