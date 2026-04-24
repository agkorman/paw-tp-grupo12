package ar.edu.itba.paw.webapp.auth;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;

public final class LoginRedirectUtils {

    public static final String REDIRECT_PARAM = "redirect";
    public static final String INTENT_PARAM = "intent";

    private static final Pattern INTENT_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    private LoginRedirectUtils() {
        // Utility class.
    }

    public static Optional<String> safeRedirect(final String value) {
        if (value == null) {
            return Optional.empty();
        }
        final String trimmed = value.trim();
        if (trimmed.isEmpty()
                || !trimmed.startsWith("/")
                || trimmed.startsWith("//")
                || trimmed.indexOf('\r') >= 0
                || trimmed.indexOf('\n') >= 0) {
            return Optional.empty();
        }
        try {
            final URI uri = URI.create(trimmed);
            if (uri.isAbsolute() || uri.getHost() != null) {
                return Optional.empty();
            }
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
        return Optional.of(trimmed);
    }

    public static Optional<String> safeIntent(final String value) {
        if (value == null) {
            return Optional.empty();
        }
        final String trimmed = value.trim();
        if (!INTENT_PATTERN.matcher(trimmed).matches()) {
            return Optional.empty();
        }
        return Optional.of(trimmed);
    }

    public static String appendIntent(final String target, final String intent) {
        final Optional<String> safeIntent = safeIntent(intent);
        if (safeIntent.isEmpty()) {
            return target;
        }
        return appendQueryParam(target, INTENT_PARAM, safeIntent.get());
    }

    public static String appendQueryParam(final String target, final String name, final String value) {
        final int fragmentIndex = target.indexOf('#');
        final String base = fragmentIndex >= 0 ? target.substring(0, fragmentIndex) : target;
        final String fragment = fragmentIndex >= 0 ? target.substring(fragmentIndex) : "";
        final String separator = base.contains("?") ? "&" : "?";

        return base
                + separator
                + encode(name)
                + "="
                + encode(value)
                + fragment;
    }

    private static String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
