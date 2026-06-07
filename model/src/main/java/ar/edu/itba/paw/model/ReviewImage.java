package ar.edu.itba.paw.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "review_images")
public class ReviewImage extends BaseImage {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    ReviewImage() {}

    public ReviewImage(final Review review, final int displayOrder, final String contentType,
                       final byte[] imageData) {
        super(displayOrder, contentType, imageData);
        this.review = review;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(final Review review) {
        this.review = review;
    }

    public long getReviewId() {
        return review != null ? review.getId() : 0;
    }
}
