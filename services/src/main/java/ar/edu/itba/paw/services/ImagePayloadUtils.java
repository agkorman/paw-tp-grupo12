package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.services.exception.InvalidImagePayloadException;

import java.util.Collections;
import java.util.List;

final class ImagePayloadUtils {
    private ImagePayloadUtils() {}

    static List<CarImagePayload> normalizeImages(final List<CarImagePayload> images) {
        if (images == null) {
            return Collections.emptyList();
        }
        for (final CarImagePayload image : images) {
            if (image == null || image.getContentType() == null || image.getContentType().isBlank()
                    || image.getImageData() == null || image.getImageData().length == 0) {
                throw new InvalidImagePayloadException("Image metadata and payload must be provided together.");
            }
        }
        return List.copyOf(images);
    }
}
