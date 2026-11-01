package ar.edu.itba.paw.webapp.api.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ReaderInterceptorContext;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.MethodHandler;
import org.glassfish.jersey.server.spi.ValidationInterceptorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiValidationMappersTest {

    private final ObjectMapper json = new ObjectMapper();
    @Mock private ReaderInterceptorContext reader;
    @Mock private ValidationInterceptorContext validation;

    @Test
    void malformedInputHasSafeTopLevelError() throws Exception {
        // Arrange
        final Exception parseFailure = assertThrows(Exception.class, () -> json.readValue("{private", Request.class));
        when(reader.proceed()).thenThrow(parseFailure);
        // Exercise
        final ApiInputException exception = assertThrows(ApiInputException.class,
                () -> new ApiInputReaderInterceptor().aroundReadFrom(reader));
        // Assertions
        final Response response = new ApiExceptionMapper().toResponse(exception);
        assertEquals(400, response.getStatus());
        final ApiError error = (ApiError) response.getEntity();
        assertEquals("INVALID_REQUEST", error.getCode());
        assertTrue(error.getFieldErrors().isEmpty());
        assertFalse(json.writeValueAsString(error).contains("private"));
    }

    @Test
    void inputTypeFailureUsesKnownJsonNamesAndIndices() throws Exception {
        // Arrange
        final JsonMappingException failure = assertThrows(JsonMappingException.class,
                () -> json.readValue("{\"children\":[{\"public_name\":\"fine\",\"age\":\"private-value\"}]}", Request.class));
        when(reader.proceed()).thenThrow(failure);
        // Exercise
        final ApiInputException exception = assertThrows(ApiInputException.class,
                () -> new ApiInputReaderInterceptor().aroundReadFrom(reader));
        // Assertions
        final ApiFieldError error = exception.getError().getFieldErrors().get(0);
        assertEquals("children[0].age", error.getField());
        assertEquals("INVALID_TYPE", error.getCode());
        assertFalse(json.writeValueAsString(exception.getError()).contains("private-value"));
    }

    @Test
    void unknownIncomingPropertyIsNotEchoedAsAField() throws Exception {
        // Arrange
        final JsonMappingException failure = assertThrows(JsonMappingException.class,
                () -> json.readValue("{\"private-unknown-field\":true}", Request.class));
        when(reader.proceed()).thenThrow(failure);
        // Exercise
        final ApiInputException exception = assertThrows(ApiInputException.class,
                () -> new ApiInputReaderInterceptor().aroundReadFrom(reader));
        // Assertions
        assertTrue(exception.getError().getFieldErrors().isEmpty());
        assertFalse(json.writeValueAsString(exception.getError()).contains("private-unknown-field"));
    }

    @Test
    void serverDeserializationDefinitionFailureIsNotBadInput() throws Exception {
        // Arrange
        final InvalidDefinitionException failure = InvalidDefinitionException.from(
                (com.fasterxml.jackson.core.JsonParser) null, "private-definition", json.constructType(Request.class));
        when(reader.proceed()).thenThrow(failure);
        // Exercise
        final InvalidDefinitionException result = assertThrows(InvalidDefinitionException.class,
                () -> new ApiInputReaderInterceptor().aroundReadFrom(reader));
        // Assertions
        assertSame(failure, result);
        assertEquals(500, new ApiExceptionMapper().toResponse(result).getStatus());
    }

    @Test
    void onlyInputValidationBoundaryProduces400() throws Exception {
        // Arrange
        final Fixture fixture = new Fixture();
        final Method method = Fixture.class.getMethod("accept", Request.class);
        final Request request = new Request();
        request.children = List.of(new Child());
        final ConstraintViolationException failure = new ConstraintViolationException(violations(fixture, method, request));
        doThrow(failure).when(validation).proceed();
        when(validation.getInvocable()).thenReturn(Invocable.create(MethodHandler.create(Fixture.class), method));
        // Exercise
        final ApiInputException result = assertThrows(ApiInputException.class,
                () -> new ApiInputValidationInterceptor().onValidate(validation));
        // Assertions
        assertEquals("VALIDATION_ERROR", result.getError().getCode());
        assertEquals("children[0].public_name", result.getError().getFieldErrors().get(0).getField());
        assertEquals("REQUIRED", result.getError().getFieldErrors().get(0).getCode());
        assertEquals(400, new ApiExceptionMapper().toResponse(result).getStatus());
    }

    @Test
    void containerElementConstraintsPreserveIndices() throws Exception {
        // Arrange
        final Method method = Fixture.class.getMethod("strings", List.class);
        final Set<? extends ConstraintViolation<?>> failures = violations(new Fixture(), method, List.of(""));
        // Exercise
        final List<ApiFieldError> result = ApiValidationErrors.fields(failures, method);
        // Assertions
        assertEquals("[0]", result.get(0).getField());
        assertEquals("REQUIRED", result.get(0).getCode());
    }

    @Test
    void constraintsExposeOnlyAllowlistedCodesAndNumericParameters() throws Exception {
        // Arrange
        final Method method = Fixture.class.getMethod("constraints", Constraints.class);
        final Set<? extends ConstraintViolation<?>> failures = violations(new Fixture(), method, new Constraints());
        // Exercise
        final List<ApiFieldError> result = ApiValidationErrors.fields(failures, method);
        // Assertions
        assertEquals(List.of("INVALID_VALUE", "OUT_OF_RANGE", "INVALID_FORMAT", "INVALID_FORMAT", "OUT_OF_RANGE", "SIZE"),
                result.stream().map(ApiFieldError::getCode).toList());
        assertEquals(Map.of("min", new BigDecimal("0.5"), "inclusive", false), result.get(1).getParams());
        assertEquals(Map.of("max", 5L), result.get(4).getParams());
        assertEquals(Map.of("min", 2, "max", 4), result.get(5).getParams());
        final String serialized = json.writeValueAsString(result);
        assertFalse(serialized.contains("private"));
        assertFalse(serialized.contains("regexp"));
        assertFalse(serialized.contains("rejectedValue"));
    }

    @Test
    void rootObjectAndNullEntityViolationsStayGlobal() throws Exception {
        // Arrange
        final Method objectMethod = Fixture.class.getMethod("global", GlobalRequest.class);
        final Method nullMethod = Fixture.class.getMethod("accept", Request.class);
        final Set<? extends ConstraintViolation<?>> globalFailures = violations(new Fixture(), objectMethod, new GlobalRequest());
        final Set<? extends ConstraintViolation<?>> nullFailures = violations(new Fixture(), nullMethod, (Object) null);
        // Exercise
        final List<ApiFieldError> result = ApiValidationErrors.fields(globalFailures, objectMethod);
        // Assertions
        assertTrue(result.isEmpty());
        assertFalse(globalFailures.isEmpty());
        assertFalse(nullFailures.isEmpty());
        assertTrue(ApiValidationErrors.fields(nullFailures, nullMethod).isEmpty());
    }

    @Test
    void internalValidationFailuresHaveSafe500() throws Exception {
        // Arrange
        final ValidationException failure = new ValidationException("private-validation-detail");
        // Exercise
        final Response response = new ApiValidationExceptionMapper().toResponse(failure);
        // Assertions
        assertEquals(500, response.getStatus());
        assertEquals(ApiMediaTypes.ERROR_V1, response.getMediaType().toString());
        assertEquals("INTERNAL_ERROR", ((ApiError) response.getEntity()).getCode());
        assertFalse(json.writeValueAsString(response.getEntity()).contains("private"));
    }

    @Test
    void jacksonFailureOutsideInputBoundaryHasSafe500() throws Exception {
        // Arrange
        final JsonMappingException failure = JsonMappingException.from((com.fasterxml.jackson.core.JsonParser) null,
                "private-output-detail");
        // Exercise
        final Response response = new ApiExceptionMapper().toResponse(failure);
        // Assertions
        assertEquals(500, response.getStatus());
        assertEquals("INTERNAL_ERROR", ((ApiError) response.getEntity()).getCode());
        assertFalse(json.writeValueAsString(response.getEntity()).contains("private"));
    }

    @Test
    void httpErrorsKeepAllowAndIgnoreUnsafeCustomReasonPhrases() throws Exception {
        // Arrange
        final WebApplicationException failure = new WebApplicationException(Response.status(405, "private-reason")
                .header("Allow", "GET").entity("private-body").build());
        // Exercise
        final Response response = new ApiExceptionMapper().toResponse(failure);
        // Assertions
        assertEquals("GET", response.getHeaderString("Allow"));
        assertEquals("METHOD_NOT_ALLOWED", ((ApiError) response.getEntity()).getCode());
        assertEquals("Method Not Allowed", ((ApiError) response.getEntity()).getMessage());
        assertFalse(json.writeValueAsString(response.getEntity()).contains("private"));
    }

    private static Set<? extends ConstraintViolation<?>> violations(final Fixture fixture, final Method method,
                                                                   final Object... args) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().forExecutables().validateParameters(fixture, method, args);
        }
    }

    public static class Fixture {
        public void accept(@NotNull @Valid final Request request) { }
        public void strings(@Valid final List<@NotBlank String> values) { }
        public void constraints(@Valid final Constraints request) { }
        public void global(@Valid final GlobalRequest request) { }
    }

    public static class Request {
        @Valid public List<Child> children;
    }

    public static class Child {
        @NotBlank @JsonProperty("public_name") public String name;
        public int age;
    }

    public static class Constraints {
        @AssertTrue public boolean accepted;
        @DecimalMin(value = "0.5", inclusive = false) public BigDecimal decimal = BigDecimal.ZERO;
        @Email public String email = "private-invalid";
        @Pattern(regexp = "private-pattern") public String format = "private-invalid";
        @Max(5) public int rank = 6;
        @Size(min = 2, max = 4) public String size = "private-invalid";
    }

    @InvalidPair
    public static class GlobalRequest { }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = InvalidPairValidator.class)
    public @interface InvalidPair {
        String message() default "private-object-detail";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    public static class InvalidPairValidator implements ConstraintValidator<InvalidPair, GlobalRequest> {
        @Override public boolean isValid(final GlobalRequest value, final ConstraintValidatorContext context) {
            return false;
        }
    }
}
