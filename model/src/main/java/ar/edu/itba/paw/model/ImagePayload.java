package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.Arrays;

public class ImagePayload implements Serializable {

    private final String contentType;
    private final byte[] imageData;

    public ImagePayload(final String contentType, final byte[] imageData) {
        this.contentType = contentType;
        this.imageData = imageData == null ? null : Arrays.copyOf(imageData, imageData.length);
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getImageData() {
        return imageData == null ? null : Arrays.copyOf(imageData, imageData.length);
    }
}
