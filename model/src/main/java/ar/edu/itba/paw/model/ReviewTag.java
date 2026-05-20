package ar.edu.itba.paw.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_tags")
public class ReviewTag implements Serializable {

    public static final String SENTIMENT_POSITIVE = "positive";
    public static final String SENTIMENT_NEGATIVE = "negative";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private short id;

    @Column(name = "code")
    private String code;

    @Column(name = "label_es")
    private String labelEs;

    @Column(name = "sentiment")
    private String sentiment;

    @Column(name = "dimension")
    private String dimension;

    @Column(name = "created_at")
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
