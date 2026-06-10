package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.StoredImagePayload;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CarImageDao {

    Optional<StoredImagePayload> findFirstByCarIdWithData(long carId);

    Optional<ImageMetadata> findFirstMetadataByCarId(long carId);

    List<ImageMetadata> findAllByCarId(long carId);

    List<StoredImagePayload> findAllByCarIdWithData(long carId);

    List<StoredImagePayload> findByCarIdAndImageIdsWithData(long carId, Collection<Long> imageIds);

    Optional<StoredImagePayload> findByCarIdAndImageId(long carId, long imageId);

    Optional<ImageMetadata> findMetadataByCarIdAndImageId(long carId, long imageId);

    void replaceAll(long carId, List<ImagePayload> images);

    void appendAll(long carId, List<ImagePayload> images);
}
