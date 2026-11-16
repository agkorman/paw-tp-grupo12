package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.AuthRefreshToken;
import ar.edu.itba.paw.model.AuthSession;
import ar.edu.itba.paw.model.RefreshResult;
import ar.edu.itba.paw.model.SessionCredentials;
import ar.edu.itba.paw.model.SessionRevocationResult;
import ar.edu.itba.paw.persistence.AuthSessionDao;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthSessionServiceImpl implements AuthSessionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthSessionServiceImpl.class);
    private static final Duration STANDARD_LIFETIME = Duration.ofHours(24);
    private static final Duration REMEMBERED_LIFETIME = Duration.ofDays(30);

    private final AuthSessionDao sessionDao;
    private final RefreshTokenCodec tokenCodec;
    private final Clock clock;

    @Autowired
    public AuthSessionServiceImpl(final AuthSessionDao sessionDao, final RefreshTokenCodec tokenCodec,
                                  @Qualifier("authClock") final Clock clock) {
        this.sessionDao = sessionDao;
        this.tokenCodec = tokenCodec;
        this.clock = clock;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SessionCredentials createSession(final long userId, final boolean rememberMe) {
        final Instant now = now();
        final AuthSession session = sessionDao.create(userId, now,
                now.plus(rememberMe ? REMEMBERED_LIFETIME : STANDARD_LIFETIME), rememberMe);
        final SessionCredentials credentials = issue(session, now);
        LOGGER.info("created auth session id={} userId={}", session.getId(), userId);
        return credentials;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RefreshResult rotate(final String refreshToken) {
        final Optional<String> hash = tokenCodec.digest(refreshToken);
        if (hash.isEmpty()) {
            return RefreshResult.rejected(RefreshResult.Status.INVALID);
        }
        final Optional<AuthSession> found = sessionDao.findSessionIdByTokenHash(hash.get())
                .flatMap(sessionDao::lockById);
        if (found.isEmpty()) {
            return RefreshResult.rejected(RefreshResult.Status.INVALID);
        }
        final AuthSession session = found.get();
        // Read the clock and token AFTER acquiring the session lock. Another renewal may have waited here.
        final Instant now = now();
        if (!now.isBefore(session.getExpiresAt())) {
            return RefreshResult.rejected(RefreshResult.Status.EXPIRED);
        }
        if (session.getRevokedAt() != null) {
            return RefreshResult.rejected(RefreshResult.Status.REVOKED);
        }
        final Optional<AuthRefreshToken> token = sessionDao.findTokenByHash(session.getId(), hash.get());
        if (token.isEmpty()) {
            return RefreshResult.rejected(RefreshResult.Status.INVALID);
        }
        if (token.get().getConsumedAt() != null) {
            revokeSession(session, now, AuthSession.RevocationReason.REFRESH_REUSE);
            return RefreshResult.rejected(RefreshResult.Status.REUSED);
        }
        token.get().setConsumedAt(now);
        return RefreshResult.issued(issue(session, now));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SessionRevocationResult revoke(final long authenticatedUserId, final long authenticatedSessionId,
                                          final long targetUserId, final long targetSessionId) {
        if (authenticatedUserId != targetUserId || authenticatedSessionId != targetSessionId) {
            return SessionRevocationResult.FORBIDDEN;
        }
        final Optional<AuthSession> found = sessionDao.lockById(targetSessionId);
        if (found.isEmpty()) {
            return SessionRevocationResult.NOT_FOUND;
        }
        final AuthSession session = found.get();
        if (session.getUser().getId() != authenticatedUserId) {
            return SessionRevocationResult.FORBIDDEN;
        }
        revokeSession(session, now(), AuthSession.RevocationReason.LOGOUT);
        return SessionRevocationResult.REVOKED;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SessionRevocationResult revokeWithRefresh(final long targetUserId, final long targetSessionId,
                                                     final String refreshToken) {
        final Optional<String> hash = tokenCodec.digest(refreshToken);
        if (hash.isEmpty()) {
            return SessionRevocationResult.INVALID;
        }
        final Optional<Long> sessionId = sessionDao.findSessionIdByTokenHash(hash.get());
        if (sessionId.isEmpty()) {
            return SessionRevocationResult.INVALID;
        }
        if (sessionId.get() != targetSessionId) {
            return SessionRevocationResult.FORBIDDEN;
        }
        final Optional<AuthSession> found = sessionDao.lockById(targetSessionId);
        if (found.isEmpty()) {
            return SessionRevocationResult.INVALID;
        }
        final AuthSession session = found.get();
        if (session.getUser().getId() != targetUserId) {
            return SessionRevocationResult.FORBIDDEN;
        }
        final Instant now = now();
        if (!now.isBefore(session.getExpiresAt())) {
            return SessionRevocationResult.EXPIRED;
        }
        final Optional<AuthRefreshToken> token = sessionDao.findTokenByHash(session.getId(), hash.get());
        if (token.isEmpty()) {
            return SessionRevocationResult.INVALID;
        }
        if (token.get().getConsumedAt() != null) {
            revokeSession(session, now, AuthSession.RevocationReason.REFRESH_REUSE);
            return SessionRevocationResult.REUSED;
        }
        revokeSession(session, now, AuthSession.RevocationReason.LOGOUT);
        return SessionRevocationResult.REVOKED;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthSession> findActiveSession(final long authenticatedUserId, final long userId, final long sessionId) {
        if (authenticatedUserId != userId) {
            return Optional.empty();
        }
        final Optional<AuthSession> session = sessionDao.findByUserAndId(userId, sessionId);
        if (session.isEmpty() || session.get().getRevokedAt() != null || !now().isBefore(session.get().getExpiresAt())) {
            return Optional.empty();
        }
        return session;
    }

    @Override
    @Scheduled(fixedDelayString = "${auth.sessions.cleanup.delay-ms:3600000}", initialDelay = 60000)
    @Transactional
    public int deleteExpiredSessions() {
        final int removed = sessionDao.deleteExpired(now());
        if (removed > 0) {
            LOGGER.info("deleted expired auth sessions count={}", removed);
        }
        return removed;
    }

    private SessionCredentials issue(final AuthSession session, final Instant now) {
        final String rawToken = tokenCodec.generate();
        sessionDao.createRefreshToken(session.getId(), tokenCodec.digest(rawToken).orElseThrow(), now);
        return new SessionCredentials(session.getUser().getId(), session.getId(), now,
                session.getExpiresAt(), rawToken);
    }

    private void revokeSession(final AuthSession session, final Instant now, final AuthSession.RevocationReason reason) {
        if (session.getRevokedAt() == null) {
            session.setRevokedAt(now);
            session.setRevocationReason(reason);
            LOGGER.info("revoked auth session id={} userId={} reason={}", session.getId(), session.getUser().getId(), reason);
        }
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }
}
