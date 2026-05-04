package ar.edu.itba.paw.webapp.controller;

import java.util.Locale;

final class ControllerUtils {

    private ControllerUtils() {}

    static String normalize(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static String normalizeEmail(final String value) {
        final String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    static String normalizeContentType(final String contentType) {
        if (contentType == null) {
            return null;
        }
        final String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    static String normalizeSpecValue(final String value) {
        if (value == null) {
            return null;
        }
        final String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    static boolean isAjaxRequest(final String requestedWith) {
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith);
    }

    static String submittedToastMessageCode(final String submitted) {
        if (submitted == null || submitted.isBlank()) {
            return null;
        }
        final String normalized = submitted.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "true":
            case "car":
                return "cars.submittedToast";
            case "brand":
                return "request.brand.submittedToast";
            case "body-type":
            case "bodytype":
                return "request.bodyType.submittedToast";
            case "moderator":
            case "admin":
                return "request.admin.submittedToast";
            default:
                return null;
        }
    }
}
