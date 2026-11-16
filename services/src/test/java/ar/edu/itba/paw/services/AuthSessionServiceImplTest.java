package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.AuthRefreshToken;
import ar.edu.itba.paw.model.AuthSession;
import ar.edu.itba.paw.model.RefreshResult;
import ar.edu.itba.paw.model.SessionCredentials;
import ar.edu.itba.paw.model.SessionRevocationResult;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.AuthSessionDao;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthSessionServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-11-15T18:00:00Z");
    private static final String HASH = "a".repeat(64);
    private static final String NEXT_HASH = "b".repeat(64);

    @Mock private AuthSessionDao sessionDao;
    @Mock private RefreshTokenCodec tokenCodec;
    @Mock private Clock clock;
    @InjectMocks private AuthSessionServiceImpl service;

    @Test
    void createsStandardSessionWith24HourAbsoluteDeadline() {
        // Arrange
        final AuthSession session = session(NOW.plus(Duration.ofHours(24)), false);
        when(clock.instant()).thenReturn(NOW);
        when(sessionDao.create(42, NOW, session.getExpiresAt(), false)).thenReturn(session);
        stubIssuance();
        // Exercise
        final SessionCredentials result = service.createSession(42, false);
        // Assertions
        assertEquals(NOW.plus(Duration.ofHours(24)), result.getExpiresAt());
        assertEquals(42, result.getUserId());
        assertEquals(7, result.getSessionId());
        assertEquals("next", result.getRefreshToken());
    }

    @Test
    void createsRememberedSessionWith30DayAbsoluteDeadline() {
        // Arrange
        final AuthSession session = session(NOW.plus(Duration.ofDays(30)), true);
        when(clock.instant()).thenReturn(NOW);
        when(sessionDao.create(42, NOW, session.getExpiresAt(), true)).thenReturn(session);
        stubIssuance();
        // Exercise
        final SessionCredentials result = service.createSession(42, true);
        // Assertions
        assertEquals(NOW.plus(Duration.ofDays(30)), result.getExpiresAt());
        assertEquals(NOW, result.getIssuedAt());
    }

    @Test
    void rotationConsumesTokenWithoutExtendingNearExpirySession() {
        // Arrange
        final AuthSession session = session(NOW.plusSeconds(5), true);
        final AuthRefreshToken token = new AuthRefreshToken(session, HASH, NOW.minusSeconds(60));
        stubLocked(session);
        when(sessionDao.findTokenByHash(7, HASH)).thenReturn(Optional.of(token));
        stubIssuance();
        // Exercise
        final RefreshResult result = service.rotate("old");
        // Assertions
        assertEquals(RefreshResult.Status.ISSUED, result.getStatus());
        assertEquals(NOW.plusSeconds(5), result.getCredentials().orElseThrow().getExpiresAt());
        assertEquals("next", result.getCredentials().orElseThrow().getRefreshToken());
        assertEquals(NOW, token.getConsumedAt());
        assertNull(session.getRevokedAt());
    }

    @Test
    void reusedRefreshRevokesSessionAndReturnsRejectionNormally() {
        // Arrange
        final AuthSession session = session(NOW.plusSeconds(600), false);
        final AuthRefreshToken token = new AuthRefreshToken(session, HASH, NOW.minusSeconds(60));
        token.setConsumedAt(NOW.minusSeconds(10));
        stubLocked(session);
        when(sessionDao.findTokenByHash(7, HASH)).thenReturn(Optional.of(token));
        // Exercise
        final RefreshResult result = service.rotate("old");
        // Assertions
        assertEquals(RefreshResult.Status.REUSED, result.getStatus());
        assertTrue(result.getCredentials().isEmpty());
        assertEquals(NOW, session.getRevokedAt());
        assertEquals(AuthSession.RevocationReason.REFRESH_REUSE, session.getRevocationReason());
    }

    @Test
    void rejectsSessionAtExactExpiryBoundary() {
        // Arrange
        final AuthSession session = session(NOW, false);
        stubLocked(session);
        // Exercise
        final RefreshResult result = service.rotate("old");
        // Assertions
        assertEquals(RefreshResult.Status.EXPIRED, result.getStatus());
        assertTrue(result.getCredentials().isEmpty());
        assertNull(session.getRevokedAt());
    }

    @Test
    void rejectsPreviouslyRevokedSessionWithoutChangingRevocation() {
        // Arrange
        final AuthSession session = session(NOW.plusSeconds(600), false);
        session.setRevokedAt(NOW.minusSeconds(30));
        session.setRevocationReason(AuthSession.RevocationReason.LOGOUT);
        stubLocked(session);
        // Exercise
        final RefreshResult result = service.rotate("old");
        // Assertions
        assertEquals(RefreshResult.Status.REVOKED, result.getStatus());
        assertEquals(NOW.minusSeconds(30), session.getRevokedAt());
        assertEquals(AuthSession.RevocationReason.LOGOUT, session.getRevocationReason());
    }

    @Test
    void malformedRefreshReturnsInvalid() {
        // Arrange
        final String token = "malformed";
        when(tokenCodec.digest(token)).thenReturn(Optional.empty());
        // Exercise
        final RefreshResult result = service.rotate(token);
        // Assertions
        assertEquals(RefreshResult.Status.INVALID, result.getStatus());
        assertTrue(result.getCredentials().isEmpty());
    }

    @Test
    void unknownRefreshReturnsInvalid() {
        // Arrange
        final String token = "unknown";
        when(tokenCodec.digest(token)).thenReturn(Optional.of(HASH));
        when(sessionDao.findSessionIdByTokenHash(HASH)).thenReturn(Optional.empty());
        // Exercise
        final RefreshResult result = service.rotate(token);
        // Assertions
        assertEquals(RefreshResult.Status.INVALID, result.getStatus());
        assertTrue(result.getCredentials().isEmpty());
    }

    @Test
    void sessionRemovedByCleanupBeforeLockReturnsInvalid() {
        // Arrange
        final String token = "old";
        when(tokenCodec.digest(token)).thenReturn(Optional.of(HASH));
        when(sessionDao.findSessionIdByTokenHash(HASH)).thenReturn(Optional.of(7L));
        when(sessionDao.lockById(7)).thenReturn(Optional.empty());
        // Exercise
        final RefreshResult result = service.rotate(token);
        // Assertions
        assertEquals(RefreshResult.Status.INVALID, result.getStatus());
        assertTrue(result.getCredentials().isEmpty());
    }

    @Test
    void tokenMissingAfterLockReturnsInvalid() {
        // Arrange
        final AuthSession session = session(NOW.plusSeconds(600), false);
        stubLocked(session);
        when(sessionDao.findTokenByHash(7, HASH)).thenReturn(Optional.empty());
        // Exercise
        final RefreshResult result = service.rotate("old");
        // Assertions
        assertEquals(RefreshResult.Status.INVALID, result.getStatus());
        assertNull(session.getRevokedAt());
    }

    @Test
    void verifiedAccessCannotRevokeAnotherSessionOfSameUser() {
        // Arrange
        final long authenticatedSessionId = 7;
        final long targetSessionId = 8;
        // Exercise
        final SessionRevocationResult result = service.revoke(42, authenticatedSessionId, 42, targetSessionId);
        // Assertions
        assertEquals(SessionRevocationResult.FORBIDDEN, result);
    }

    @Test
    void verifiedAccessCannotRevokeAnotherUsersSession() {
        // Arrange
        final long authenticatedUserId = 42;
        final long targetUserId = 99;
        // Exercise
        final SessionRevocationResult result = service.revoke(authenticatedUserId, 7, targetUserId, 7);
        // Assertions
        assertEquals(SessionRevocationResult.FORBIDDEN, result);
    }

    @Test
    void revokeChecksPersistedOwner() {
        // Arrange
        final AuthSession session = session(NOW.plusSeconds(600), false);
        when(sessionDao.lockById(7)).thenReturn(Optional.of(session));
        // Exercise
        final SessionRevocationResult result = service.revoke(99, 7, 99, 7);
        // Assertions
        assertEquals(SessionRevocationResult.FORBIDDEN, result);
        assertNull(session.getRevokedAt());
    }

    @Test
    void revokeIsIdempotentAndPreservesOriginalReason() {
        // Arrange
        final AuthSession session = session(NOW.plusSeconds(600), false);
        session.setRevokedAt(NOW.minusSeconds(10));
        session.setRevocationReason(AuthSession.RevocationReason.REFRESH_REUSE);
        when(sessionDao.lockById(7)).thenReturn(Optional.of(session));
        when(clock.instant()).thenReturn(NOW);
        // Exercise
        final SessionRevocationResult result = service.revoke(42, 7, 42, 7);
        // Assertions
        assertEquals(SessionRevocationResult.REVOKED, result);
        assertEquals(NOW.minusSeconds(10), session.getRevokedAt());
        assertEquals(AuthSession.RevocationReason.REFRESH_REUSE, session.getRevocationReason());
    }

    @Test
    void logoutWithRefreshDoesNotConsumeOrReplaceIt() {
        // Arrange
        final AuthSession session = session(NOW.plusSeconds(600), false);
        final AuthRefreshToken token = new AuthRefreshToken(session, HASH, NOW.minusSeconds(60));
        stubLocked(session);
        when(sessionDao.findTokenByHash(7, HASH)).thenReturn(Optional.of(token));
        // Exercise
        final SessionRevocationResult result = service.revokeWithRefresh(42, 7, "old");
        // Assertions
        assertEquals(SessionRevocationResult.REVOKED, result);
        assertEquals(NOW, session.getRevokedAt());
        assertEquals(AuthSession.RevocationReason.LOGOUT, session.getRevocationReason());
        assertNull(token.getConsumedAt());
    }

    @Test
    void logoutWithConsumedRefreshRevokesSessionAsReuse() {
        // Arrange
        final AuthSession session = session(NOW.plusSeconds(600), false);
        final AuthRefreshToken token = new AuthRefreshToken(session, HASH, NOW.minusSeconds(60));
        token.setConsumedAt(NOW.minusSeconds(10));
        stubLocked(session);
        when(sessionDao.findTokenByHash(7, HASH)).thenReturn(Optional.of(token));
        // Exercise
        final SessionRevocationResult result = service.revokeWithRefresh(42, 7, "old");
        // Assertions
        assertEquals(SessionRevocationResult.REUSED, result);
        assertEquals(AuthSession.RevocationReason.REFRESH_REUSE, session.getRevocationReason());
    }

    @Test
    void refreshCannotRevokeAnotherSession() {
        // Arrange
        when(tokenCodec.digest("old")).thenReturn(Optional.of(HASH));
        when(sessionDao.findSessionIdByTokenHash(HASH)).thenReturn(Optional.of(7L));
        // Exercise
        final SessionRevocationResult result = service.revokeWithRefresh(42, 8, "old");
        // Assertions
        assertEquals(SessionRevocationResult.FORBIDDEN, result);
    }

    @Test
    void ownerCanReadActiveSession() {
        // Arrange
        final AuthSession expected = session(NOW.plusSeconds(100), false);
        when(sessionDao.findByUserAndId(42, 7)).thenReturn(Optional.of(expected));
        when(clock.instant()).thenReturn(NOW);
        // Exercise
        final Optional<AuthSession> result = service.findActiveSession(42, 42, 7);
        // Assertions
        assertEquals(7, result.orElseThrow().getId());
        assertEquals(NOW.plusSeconds(100), result.orElseThrow().getExpiresAt());
    }

    @Test
    void hidesSessionFromOtherUser() {
        // Arrange
        final long otherUser = 99;
        // Exercise
        final Optional<AuthSession> result = service.findActiveSession(otherUser, 42, 7);
        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void hidesSessionAtExactExpiry() {
        // Arrange
        when(sessionDao.findByUserAndId(42, 7)).thenReturn(Optional.of(session(NOW, false)));
        when(clock.instant()).thenReturn(NOW);
        // Exercise
        final Optional<AuthSession> result = service.findActiveSession(42, 42, 7);
        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void hidesRevokedSession() {
        // Arrange
        final AuthSession session = session(NOW.plusSeconds(100), false);
        session.setRevokedAt(NOW);
        when(sessionDao.findByUserAndId(42, 7)).thenReturn(Optional.of(session));
        // Exercise
        final Optional<AuthSession> result = service.findActiveSession(42, 42, 7);
        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void missingSessionRemainsAbsent() {
        // Arrange
        when(sessionDao.findByUserAndId(42, 7)).thenReturn(Optional.empty());
        // Exercise
        final Optional<AuthSession> result = service.findActiveSession(42, 42, 7);
        // Assertions
        assertTrue(result.isEmpty());
    }

    private AuthSession session(final Instant expiry, final boolean rememberMe) {
        final User user = new User("driver", "driver@example.test", "hash", "user", "es");
        user.setId(42);
        final AuthSession session = new AuthSession(user, NOW.minusSeconds(3600), expiry, rememberMe);
        session.setId(7);
        return session;
    }

    private void stubLocked(final AuthSession session) {
        when(tokenCodec.digest("old")).thenReturn(Optional.of(HASH));
        when(sessionDao.findSessionIdByTokenHash(HASH)).thenReturn(Optional.of(7L));
        when(sessionDao.lockById(7)).thenReturn(Optional.of(session));
        when(clock.instant()).thenReturn(NOW);
    }

    private void stubIssuance() {
        when(tokenCodec.generate()).thenReturn("next");
        when(tokenCodec.digest("next")).thenReturn(Optional.of(NEXT_HASH));
    }
}
