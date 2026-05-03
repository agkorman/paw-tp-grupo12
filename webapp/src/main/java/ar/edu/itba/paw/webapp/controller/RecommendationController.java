package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.CarRecommendation;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.services.RecommendationService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.form.RecommendationForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class RecommendationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationController.class);

    private static final int DEFAULT_LIMIT = 5;

    private final RecommendationService recommendationService;
    private final BodyTypeService bodyTypeService;
    private final ReviewService reviewService;

    @Autowired
    public RecommendationController(final RecommendationService recommendationService,
                                    final BodyTypeService bodyTypeService,
                                    final ReviewService reviewService) {
        this.recommendationService = recommendationService;
        this.bodyTypeService = bodyTypeService;
        this.reviewService = reviewService;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "/cars/recommend", method = RequestMethod.GET)
    public String recommend(@ModelAttribute("recommendationForm") final RecommendationForm recommendationForm,
                            final Model model) {
        populateFormModel(model);
        return "recommend.jsp";
    }

    @RequestMapping(value = "/cars/recommend/results", method = RequestMethod.GET)
    public String results(@Valid @ModelAttribute("recommendationForm") final RecommendationForm recommendationForm,
                          final BindingResult errors,
                          final Model model) {
        rejectInvalidBodyType(recommendationForm, errors);
        populateFormModel(model);
        if (errors.hasErrors()) {
            return "recommend.jsp";
        }

        final List<CarRecommendation> recommendations =
                recommendationService.recommend(recommendationForm.toCriteria(), DEFAULT_LIMIT);
        final List<Long> carIds = recommendations.stream()
                .map(recommendation -> recommendation.getCar().getId())
                .collect(Collectors.toList());
        final Map<Long, ReviewStats> reviewStatsByCarId = reviewService.getReviewStatsByCarIds(carIds).stream()
                .collect(Collectors.toMap(ReviewStats::getCarId, stats -> stats));
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("reviewStatsByCarId", reviewStatsByCarId);
        model.addAttribute("hasRecommendations", !recommendations.isEmpty());
        return "recommend-results.jsp";
    }

    private void populateFormModel(final Model model) {
        model.addAttribute("questions", recommendationService.getQuestions());
        model.addAttribute("bodyTypes", bodyTypeService.findAll());
        model.addAttribute("fuelTypes", CarSearchCriteria.ALLOWED_FUEL_TYPES);
    }

    private void rejectInvalidBodyType(final RecommendationForm form, final BindingResult errors) {
        final String bodyType = form.getBodyType();
        if (bodyType == null || bodyType.trim().isEmpty()) {
            return;
        }
        final boolean exists = bodyTypeService.findAll().stream()
                .map(BodyType::getName)
                .anyMatch(bodyType::equals);
        if (!exists) {
            LOGGER.warn("recommendation rejected: invalid body type name={}", bodyType);
            errors.rejectValue("bodyType", "recommend.bodyType.invalid", "Tipo de carrocería inválido.");
        }
    }
}
