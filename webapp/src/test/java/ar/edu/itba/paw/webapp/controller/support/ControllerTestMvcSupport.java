package ar.edu.itba.paw.webapp.controller.support;

import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Helpers for standalone {@link org.springframework.test.web.servlet.MockMvc} with Spring Security
 * principals. {@link org.springframework.security.core.annotation.AuthenticationPrincipal} is fed from
 * {@link SecurityContextHolder}; plain {@link javax.servlet.http.HttpServletRequest#getUserPrincipal()}
 * requires {@link #authenticationPrincipalRequestPostProcessor()} on the request.
 */
public final class ControllerTestMvcSupport {

    private ControllerTestMvcSupport() {}

    public static void bindPrincipal(final AuthenticatedUser user) {
        final UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Resolves controller parameters of type {@code Authentication} backed by {@code getUserPrincipal()}.
     */
    public static RequestPostProcessor authenticationPrincipalRequestPostProcessor() {
        return request -> {
            request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication());
            return request;
        };
    }
}
