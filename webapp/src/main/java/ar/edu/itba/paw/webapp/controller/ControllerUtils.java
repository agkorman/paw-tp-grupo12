package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.webapp.validation.ImageSignatureValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
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
            return required ? "La imagen es obligatoria." : null;
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            return "La imagen no debe superar los 10 MB.";
        }
        final String contentType = normalizeContentType(file.getContentType());
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            return "Tipo de imagen no soportado. Usá JPEG, PNG o WEBP.";
        }
        try {
            if (!ImageSignatureValidator.hasMatchingImageSignature(file, contentType)) {
                return "El archivo no coincide con una imagen JPEG, PNG o WEBP válida.";
            }
        } catch (final IOException e) {
            LOGGER.warn("failed to read uploaded image bytes for signature check", e);
            return "No pudimos leer la imagen. Intentá con otro archivo.";
        }
        return null;
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
