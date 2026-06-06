package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.ActivityFeedCriteria;
import ar.edu.itba.paw.model.ActivityFeedItem;
import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.services.ActivityService;
import ar.edu.itba.paw.services.CommunityService;
import ar.edu.itba.paw.services.ReviewLikeService;
import ar.edu.itba.paw.webapp.controller.support.RelativeTimeFormatter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class ActivityControllerTest {

    @Mock
    private ActivityService activityService;

    @Mock
    private ReviewLikeService reviewLikeService;

    @Mock
    private CommunityService communityService;

    @Mock
    private RelativeTimeFormatter relativeTimeFormatter;

    @InjectMocks
    private ActivityController controller;

    private MockMvc activityMockMvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    void activity_rendersMixedFeedPage() throws Exception {
        // Arrange
        when(activityService.getActivityFeed(any(ActivityFeedCriteria.class))).thenReturn(Page.empty(1, Pagination.ACTIVITY_PAGE_SIZE));
        final MockMvc mockMvc = activityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/activity"));

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("activity.jsp"))
                .andExpect(model().attributeExists("activityCards"))
                .andExpect(model().attributeDoesNotExist("activeTab"));
    }

    @Test
    void activity_usesRequestedPageAndExposesPagination() throws Exception {
        // Arrange
        when(activityService.getActivityFeed(any(ActivityFeedCriteria.class))).thenReturn(new Page<>(List.of(), 3, Pagination.ACTIVITY_PAGE_SIZE, 20L));
        final MockMvc mockMvc = activityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/activity").param("page", "3"));

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(model().attribute("activityCurrentPage", 3))
                .andExpect(model().attribute("activityTotalPages", 4));
    }

    @Test
    void activity_bindsFilterParamsAndExposesCriteria() throws Exception {
        // Arrange
        when(activityService.getActivityFeed(any(ActivityFeedCriteria.class))).thenReturn(Page.empty(1, Pagination.ACTIVITY_PAGE_SIZE));
        final MockMvc mockMvc = activityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/activity")
                .param("sort", "controversial")
                .param("type", "reviews")
                .param("timeframe", "week")
                .param("page", "2"));

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(result -> {
                    final ActivityFeedCriteria criteria = (ActivityFeedCriteria)
                            result.getModelAndView().getModel().get("activityCriteria");
                    assertEquals("controversial", criteria.getSort());
                    assertEquals("reviews", criteria.getType());
                    assertEquals("week", criteria.getTimeframe());
                    assertEquals(Integer.valueOf(2), criteria.getPage());
                });
    }

    @Test
    void activity_mapsReviewAndCommunityPostIntoGenericCards() throws Exception {
        // Arrange
        final LocalDateTime now = LocalDateTime.now();
        when(relativeTimeFormatter.format(any(LocalDateTime.class))).thenReturn("now");
        when(activityService.getActivityFeed(any(ActivityFeedCriteria.class))).thenReturn(new Page<>(
                List.of(
                        ActivityFeedItem.reviewItem(review(now.minusMinutes(3)), 0L, 0L, null, 2, List.of()),
                        ActivityFeedItem.communityPostItem(post(now.minusMinutes(1)), 5L, 2L, List.of())
                ),
                1,
                Pagination.ACTIVITY_PAGE_SIZE,
                2L
        ));
        final MockMvc mockMvc = activityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/activity"));

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("activityCards"))
                .andExpect(model().attribute("activityTotalPages", 1));
    }

    private static Review review(final LocalDateTime createdAt) {
        final Review review = TestModels.review(9L, 3L, "writer@test.com", "Writer", 20L,
                BigDecimal.valueOf(4.0), "t", "b", "owner", 2020, 10000, true, createdAt, createdAt);
        return review;
    }

    private static CommunityPost post(final LocalDateTime createdAt) {
        final Community community = new Community("classics", "Classics", "Community description");
        community.setId(7L);

        final CommunityPost post = new CommunityPost();
        post.setId(15L);
        post.setCommunity(community);
        post.setAuthor(TestModels.user(8L, "mateo.classics", "mateo@classics.com", "pw", "user", createdAt.minusDays(2)));
        post.setSlug("falcon");
        post.setTitle("Falcon");
        post.setBody("Body");
        post.setCreatedAt(createdAt);
        return post;
    }
}
