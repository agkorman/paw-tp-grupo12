package ar.edu.itba.paw.services;

import java.io.Serializable;

public class RecommendationAnswer implements Serializable {

    private String id;
    private String labelKey;

    public RecommendationAnswer() {}

    public RecommendationAnswer(final String id, final String labelKey) {
        this.id = id;
        this.labelKey = labelKey;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public void setLabelKey(final String labelKey) {
        this.labelKey = labelKey;
    }
}
