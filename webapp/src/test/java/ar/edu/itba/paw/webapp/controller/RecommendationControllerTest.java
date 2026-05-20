package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.services.BrandService;
import ar.edu.itba.paw.services.RecommendationCriteria;
import ar.edu.itba.paw.services.RecommendationService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.ReviewTagService;
import ar.edu.itba.paw.webapp.controller.support.ControllerTestValidationSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecommendationControllerTest {

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private BrandService brandService;

    @Mock
    private BodyTypeService bodyTypeService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private ReviewTagService reviewTagService;

    @InjectMocks
    private RecommendationController controller;

    private MockMvc recommendationMockMvc() throws Exception {
        return MockMvcBuilders.standaloneSetup(controller)
                .setValidator(ControllerTestValidationSupport.recommendationFormSpringValidator(bodyTypeService))
                .setControllerAdvice(new SharedModelAttributesAdvice(brandService, bodyTypeService, reviewTagService))
                .build();
    }

    private void arrangeSharedModelAndQuestions() throws Exception {
        when(brandService.findAll()).thenReturn(Collections.emptyList());
        when(bodyTypeService.findAll())
                .thenReturn(Collections.singletonList(TestModels.bodyType(1L, "Sedan", LocalDateTime.now())));
        when(reviewTagService.getAllGroupedBySentiment()).thenReturn(Collections.emptyMap());
        when(recommendationService.getQuestions()).thenReturn(Collections.emptyList());
        when(bodyTypeService.existsByName(anyString())).thenReturn(true);
    }

    @Test
    void recommend_get_showsForm() throws Exception {
        // Arrange
        arrangeSharedModelAndQuestions();
        final MockMvc mockMvc = recommendationMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/cars/recommend"));
        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("recommend.jsp"))
                .andExpect(model().attributeExists("questions"));
    }

    @Test
    void recommendResults_validationError_staysOnForm() throws Exception {
        // Arrange
        arrangeSharedModelAndQuestions();
        final MockMvc mockMvc = recommendationMockMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(get("/cars/recommend/results").param("driving", "INVALID"));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(view().name("recommend.jsp"));
    }

    @Test
    void recommendResults_valid_showsResults() throws Exception {
        // Arrange
        arrangeSharedModelAndQuestions();
        when(recommendationService.recommend(any(RecommendationCriteria.class), eq(5))).thenReturn(Collections.emptyList());
        when(reviewService.getReviewStatsByCarIds(anyList())).thenReturn(Collections.emptyList());

        final MockMvc mockMvc = recommendationMockMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(
                        get("/cars/recommend/results")
                                .param("bodyType", "Sedan")
                                .param("fuelType", "combustion"));
        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("recommend-results.jsp"))
                .andExpect(model().attribute("hasRecommendations", false));
    }
}
