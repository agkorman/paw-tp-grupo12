package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.webapp.validation.ImageSignatureValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

final class ControllerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerUtils.class);

    static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;
    static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp"
    );

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

    static String pathWithoutQuery(final String target) {
        if (target == null) {
            return null;
        }
        final int queryIndex = target.indexOf('?');
        return queryIndex >= 0 ? target.substring(0, queryIndex) : target;
    }

    static String queryOf(final String target) {
        if (target == null) {
            return null;
        }
        final int queryIndex = target.indexOf('?');
        return queryIndex >= 0 ? target.substring(queryIndex + 1) : null;
    }

    static String currentContextPath() {
        final org.springframework.web.context.request.RequestAttributes requestAttributes =
                RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return "";
        }
        return ((ServletRequestAttributes) requestAttributes)
                .getRequest()
                .getContextPath();
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

    static String validateUploadedImage(final MultipartFile file, final boolean required) {
        if (file == null || file.isEmpty()) {
            return required ? "validation.image.required" : null;
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            return "validation.image.maxSize";
        }
        final String contentType = normalizeContentType(file.getContentType());
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            return "validation.image.contentType";
        }
        try {
            if (!ImageSignatureValidator.hasMatchingImageSignature(file, contentType)) {
                return "validation.image.signature";
            }
        } catch (final IOException e) {
            LOGGER.warn("failed to read uploaded image bytes for signature check", e);
            return "validation.image.read";
        }
        return null;
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
