package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class CarRecommendation implements Serializable {

    private Car car;
    private BigDecimal score;
    private int reviewCount;
    private List<TagHighlight> positiveHighlights;
    private List<TagHighlight> negativeHighlights;

    public CarRecommendation() {}

    public CarRecommendation(final Car car, final BigDecimal score, final int reviewCount,
                             final List<TagHighlight> positiveHighlights) {
        this(car, score, reviewCount, positiveHighlights, Collections.emptyList());
    }

    public CarRecommendation(final Car car, final BigDecimal score, final int reviewCount,
                             final List<TagHighlight> positiveHighlights,
                             final List<TagHighlight> negativeHighlights) {
        this.car = car;
        this.score = score;
        this.reviewCount = reviewCount;
        this.positiveHighlights = freeze(positiveHighlights);
        this.negativeHighlights = freeze(negativeHighlights);
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

    public List<TagHighlight> getPositiveHighlights() {
        return positiveHighlights;
    }

    public void setPositiveHighlights(final List<TagHighlight> positiveHighlights) {
        this.positiveHighlights = freeze(positiveHighlights);
    }

    public List<TagHighlight> getNegativeHighlights() {
        return negativeHighlights;
    }

    public void setNegativeHighlights(final List<TagHighlight> negativeHighlights) {
        this.negativeHighlights = freeze(negativeHighlights);
    }

    private static List<TagHighlight> freeze(final List<TagHighlight> source) {
        return source == null ? Collections.emptyList() : Collections.unmodifiableList(source);
    }
}
