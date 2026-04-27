package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.CarRecommendation;

import java.util.List;

public interface RecommendationService {

    List<RecommendationQuestion> getQuestions();

    List<CarRecommendation> recommend(RecommendationCriteria criteria, int limit);
}
