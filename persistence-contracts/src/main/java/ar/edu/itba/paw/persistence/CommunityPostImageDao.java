package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.CommunityPostImage;
import ar.edu.itba.paw.model.ImagePayload;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommunityPostImageDao {

    List<CommunityPostImage> findAllByPostId(long postId);

    List<CommunityPostImage> findAllByPostIds(Collection<Long> postIds);

    Optional<CommunityPostImage> findByPostIdAndImageId(long postId, long imageId);

    void replaceAll(long postId, List<ImagePayload> images);
}
