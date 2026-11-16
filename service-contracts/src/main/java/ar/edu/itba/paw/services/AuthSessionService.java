package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.AuthSession;
import ar.edu.itba.paw.model.RefreshResult;
import ar.edu.itba.paw.model.SessionCredentials;
import ar.edu.itba.paw.model.SessionRevocationResult;
import java.util.Optional;

/** Persistent token sessions, independent of the legacy HttpSession registry. */
public interface AuthSessionService {
    /** Called only after the user's Basic credentials have been authenticated. */
    SessionCredentials createSession(long userId, boolean rememberMe);
    /** Commits authentication state before the caller handles rejection or executes a resource. */
    RefreshResult rotate(String refreshToken);
    /** Authenticated IDs must come from verified access claims, never request parameters. */
    SessionRevocationResult revoke(long authenticatedUserId, long authenticatedSessionId,
                                   long targetUserId, long targetSessionId);
    SessionRevocationResult revokeWithRefresh(long targetUserId, long targetSessionId, String refreshToken);
    Optional<AuthSession> findActiveSession(long authenticatedUserId, long userId, long sessionId);
    int deleteExpiredSessions();
}
