package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ReviewTag implements Serializable {

    public static final String SENTIMENT_POSITIVE = "positive";
    public static final String SENTIMENT_NEGATIVE = "negative";

    private short id;
    private String code;
    private String labelEs;
    private String sentiment;
    private String dimension;
    private LocalDateTime createdAt;

    public ReviewTag() {}

    public ReviewTag(final short id, final String code, final String labelEs, final String sentiment,
                     final String dimension, final LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.labelEs = labelEs;
        this.sentiment = sentiment;
        this.dimension = dimension;
        this.createdAt = createdAt;
    }

    public short getId() {
        return id;
    }

    public void setId(final short id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getLabelEs() {
        return labelEs;
    }

    public void setLabelEs(final String labelEs) {
        this.labelEs = labelEs;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(final String sentiment) {
        this.sentiment = sentiment;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(final String dimension) {
        this.dimension = dimension;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isPositive() {
        return SENTIMENT_POSITIVE.equals(sentiment);
    }

    public boolean isNegative() {
        return SENTIMENT_NEGATIVE.equals(sentiment);
    }
}
