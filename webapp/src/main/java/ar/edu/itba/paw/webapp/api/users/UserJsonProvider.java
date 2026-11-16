package ar.edu.itba.paw.webapp.api.users;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import org.springframework.stereotype.Component;

/** Strict input types for identity DTOs without changing other domains' JSON configuration. */
@Component
@Provider
public class UserJsonProvider implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public UserJsonProvider() {
        mapper = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        mapper.coercionConfigFor(LogicalType.Textual)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
    }

    @Override
    public ObjectMapper getContext(final Class<?> type) {
        return type == UserRegistrationRequest.class || type == UserPatchRequest.class ? mapper : null;
    }
}
