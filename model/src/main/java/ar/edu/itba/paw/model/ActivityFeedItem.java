package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ActivityFeedItem implements Serializable {

    private final ActivityFeedReference reference;
    private final Review review;
    private final Car car;
    private final int reviewPage;
    private final List<ReviewImage> reviewImages;
    private final CommunityPost communityPost;
    private final long helpfulCount;
    private final long commentCount;
    private final List<CommunityPostImage> communityPostImages;

    private ActivityFeedItem(final ActivityFeedReference reference,
                             final Review review,
                             final Car car,
                             final int reviewPage,
                             final List<ReviewImage> reviewImages,
                             final CommunityPost communityPost,
                             final long helpfulCount,
                             final long commentCount,
                             final List<CommunityPostImage> communityPostImages) {
        this.reference = reference;
        this.review = review;
        this.car = car;
        this.reviewPage = reviewPage;
        this.reviewImages = reviewImages == null ? new ArrayList<>() : new ArrayList<>(reviewImages);
        this.communityPost = communityPost;
        this.helpfulCount = helpfulCount;
        this.commentCount = commentCount;
        this.communityPostImages = communityPostImages == null ? new ArrayList<>() : new ArrayList<>(communityPostImages);
    }

    public static ActivityFeedItem reviewItem(final Review review,
                                              final Car car,
                                              final int reviewPage,
                                              final List<ReviewImage> reviewImages) {
        return new ActivityFeedItem(
                new ActivityFeedReference(ActivityFeedReference.TYPE_REVIEW, review.getId()),
                review,
                car,
                reviewPage,
                reviewImages,
                null,
                0L,
                0L,
                null
        );
    }

    public static ActivityFeedItem communityPostItem(final CommunityPost communityPost,
                                                     final long helpfulCount,
                                                     final long commentCount,
                                                     final List<CommunityPostImage> communityPostImages) {
        return new ActivityFeedItem(
                new ActivityFeedReference(ActivityFeedReference.TYPE_COMMUNITY_POST, communityPost.getId()),
                null,
                null,
                Pagination.DEFAULT_PAGE,
                null,
                communityPost,
                helpfulCount,
                commentCount,
                communityPostImages
        );
    }

    public ActivityFeedReference getReference() {
        return reference;
    }

    public String getType() {
        return reference.getType();
    }

    public boolean isReview() {
        return reference.isReview();
    }

    public boolean isCommunityPost() {
        return reference.isCommunityPost();
    }

    public Review getReview() {
        return review;
    }

    public Car getCar() {
        return car;
    }

    public int getReviewPage() {
        return reviewPage;
    }

    public List<ReviewImage> getReviewImages() {
        return reviewImages;
    }

    public CommunityPost getCommunityPost() {
        return communityPost;
    }

    public long getHelpfulCount() {
        return helpfulCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public List<CommunityPostImage> getCommunityPostImages() {
        return communityPostImages;
    }
}
