package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.AuthRefreshToken;
import ar.edu.itba.paw.model.AuthSession;
import ar.edu.itba.paw.model.User;
import java.time.Instant;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class AuthSessionJpaDao implements AuthSessionDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public AuthSession create(final long userId, final Instant createdAt, final Instant expiresAt,
                              final boolean rememberMe) {
        final AuthSession session = new AuthSession(em.getReference(User.class, userId), createdAt,
                expiresAt, rememberMe);
        em.persist(session);
        return session;
    }

    @Override
    public AuthRefreshToken createRefreshToken(final long sessionId, final String tokenHash,
                                             final Instant createdAt) {
        final AuthRefreshToken token = new AuthRefreshToken(em.getReference(AuthSession.class, sessionId),
                tokenHash, createdAt);
        em.persist(token);
        return token;
    }

    @Override
    public Optional<AuthSession> findByUserAndId(final long userId, final long sessionId) {
        return em.createQuery("SELECT s FROM AuthSession s WHERE s.id = :id AND s.user.id = :userId",
                        AuthSession.class)
                .setParameter("id", sessionId).setParameter("userId", userId)
                .getResultList().stream().findFirst();
    }

    @Override
    public Optional<Long> findSessionIdByTokenHash(final String tokenHash) {
        return em.createQuery("SELECT t.session.id FROM AuthRefreshToken t WHERE t.tokenHash = :hash", Long.class)
                .setParameter("hash", tokenHash).getResultList().stream().findFirst();
    }

    @Override
    public Optional<AuthSession> lockById(final long sessionId) {
        return Optional.ofNullable(em.find(AuthSession.class, sessionId, LockModeType.PESSIMISTIC_WRITE));
    }

    @Override
    public Optional<AuthRefreshToken> findTokenByHash(final long sessionId, final String tokenHash) {
        return em.createQuery("SELECT t FROM AuthRefreshToken t WHERE t.session.id = :sessionId AND t.tokenHash = :hash",
                        AuthRefreshToken.class)
                .setParameter("sessionId", sessionId).setParameter("hash", tokenHash)
                .getResultList().stream().findFirst();
    }

    @Override
    public int deleteExpired(final Instant cutoff) {
        return em.createQuery("DELETE FROM AuthSession s WHERE s.expiresAt <= :cutoff")
                .setParameter("cutoff", cutoff).executeUpdate();
    }
}
