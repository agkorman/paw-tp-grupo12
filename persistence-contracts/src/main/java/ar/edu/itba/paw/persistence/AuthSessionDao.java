package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.AuthRefreshToken;
import ar.edu.itba.paw.model.AuthSession;
import java.time.Instant;
import java.util.Optional;

public interface AuthSessionDao {
    AuthSession create(long userId, Instant createdAt, Instant expiresAt, boolean rememberMe);
    AuthRefreshToken createRefreshToken(long sessionId, String tokenHash, Instant createdAt);
    Optional<AuthSession> findByUserAndId(long userId, long sessionId);
    /** Returns only the ID: no managed token may be loaded before the session lock. */
    Optional<Long> findSessionIdByTokenHash(String tokenHash);
    /** Requires a service transaction; all token writes for this session must use this lock. */
    Optional<AuthSession> lockById(long sessionId);
    /** Read after locking the session, so consumedAt reflects previous committed renewals. */
    Optional<AuthRefreshToken> findTokenByHash(long sessionId, String tokenHash);
    /** Bulk deletion; database FK cascade deletes the associated token history. */
    int deleteExpired(Instant cutoff);
}
