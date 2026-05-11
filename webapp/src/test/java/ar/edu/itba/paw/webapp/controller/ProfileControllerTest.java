package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.AdminRequestService;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewLikeService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.UserFollowService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.exception.SelfFollowException;
import ar.edu.itba.paw.services.exception.UsernameAlreadyExistsException;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.controller.support.ControllerTestMvcSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProfileControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private ReviewLikeService reviewLikeService;

    @Mock
    private CarService carService;

    @Mock
    private CarFavoriteService carFavoriteService;

    @Mock
    private UserService userService;

    @Mock
    private UserFollowService userFollowService;

    @Mock
    private AdminRequestService adminRequestService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private LocaleResolver localeResolver;

    @InjectMocks
    private ProfileController controller;

    private MockMvc profileMockMvc() throws Exception {
        final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private void arrangeMessageBundle() {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("msg");
    }

    private void arrangeExistingProfile(final long profileUserId) {
        arrangeMessageBundle();

        final User profileUser =
                new User(profileUserId, "ProfileUser", profileUserId + "@test.com", "pw", "user", LocalDateTime.now());
        when(userService.getUserById(eq(profileUserId))).thenReturn(Optional.of(profileUser));
        when(reviewService.countReviewsByUser(eq(profileUserId))).thenReturn(0L);
        when(carFavoriteService.countFavoriteCars(eq(profileUserId))).thenReturn(1L);
        when(reviewLikeService.countLikedReviewsByUser(eq(profileUserId))).thenReturn(0L);
        when(reviewLikeService.countReviewLikesByReviewIds(any())).thenReturn(Collections.emptyMap());

        when(reviewService.getReviewsByUser(eq(profileUserId), anyInt()))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(1), Pagination.REVIEWS_PAGE_SIZE));
        when(carFavoriteService.getFavoriteCars(eq(profileUserId), anyInt()))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(1), Pagination.CARS_PAGE_SIZE));
        when(reviewLikeService.getLikedReviewIdsByUser(eq(profileUserId), anyInt()))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(1), Pagination.REVIEWS_PAGE_SIZE));

        when(userFollowService.countFollowing(eq(profileUserId))).thenReturn(0L);
        when(userFollowService.countFollowers(eq(profileUserId))).thenReturn(0L);
        when(userFollowService.isFollowing(anyLong(), eq(profileUserId))).thenReturn(false);
        when(adminRequestService.hasPendingRequest(eq(profileUserId))).thenReturn(false);
        when(carService.getCarsByIds(any())).thenReturn(Collections.emptyList());
        when(reviewService.getReviewStatsByCarIds(any())).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByIds(any())).thenReturn(Collections.emptyList());
    }

    private static AuthenticatedUser testUser(final long id, final String name) {
        return new AuthenticatedUser(id, name, id + "@test.com", "pw", Collections.emptyList());
    }

    @Test
    void profile_anonymous_redirectsToLogin() throws Exception {
        // Arrange
        arrangeMessageBundle();
        final MockMvc mockMvc = profileMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/profile"));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test
    void profile_authenticated_showsProfile() throws Exception {
        // Arrange
        ControllerTestMvcSupport.bindPrincipal(testUser(10L, "Owner"));
        arrangeExistingProfile(10L);

        try {
            final MockMvc mockMvc = profileMockMvc();
            // Exercise
            final ResultActions resultActions = mockMvc.perform(get("/profile"));
            // Assertions
            resultActions.andExpect(status().isOk()).andExpect(view().name("profile.jsp"));
        } finally {
            ControllerTestMvcSupport.clearSecurityContext();
        }
    }

    @Test
    void profile_ownFavorites_tabLoads() throws Exception {
        // Arrange
        ControllerTestMvcSupport.bindPrincipal(testUser(45L, "FavUser"));
        arrangeExistingProfile(45L);
        when(carFavoriteService.getFavoriteCars(eq(45L), anyInt()))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(1), Pagination.CARS_PAGE_SIZE));

        try {
            final MockMvc mockMvc = profileMockMvc();
            // Exercise
            final ResultActions resultActions = mockMvc.perform(get("/profile").param("tab", "favorites"));
            // Assertions
            resultActions.andExpect(status().isOk()).andExpect(model().attribute("activeTab", "favorites"));
        } finally {
            ControllerTestMvcSupport.clearSecurityContext();
        }
    }

    @Test
    void public_profile_missingUser_returns404ViaAdvice() throws Exception {
        // Arrange
        arrangeMessageBundle();
        when(userService.getUserById(eq(999L))).thenReturn(Optional.empty());
        final MockMvc mockMvc = profileMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/profiles/999"));
        // Assertions
        resultActions.andExpect(status().isNotFound()).andExpect(forwardedUrl("/error/404"));
    }

    @Test
    void profile_update_validationError_redirectsToProfile() throws Exception {
        // Arrange
        ControllerTestMvcSupport.bindPrincipal(testUser(11L, "BadInput"));
        arrangeExistingProfile(11L);

        try {
            final MockMvc mockMvc = profileMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(post("/profile").param("displayName", ""));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/profile"));
        } finally {
            ControllerTestMvcSupport.clearSecurityContext();
        }
    }

    @Test
    void profile_update_success_redirectsToProfile() throws Exception {
        // Arrange
        ControllerTestMvcSupport.bindPrincipal(testUser(12L, "GoodName"));
        arrangeExistingProfile(12L);

        final User refreshed =
                new User(12L, "GoodNameRenamed", "12@test.com", "pw", "user", LocalDateTime.now());
        when(userService.updateUsername(eq(12L), eq("RenamedGood"))).thenReturn(refreshed);

        try {
            final MockMvc mockMvc = profileMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(post("/profile").param("displayName", "RenamedGood"));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/profile"));
        } finally {
            ControllerTestMvcSupport.clearSecurityContext();
        }
    }

    @Test
    void profile_update_usernameConflict_redirectsToProfile() throws Exception {
        // Arrange
        ControllerTestMvcSupport.bindPrincipal(testUser(33L, "DupTry"));
        arrangeExistingProfile(33L);
        when(userService.updateUsername(eq(33L), eq("TakenName"))).thenThrow(new UsernameAlreadyExistsException("TakenName"));

        try {
            final MockMvc mockMvc = profileMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(post("/profile").param("displayName", "TakenName"));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/profile"));
        } finally {
            ControllerTestMvcSupport.clearSecurityContext();
        }
    }

    @Test
    void toggleFollow_anonymous_redirectsLogin() throws Exception {
        // Arrange
        arrangeExistingProfile(20L);
        final MockMvc mockMvc = profileMockMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(post("/profiles/20/follow"));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test
    void toggleFollow_anonymous_ajaxReturns401() throws Exception {
        // Arrange
        arrangeExistingProfile(21L);
        final MockMvc mockMvc = profileMockMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(
                        post("/profiles/21/follow").header("X-Requested-With", "XMLHttpRequest"));
        // Assertions
        resultActions.andExpect(status().isUnauthorized()).andExpect(content().string("/login"));
    }

    @Test
    void toggleFollow_selfAttempt_redirectsToOwnProfile() throws Exception {
        // Arrange
        ControllerTestMvcSupport.bindPrincipal(testUser(22L, "SelfFollow"));
        arrangeExistingProfile(22L);
        when(userFollowService.toggleFollow(eq(22L), eq(22L))).thenThrow(new SelfFollowException(22L));

        try {
            final MockMvc mockMvc = profileMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(post("/profiles/22/follow"));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/profile"));
        } finally {
            ControllerTestMvcSupport.clearSecurityContext();
        }
    }

    @Test
    void toggleFollow_unknownUser_ajaxReturns404Forwarded() throws Exception {
        // Arrange
        ControllerTestMvcSupport.bindPrincipal(testUser(44L, "Follower"));
        arrangeMessageBundle();
        when(userService.getUserById(eq(777L))).thenReturn(Optional.empty());
        try {
            final MockMvc mockMvc = profileMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            post("/profiles/777/follow").header("X-Requested-With", "XMLHttpRequest"));
            // Assertions
            resultActions.andExpect(status().isNotFound()).andExpect(forwardedUrl("/error/404"));
        } finally {
            ControllerTestMvcSupport.clearSecurityContext();
        }
    }

    @Test
    void toggleFollow_valid_ajaxUpdatesFollowStateAndCount() throws Exception {
        // Arrange
        ControllerTestMvcSupport.bindPrincipal(testUser(23L, "FollowActor"));
        arrangeMessageBundle();
        when(userService.getUserById(eq(24L)))
                .thenReturn(
                        Optional.of(new User(24L, "Target", "24@test.com", "pw", "user", LocalDateTime.now())));
        when(reviewService.countReviewsByUser(eq(24L))).thenReturn(1L);
        when(reviewLikeService.countLikedReviewsByUser(eq(24L))).thenReturn(0L);
        when(reviewService.getReviewsByUser(eq(24L), anyInt()))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(1), Pagination.REVIEWS_PAGE_SIZE));
        when(carFavoriteService.countFavoriteCars(eq(24L))).thenReturn(0L);
        when(carFavoriteService.getFavoriteCars(eq(24L), anyInt()))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(1), Pagination.CARS_PAGE_SIZE));
        when(reviewLikeService.getLikedReviewIdsByUser(eq(24L), anyInt()))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(1), Pagination.REVIEWS_PAGE_SIZE));
        when(userFollowService.countFollowing(eq(24L))).thenReturn(1L);
        when(userFollowService.countFollowers(eq(24L))).thenReturn(4L);
        when(userFollowService.toggleFollow(eq(23L), eq(24L))).thenReturn(true);
        when(userFollowService.isFollowing(eq(23L), eq(24L))).thenReturn(false);
        when(carService.getCarsByIds(any())).thenReturn(Collections.emptyList());
        when(reviewService.getReviewStatsByCarIds(any())).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByIds(any())).thenReturn(Collections.emptyList());
        when(adminRequestService.hasPendingRequest(eq(24L))).thenReturn(false);

        try {
            final MockMvc mockMvc = profileMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            post("/profiles/24/follow").header("X-Requested-With", "XMLHttpRequest"));
            // Assertions
            resultActions.andExpect(status().isOk()).andExpect(content().string("true|4"));
        } finally {
            ControllerTestMvcSupport.clearSecurityContext();
        }
    }
}
