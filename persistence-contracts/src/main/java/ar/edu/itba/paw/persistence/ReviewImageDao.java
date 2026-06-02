package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.ReviewImage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewImageDao {

    List<ReviewImage> findAllByReviewId(long reviewId);

    List<ReviewImage> findByReviewIdAndImageIdsWithData(long reviewId, Collection<Long> imageIds);

    List<ReviewImage> findAllByReviewIds(Collection<Long> reviewIds);

    Optional<ReviewImage> findByReviewIdAndImageId(long reviewId, long imageId);

    void replaceAll(long reviewId, List<ImagePayload> images);
}
