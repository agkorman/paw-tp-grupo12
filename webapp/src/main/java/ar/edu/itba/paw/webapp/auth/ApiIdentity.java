package ar.edu.itba.paw.webapp.auth;

import java.util.List;
import java.util.Optional;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.SecurityContext;
import org.springframework.security.core.Authentication;

/** Servlet/Jersey bridge; resources never interpret raw Spring authorities. */
public final class ApiIdentity {

    private ApiIdentity() { }

    public static Optional<ApiPrincipal> current(final SecurityContext context) {
        if (context.getUserPrincipal() instanceof Authentication authentication
                && authentication.getPrincipal() instanceof ApiPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static ApiPrincipal require(final SecurityContext context) {
        return current(context).orElseThrow(() -> new NotAuthorizedException("Bearer realm=\"paw\""));
    }

    public static List<String> roles(final SecurityContext context) {
        require(context);
        return ((Authentication) context.getUserPrincipal()).getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5)).sorted().toList();
    }
}
