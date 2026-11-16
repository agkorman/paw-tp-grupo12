package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.AuthRefreshToken;
import ar.edu.itba.paw.model.AuthSession;
import ar.edu.itba.paw.model.User;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

class AuthSessionDaoTest extends AbstractPersistenceTest {

    private static final Instant NOW = Instant.parse("2026-11-15T18:00:00Z");

    @Autowired
    private AuthSessionDao dao;

    @Test
    void createsSessionWithoutChangingUser() {
        // Arrange
        final User user = createUser("session-create");
        final Instant expires = NOW.plusSeconds(86400);
        // Exercise
        final AuthSession result = dao.create(user.getId(), NOW, expires, true);
        // Assertions
        assertTrue(result.getId() > 0);
        assertEquals(expires, result.getExpiresAt());
        assertEquals(user.getId(), jdbcTemplate.queryForObject(
                "SELECT user_id FROM auth_sessions WHERE session_id = ?", Long.class, result.getId()));
        assertEquals(expires, jdbcTemplate.queryForObject(
                "SELECT expires_at FROM auth_sessions WHERE session_id = ?",
                (rs, row) -> rs.getTimestamp(1).toInstant(), result.getId()));
        assertEquals(Boolean.TRUE, jdbcTemplate.queryForObject(
                "SELECT remember_me FROM auth_sessions WHERE session_id = ?", Boolean.class, result.getId()));
        assertEquals("secret", jdbcTemplate.queryForObject(
                "SELECT password FROM users WHERE user_id = ?", String.class, user.getId()));
    }

    @Test
    void persistsOnlyRefreshHashLinkedToSession() {
        // Arrange
        final long sessionId = insertSession("token-create", NOW.plusSeconds(86400));
        final String hash = "a".repeat(64);
        // Exercise
        final AuthRefreshToken result = dao.createRefreshToken(sessionId, hash, NOW);
        // Assertions
        assertTrue(result.getId() > 0);
        assertEquals(hash, result.getTokenHash());
        assertEquals(hash, jdbcTemplate.queryForObject(
                "SELECT token_hash FROM auth_refresh_tokens WHERE refresh_token_id = ?", String.class, result.getId()));
        assertEquals(sessionId, jdbcTemplate.queryForObject(
                "SELECT session_id FROM auth_refresh_tokens WHERE refresh_token_id = ?", Long.class, result.getId()));
        assertNull(jdbcTemplate.queryForObject(
                "SELECT consumed_at FROM auth_refresh_tokens WHERE refresh_token_id = ?", Object.class, result.getId()));
    }

    @Test
    void hashLookupIncludesConsumedHistory() {
        // Arrange
        final long sessionId = insertSession("consumed", NOW.plusSeconds(86400));
        final String hash = "b".repeat(64);
        insertToken(sessionId, hash);
        jdbcTemplate.update("UPDATE auth_refresh_tokens SET consumed_at = created_at WHERE session_id = ?", sessionId);
        // Exercise
        final Optional<Long> result = dao.findSessionIdByTokenHash(hash);
        // Assertions
        assertEquals(Optional.of(sessionId), result);
    }

