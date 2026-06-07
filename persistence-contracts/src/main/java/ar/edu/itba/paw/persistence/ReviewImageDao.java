package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.StoredImagePayload;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewImageDao {

    List<ImageMetadata> findAllByReviewId(long reviewId);

    List<StoredImagePayload> findByReviewIdAndImageIdsWithData(long reviewId, Collection<Long> imageIds);

    List<ImageMetadata> findAllByReviewIds(Collection<Long> reviewIds);

    Optional<StoredImagePayload> findByReviewIdAndImageId(long reviewId, long imageId);

    Optional<ImageMetadata> findMetadataByReviewIdAndImageId(long reviewId, long imageId);

    void replaceAll(long reviewId, List<ImagePayload> images);
}
