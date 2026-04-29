package ar.edu.itba.paw.services;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RecommendationCriteria implements Serializable {

    private Map<String, String> answers = new HashMap<>();
    private String bodyType;
    private String fuelType;

    public RecommendationCriteria() {}

    public RecommendationCriteria(final Map<String, String> answers, final String bodyType, final String fuelType) {
        this.answers = answers == null ? new HashMap<>() : new HashMap<>(answers);
        this.bodyType = bodyType;
        this.fuelType = fuelType;
    }

    public Map<String, String> getAnswers() {
        return Collections.unmodifiableMap(answers);
    }

    public void setAnswers(final Map<String, String> answers) {
        this.answers = answers == null ? new HashMap<>() : new HashMap<>(answers);
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(final String bodyType) {
        this.bodyType = bodyType;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(final String fuelType) {
        this.fuelType = fuelType;
    }
}
