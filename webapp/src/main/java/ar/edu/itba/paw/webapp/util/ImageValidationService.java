package ar.edu.itba.paw.webapp.util;

import ar.edu.itba.paw.webapp.validation.ImageSignatureValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@Component
public class ImageValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageValidationService.class);

    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp"
    );

    public String validateUploadedImage(final MultipartFile file, final boolean required) {
        if (file == null || file.isEmpty()) {
            return required ? "validation.car.image.required" : null;
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            return "validation.car.image.maxSize";
        }
        final String contentType = normalizeContentType(file.getContentType());
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            return "validation.car.image.unsupportedType";
        }
        try {
            if (!ImageSignatureValidator.hasMatchingImageSignature(file, contentType)) {
                return "validation.car.image.invalidSignature";
            }
        } catch (final IOException e) {
            LOGGER.warn("failed to read uploaded image bytes for signature check", e);
            return "validation.car.image.readError";
        }
        return null;
    }

    private String normalizeContentType(final String contentType) {
        if (contentType == null) {
            return null;
        }
        final String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}
