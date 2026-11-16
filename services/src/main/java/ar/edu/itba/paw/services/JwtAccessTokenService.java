package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.AccessTokenIdentity;
import ar.edu.itba.paw.model.AccessTokenResult;
import ar.edu.itba.paw.model.SessionCredentials;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class JwtAccessTokenService implements AccessTokenService {

    private static final Duration LIFETIME = Duration.ofMinutes(15);
    private static final String ISSUER = "urn:paw:auth";
    private static final String AUDIENCE = "urn:paw:api";
    private static final String TYPE = "at+jwt";

    private final SecretKey signingKey;
    private final Clock clock;
    private final JwtParser parser;

    @Autowired
    public JwtAccessTokenService(@Qualifier("authSigningKey") final SecretKey signingKey,
                                 @Qualifier("authClock") final Clock clock) {
        this.signingKey = signingKey;
        this.clock = clock;
        this.parser = Jwts.parser().verifyWith(signingKey)
                .sig().clear().add(Jwts.SIG.HS256).and()
                .clock(() -> Date.from(clock.instant())).build();
    }

    @Override
    public String issue(final SessionCredentials session, final Set<String> currentRoles) {
        final Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        final Instant deadline = session.getExpiresAt().truncatedTo(ChronoUnit.SECONDS);
        final Instant expiresAt = deadline.isBefore(now.plus(LIFETIME)) ? deadline : now.plus(LIFETIME);
        if (session.getUserId() <= 0 || session.getSessionId() <= 0 || !expiresAt.isAfter(now)) {
            throw new IllegalArgumentException("An active persisted session is required");
        }
        final Set<String> roles = readRoles(currentRoles);
        return Jwts.builder().header().type(TYPE).and()
                .issuer(ISSUER).audience().add(AUDIENCE).and()
                .subject(Long.toString(session.getUserId()))
                .claim("sid", Long.toString(session.getSessionId())).claim("roles", roles)
                .issuedAt(Date.from(now)).expiration(Date.from(expiresAt))
                .signWith(signingKey, Jwts.SIG.HS256).compact();
    }

    @Override
    public AccessTokenResult validate(final String token) {
        if (token == null || token.isBlank()) {
            return AccessTokenResult.rejected(AccessTokenResult.Status.INVALID);
        }
        try {
            final Header header;
            final Claims claims;
            try {
                final Jws<Claims> signed = parser.parseSignedClaims(token);
                header = signed.getHeader();
                claims = signed.getPayload();
            } catch (final ExpiredJwtException e) {
                // JJWT has verified the signature, but may not have checked other claims yet.
                // Validate our full contract before classifying this as an expired access token.
                return validateClaims(e.getHeader(), e.getClaims());
            }
            return validateClaims(header, claims);
        } catch (final JwtException | IllegalArgumentException e) {
            return AccessTokenResult.rejected(AccessTokenResult.Status.INVALID);
        }
    }

    private AccessTokenResult validateClaims(final Header header, final Claims claims) {
        if (!TYPE.equals(header.getType()) || !ISSUER.equals(claims.getIssuer())
                || !Set.of(AUDIENCE).equals(claims.getAudience())) {
            return AccessTokenResult.rejected(AccessTokenResult.Status.INVALID);
        }
        final long userId = readId(claims.getSubject());
        final long sessionId = readId(claims.get("sid"));
        final Set<String> roles = readRoles(claims.get("roles"));
        final Date issuedAt = claims.getIssuedAt();
        final Date expiresAt = claims.getExpiration();
        final Instant now = clock.instant();
        if (issuedAt == null || expiresAt == null || issuedAt.toInstant().isAfter(now)
                || !expiresAt.after(issuedAt)
                || Duration.between(issuedAt.toInstant(), expiresAt.toInstant()).compareTo(LIFETIME) > 0
                || (claims.getNotBefore() != null && claims.getNotBefore().toInstant().isAfter(now))) {
            return AccessTokenResult.rejected(AccessTokenResult.Status.INVALID);
        }
        if (!now.isBefore(expiresAt.toInstant())) {
            return AccessTokenResult.rejected(AccessTokenResult.Status.EXPIRED);
        }
        return AccessTokenResult.valid(new AccessTokenIdentity(userId, sessionId, roles, expiresAt.toInstant()));
    }

    private static long readId(final Object value) {
        if (!(value instanceof String text) || !text.matches("[1-9][0-9]*")) {
            throw new IllegalArgumentException("Invalid access identity");
        }
        return Long.parseLong(text);
    }

    private static Set<String> readRoles(final Object value) {
        if (!(value instanceof Collection<?> values) || values.isEmpty()) {
            throw new IllegalArgumentException("Missing access roles");
        }
        final Set<String> roles = new HashSet<>();
        for (final Object role : values) {
            if (!(role instanceof String name) || !name.matches("[A-Z][A-Z_]*") || name.startsWith("ROLE_")) {
                throw new IllegalArgumentException("Invalid access role");
            }
            roles.add(name);
        }
        return Set.copyOf(roles);
    }
}