    @Test
    void unknownHashReturnsEmpty() {
        // Arrange
        final String hash = "c".repeat(64);
        // Exercise
        final Optional<Long> result = dao.findSessionIdByTokenHash(hash);
        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void tokenCannotBeReadThroughAnotherSession() {
        // Arrange
        final long owner = insertSession("owner", NOW.plusSeconds(86400));
        final long other = insertSession("other", NOW.plusSeconds(86400));
        final String hash = "d".repeat(64);
        insertToken(owner, hash);
        // Exercise
        final Optional<AuthRefreshToken> result = dao.findTokenByHash(other, hash);
        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void readsTokenConsumptionAfterClear() {
        // Arrange
        final long sessionId = insertSession("consumed-read", NOW.plusSeconds(86400));
        final String hash = "e".repeat(64);
        insertToken(sessionId, hash);
        jdbcTemplate.update("UPDATE auth_refresh_tokens SET consumed_at = created_at WHERE session_id = ?", sessionId);
        // Exercise
        final Optional<AuthRefreshToken> result = dao.findTokenByHash(sessionId, hash);
        // Assertions
        assertEquals(NOW, result.orElseThrow().getConsumedAt());
        assertEquals(sessionId, result.orElseThrow().getSession().getId());
    }

    @Test
    void ownershipQueryDoesNotReturnAnotherUsersSession() {
        // Arrange
        final long sessionId = insertSession("owned", NOW.plusSeconds(86400));
        final User other = createUser("nonowner");
        // Exercise
        final Optional<AuthSession> result = dao.findByUserAndId(other.getId(), sessionId);
        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void ownershipQueryReturnsSessionForOwner() {
        // Arrange
        final long sessionId = insertSession("own-read", NOW.plusSeconds(86400));
        final long userId = jdbcTemplate.queryForObject("SELECT user_id FROM auth_sessions WHERE session_id = ?",
                Long.class, sessionId);
        // Exercise
        final Optional<AuthSession> result = dao.findByUserAndId(userId, sessionId);
        // Assertions
        assertEquals(sessionId, result.orElseThrow().getId());
        assertEquals(NOW, result.orElseThrow().getCreatedAt());
    }

    @Test
    void lockReturnsMissingSessionAsEmpty() {
        // Arrange
        final long absentId = Long.MAX_VALUE;
        // Exercise
        final Optional<AuthSession> result = dao.lockById(absentId);
        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void locksExistingSession() {
        // Arrange
        final long sessionId = insertSession("lock", NOW.plusSeconds(86400));
        // Exercise
        final Optional<AuthSession> result = dao.lockById(sessionId);
        // Assertions
        assertEquals(sessionId, result.orElseThrow().getId());
        assertEquals(NOW.plusSeconds(86400), result.orElseThrow().getExpiresAt());
    }

    @Test
    void cleanupCascadesOnlyAtOrBeforeDeadlineAndPreservesUnexpiredRevocations() {
        // Arrange
        final Instant cutoff = NOW.plusSeconds(86400);
        final long expired = insertSession("expired", cutoff);
        final long active = insertSession("active", cutoff.plusSeconds(1));
        final long revoked = insertSession("revoked", cutoff.plusSeconds(1));
        insertToken(expired, "1".repeat(64));
        insertToken(active, "2".repeat(64));
        insertToken(revoked, "3".repeat(64));
        jdbcTemplate.update("UPDATE auth_sessions SET revoked_at = created_at, revocation_reason = 'LOGOUT' WHERE session_id = ?", revoked);
        // Exercise
        final int result = dao.deleteExpired(cutoff);
        // Assertions
        flushAndClear();
        assertEquals(1, result);
        assertEquals(0, countRows("SELECT COUNT(*) FROM auth_sessions WHERE session_id = ?", expired));
        assertEquals(0, countRows("SELECT COUNT(*) FROM auth_refresh_tokens WHERE session_id = ?", expired));
        assertEquals(2, countRows("SELECT COUNT(*) FROM auth_sessions WHERE session_id IN (?, ?)", active, revoked));
        assertEquals(2, countRows("SELECT COUNT(*) FROM auth_refresh_tokens WHERE session_id IN (?, ?)", active, revoked));
        assertEquals(3, countRows("SELECT COUNT(*) FROM users WHERE username IN (?, ?, ?)",
                "user-expired", "user-active", "user-revoked"));
    }

    @Test
    void rejectsDuplicateHashesAcrossSessions() {
        // Arrange
        final long first = insertSession("unique1", NOW.plusSeconds(86400));
        final long second = insertSession("unique2", NOW.plusSeconds(86400));
        final String hash = "f".repeat(64);
        insertToken(first, hash);
        // Exercise
        final DataIntegrityViolationException result = assertThrows(DataIntegrityViolationException.class, () -> dao.createRefreshToken(second, hash, NOW));
        // Assertions
        assertNotNull(result.getCause());
        assertEquals(1, countRows("SELECT COUNT(*) FROM auth_refresh_tokens WHERE token_hash = ?", hash));
        assertEquals(0, countRows("SELECT COUNT(*) FROM auth_refresh_tokens WHERE session_id = ?", second));
    }

    private long insertSession(final String suffix, final Instant expiresAt) {
        final User user = createUser(suffix);
        jdbcTemplate.update("INSERT INTO auth_sessions (user_id, created_at, expires_at, remember_me) VALUES (?, ?, ?, ?)",
                user.getId(), java.sql.Timestamp.from(NOW), java.sql.Timestamp.from(expiresAt), false);
        return jdbcTemplate.queryForObject("SELECT session_id FROM auth_sessions WHERE user_id = ?", Long.class, user.getId());
    }

    private void insertToken(final long sessionId, final String hash) {
        jdbcTemplate.update("INSERT INTO auth_refresh_tokens (session_id, token_hash, created_at) VALUES (?, ?, ?)",
                sessionId, hash, java.sql.Timestamp.from(NOW));
    }
}
