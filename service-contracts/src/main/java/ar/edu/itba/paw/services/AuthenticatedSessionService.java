package ar.edu.itba.paw.services;

/**
 * Port implemented by the web layer (which owns the live {@code HttpSession}s) so that
 * business services can propagate a role change to a user's active sessions without
 * depending on the Servlet/Spring Security web API.
 */
public interface AuthenticatedSessionService {

    /**
     * Rewrites the security context of every active session that belongs to the given
     * user so the {@code ROLE_ADMIN} grant takes effect without forcing a re-login.
     *
     * @return the number of active sessions that were refreshed
     */
    int promoteUserToAdmin(long userId);
}
