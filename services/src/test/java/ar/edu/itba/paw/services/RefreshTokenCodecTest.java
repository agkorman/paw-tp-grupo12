package ar.edu.itba.paw.services;

import java.util.Base64;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCodecTest {

    @InjectMocks private RefreshTokenCodec codec;

    @Test
    void producesOpaque256BitRefreshCredential() {
        // Arrange
        final String prefix = "rt_";
        // Exercise
        final String result = codec.generate();
        // Assertions
        assertTrue(result.startsWith(prefix));
        assertEquals(46, result.length());
        assertEquals(32, Base64.getUrlDecoder().decode(result.substring(prefix.length())).length);
        assertTrue(codec.digest(result).orElseThrow().matches("[0-9a-f]{64}"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"jwt.payload.signature", "rt_short", "rt_aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=", " rt_aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void rejectsMalformedCredentials(final String raw) {
        // Arrange
        final String input = raw;
        // Exercise
        final Optional<String> result = codec.digest(input);
        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void hashesKnownCredentialWithoutStoringItsRawValue() {
        // Arrange
        final String token = "rt_" + "a".repeat(43);
        // Exercise
        final Optional<String> result = codec.digest(token);
        // Assertions
        assertEquals(Optional.of("eadb3d53a8de588bdc3be694444e09f632d5e5b7fa97e8c972129c98b8ff83d3"), result);
    }
}
