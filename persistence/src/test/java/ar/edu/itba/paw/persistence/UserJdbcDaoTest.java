package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserJdbcDaoTest extends AbstractPersistenceTest {

    @Test
    public void shouldCreateUserAndPersistFields() {
        // Arrange
        final String username = "created-user";
        final String email = "created@example.com";
        final String role = "admin";

        // Exercise
        final User result = userDao.create(username, email, "password", role);

        // Assertions
        assertEquals(username, result.getUsername());
        assertEquals(1, countRows("SELECT COUNT(*) FROM users WHERE user_id = ?", result.getId()));
        assertEquals(email, jdbcTemplate.queryForObject(
                "SELECT email FROM users WHERE user_id = ?", String.class, result.getId()
        ));
        assertEquals(role, jdbcTemplate.queryForObject(
                "SELECT role FROM users WHERE user_id = ?", String.class, result.getId()
        ));
    }

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
        assertEquals("admin", jdbcTemplate.queryForObject(
                "SELECT role FROM users WHERE user_id = ?", String.class, created.getId()
        ));
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
        assertEquals(1, countRows("SELECT COUNT(*) FROM users"));
        assertEquals("user", jdbcTemplate.queryForObject(
                "SELECT role FROM users WHERE email = ?", String.class, "existing@example.com"
        ));
    }
}
