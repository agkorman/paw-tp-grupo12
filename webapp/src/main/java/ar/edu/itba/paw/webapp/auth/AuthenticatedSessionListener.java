package ar.edu.itba.paw.webapp.auth;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class AuthenticatedSessionListener
    implements HttpSessionAttributeListener, HttpSessionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        AuthenticatedSessionListener.class
    );
    private static final String SECURITY_CONTEXT_ATTRIBUTE =
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

    private volatile AuthenticatedSessionRegistry registry;

    @Override
    public void attributeAdded(final HttpSessionBindingEvent event) {
        registerIfSecurityContext(event);
    }

    @Override
    public void attributeReplaced(final HttpSessionBindingEvent event) {
        registerIfSecurityContext(event);
    }

    @Override
    public void attributeRemoved(final HttpSessionBindingEvent event) {
        if (SECURITY_CONTEXT_ATTRIBUTE.equals(event.getName())) {
            final AuthenticatedSessionRegistry resolvedRegistry =
                resolveRegistry(event.getSession().getServletContext());
            if (resolvedRegistry != null) {
                resolvedRegistry.unregisterSession(event.getSession().getId());
            }
        }
    }

    @Override
    public void sessionCreated(final HttpSessionEvent event) {
        // Sessions are registered only after Spring Security stores a context.
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent event) {
        final AuthenticatedSessionRegistry resolvedRegistry =
            resolveRegistry(event.getSession().getServletContext());
        if (resolvedRegistry != null) {
            resolvedRegistry.unregisterSession(event.getSession().getId());
        }
    }

    private void registerIfSecurityContext(
        final HttpSessionBindingEvent event
    ) {
        if (!SECURITY_CONTEXT_ATTRIBUTE.equals(event.getName())) {
            return;
        }
        final Object value = event.getSession().getAttribute(
            SECURITY_CONTEXT_ATTRIBUTE
        );
        if (value instanceof SecurityContext securityContext) {
            final AuthenticatedSessionRegistry resolvedRegistry =
                resolveRegistry(event.getSession().getServletContext());
            if (resolvedRegistry != null) {
                resolvedRegistry.registerSession(
                    event.getSession(),
                    securityContext
                );
            }
            return;
        }
        final AuthenticatedSessionRegistry resolvedRegistry =
            resolveRegistry(event.getSession().getServletContext());
        if (resolvedRegistry != null) {
            resolvedRegistry.unregisterSession(event.getSession().getId());
        }
    }

    private AuthenticatedSessionRegistry resolveRegistry(
        final ServletContext servletContext
    ) {
        AuthenticatedSessionRegistry resolvedRegistry = registry;
        if (resolvedRegistry != null) {
            return resolvedRegistry;
        }

        final WebApplicationContext applicationContext;
        try {
            applicationContext =
                WebApplicationContextUtils.getWebApplicationContext(
                    servletContext
                );
            if (applicationContext == null) {
                LOGGER.debug("authenticated session registry unavailable");
                return null;
            }
            resolvedRegistry = applicationContext.getBean(
                AuthenticatedSessionRegistry.class
            );
        } catch (final BeansException | IllegalStateException e) {
            LOGGER.debug("authenticated session registry unavailable", e);
            return null;
        }
        registry = resolvedRegistry;
        LOGGER.debug("resolved authenticated session registry");
        return resolvedRegistry;
    }
}
