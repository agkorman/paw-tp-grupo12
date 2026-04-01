package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class ReviewStats implements Serializable {

    private long carId;
    private long reviewCount;
    private BigDecimal averageRating;

    public ReviewStats() {}

    public ReviewStats(final long carId, final long reviewCount, final BigDecimal averageRating) {
        this.carId = carId;
        this.reviewCount = reviewCount;
        this.averageRating = averageRating;
    }

    public long getCarId() {
        return carId;
    }

    public void setCarId(final long carId) {
        this.carId = carId;
    }

    public long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(final long reviewCount) {
        this.reviewCount = reviewCount;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(final BigDecimal averageRating) {
        this.averageRating = averageRating;
    }
}
