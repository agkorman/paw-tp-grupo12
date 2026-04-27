package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

final class RecommendationQuestionnaire {

    static final String DRIVING = "driving";
    static final String FIRST_CAR = "firstCar";
    static final String FUEL_ECONOMY = "fuelEconomy";
    static final String COMFORT = "comfort";
    static final String CARGO = "cargo";
    static final String PERFORMANCE = "performance";

    private static final String ANY = "any";
    private static final String VERY = "very";
    private static final String SOMEWHAT = "somewhat";
    private static final String NOT = "not";

    private static final Map<String, Map<String, Map<String, BigDecimal>>> WEIGHTS = Map.of(
            DRIVING, Map.of(
                    "city", weights("good_for_city", 1.0, "easy_to_park", 0.7, "hard_to_park", -0.7),
                    "highway", weights("good_for_highway", 1.0, "bad_for_highway", -1.0, "noisy_cabin", -0.5),
                    "mixed", weights("good_for_city", 0.6, "good_for_highway", 0.6, "bad_for_highway", -0.5),
                    ANY, Map.of()
            ),
            FIRST_CAR, Map.of(
                    "yes", weights("good_first_car", 1.0, "safe", 0.5, "easy_to_park", 0.5),
                    "no", Map.of(),
                    ANY, Map.of()
            ),
            FUEL_ECONOMY, Map.of(
                    VERY, weights("low_fuel_consumption", 1.0, "high_fuel_consumption", -1.0),
                    SOMEWHAT, weights("low_fuel_consumption", 0.5, "high_fuel_consumption", -0.5),
                    NOT, Map.of()
            ),
            COMFORT, Map.of(
                    VERY, weights("comfortable", 1.0, "uncomfortable", -1.0, "noisy_cabin", -0.5),
                    SOMEWHAT, weights("comfortable", 0.5, "uncomfortable", -0.5),
                    NOT, Map.of()
            ),
            CARGO, Map.of(
                    "lot", weights("big_trunk", 1.0, "small_trunk", -1.0),
                    "sometimes", weights("big_trunk", 0.5, "small_trunk", -0.4),
                    "rarely", Map.of(),
                    ANY, Map.of()
            ),
            PERFORMANCE, Map.of(
                    VERY, weights("agile_engine", 1.0, "underpowered", -1.0),
                    SOMEWHAT, weights("agile_engine", 0.5, "underpowered", -0.5),
                    NOT, Map.of()
            )
    );

    private static final List<RecommendationQuestion> QUESTIONS = List.of(
            question(DRIVING, "recommend.question.driving",
                    answer("city"), answer("highway"), answer("mixed"), answer(ANY)),
            question(FIRST_CAR, "recommend.question.firstCar",
                    answer("yes"), answer("no"), answer(ANY)),
            question(FUEL_ECONOMY, "recommend.question.fuelEconomy",
                    answer(VERY), answer(SOMEWHAT), answer(NOT)),
            question(COMFORT, "recommend.question.comfort",
                    answer(VERY), answer(SOMEWHAT), answer(NOT)),
            question(CARGO, "recommend.question.cargo",
                    answer("lot"), answer("sometimes"), answer("rarely"), answer(ANY)),
            question(PERFORMANCE, "recommend.question.performance",
                    answer(VERY), answer(SOMEWHAT), answer(NOT))
    );

    private RecommendationQuestionnaire() {}

    static List<RecommendationQuestion> questions() {
        return QUESTIONS;
    }

    static Map<String, Map<String, Map<String, BigDecimal>>> weights() {
        return WEIGHTS;
    }

    static Map<String, BigDecimal> weightsFor(final String questionId, final String answerId) {
        return WEIGHTS.getOrDefault(questionId, Map.of()).getOrDefault(answerId, Map.of());
    }

    private static RecommendationQuestion question(final String id, final String labelKey,
                                                   final RecommendationAnswer... answers) {
        return new RecommendationQuestion(id, labelKey, List.of(answers));
    }

    private static RecommendationAnswer answer(final String id) {
        return new RecommendationAnswer(id, "recommend.answer." + id);
    }

    private static Map<String, BigDecimal> weights(final String tagCode1, final double weight1,
                                                   final String tagCode2, final double weight2) {
        return Map.of(tagCode1, BigDecimal.valueOf(weight1), tagCode2, BigDecimal.valueOf(weight2));
    }

    private static Map<String, BigDecimal> weights(final String tagCode1, final double weight1,
                                                   final String tagCode2, final double weight2,
                                                   final String tagCode3, final double weight3) {
        return Map.of(
                tagCode1, BigDecimal.valueOf(weight1),
                tagCode2, BigDecimal.valueOf(weight2),
                tagCode3, BigDecimal.valueOf(weight3)
        );
    }
}
