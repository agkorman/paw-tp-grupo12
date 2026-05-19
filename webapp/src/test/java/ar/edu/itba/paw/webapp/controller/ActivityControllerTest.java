package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.controller.support.ControllerTestMvcSupport;
import ar.edu.itba.paw.webapp.controller.support.RelativeTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActivityControllerTest {

    private static final int ACTIVITY_PAGE_SIZE = 10;

    @Mock
    private ReviewService reviewService;

    @Mock
    private CarService carService;

    @Mock
    private RelativeTimeFormatter relativeTimeFormatter;

    @InjectMocks
    private ActivityController controller;

    private MockMvc activityMockMvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private void arrangeLatestTabEmptyFeed() {
        when(reviewService.getActivityFeedReviews(eq(ReviewService.FEED_LATEST), eq(null), anyInt())).thenAnswer(invocation ->
                Page.empty(invocation.getArgument(2, Integer.class), ACTIVITY_PAGE_SIZE));
        when(reviewService.countAllReviews()).thenReturn(0L);
        when(carService.getCarsByIds(anyList())).thenReturn(Collections.emptyList());
        when(reviewService.getDefaultPagesForReviewIds(anyList())).thenReturn(Collections.emptyMap());
        when(relativeTimeFormatter.format(any(LocalDateTime.class))).thenReturn("now");
    }

    private static AuthenticatedUser testUser(final long id) {
        return new AuthenticatedUser(id, "Tester", id + "@test.com", "pw", Collections.emptyList());
    }

    @Test
    void activity_anonymous_showsLatestTab() throws Exception {
        // Arrange
        arrangeLatestTabEmptyFeed();
        final MockMvc mockMvc = activityMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/activity"));
        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("activity.jsp"))
                .andExpect(model().attribute("activeTab", "latest"));
    }

    @Test
    void activity_anonymous_ignoresFollowingTabParam() throws Exception {
        // Arrange
        arrangeLatestTabEmptyFeed();
        final MockMvc mockMvc = activityMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/activity").param("tab", "following"));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(model().attribute("activeTab", "latest"));
    }

    @Test
    void activity_authenticated_followersTab_queriesFollowedReviews() throws Exception {
        // Arrange
        ControllerTestMvcSupport.bindPrincipal(testUser(1L));
        try {
            when(reviewService.getActivityFeedReviews(eq(ReviewService.FEED_FOLLOWING), eq(1L), eq(2))).thenAnswer(invocation ->
                    Page.empty(invocation.getArgument(2, Integer.class), ACTIVITY_PAGE_SIZE));
            when(reviewService.countAllReviews()).thenReturn(10L);
            when(reviewService.countReviewsByFollowedUsers(eq(1L))).thenReturn(5L);
            when(reviewService.countReviewsByFavoriteCars(eq(1L))).thenReturn(3L);
            when(carService.getCarsByIds(anyList())).thenReturn(Collections.emptyList());
            when(reviewService.getDefaultPagesForReviewIds(anyList())).thenReturn(Collections.emptyMap());
            when(relativeTimeFormatter.format(any(LocalDateTime.class))).thenReturn("now");

            final MockMvc mockMvc = activityMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            get("/activity")
                                    .param("tab", "following")
                                    .param("page", "2")
                                    .with(ControllerTestMvcSupport.authenticationPrincipalRequestPostProcessor()));
            // Assertions
            resultActions.andExpect(status().isOk()).andExpect(model().attribute("activeTab", "following"));
        } finally {
            ControllerTestMvcSupport.clearSecurityContext();
        }
    }

    @Test
    void activity_authenticated_favoritesTab_queriesFavoriteCars() throws Exception {
        // Arrange
        ControllerTestMvcSupport.bindPrincipal(testUser(2L));
        try {
            when(reviewService.getActivityFeedReviews(eq(ReviewService.FEED_FAVORITES), eq(2L), eq(1))).thenAnswer(invocation ->
                    Page.empty(invocation.getArgument(2, Integer.class), ACTIVITY_PAGE_SIZE));
            when(reviewService.countAllReviews()).thenReturn(0L);
            when(reviewService.countReviewsByFollowedUsers(eq(2L))).thenReturn(0L);
            when(reviewService.countReviewsByFavoriteCars(eq(2L))).thenReturn(8L);
            when(carService.getCarsByIds(anyList())).thenReturn(Collections.emptyList());
            when(reviewService.getDefaultPagesForReviewIds(anyList())).thenReturn(Collections.emptyMap());
            when(relativeTimeFormatter.format(any(LocalDateTime.class))).thenReturn("now");

            final MockMvc mockMvc = activityMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            get("/activity")
                                    .param("tab", "favorites")
                                    .with(ControllerTestMvcSupport.authenticationPrincipalRequestPostProcessor()));
            // Assertions
            resultActions.andExpect(status().isOk()).andExpect(model().attribute("activeTab", "favorites"));
        } finally {
            ControllerTestMvcSupport.clearSecurityContext();
        }
    }

    @Test
    void activity_rendersReviewCards_whenReviewsPresent() throws Exception {
        // Arrange
        final Review review =
                TestModels.review(
                        9L,
                        3L,
                        "writer@test.com",
                        "Writer",
                        20L,
                        BigDecimal.valueOf(4.0),
                        "t",
                        "b",
                        "owner",
                        2020,
                        10000,
                        true,
                        LocalDateTime.now(),
                        LocalDateTime.now());

        arrangeLatestTabEmptyFeed();
        when(reviewService.getActivityFeedReviews(eq(ReviewService.FEED_LATEST), eq(null), eq(1))).thenReturn(
                new Page<>(Collections.singletonList(review), 1, ACTIVITY_PAGE_SIZE, 1L));

        final MockMvc mockMvc = activityMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/activity"));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(model().attributeExists("activityReviews"));
    }
}
