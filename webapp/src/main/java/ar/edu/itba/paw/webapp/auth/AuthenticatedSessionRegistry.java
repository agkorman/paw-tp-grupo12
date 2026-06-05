package ar.edu.itba.paw.webapp.auth;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import ar.edu.itba.paw.services.AuthenticatedSessionService;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedSessionRegistry implements AuthenticatedSessionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        AuthenticatedSessionRegistry.class
    );
    private static final String ADMIN_ROLE = "admin";
    private static final String SECURITY_CONTEXT_ATTRIBUTE =
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

    private final ConcurrentMap<String, HttpSession> sessionsById =
        new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> usersBySessionId =
        new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Set<String>> sessionIdsByUserId =
        new ConcurrentHashMap<>();

    public void registerSession(
        final HttpSession session,
        final SecurityContext securityContext
    ) {
        if (session == null) {
            return;
        }
        final AuthenticatedUser user = authenticatedUser(securityContext);
        if (user == null) {
            unregisterSession(session.getId());
            return;
        }

        final String sessionId = session.getId();
        final long userId = user.getId();
        sessionsById.put(sessionId, session);
        final Long previousUserId = usersBySessionId.put(sessionId, userId);
        if (previousUserId != null && previousUserId != userId) {
            removeSessionFromUser(previousUserId, sessionId);
        }
        sessionIdsByUserId
            .computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet())
            .add(sessionId);
    }

    public void unregisterSession(final String sessionId) {
        if (sessionId == null) {
            return;
        }
        sessionsById.remove(sessionId);
        final Long userId = usersBySessionId.remove(sessionId);
        if (userId != null) {
            removeSessionFromUser(userId, sessionId);
        }
    }

    @Override
    public int promoteUserToAdmin(final long userId) {
        final Set<String> sessionIds = sessionIdsByUserId.get(userId);
        if (sessionIds == null || sessionIds.isEmpty()) {
            LOGGER.debug("no active sessions registered for promoted user id={}", userId);
            return 0;
        }

        int updatedCount = 0;
        for (final String sessionId : new ArrayList<>(sessionIds)) {
            final HttpSession session = sessionsById.get(sessionId);
            if (session == null) {
                unregisterSession(sessionId);
                continue;
            }
            if (promoteSessionToAdmin(session, userId)) {
                updatedCount++;
            }
        }
        LOGGER.info(
            "refreshed admin role in active sessions userId={} sessions={}",
            userId,
            updatedCount
        );
        return updatedCount;
    }

    private boolean promoteSessionToAdmin(
        final HttpSession session,
        final long userId
    ) {
        final SecurityContext securityContext;
        try {
            final Object value = session.getAttribute(SECURITY_CONTEXT_ATTRIBUTE);
            if (!(value instanceof SecurityContext context)) {
                unregisterSession(session.getId());
                return false;
            }
            securityContext = context;
        } catch (final IllegalStateException e) {
            unregisterSession(session.getId());
            return false;
        }

        final Authentication currentAuthentication =
            securityContext.getAuthentication();
        final AuthenticatedUser currentUser =
            authenticatedUser(securityContext);
        if (currentUser == null || currentUser.getId() != userId) {
            unregisterSession(session.getId());
            return false;
        }

        final AuthenticatedUser refreshedUser = new AuthenticatedUser(
            currentUser.getId(),
            currentUser.getDisplayName(),
            currentUser.getEmail(),
            currentUser.getPassword(),
            currentUser.getPreferredLocale(),
            RoleAuthorityUtils.authoritiesForRole(ADMIN_ROLE)
        );
        final UsernamePasswordAuthenticationToken refreshedAuthentication =
            new UsernamePasswordAuthenticationToken(
                refreshedUser,
                refreshedUser.getPassword(),
                refreshedUser.getAuthorities()
            );
        if (currentAuthentication != null) {
            refreshedAuthentication.setDetails(currentAuthentication.getDetails());
        }

        securityContext.setAuthentication(refreshedAuthentication);
        session.setAttribute(SECURITY_CONTEXT_ATTRIBUTE, securityContext);
        return true;
    }

    private AuthenticatedUser authenticatedUser(
        final SecurityContext securityContext
    ) {
        if (securityContext == null) {
            return null;
        }
        final Authentication authentication = securityContext.getAuthentication();
        if (
            authentication == null ||
            !(authentication.getPrincipal() instanceof AuthenticatedUser user)
        ) {
            return null;
        }
        return user;
    }

    private void removeSessionFromUser(final long userId, final String sessionId) {
        final Set<String> userSessionIds = sessionIdsByUserId.get(userId);
        if (userSessionIds == null) {
            return;
        }
        userSessionIds.remove(sessionId);
        if (userSessionIds.isEmpty()) {
            sessionIdsByUserId.remove(userId, userSessionIds);
        }
    }
}
