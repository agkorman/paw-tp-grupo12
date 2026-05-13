package ar.edu.itba.paw.webapp.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;

public final class LoginRedirectUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginRedirectUtils.class);

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
            LOGGER.warn("rejected unsafe redirect target reason={}", rejectionReason(trimmed));
            return Optional.empty();
        }
        try {
            final URI uri = URI.create(trimmed);
            if (uri.isAbsolute() || uri.getHost() != null) {
                LOGGER.warn("rejected unsafe redirect target reason={}", "absolute");
                return Optional.empty();
            }
        } catch (final IllegalArgumentException e) {
            LOGGER.warn("rejected unsafe redirect target reason={}", "malformed");
            return Optional.empty();
        }
        return Optional.of(trimmed);
    }

    public static Optional<String> safeRedirect(final String value, final String contextPath) {
        return safeRedirect(value).map(target -> stripContextPath(target, contextPath));
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

    private static String stripContextPath(final String target, final String contextPath) {
        if (contextPath == null || contextPath.isBlank() || "/".equals(contextPath)) {
            return target;
        }
        final String normalizedContextPath = contextPath.trim();
        if (!normalizedContextPath.startsWith("/")) {
            return target;
        }
        if (target.equals(normalizedContextPath)) {
            return "/";
        }
        if (target.startsWith(normalizedContextPath + "/")) {
            return target.substring(normalizedContextPath.length());
        }
        if (target.startsWith(normalizedContextPath + "?")
                || target.startsWith(normalizedContextPath + "#")) {
            return "/" + target.substring(normalizedContextPath.length());
        }
        return target;
    }

    private static String rejectionReason(final String value) {
        if (value.isEmpty()) {
            return "empty";
        }
        if (!value.startsWith("/")) {
            return "relative";
        }
        if (value.startsWith("//")) {
            return "protocol-relative";
        }
        if (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
            return "control-character";
        }
        return "invalid";
    }
}
