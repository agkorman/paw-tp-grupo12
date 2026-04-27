package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class TagHighlight implements Serializable {

    private ReviewTag tag;
    private int mentionCount;
    private int reviewCount;
    private BigDecimal frequency;

    public TagHighlight() {}

    public TagHighlight(final ReviewTag tag, final int mentionCount, final int reviewCount,
                        final BigDecimal frequency) {
        this.tag = tag;
        this.mentionCount = mentionCount;
        this.reviewCount = reviewCount;
        this.frequency = frequency;
    }

    public ReviewTag getTag() {
        return tag;
    }

    public void setTag(final ReviewTag tag) {
        this.tag = tag;
    }

    public int getMentionCount() {
        return mentionCount;
    }

    public void setMentionCount(final int mentionCount) {
        this.mentionCount = mentionCount;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(final int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public BigDecimal getFrequency() {
        return frequency;
    }

    public void setFrequency(final BigDecimal frequency) {
        this.frequency = frequency;
    }

    public int getPercentage() {
        return frequency == null ? 0 : frequency.multiply(BigDecimal.valueOf(100)).setScale(0, java.math.RoundingMode.HALF_UP).intValue();
    }
}
