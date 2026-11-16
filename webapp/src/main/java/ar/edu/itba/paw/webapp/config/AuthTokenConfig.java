package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.webapp.exception.WebAppConfigurationException;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthTokenConfig {

    @Bean
    public SecretKey authSigningKey(@Value("#{systemEnvironment['AUTH_JWT_SECRET']}") final String encoded) {
        if (encoded == null || encoded.isBlank()) {
            throw new WebAppConfigurationException("AUTH_JWT_SECRET is required (Base64 of at least 32 random bytes).");
        }
        final byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(encoded);
        } catch (final IllegalArgumentException e) {
            // Do not include the value or decoder exception in configuration errors.
            throw new WebAppConfigurationException("AUTH_JWT_SECRET must be valid Base64.");
        }
        if (bytes.length < 32) {
            throw new WebAppConfigurationException("AUTH_JWT_SECRET must decode to at least 32 bytes.");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
