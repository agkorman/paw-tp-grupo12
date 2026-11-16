package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.webapp.exception.WebAppConfigurationException;
import java.util.Base64;
import java.util.Map;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import static org.junit.jupiter.api.Assertions.*;

class AuthTokenConfigTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "not-base64!", "c2hvcnQ="})
    void rejectsMissingMalformedAndShortSecrets(final String encoded) {
        // Arrange
        final AuthTokenConfig config = new AuthTokenConfig();
        // Exercise
        final WebAppConfigurationException error = assertThrows(WebAppConfigurationException.class,
                () -> config.authSigningKey(encoded));
        // Assertions
        assertTrue(error.getMessage().startsWith("AUTH_JWT_SECRET"));
        assertNull(error.getCause());
    }

    @Test
    void loadsKeyFromEnvironmentAtContextStartup() {
        // Arrange
        final byte[] bytes = new byte[32];
        final String encoded = Base64.getEncoder().encodeToString(bytes);
        try (final AnnotationConfigApplicationContext context = context(Map.of("AUTH_JWT_SECRET", encoded))) {
            // Exercise
            context.refresh();
            // Assertions
            final SecretKey key = context.getBean("authSigningKey", SecretKey.class);
            assertEquals("HmacSHA256", key.getAlgorithm());
            assertArrayEquals(bytes, key.getEncoded());
        }
    }

    @Test
    void refusesStartupWithoutEnvironmentSecretEvenIfApplicationPropertyExists() {
        // Arrange
        try (final AnnotationConfigApplicationContext context = context(Map.of())) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("application",
                    Map.of("AUTH_JWT_SECRET", Base64.getEncoder().encodeToString(new byte[32]))));
            // Exercise
            final BeanCreationException error = assertThrows(BeanCreationException.class, context::refresh);
            // Assertions
            assertInstanceOf(WebAppConfigurationException.class, error.getMostSpecificCause());
            assertTrue(error.getMostSpecificCause().getMessage().contains("AUTH_JWT_SECRET is required"));
        }
    }

    private static AnnotationConfigApplicationContext context(final Map<String, Object> environment) {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getBeanFactory().registerSingleton("systemEnvironment", environment);
        context.getEnvironment().getPropertySources().replace("systemEnvironment",
                new MapPropertySource("systemEnvironment", environment));
        context.register(AuthTokenConfig.class);
        return context;
    }
}
