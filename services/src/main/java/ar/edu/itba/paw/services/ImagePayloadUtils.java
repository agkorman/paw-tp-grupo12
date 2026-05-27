package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.services.exception.InvalidImagePayloadException;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ImagePayloadUtils {

    private ImagePayloadUtils() {}

    static List<ImagePayload> normalizeImages(
        final List<ImagePayload> images
    ) {
        if (images == null) {
            return Collections.emptyList();
        }
        for (final ImagePayload image : images) {
            if (
                image == null ||
                image.getContentType() == null ||
                image.getContentType().isBlank() ||
                image.getImageData() == null ||
                image.getImageData().length == 0
            ) {
                throw new InvalidImagePayloadException(
                    "Image metadata and payload must be provided together."
                );
            }
        }
        return List.copyOf(images);
    }
}
