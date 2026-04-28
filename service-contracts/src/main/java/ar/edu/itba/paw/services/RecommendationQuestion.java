package ar.edu.itba.paw.services;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class RecommendationQuestion implements Serializable {

    private String id;
    private String labelKey;
    private List<RecommendationAnswer> answers;

    public RecommendationQuestion() {}

    public RecommendationQuestion(final String id, final String labelKey,
                                  final List<RecommendationAnswer> answers) {
        this.id = id;
        this.labelKey = labelKey;
        this.answers = answers == null ? Collections.emptyList() : Collections.unmodifiableList(answers);
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

    public List<RecommendationAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(final List<RecommendationAnswer> answers) {
        this.answers = answers == null ? Collections.emptyList() : Collections.unmodifiableList(answers);
    }
}
