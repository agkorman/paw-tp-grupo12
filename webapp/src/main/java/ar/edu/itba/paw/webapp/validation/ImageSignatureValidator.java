package ar.edu.itba.paw.webapp.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public final class ImageSignatureValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageSignatureValidator.class);

    private static final String WEBP_CONTENT_TYPE = "image/webp";
    private static final int MAX_SIGNATURE_BYTES = 12;

    private ImageSignatureValidator() {
        // Utility class.
    }

    public static boolean hasMatchingImageSignature(final MultipartFile file, final String contentType)
            throws IOException {
        if (file == null || contentType == null) {
            return false;
        }

        final byte[] header;
        try (InputStream inputStream = file.getInputStream()) {
            header = inputStream.readNBytes(MAX_SIGNATURE_BYTES);
        }

        final boolean valid;
        if (MediaType.IMAGE_JPEG_VALUE.equals(contentType)) {
            valid = isJpeg(header);
        } else if (MediaType.IMAGE_PNG_VALUE.equals(contentType)) {
            valid = isPng(header);
        } else if (WEBP_CONTENT_TYPE.equals(contentType)) {
            valid = isWebp(header);
        } else {
            LOGGER.warn("rejected image upload: unsupported content type={} filename={}",
                    contentType, file.getOriginalFilename());
            return false;
        }
        if (!valid) {
            LOGGER.warn("rejected image upload: magic-byte mismatch declaredContentType={} filename={}",
                    contentType, file.getOriginalFilename());
        }
        return valid;
    }

    private static boolean isJpeg(final byte[] header) {
        return header.length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF;
    }

    private static boolean isPng(final byte[] header) {
        return header.length >= 8
                && (header[0] & 0xFF) == 0x89
                && header[1] == 'P'
                && header[2] == 'N'
                && header[3] == 'G'
                && header[4] == 0x0D
                && header[5] == 0x0A
                && header[6] == 0x1A
                && header[7] == 0x0A;
    }

    private static boolean isWebp(final byte[] header) {
        return header.length >= 12
                && header[0] == 'R'
                && header[1] == 'I'
                && header[2] == 'F'
                && header[3] == 'F'
                && header[8] == 'W'
                && header[9] == 'E'
                && header[10] == 'B'
                && header[11] == 'P';
    }
}
