package ar.edu.itba.paw.webapp.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

final class UploadedImageValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadedImageValidation.class);

    static final int MAX_IMAGE_COUNT = 5;
    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp"
    );

    private UploadedImageValidation() {}

    static String validate(final MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            return "{validation.image.maxSize}";
        }
        final String contentType = normalizeContentType(file.getContentType());
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            return "{validation.image.contentType}";
        }
        try {
            if (!ImageSignatureValidator.hasMatchingImageSignature(file, contentType)) {
                return "{validation.image.signature}";
            }
        } catch (final IOException e) {
            LOGGER.warn("failed to read uploaded image bytes for signature check", e);
            return "{validation.image.read}";
        }
        return null;
    }

    static String normalizeContentType(final String contentType) {
        if (contentType == null) {
            return null;
        }
        final String normalized = contentType.trim().toLowerCase(java.util.Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}
