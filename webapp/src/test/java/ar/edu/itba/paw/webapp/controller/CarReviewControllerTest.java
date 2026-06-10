package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.EmailService;
import ar.edu.itba.paw.services.ReviewLikeService;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.services.ReviewReplyService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.ReviewTagService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.exception.ReviewOwnershipException;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.controller.support.ControllerTestValidationSupport;
import ar.edu.itba.paw.webapp.form.ReviewForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarReviewControllerTest {

    @Mock
    private CarService carService;

    @Mock
    private CarFavoriteService carFavoriteService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private ReviewReplyService reviewReplyService;

    @Mock
    private ReviewLikeService reviewLikeService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ReviewTagService reviewTagService;

    @InjectMocks
    private CarReviewController controller;

    private MockMvc reviewMockMvc() throws Exception {
        LocaleContextHolder.setLocale(Locale.ROOT);
        try {
            return MockMvcBuilders.standaloneSetup(controller)
                    .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                    .setValidator(ControllerTestValidationSupport.reviewFormSpringValidator(reviewTagService))
                    .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                    .build();
        } finally {
            LocaleContextHolder.resetLocaleContext();
        }
    }

    private static void bindPrincipal(final AuthenticatedUser user) {
        final UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void arrangeStandardReviewCollaboratorsAndI18n() {
        when(reviewService.getReviewsByCar(anyLong(), anyInt())).thenAnswer(invocation ->
                Page.empty(invocation.getArgument(1, Integer.class), Pagination.REVIEWS_PAGE_SIZE));
        when(reviewService.getReviewsByCarOrderByRatingAsc(anyLong(), anyInt())).thenAnswer(invocation ->
                Page.empty(invocation.getArgument(1, Integer.class), Pagination.REVIEWS_PAGE_SIZE));
        when(reviewService.getReviewsByCarOrderByRatingDesc(anyLong(), anyInt())).thenAnswer(invocation ->
                Page.empty(invocation.getArgument(1, Integer.class), Pagination.REVIEWS_PAGE_SIZE));

        when(reviewService.getReviewStatsByCar(anyLong())).thenReturn(Optional.empty());
        when(reviewService.getLatestReviewByCar(anyLong())).thenReturn(Optional.empty());
        when(carService.getCarImagesByCarId(anyLong())).thenReturn(Collections.emptyList());
        when(reviewReplyService.getFirstNRepliesByReviewIds(any(), anyInt())).thenReturn(Collections.emptyMap());
        when(reviewReplyService.countRepliesByReviewIds(any())).thenReturn(Collections.emptyMap());
        when(reviewLikeService.countReviewLikesByReviewIds(any())).thenReturn(Collections.emptyMap());
        when(reviewLikeService.getLikedReviewIds(any(), anyLong())).thenReturn(Collections.emptySet());
        when(reviewLikeService.countReplyLikesByReplyIds(any())).thenReturn(Collections.emptyMap());
        when(reviewLikeService.getLikedReplyIds(any(), anyLong())).thenReturn(Collections.emptySet());
        when(carService.getCarsByBrandAndBodyType(anyString(), anyString())).thenReturn(Collections.emptyList());

        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("");
        when(reviewTagService.validateSelection(any())).thenReturn(Collections.emptyList());
    }

    @Test
    void reviewForm_carNotFound_throwsResourceNotFound() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        when(carService.getCarById(eq(99L))).thenReturn(Optional.empty());
        final MockMvc mockMvc = reviewMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/reviews/car/99"));
        // Assertions
        resultActions.andExpect(status().isNotFound()).andExpect(forwardedUrl("/error/404"));
    }

    @Test
    void reviewForm_validCar_showsReviewPage() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        when(carService.getCarById(eq(1L))).thenReturn(Optional.of(aCar(1L)));
        final MockMvc mockMvc = reviewMockMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(get("/reviews/car/1"));
        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("car-review.jsp"))
                .andExpect(model().attributeExists("selectedCar"));
    }

    @Test
    void newReview_carFound_showsForm() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        when(carService.getCarById(eq(1L))).thenReturn(Optional.of(aCar(1L)));
        final MockMvc mockMvc = reviewMockMvc();
        // Exercise
        final MvcResult mvcResult = mockMvc.perform(get("/reviews/new").param("carId", "1")).andReturn();
        // Assertions
        assertNotNull(mvcResult.getModelAndView(), "Expected ModelAndView");
        assertEquals("review-form.jsp", mvcResult.getModelAndView().getViewName());
        assertNotNull(mvcResult.getModelAndView().getModel().get("reviewForm"));
        assertInstanceOf(ReviewForm.class, mvcResult.getModelAndView().getModel().get("reviewForm"));
        assertEquals(1L, ((ReviewForm) mvcResult.getModelAndView().getModel().get("reviewForm")).getCarId());
    }

    @Test
    void newReview_carNotFound_redirectsToCars() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        when(carService.getCarById(eq(99L))).thenReturn(Optional.empty());
        final MockMvc mockMvc = reviewMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/reviews/new").param("carId", "99"));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cars"));
    }

    @Test
    void createReview_anonymous_redirectsToNewWithCarId() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        final MockMvc mockMvc = reviewMockMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(
                        post("/reviews")
                                .param("carId", "1")
                                .param("rating", "4.5")
                                .param("title", "Great car")
                                .param("body", "Really enjoyed driving it every day.")
                                .param("modelYear", "2020")
                                .param("mileageKm", "15000"));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/reviews/new?carId=1"));
    }

    @Test
    void createReview_carNotFound_throwsResourceNotFound() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        when(carService.getCarById(eq(99L))).thenReturn(Optional.empty());
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            post("/reviews")
                                    .param("carId", "99")
                                    .param("rating", "4.5")
                                    .param("title", "Great car")
                                    .param("body", "Really enjoyed driving it every day.")
                                    .param("modelYear", "2020")
                                    .param("mileageKm", "15000"));
            // Assertions
            resultActions.andExpect(status().isNotFound()).andExpect(forwardedUrl("/error/404"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void createReview_validationErrors_showsForm() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        when(carService.getCarById(eq(1L))).thenReturn(Optional.of(aCar(1L)));
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            post("/reviews")
                                    .param("carId", "1")
                                    .param("title", "Great car")
                                    .param("body", "Really enjoyed driving it every day.")
                                    .param("modelYear", "2020")
                                    .param("mileageKm", "15000"));
            // Assertions
            resultActions.andExpect(status().isOk()).andExpect(view().name("review-form.jsp"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void createReview_valid_redirectsToReviews() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        when(carService.getCarById(eq(1L))).thenReturn(Optional.of(aCar(1L)));
        when(reviewService.createReview(anyLong(), eq(1L), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(reviewOwnedBy(99L, 1L));
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            post("/reviews")
                                    .param("carId", "1")
                                    .param("rating", "4.5")
                                    .param("title", "Great car")
                                    .param("body", "Really enjoyed driving it every day.")
                                    .param("modelYear", "2020")
                                    .param("mileageKm", "15000"));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/reviews/car/1?reviewCreated=1#review-99"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void toggleReviewLike_anonymous_browserRedirectsToLogin() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        final MockMvc mockMvc = reviewMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(post("/reviews/1/like"));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test
    void toggleReviewLike_reviewNotFound_throwsResourceNotFound() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        when(reviewService.getReviewById(eq(1L))).thenReturn(Optional.empty());
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions = mockMvc.perform(post("/reviews/1/like"));
            // Assertions
            resultActions.andExpect(status().isNotFound()).andExpect(forwardedUrl("/error/404"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void toggleReviewLike_validWithoutRedirect_redirectsToCarReview() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        final Review review = reviewOwnedBy(1L, 42L);
        when(reviewService.getReviewById(eq(1L))).thenReturn(Optional.of(review));
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions = mockMvc.perform(post("/reviews/1/like"));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/reviews/car/42#review-1"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void toggleReviewLike_validWithProfileRedirect_staysOnProfile() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        final Review review = reviewOwnedBy(1L, 42L);
        when(reviewService.getReviewById(eq(1L))).thenReturn(Optional.of(review));
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions = mockMvc.perform(
                    post("/reviews/1/like")
                            .param("redirect", "/user?tab=liked&page=2#profileLikedPanel"));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/user?tab=liked&page=2#profileLikedPanel"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void updateReview_validWithProfileRedirect_staysOnProfile() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        final Review existing = reviewOwnedBy(1L, 42L);
        when(reviewService.getReviewAndCheckAccess(eq(1L), eq(1L), eq(false))).thenReturn(existing);
        when(carService.getCarById(eq(42L))).thenReturn(Optional.of(aCar(42L)));
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions = mockMvc.perform(
                    post("/reviews/1")
                            .param("carId", "42")
                            .param("rating", "4.5")
                            .param("title", "Updated title")
                            .param("body", "Updated body with enough detail.")
                            .param("mileageKm", "15000")
                            .param("redirect", "/user?tab=reviews#profileReviewsPanel"));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/user?tab=reviews#profileReviewsPanel"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void deleteReview_owner_redirectsToCarReviewFeedAnchor() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        final Review existing = reviewOwnedBy(1L, 42L);
        when(reviewService.getReviewById(eq(1L))).thenReturn(Optional.of(existing));
        when(reviewService.getReviewAndCheckAccess(eq(1L), eq(1L), eq(false))).thenReturn(existing);
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions = mockMvc.perform(post("/reviews/1/delete"));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/reviews/car/42#reviewsFeed"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void deleteReview_contextPrefixedRedirect_redirectsWithSingleContextPath() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        final Review existing = reviewOwnedBy(1L, 42L);
        when(reviewService.getReviewAndCheckAccess(eq(1L), eq(1L), eq(false))).thenReturn(existing);
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions = mockMvc.perform(
                    post("/paw-2026a-12/reviews/1/delete")
                            .contextPath("/paw-2026a-12")
                            .param("redirect", "/paw-2026a-12/reviews/car/42?page=2#reviewsFeed"));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/paw-2026a-12/reviews/car/42?page=2#reviewsFeed"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void hideReview_withFeedRedirect_preservesFiltersPageAndFeedAnchor() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        final Review existing = reviewOwnedBy(1L, 42L);
        when(reviewService.getReviewById(eq(1L))).thenReturn(Optional.of(existing));
        when(reviewService.hideReview(eq(1L), eq("Duplicated review."))).thenReturn(true);
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions = mockMvc.perform(
                    post("/reviews/1/hide")
                            .param("reason", "Duplicated review.")
                            .param("redirect", "/reviews/car/42?page=2&sort=rating_desc#reviewsFeed"));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/reviews/car/42?page=2&sort=rating_desc#reviewsFeed"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void deleteReview_notOwner_throwsForbidden() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        when(reviewService.getReviewAndCheckAccess(eq(1L), eq(2L), eq(false)))
                .thenThrow(new ReviewOwnershipException(1L));
        bindPrincipal(testUser(2L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions = mockMvc.perform(post("/reviews/1/delete"));
            // Assertions
            resultActions.andExpect(status().isForbidden()).andExpect(forwardedUrl("/error/403"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void createReply_anonymous_redirectsToLogin() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        final MockMvc mockMvc = reviewMockMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(post("/reviews/1/replies").param("body", "Terrific insights."));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test
    void createReply_reviewNotFound_throwsResourceNotFound() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        when(reviewService.getReviewById(eq(1L))).thenReturn(Optional.empty());
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(post("/reviews/1/replies").param("body", "Terrific insights."));
            // Assertions
            resultActions.andExpect(status().isNotFound()).andExpect(forwardedUrl("/error/404"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void createReply_valid_redirectsToReviewDetail() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        final Review review = reviewOwnedBy(1L, 7L);
        when(reviewService.getReviewById(eq(1L))).thenReturn(Optional.of(review));
        when(reviewReplyService.createReply(eq(1L), anyLong(), anyString())).thenReturn(aReply(55L));
        when(reviewReplyService.countRepliesByReviewIds(eq(java.util.List.of(1L))))
                .thenReturn(java.util.Map.of(1L, 1L));
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(post("/reviews/1/replies").param("body", "Terrific insights."));
            // Assertions
            resultActions
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/reviews/1#reply-55"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void toggleReplyLike_withSafeRedirect_preservesReviewDetailPage() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        final Review review = reviewOwnedBy(1L, 7L);
        final ReviewReply reply = new ReviewReply(review, review.getUser(), "Thanks.");
        reply.setId(55L);
        when(reviewReplyService.getReplyById(eq(55L))).thenReturn(Optional.of(reply));
        when(reviewService.getReviewById(eq(1L))).thenReturn(Optional.of(review));
        when(reviewLikeService.toggleReplyLike(eq(55L), anyLong())).thenReturn(true);
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions = mockMvc.perform(post("/reviews/replies/55/like")
                    .param("redirect", "/reviews/1?repliesPage=2#reply-55"));
            // Assertions
            resultActions
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/reviews/1?repliesPage=2#reply-55"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void hideReview_blankReason_redirectsBackWithoutHiding() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        when(reviewService.getReviewById(eq(1L))).thenReturn(Optional.of(reviewOwnedBy(1L, 42L)));
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions = mockMvc.perform(post("/reviews/1/hide").param("reason", ""));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/reviews/car/42#review-1"))
                    .andExpect(flash().attributeCount(0));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void hideReply_tooShortReason_redirectsBackWithoutHiding() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        final Review review = reviewOwnedBy(1L, 7L);
        final ReviewReply reply = new ReviewReply(review, review.getUser(), "Thanks.");
        reply.setId(55L);
        when(reviewReplyService.getReplyById(eq(55L))).thenReturn(Optional.of(reply));
        when(reviewService.getReviewById(eq(1L))).thenReturn(Optional.of(review));
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions = mockMvc.perform(post("/reviews/replies/55/hide").param("reason", "tooshort"));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/reviews/car/7#review-1"))
                    .andExpect(flash().attributeCount(0));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void createReply_blankBody_rendersPageWithReplyError() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        when(reviewService.getReviewById(eq(1L))).thenReturn(Optional.of(reviewOwnedBy(1L, 7L)));
        when(carService.getCarById(eq(7L))).thenReturn(Optional.of(aCar(7L)));
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions = mockMvc.perform(post("/reviews/1/replies").param("body", ""));
            // Assertions
            resultActions.andExpect(status().isOk())
                    .andExpect(view().name("car-review.jsp"))
                    .andExpect(model().attributeExists("replyError"))
                    .andExpect(model().attribute("replyErrorReviewId", 1L));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void updateReply_blankBody_redirectsBackWithoutUpdating() throws Exception {
        // Arrange
        arrangeStandardReviewCollaboratorsAndI18n();
        final Review review = reviewOwnedBy(1L, 7L);
        final ReviewReply reply = new ReviewReply(review, review.getUser(), "Original reply body.");
        reply.setId(55L);
        when(reviewReplyService.getReplyById(eq(55L))).thenReturn(Optional.of(reply));
        when(reviewService.getReviewById(eq(1L))).thenReturn(Optional.of(review));
        bindPrincipal(testUser(1L));

        try {
            final MockMvc mockMvc = reviewMockMvc();
            // Exercise
            final ResultActions resultActions = mockMvc.perform(post("/reviews/replies/55/update").param("body", ""));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/reviews/car/7#reply-55"))
                    .andExpect(flash().attributeCount(0));
        } finally {
            clearSecurityContext();
        }
    }

    private static AuthenticatedUser testUser(final long id) {
        return new AuthenticatedUser(id, "u" + id, "user" + id + "@test.com", "pass", Collections.emptyList());
    }

    private static Car aCar(final long carId) {
        return TestModels.car(carId, 1L, "Toyota", "Corolla", 1L, 2024, "Sedan",
                "desc", LocalDateTime.now(), false,
                ar.edu.itba.paw.model.CarSearchCriteria.FUEL_TYPE_COMBUSTION, 140,
                6, ar.edu.itba.paw.model.CarSearchCriteria.TRANSMISSION_AUTOMATIC,
                BigDecimal.valueOf(8.0), 200, BigDecimal.valueOf(20000));
    }

    private static ReviewReply aReply(final long replyId) {
        final ReviewReply reply = new ReviewReply(null, null, null);
        reply.setId(replyId);
        return reply;
    }

    private static Review reviewOwnedBy(final long reviewId, final long carId) {
        final User user = new User("user1", "user1@test.com", "pass", "user", "es");
        user.setId(1L);
        final Car car = TestModels.car(carId, 1L, "Toyota", "Corolla", 1L, 2024, "Sedan",
                "desc", LocalDateTime.now(), false,
                ar.edu.itba.paw.model.CarSearchCriteria.FUEL_TYPE_COMBUSTION, 140,
                6, ar.edu.itba.paw.model.CarSearchCriteria.TRANSMISSION_AUTOMATIC,
                BigDecimal.valueOf(8.0), 200, BigDecimal.valueOf(20000));
        final Review r = new Review(car, new BigDecimal("4.5"), "t", "b");
        r.setId(reviewId);
        r.setUser(user);
        car.setId(carId);
        return r;
    }
}
