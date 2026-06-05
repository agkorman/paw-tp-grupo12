package ar.edu.itba.paw.webapp.auth;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class RoleAuthorityUtils {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String DEFAULT_ROLE = "USER";

    private RoleAuthorityUtils() {
        // Utility class.
    }

    public static Collection<? extends GrantedAuthority> authoritiesForRole(
        final String role
    ) {
        return Collections.singletonList(new SimpleGrantedAuthority(toAuthority(role)));
    }

    public static String toAuthority(final String role) {
        final String normalizedRole = role == null || role.trim().isEmpty()
            ? DEFAULT_ROLE
            : role.trim().toUpperCase(Locale.ROOT);
        if (normalizedRole.startsWith(ROLE_PREFIX)) {
            return normalizedRole;
        }
        return ROLE_PREFIX + normalizedRole;
    }
}
