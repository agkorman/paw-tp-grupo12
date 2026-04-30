package ar.edu.itba.paw.services.utils;

import ar.edu.itba.paw.model.CarImagePayload;

import java.util.Collections;
import java.util.List;

public final class ImagePayloadUtils {
    private ImagePayloadUtils() {}

    public static List<CarImagePayload> normalizeImages(final List<CarImagePayload> images) {
        if (images == null) {
            return Collections.emptyList();
        }
        for (final CarImagePayload image : images) {
            if (image == null || image.getContentType() == null || image.getContentType().isBlank()
                    || image.getImageData() == null || image.getImageData().length == 0) {
                throw new IllegalArgumentException("Image metadata and payload must be provided together.");
            }
        }
        return List.copyOf(images);
    }
}
