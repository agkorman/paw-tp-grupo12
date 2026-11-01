package ar.edu.itba.paw.webapp.api.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiErrorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesLegacyErrorWithUnchangedJsonShape() throws Exception {
        // Arrange
        final ApiError error = new ApiError("INVALID_REQUEST", "Request is invalid");
        // Exercise
        final JsonNode result = objectMapper.readTree(objectMapper.writeValueAsString(error));
        // Assertions
        assertEquals(objectMapper.readTree("{\"code\":\"INVALID_REQUEST\",\"message\":\"Request is invalid\"}"), result);
    }

    @Test
    void omitsEmptyFieldErrorsAndParams() throws Exception {
        // Arrange
        final ApiError error = new ApiError("VALIDATION_ERROR", "Input is invalid",
                List.of(new ApiFieldError("name", "REQUIRED"), new ApiFieldError("age", "INVALID_VALUE", Map.of())));
        // Exercise
        final JsonNode result = objectMapper.readTree(objectMapper.writeValueAsString(error));
        // Assertions
        assertEquals(objectMapper.readTree("{\"code\":\"VALIDATION_ERROR\",\"message\":\"Input is invalid\","
                + "\"fieldErrors\":[{\"field\":\"name\",\"code\":\"REQUIRED\"},"
                + "{\"field\":\"age\",\"code\":\"INVALID_VALUE\"}]}"), result);
    }

    @Test
    void serializesMultipleErrorsForSameFieldAndTheirParams() throws Exception {
        // Arrange
        final ApiError error = new ApiError("VALIDATION_ERROR", "Input is invalid", List.of(
                new ApiFieldError("profile.age", "OUT_OF_RANGE", Map.of("min", 18, "inclusive", true)),
                new ApiFieldError("profile.age", "INVALID_FORMAT", Map.of("format", "integer"))));
        // Exercise
        final JsonNode result = objectMapper.readTree(objectMapper.writeValueAsString(error));
        // Assertions
        assertEquals(objectMapper.readTree("{\"code\":\"VALIDATION_ERROR\",\"message\":\"Input is invalid\","
                + "\"fieldErrors\":[{\"field\":\"profile.age\",\"code\":\"OUT_OF_RANGE\","
                + "\"params\":{\"min\":18,\"inclusive\":true}},{\"field\":\"profile.age\","
                + "\"code\":\"INVALID_FORMAT\",\"params\":{\"format\":\"integer\"}}]}"), result);
    }

    @Test
    void copiesFieldErrorAndParamCollectionsDefensively() {
        // Arrange
        final List<ApiFieldError> fieldErrors = new ArrayList<>();
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put("min", 1);
        fieldErrors.add(new ApiFieldError("count", "OUT_OF_RANGE", params));
        final ApiError error = new ApiError("VALIDATION_ERROR", "Input is invalid", fieldErrors);
        // Exercise
        fieldErrors.clear();
        params.put("max", 9);
        // Assertions
        assertEquals(1, error.getFieldErrors().size());
        assertEquals(Map.of("min", 1), error.getFieldErrors().get(0).getParams());
        assertThrows(UnsupportedOperationException.class, () -> error.getFieldErrors().clear());
        assertThrows(UnsupportedOperationException.class, () -> error.getFieldErrors().get(0).getParams().put("max", 9));
    }
}
