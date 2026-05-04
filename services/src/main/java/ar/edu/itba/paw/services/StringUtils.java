package ar.edu.itba.paw.services;

import ar.edu.itba.paw.services.exception.InvalidServiceInputException;

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
            throw new InvalidServiceInputException(errorMessage);
        }
        return trimmed;
    }
}
