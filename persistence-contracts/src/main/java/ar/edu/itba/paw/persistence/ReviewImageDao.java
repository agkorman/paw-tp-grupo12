package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.ReviewImage;

import java.util.List;
import java.util.Optional;

public interface ReviewImageDao {

    List<ReviewImage> findAllByReviewId(long reviewId);

    List<ReviewImage> findAllByReviewIdWithData(long reviewId);

    Optional<ReviewImage> findByReviewIdAndImageId(long reviewId, long imageId);

    void replaceAll(long reviewId, List<ImagePayload> images);
}
