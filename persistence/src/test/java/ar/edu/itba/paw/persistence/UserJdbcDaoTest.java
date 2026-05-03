package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserJdbcDaoTest extends AbstractPersistenceTest {

    @Test
    public void shouldFindUserByEmailIgnoringCaseWhenUserExists() {
        // Arrange
        final User created = userDao.create("alice", "Alice@Example.com", "password", "user");

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
        final User created = userDao.create("admin-candidate", "admin-candidate@example.com", "password", "user");

        // Exercise
        final boolean result = userDao.updateRole(created.getId(), "admin");

        // Assertions
        assertTrue(result);
        assertEquals("admin", userDao.findById(created.getId()).orElseThrow().getRole());
    }

    @Test
    public void shouldUpdatePersistedUsernameWhenUserExists() {
        // Arrange
        final User created = userDao.create("old-name", "rename@example.com", "password", "user");

        // Exercise
        final boolean result = userDao.updateUsername(created.getId(), "new-name");

        // Assertions
        assertTrue(result);
        assertEquals("new-name", userDao.findById(created.getId()).orElseThrow().getUsername());
        assertEquals("rename@example.com", userDao.findById(created.getId()).orElseThrow().getEmail());
    }

    @Test
    public void shouldRejectUsernameUpdateWhenNormalizedUsernameAlreadyExists() {
        // Arrange
        userDao.create("Nica", "nica@example.com", "password", "user");
        final User created = userDao.create("other", "other@example.com", "password", "user");

        // Exercise
        assertThrows(DataIntegrityViolationException.class,
                () -> userDao.updateUsername(created.getId(), "nica"));

        // Assertions
        assertEquals("other", userDao.findById(created.getId()).orElseThrow().getUsername());
    }

    @Test
    public void shouldFindEmailsByNormalizedRolesOnly() {
        // Arrange
        userDao.create("regular", "regular@example.com", "password", "user");
        userDao.create("admin", "admin@example.com", "password", "admin");

        // Exercise
        final List<String> result = userDao.findEmailsByRoles(List.of(" ADMIN ", "missing"));

        // Assertions
        assertEquals(1, result.size());
        assertEquals("admin@example.com", result.get(0));
    }

    @Test
    public void shouldReturnFalseWhenUpdatingMissingUserRole() {
        // Arrange
        userDao.create("existing", "existing@example.com", "password", "user");

        // Exercise
        final boolean result = userDao.updateRole(9999L, "admin");

        // Assertions
        assertFalse(result);
        assertEquals(1, userDao.findAll().size());
    }
}
