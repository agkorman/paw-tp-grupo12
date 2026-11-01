package ar.edu.itba.paw.webapp.api.common;

import javax.ws.rs.core.UriInfo;
import org.glassfish.jersey.uri.UriComponent;
import org.glassfish.jersey.uri.UriComponent.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** Builds safe, origin-relative links for API responses. */
public final class ApiLinks {

    private ApiLinks() { }

    public static ApiLink resource(final UriInfo uriInfo, final Class<?> resourceType,
                                   final String template, final Object... values) {
        Objects.requireNonNull(uriInfo, "uriInfo");
        Objects.requireNonNull(resourceType, "resourceType");
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(values, "values");
        final URI uri = uriInfo.getBaseUriBuilder()
                .path(resourceType)
                .path(template)
                .buildFromEncoded(encodeSegments(values));
        return new ApiLink(URI.create(originRelative(uri)).toASCIIString());
    }

    public static URI page(final UriInfo uriInfo, final int page) {
        Objects.requireNonNull(uriInfo, "uriInfo");
        if (page < 1) {
            throw new IllegalArgumentException("page must be at least 1");
        }
        final URI requestUri = uriInfo.getRequestUriBuilder().build();
        final String path = originRelative(requestUri);
        final String query = replacePage(requestUri.getRawQuery(), page);
        return URI.create(path + "?" + query);
    }

    private static Object[] encodeSegments(final Object[] values) {
        if (values.length == 0) {
            return values;
        }
        final Object[] encoded = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            encoded[i] = UriComponent.encode(Objects.requireNonNull(values[i], "template value").toString(),
                    Type.UNRESERVED);
        }
        return encoded;
    }

    private static String replacePage(final String rawQuery, final int page) {
        final StringBuilder query = new StringBuilder();
        if (rawQuery != null && !rawQuery.isEmpty()) {
            for (final String parameter : rawQuery.split("&", -1)) {
                final int equals = parameter.indexOf('=');
                final String rawName = equals < 0 ? parameter : parameter.substring(0, equals);
                if (isPageName(rawName)) {
                    continue;
                }
                if (query.length() > 0) {
                    query.append('&');
                }
                query.append(parameter);
            }
        }
        if (query.length() > 0) {
            query.append('&');
        }
        return query.append("page=").append(page).toString();
    }

    private static boolean isPageName(final String rawName) {
        try {
            return "page".equals(java.net.URLDecoder.decode(rawName, StandardCharsets.UTF_8));
        } catch (final IllegalArgumentException malformedEncoding) {
            return false;
        }
    }

    private static String originRelative(final URI uri) {
        final String path = uri.getRawPath();
        if (path == null || !path.startsWith("/") || path.startsWith("//")) {
            throw new IllegalArgumentException("URI path must be an unambiguous origin-relative path");
        }
        return path;
    }
}
