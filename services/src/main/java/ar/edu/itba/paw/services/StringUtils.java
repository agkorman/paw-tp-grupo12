package ar.edu.itba.paw.services;

final class StringUtils {
    private StringUtils() {}

    static String normalize(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static String normalizeRequired(final String value, final String errorMessage) {
        final String trimmed = normalize(value);
        if (trimmed == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return trimmed;
    }
}
