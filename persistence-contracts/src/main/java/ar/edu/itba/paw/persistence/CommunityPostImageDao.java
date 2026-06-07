package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.StoredImagePayload;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommunityPostImageDao {

    List<ImageMetadata> findAllByPostId(long postId);

    List<ImageMetadata> findAllByPostIds(Collection<Long> postIds);

    Optional<StoredImagePayload> findByPostIdAndImageId(long postId, long imageId);

    Optional<ImageMetadata> findMetadataByPostIdAndImageId(long postId, long imageId);

    List<StoredImagePayload> findByPostIdAndImageIdsWithData(long postId, Collection<Long> imageIds);

    void replaceAll(long postId, List<ImagePayload> images);
}
