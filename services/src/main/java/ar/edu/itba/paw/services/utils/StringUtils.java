package ar.edu.itba.paw.services.utils;

public final class StringUtils {
    private StringUtils() {}

    public static String normalize(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String normalizeRequired(final String value, final String errorMessage) {
        final String trimmed = normalize(value);
        if (trimmed == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return trimmed;
    }
}
