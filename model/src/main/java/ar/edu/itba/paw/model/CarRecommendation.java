package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class CarRecommendation implements Serializable {

    private Car car;
    private BigDecimal score;
    private int reviewCount;
    private List<TagHighlight> highlights;

    public CarRecommendation() {}

    public CarRecommendation(final Car car, final BigDecimal score, final int reviewCount,
                             final List<TagHighlight> highlights) {
        this.car = car;
        this.score = score;
        this.reviewCount = reviewCount;
        this.highlights = highlights == null ? Collections.emptyList() : Collections.unmodifiableList(highlights);
    }

    public Car getCar() {
        return car;
    }

    public void setCar(final Car car) {
        this.car = car;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(final BigDecimal score) {
        this.score = score;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(final int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public List<TagHighlight> getHighlights() {
        return highlights;
    }

    public void setHighlights(final List<TagHighlight> highlights) {
        this.highlights = highlights == null ? Collections.emptyList() : Collections.unmodifiableList(highlights);
    }
}
