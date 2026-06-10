package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

public class UserLocaleInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserLocaleInterceptor.class);

    private static final String LOCALE_SYNCED_ATTR = "userLocaleSynced";

    private final LocaleResolver localeResolver;

    public UserLocaleInterceptor(final LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) {
        if (request.getSession(false) != null && request.getSession().getAttribute(LOCALE_SYNCED_ATTR) != null) {
            return true;
        }

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser) {
            final AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
            final Locale locale = Locale.forLanguageTag(user.getPreferredLocale());
            localeResolver.setLocale(request, response, locale);
            request.getSession().setAttribute(LOCALE_SYNCED_ATTR, Boolean.TRUE);
            LOGGER.debug("synced session locale to user preference locale={} userId={}", locale, user.getId());
        }

        return true;
    }
}
