package ar.edu.itba.paw.webapp.auth;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ar.edu.itba.paw.webapp.util.LogSanitizer;
import java.util.Collections;
import java.util.Locale;

public class PawUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PawUserDetailsService.class);

    private static final String ROLE_PREFIX = "ROLE_";

    private final UserService userService;

    public PawUserDetailsService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        LOGGER.debug("loading user details for email={}", email);
        final User user = userService.findByEmail(email)
                .orElseThrow(() -> {
                    LOGGER.warn("no user found for email={}", LogSanitizer.forLog(email, LogSanitizer.MAX_LOG_EMAIL_CODE_POINTS));
                    return new UsernameNotFoundException("No user found for email " + email);
                });

        return new AuthenticatedUser(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getPreferredLocale(),
                Collections.singletonList(new SimpleGrantedAuthority(toAuthority(user.getRole())))
        );
    }

    private String toAuthority(final String role) {
        final String normalizedRole = role == null || role.trim().isEmpty()
                ? "USER"
                : role.trim().toUpperCase(Locale.ROOT);
        if (normalizedRole.startsWith(ROLE_PREFIX)) {
            return normalizedRole;
        }
        return ROLE_PREFIX + normalizedRole;
    }
}
