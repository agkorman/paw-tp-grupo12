package ar.edu.itba.paw.webapp.config;

import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Proxy;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WebConfigTest {

    @Test
    public void localeResolverFallsBackToRequestLocale() {
        final WebConfig config = new WebConfig();
        final LocaleResolver resolver = config.localeResolver();

        final Locale locale = resolver.resolveLocale(requestWithLocale(Locale.ENGLISH));

        assertEquals(Locale.ENGLISH, locale);
    }

    @Test
    public void localeChangeInterceptorUsesLangParameterAndIgnoresInvalidValues() {
        final WebConfig config = new WebConfig();
        final LocaleChangeInterceptor interceptor = config.localeChangeInterceptor();

        assertEquals("lang", interceptor.getParamName());
        assertTrue(interceptor.isIgnoreInvalidLocale());
    }

    @Test
    public void spanishLocaleUsesBaseBundleEvenWhenSystemLocaleIsEnglish() {
        final Locale originalDefault = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            final WebConfig config = new WebConfig();
            final MessageSource messageSource = config.messageSource();

            final String title = messageSource.getMessage("landing.featuredReviews.title", null, Locale.forLanguageTag("es"));

            assertEquals("Reseñas destacadas", title);
        } finally {
            Locale.setDefault(originalDefault);
        }
    }

    private HttpServletRequest requestWithLocale(final Locale locale) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { HttpServletRequest.class },
                (proxy, method, args) -> {
                    if ("getLocale".equals(method.getName())) {
                        return locale;
                    }
                    if ("getSession".equals(method.getName())) {
                        return null;
                    }
                    return null;
                }
        );
    }
}
