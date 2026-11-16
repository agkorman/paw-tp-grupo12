package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.AccessTokenIdentity;
import ar.edu.itba.paw.model.AccessTokenResult;
import ar.edu.itba.paw.model.SessionCredentials;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAccessTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-15T18:00:00Z");
    // Deterministic test-only key, never application configuration.
    private static final SecretKey KEY = new SecretKeySpec(new byte[64], "HmacSHA256");
    private static final long USER_ID = 9007199254740993L;

    @Mock private Clock clock;
    @Mock private SecretKey signingKey;
    @InjectMocks private JwtAccessTokenService service;

    @Test
    void issuesMinimalSignedClaimsWithoutLosingLargeIdentifiers() {
        // Arrange
        useKey();
        when(clock.instant()).thenReturn(NOW);
        final SessionCredentials session = session(NOW.plusSeconds(86400));
        // Exercise
        final String token = service.issue(session, Set.of("ADMIN"));
        // Assertions
        final Jws<Claims> signed = Jwts.parser().verifyWith(KEY).clock(() -> Date.from(NOW))
                .build().parseSignedClaims(token);
        assertEquals("HS256", signed.getHeader().getAlgorithm());
        assertEquals("at+jwt", signed.getHeader().getType());
        assertEquals(Set.of("iss", "aud", "sub", "sid", "roles", "iat", "exp"), signed.getPayload().keySet());
        assertEquals(Long.toString(USER_ID), signed.getPayload().getSubject());
        assertEquals("12", signed.getPayload().get("sid"));
        assertEquals(List.of("ADMIN"), signed.getPayload().get("roles"));
        assertEquals(Date.from(NOW.plusSeconds(900)), signed.getPayload().getExpiration());
    }

    @Test
    void capsExpirationAtSessionDeadlineRoundedDownToSeconds() {
        // Arrange
        useKey();
        when(clock.instant()).thenReturn(NOW);
        final SessionCredentials session = session(NOW.plusSeconds(37).plusMillis(900));
        // Exercise
        final String token = service.issue(session, Set.of("USER"));
        // Assertions
        final Claims claims = Jwts.parser().verifyWith(KEY).clock(() -> Date.from(NOW))
                .build().parseSignedClaims(token).getPayload();
        assertEquals(Date.from(NOW.plusSeconds(37)), claims.getExpiration());
    }

    @Test
    void refusesToIssueForExpiredSession() {
        // Arrange
        when(clock.instant()).thenReturn(NOW);
        final SessionCredentials session = session(NOW);
        // Exercise
        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.issue(session, Set.of("USER")));
        // Assertions
        assertEquals("An active persisted session is required", error.getMessage());
    }

    @Test
    void returnsVerifiedIdentityAndImmutableRoles() {
        // Arrange
        useKey();
        when(clock.instant()).thenReturn(NOW);
        final String token = signed(validClaims());
        // Exercise
        final AccessTokenResult result = service.validate(token);
        // Assertions
        assertEquals(AccessTokenResult.Status.VALID, result.getStatus());
        final AccessTokenIdentity identity = result.getIdentity().orElseThrow();
        assertEquals(USER_ID, identity.getUserId());
        assertEquals(12, identity.getSessionId());
        assertEquals(Set.of("USER"), identity.getRoles());
        assertEquals(NOW.plusSeconds(900), identity.getExpiresAt());
        assertThrows(UnsupportedOperationException.class, () -> identity.getRoles().add("ADMIN"));
    }

    @ParameterizedTest
    @ValueSource(longs = {900, 901})
    void recognizesExpiryAtAndAfterDeadline(final long elapsed) {
        // Arrange
        useKey();
        when(clock.instant()).thenReturn(NOW.plusSeconds(elapsed));
        final String token = signed(validClaims());
        // Exercise
        final AccessTokenResult result = service.validate(token);
        // Assertions
        assertEquals(AccessTokenResult.Status.EXPIRED, result.getStatus());
        assertTrue(result.getIdentity().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("invalidClaims")
    void rejectsInvalidClaimsEvenWhenTokenIsExpired(final String field, final Object value, final boolean expired) {
        // Arrange
        useKey();
        when(clock.instant()).thenReturn(expired ? NOW.plusSeconds(1000) : NOW);
        final Map<String, Object> claims = validClaims();
        if (value == null) { claims.remove(field); } else { claims.put(field, value); }
        final String token = signed(claims);
        // Exercise
        final AccessTokenResult result = service.validate(token);
        // Assertions
        assertEquals(AccessTokenResult.Status.INVALID, result.getStatus());
        assertTrue(result.getIdentity().isEmpty());
    }

    static Stream<Arguments> invalidClaims() {
        final Map<String, Object> invalid = Map.ofEntries(
                Map.entry("iss", "another-issuer"), Map.entry("aud", List.of("another-api")),
                Map.entry("sub", "9223372036854775808"), Map.entry("sid", "-1"),
                Map.entry("roles", List.of("ROLE_ADMIN")),
                Map.entry("iat", Date.from(NOW.plusSeconds(1100))),
                Map.entry("exp", Date.from(NOW.plusSeconds(901))),
                Map.entry("nbf", Date.from(NOW.plusSeconds(1100))));
        final Stream<Arguments> missing = Stream.of("iss", "aud", "sub", "sid", "roles", "iat", "exp")
                .flatMap(field -> Stream.of(Arguments.of(field, null, false), Arguments.of(field, null, true)));
        final Stream<Arguments> malformed = Stream.of(
                Arguments.of("roles", List.of(), false), Arguments.of("roles", "ADMIN", false),
                Arguments.of("sid", 12, false), Arguments.of("sub", "0", false),
                Arguments.of("exp", Date.from(NOW), false),
                Arguments.of("aud", List.of("urn:paw:api", "another-api"), false));
        return Stream.concat(malformed, Stream.concat(missing, invalid.entrySet().stream().flatMap(entry -> Stream.of(
                Arguments.of(entry.getKey(), entry.getValue(), false),
                Arguments.of(entry.getKey(), entry.getValue(), true)))));
    }

    @Test
    void rejectsWrongTokenTypeEvenIfExpired() {
        // Arrange
        useKey();
        when(clock.instant()).thenReturn(NOW.plusSeconds(1000));
        final String token = Jwts.builder().header().type("refresh+jwt").and()
                .claims(validClaims()).signWith(KEY, Jwts.SIG.HS256).compact();
        // Exercise
        final AccessTokenResult result = service.validate(token);
        // Assertions
        assertEquals(AccessTokenResult.Status.INVALID, result.getStatus());
        assertTrue(result.getIdentity().isEmpty());
    }

    @Test
    void rejectsForeignSignatureRatherThanReportingExpiry() {
        // Arrange
        useKey();
        final SecretKey otherKey = new SecretKeySpec(
                "different-test-key-32-bytes-long!!".getBytes(StandardCharsets.US_ASCII), "HmacSHA256");
        final Map<String, Object> claims = validClaims();
        claims.put("iat", Date.from(NOW.minusSeconds(1800)));
        claims.put("exp", Date.from(NOW.minusSeconds(900)));
        final String token = Jwts.builder().header().type("at+jwt").and()
                .claims(claims).signWith(otherKey, Jwts.SIG.HS256).compact();
        // Exercise
        final AccessTokenResult result = service.validate(token);
        // Assertions
        assertEquals(AccessTokenResult.Status.INVALID, result.getStatus());
        assertTrue(result.getIdentity().isEmpty());
    }

    @Test
    void rejectsSignedTokenUsingDifferentAlgorithm() {
        // Arrange
        useKey();
        when(clock.instant()).thenReturn(NOW);
        final String accepted = service.issue(session(NOW.plusSeconds(86400)), Set.of("USER"));
        final SecretKey key384 = new SecretKeySpec(KEY.getEncoded(), "HmacSHA384");
        final String token = Jwts.builder().header().type("at+jwt").and()
                .claims(validClaims()).signWith(key384, Jwts.SIG.HS384).compact();
        // Exercise
        final AccessTokenResult result = service.validate(token);
        // Assertions
        assertEquals(AccessTokenResult.Status.VALID, service.validate(accepted).getStatus());
        assertEquals("HS384", Jwts.parser().verifyWith(KEY).clock(() -> Date.from(NOW))
                .build().parseSignedClaims(token).getHeader().getAlgorithm());
        assertEquals(AccessTokenResult.Status.INVALID, result.getStatus());
    }

    @Test
    void rejectsModifiedIdentityWithOriginalSignature() {
        // Arrange
        useKey();
        final String[] parts = signed(validClaims()).split("\\.");
        final String altered = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                .replace(Long.toString(USER_ID), "123");
        final String token = parts[0] + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(altered.getBytes(StandardCharsets.UTF_8)) + "." + parts[2];
        // Exercise
        final AccessTokenResult result = service.validate(token);
        // Assertions
        assertEquals(AccessTokenResult.Status.INVALID, result.getStatus());
        assertTrue(result.getIdentity().isEmpty());
    }

    @Test
    void rejectsUnsignedToken() {
        // Arrange
        final String token = Jwts.builder().header().type("at+jwt").and().claims(validClaims()).compact();
        // Exercise
        final AccessTokenResult result = service.validate(token);
        // Assertions
        assertEquals(AccessTokenResult.Status.INVALID, result.getStatus());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "not-a-jwt", "rt_aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void rejectsMissingMalformedAndRefreshTokens(final String input) {
        // Arrange
        final String token = input;
        // Exercise
        final AccessTokenResult result = service.validate(token);
        // Assertions
        assertEquals(AccessTokenResult.Status.INVALID, result.getStatus());
        assertTrue(result.getIdentity().isEmpty());
    }

    private void useKey() {
        when(signingKey.getAlgorithm()).thenReturn(KEY.getAlgorithm());
        when(signingKey.getFormat()).thenReturn("RAW");
        when(signingKey.getEncoded()).thenReturn(KEY.getEncoded());
    }

    private static SessionCredentials session(final Instant deadline) {
        return new SessionCredentials(USER_ID, 12, NOW, deadline, "refresh-must-not-appear-in-jwt");
    }

    private static Map<String, Object> validClaims() {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "urn:paw:auth");
        claims.put("aud", List.of("urn:paw:api"));
        claims.put("sub", Long.toString(USER_ID));
        claims.put("sid", "12");
        claims.put("roles", List.of("USER"));
        claims.put("iat", Date.from(NOW));
        claims.put("exp", Date.from(NOW.plusSeconds(900)));
        return claims;
    }

    private static String signed(final Map<String, Object> claims) {
        return Jwts.builder().header().type("at+jwt").and().claims(claims)
                .signWith(KEY, Jwts.SIG.HS256).compact();
    }
}
