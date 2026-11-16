package ar.edu.itba.paw.webapp.api.common;

import ar.edu.itba.paw.webapp.api.ApiIndexResponse;
import ar.edu.itba.paw.webapp.api.users.SessionResponse;
import ar.edu.itba.paw.webapp.api.users.UserResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdentityRepresentationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void publicUserOmitsPrivateFieldsAndPreservesLargeStringId() throws Exception {
        // Arrange
        final UserResponse user = UserResponse.publicProfile("9007199254740993", "Driver",
                Map.of("self", new ApiLink("/webapp/api/users/9007199254740993")));
        // Exercise
        final JsonNode result = objectMapper.valueToTree(user);
        // Assertions
        assertEquals(objectMapper.readTree("""
                {
                  "id": "9007199254740993",
                  "displayName": "Driver",
                  "_links": {"self": {"href": "/webapp/api/users/9007199254740993"}}
                }
                """), result);
    }

    @Test
    void ownerUserIncludesPrivateFieldsWithoutEmbeddingSessionOrCredentials() throws Exception {
        // Arrange
        final UserResponse user = UserResponse.ownerProfile("123", "Driver", "driver@example.test", "es",
                List.of("USER"), Map.of("self", new ApiLink("/webapp/api/users/123")));
        // Exercise
        final JsonNode result = objectMapper.valueToTree(user);
        // Assertions
        assertEquals(objectMapper.readTree("""
                {
                  "id": "123",
                  "displayName": "Driver",
                  "email": "driver@example.test",
                  "preferredLocale": "es",
                  "roles": ["USER"],
                  "_links": {"self": {"href": "/webapp/api/users/123"}}
                }
                """), result);
    }

    @Test
    void sessionUsesUtcDeadlineAndCanonicalLinksWithoutTokenData() throws Exception {
        // Arrange
        final Instant deadline = OffsetDateTime.parse("2026-11-16T15:00:00-03:00").toInstant();
        final SessionResponse session = new SessionResponse("456", deadline, Map.of(
                "self", new ApiLink("/another-context/api/users/123/sessions/456"),
                "user", new ApiLink("/another-context/api/users/123")));
        // Exercise
        final JsonNode result = objectMapper.valueToTree(session);
        // Assertions
        assertEquals(objectMapper.readTree("""
                {
                  "id": "456",
                  "expiresAt": "2026-11-16T18:00:00Z",
                  "_links": {
                    "self": {"href": "/another-context/api/users/123/sessions/456"},
                    "user": {"href": "/another-context/api/users/123"}
                  }
                }
                """), result);
    }

    @Test
    void indexRepresentsNavigationWithoutEmbeddingIdentity() throws Exception {
        // Arrange
        final ApiIndexResponse index = new ApiIndexResponse(Map.of("self", new ApiLink("/webapp/api")));
        // Exercise
        final JsonNode result = objectMapper.valueToTree(index);
        // Assertions
        assertEquals(objectMapper.readTree("""
                {"_links": {"self": {"href": "/webapp/api"}}}
                """), result);
    }

    @Test
    void callerCannotChangeRolesOrLinksAfterConstructingOwnerRepresentation() {
        // Arrange
        final List<String> roles = new ArrayList<>(List.of("USER"));
        final Map<String, ApiLink> links = new LinkedHashMap<>();
        links.put("self", new ApiLink("/webapp/api/users/123"));
        final UserResponse user = UserResponse.ownerProfile("123", "Driver", "driver@example.test", "es",
                roles, links);
        roles.add("ADMIN");
        links.clear();
        // Exercise
        final JsonNode result = objectMapper.valueToTree(user);
        // Assertions
        assertEquals(1, result.get("roles").size());
        assertEquals("USER", result.get("roles").get(0).asText());
        assertEquals("/webapp/api/users/123", result.at("/_links/self/href").asText());
        assertThrows(UnsupportedOperationException.class, () -> user.getRoles().add("ADMIN"));
        assertThrows(UnsupportedOperationException.class, () -> user.getLinks().clear());
    }
}
