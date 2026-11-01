package ar.edu.itba.paw.webapp.api.common;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/** Resolves public input names from Jackson metadata, without reading values or exposing Java executable names. */
final class ApiJsonFields {

    private static final ObjectMapper JSON = new ObjectMapper();

    private ApiJsonFields() {
    }

    static String mappingField(final JsonMappingException exception) {
        final StringBuilder field = new StringBuilder();
        for (final JsonMappingException.Reference reference : exception.getPath()) {
            if (reference.getIndex() >= 0) {
                field.append('[').append(reference.getIndex()).append(']');
            } else if (reference.getFieldName() != null && reference.getFrom() != null) {
                final Class<?> owner = reference.getFrom() instanceof Class<?> type
                        ? type : reference.getFrom().getClass();
                final BeanPropertyDefinition property = property(JSON.constructType(owner), reference.getFieldName());
                if (property == null) {
                    return null;
                }
                append(field, property.getName());
            } else {
                return null;
            }
        }
        return field.isEmpty() ? null : field.toString();
    }

    static String validationField(final Path path, final Method method) {
        final StringBuilder field = new StringBuilder();
        JavaType owner = JSON.constructType(method.getDeclaringClass());
        for (final Path.Node node : path) {
            if (node.getKind() == ElementKind.METHOD) {
                continue;
            }
            if (node.getKind() == ElementKind.PARAMETER) {
                final int index = node.as(Path.ParameterNode.class).getParameterIndex();
                owner = JSON.constructType(method.getGenericParameterTypes()[index]);
                final String name = parameterName(method.getParameterAnnotations()[index]);
                if (name != null) {
                    append(field, name);
                }
                continue;
            }
            if (node.getKind() == ElementKind.BEAN || node.getKind() == ElementKind.CROSS_PARAMETER
                    || node.getKind() == ElementKind.RETURN_VALUE) {
                return null;
            }
            if (node.isInIterable()) {
                if (node.getIndex() == null) {
                    // Map keys and unordered collection values are input, not safe metadata for a public field path.
                    return null;
                }
                field.append('[').append(node.getIndex()).append(']');
                owner = owner.getContentType();
            }
            if (node.getKind() == ElementKind.PROPERTY) {
                final BeanPropertyDefinition property = property(owner, node.getName());
                if (property == null) {
                    return null;
                }
                append(field, property.getName());
                owner = property.getPrimaryType();
            } else if (node.getKind() != ElementKind.CONTAINER_ELEMENT) {
                return null;
            }
        }
        return field.isEmpty() ? null : field.toString();
    }

    private static BeanPropertyDefinition property(final JavaType owner, final String name) {
        if (owner == null) {
            return null;
        }
        for (final BeanPropertyDefinition property : JSON.getDeserializationConfig().introspect(owner).findProperties()) {
            if (property.couldDeserialize() && (property.getInternalName().equals(name) || property.getName().equals(name))) {
                return property;
            }
        }
        return null;
    }

    private static void append(final StringBuilder path, final String name) {
        if (!path.isEmpty()) {
            path.append('.');
        }
        path.append(name);
    }

    private static String parameterName(final Annotation[] annotations) {
        for (final Annotation annotation : annotations) {
            if (annotation instanceof QueryParam parameter) return parameter.value();
            if (annotation instanceof PathParam parameter) return parameter.value();
            if (annotation instanceof FormParam parameter) return parameter.value();
            if (annotation instanceof HeaderParam parameter) return parameter.value();
            if (annotation instanceof CookieParam parameter) return parameter.value();
            if (annotation instanceof MatrixParam parameter) return parameter.value();
        }
        return null;
    }
}
