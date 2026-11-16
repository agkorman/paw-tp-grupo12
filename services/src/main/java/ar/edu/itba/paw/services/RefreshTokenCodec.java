package ar.edu.itba.paw.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/** Opaque 256-bit credentials, explicitly distinguishable from access JWTs. */
@Service
public class RefreshTokenCodec {

    private static final Pattern FORMAT = Pattern.compile("rt_[A-Za-z0-9_-]{43}");

    public String generate() {
        final byte[] secret = new byte[32];
        new SecureRandom().nextBytes(secret);
        return "rt_" + Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
    }

    public Optional<String> digest(final String token) {
        if (token == null || !FORMAT.matcher(token).matches()) {
            return Optional.empty();
        }
        try {
            return Optional.of(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.US_ASCII))));
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by the Java runtime", e);
        }
    }
}
