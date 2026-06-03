package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityDetailData;
import ar.edu.itba.paw.model.CommunityHubEntry;
import ar.edu.itba.paw.model.CommunityMembersData;
import ar.edu.itba.paw.model.CommunityMembershipEntry;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.CommunityPostComment;
import ar.edu.itba.paw.model.CommunityPostDetailData;
import ar.edu.itba.paw.model.CommunityPostImage;
import ar.edu.itba.paw.model.CommunityPostSummary;
import ar.edu.itba.paw.model.CommunityTopic;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.CommunityService;
import ar.edu.itba.paw.services.exception.CommunityMembershipRequiredException;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.controller.support.ControllerTestValidationSupport;
import ar.edu.itba.paw.webapp.controller.support.RelativeTimeFormatter;
import ar.edu.itba.paw.webapp.form.CommunityForm;
import ar.edu.itba.paw.webapp.form.CommunityPostForm;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class CommunityControllerTest {

    @Mock
    private CommunityService communityService;

    @Mock
    private RelativeTimeFormatter relativeTimeFormatter;

    @InjectMocks
    private CommunityController controller;

    private MockMvc communityMockMvc() {
        LocaleContextHolder.setLocale(Locale.ROOT);
        try {
            return MockMvcBuilders.standaloneSetup(controller)
                    .setValidator(ControllerTestValidationSupport.communityFormSpringValidator(communityService))
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

    private static void assertGlobalErrorCode(
            final MvcResult result,
            final String formName,
            final String expectedCode
    ) {
        final Object errorsObject = result.getModelAndView()
                .getModel()
                .get(BindingResult.MODEL_KEY_PREFIX + formName);
        assertTrue(errorsObject instanceof BindingResult);
        final BindingResult errors = (BindingResult) errorsObject;
        assertTrue(errors.getGlobalErrors()
                .stream()
                .anyMatch(error -> expectedCode.equals(error.getCode())));
    }

    @Test
    void communitiesPage_rendersHubView() throws Exception {
        // Arrange
        when(communityService.getCommunityHub(any(), any()))
                .thenReturn(new Page<>(List.of(communityHubEntry()), 1, 12, 1L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/communities"));

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("communities.jsp"))
                .andExpect(model().attributeExists("communityCards"))
                .andExpect(model().attribute("communitiesCurrentPage", 1))
                .andExpect(model().attribute("communitiesTotalPages", 1));
    }

    @Test
    void createCommunityPage_rendersCreateCommunityView() throws Exception {
        // Arrange
        when(communityService.getAvailableTopics()).thenReturn(List.of(topic((short) 1, "classics")));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/communities/new"));

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("community-create.jsp"))
                .andExpect(model().attributeExists("communityTopics"));
    }

    @Test
    void communityDetail_knownSlug_rendersDetailView() throws Exception {
        // Arrange
        when(communityService.getCommunityDetail(anyString(), any(), any(), anyInt()))
                .thenReturn(Optional.of(communityDetailData()));
        when(relativeTimeFormatter.format(any(LocalDateTime.class))).thenReturn("2 hours ago");
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/communities/classics"));

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("community-detail.jsp"))
                .andExpect(model().attributeExists("communityDetail"))
                .andExpect(model().attributeExists("postCards"))
                .andExpect(model().attribute("postsCurrentPage", 1))
                .andExpect(model().attribute("postsTotalPages", 1));
    }

    @Test
    void communityPostDetail_knownPost_rendersPostDetailView() throws Exception {
        // Arrange
        when(communityService.getCommunityPostDetail(anyString(), anyString(), any()))
                .thenReturn(Optional.of(communityPostDetailData()));
        when(relativeTimeFormatter.format(any(LocalDateTime.class))).thenReturn("2 hours ago");
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(get("/communities/classics/posts/falcon-60"));

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("community-post-detail.jsp"))
                .andExpect(model().attributeExists("postDetail"))
                .andExpect(model().attributeExists("postView"));
    }

    @Test
    void createCommunityPost_knownSlug_rendersPostFormView() throws Exception {
        // Arrange
        when(communityService.getCommunityBySlug(anyString())).thenReturn(Optional.of(community()));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(get("/communities/classics/submit"));

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("community-post-form.jsp"))
                .andExpect(model().attributeExists("community"))
                .andExpect(model().attributeExists("communityPostForm"));
    }

    @Test
    void createCommunity_validForm_redirectsToCommunityDetail() throws Exception {
        // Arrange
        when(communityService.getAvailableTopics()).thenReturn(List.of(topic((short) 1, "classics")));
        when(communityService.createCommunity(anyLong(), anyString(), anyString(), anyCollection()))
                .thenReturn(community());
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/communities")
                .param("name", "Classics")
                .param("description", "Pre-1990 cars and honest restoration projects.")
                .param("selectedTopicIds", "1"));

        // Assertions
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/communities/classics"));
        clearSecurityContext();
    }

    @Test
    void createCommunity_missingTopics_returnsFormWithErrors() throws Exception {
        // Arrange
        when(communityService.getAvailableTopics()).thenReturn(List.of(topic((short) 1, "classics")));
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/communities")
                .param("name", "Classics")
                .param("description", "Pre-1990 cars and honest restoration projects."));

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("community-create.jsp"))
                .andExpect(model().attributeHasFieldErrors("communityForm", "selectedTopicIds"))
                .andExpect(model().attributeExists("communityTopics"));
        clearSecurityContext();
    }

    @Test
    void createCommunity_blankName_returnsFormWithErrors() throws Exception {
        // Arrange
        when(communityService.getAvailableTopics()).thenReturn(List.of(topic((short) 1, "classics")));
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/communities")
                .param("name", "   ")
                .param("description", "Pre-1990 cars and honest restoration projects.")
                .param("selectedTopicIds", "1"));

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("community-create.jsp"))
                .andExpect(model().attributeHasFieldErrors("communityForm", "name"))
                .andExpect(model().attributeExists("communityTopics"));
        clearSecurityContext();
    }

    @Test
    void createCommunity_slugConflict_returnsFormWithGlobalError() throws Exception {
        // Arrange
        when(communityService.getAvailableTopics()).thenReturn(List.of(topic((short) 1, "classics")));
        when(communityService.createCommunity(anyLong(), anyString(), anyString(), anyCollection()))
                .thenThrow(new DataIntegrityViolationException("uq_communities_slug"));
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/communities")
                .param("name", "Classics")
                .param("description", "Pre-1990 cars and honest restoration projects.")
                .param("selectedTopicIds", "1"));

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("community-create.jsp"))
                .andExpect(model().attributeHasErrors("communityForm"))
                .andExpect(result -> assertGlobalErrorCode(
                        result,
                        "communityForm",
                        "communities.create.error.slugConflict"))
                .andExpect(model().attributeExists("communityTopics"));
        clearSecurityContext();
    }

    @Test
    void createCommunity_unrelatedIntegrityViolation_rethrowsException() {
        // Arrange
        final CommunityForm form = communityForm();
        final BindingResult errors = new BeanPropertyBindingResult(form, "communityForm");
        final DataIntegrityViolationException exception =
                new DataIntegrityViolationException("uq_community_members_user_id_community_id");
        when(communityService.createCommunity(anyLong(), anyString(), anyString(), anyCollection()))
                .thenThrow(exception);

        // Exercise
        final DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class,
                () -> controller.createCommunity(form, errors, new ExtendedModelMap(), testUser(7L))
        );

        // Assertions
        assertSame(exception, thrown);
    }

    @Test
    void joinCommunity_authenticatedUser_redirectsToDetail() throws Exception {
        // Arrange
        when(communityService.toggleMembership("classics", 7L)).thenReturn(Optional.of(true));
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/communities/classics/join")
        );

        // Assertions
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/communities/classics"));
        clearSecurityContext();
    }

    @Test
    void kickCommunityMemberGet_redirectsToMembersPage() throws Exception {
        // Arrange
        final String communitySlug = "classics";
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/communities/classics/members/6/kick"));

        // Assertions
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/communities/" + communitySlug + "/members"));
    }

    @Test
    void communityMembers_marksCurrentUserRow() throws Exception {
        // Arrange
        when(communityService.getCommunityMembers("classics", 7L))
                .thenReturn(Optional.of(communityMembersData()));
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/communities/classics/members"));

        // Assertions
        final MvcResult mvcResult = resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("community-members.jsp"))
                .andReturn();
        final List<?> rows = (List<?>) mvcResult.getModelAndView().getModel().get("memberRows");
        assertTrue(rows.stream().anyMatch(row -> ((CommunityController.MemberRowView) row).getCurrentUser()));
        clearSecurityContext();
    }

    @Test
    void kickCommunityMember_selfRedirectsToMembersPage() throws Exception {
        // Arrange
        final String communitySlug = "classics";
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/communities/classics/members/7/kick")
        );

        // Assertions
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/communities/" + communitySlug + "/members"));
        clearSecurityContext();
    }

    @Test
    void submitCommunityPost_validForm_redirectsToPostDetail() throws Exception {
        // Arrange
        final CommunityPost createdPost = post();
        createdPost.setSlug("first-post");
        createdPost.setTitle("First post");
        createdPost.setBody("This is the first real community post.");
        when(communityService.getCommunityBySlug("classics")).thenReturn(Optional.of(community()));
        when(communityService.createCommunityPost("classics", 7L, "First post", "This is the first real community post.", List.of()))
                .thenReturn(Optional.of(createdPost));
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(
                multipart("/communities/classics/posts")
                        .param("title", "First post")
                        .param("body", "This is the first real community post.")
        );

        // Assertions
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/communities/classics/posts/first-post"));
        clearSecurityContext();
    }

    @Test
    void submitCommunityPost_blankTitle_returnsFormWithErrors() throws Exception {
        // Arrange
        when(communityService.getCommunityBySlug("classics")).thenReturn(Optional.of(community()));
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(
                multipart("/communities/classics/posts")
                        .param("title", "   ")
                        .param("body", "This is the first real community post.")
        );

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("community-post-form.jsp"))
                .andExpect(model().attributeHasFieldErrors("communityPostForm", "title"))
                .andExpect(model().attributeExists("community"));
        clearSecurityContext();
    }

    @Test
    void submitCommunityPost_slugConflict_returnsFormWithGlobalError() throws Exception {
        // Arrange
        when(communityService.getCommunityBySlug("classics")).thenReturn(Optional.of(community()));
        when(communityService.createCommunityPost(
                "classics",
                7L,
                "First post",
                "This is the first real community post.",
                List.of()
        )).thenThrow(new DataIntegrityViolationException("uq_community_posts_slug"));
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(
                multipart("/communities/classics/posts")
                        .param("title", "First post")
                        .param("body", "This is the first real community post.")
        );

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("community-post-form.jsp"))
                .andExpect(model().attributeHasErrors("communityPostForm"))
                .andExpect(result -> assertGlobalErrorCode(
                        result,
                        "communityPostForm",
                        "communities.postForm.error.slugConflict"))
                .andExpect(model().attributeExists("community"));
        clearSecurityContext();
    }

    @Test
    void submitCommunityPost_unrelatedIntegrityViolation_rethrowsException() {
        // Arrange
        final CommunityPostForm form = communityPostForm();
        final BindingResult errors = new BeanPropertyBindingResult(form, "communityPostForm");
        final DataIntegrityViolationException exception =
                new DataIntegrityViolationException("fk_community_post_images_post_id");
        when(communityService.getCommunityBySlug("classics")).thenReturn(Optional.of(community()));
        when(communityService.createCommunityPost(
                anyString(),
                anyLong(),
                anyString(),
                anyString(),
                any()
        )).thenThrow(exception);

        // Exercise
        final DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class,
                () -> controller.submitCommunityPost("classics", form, errors, new ExtendedModelMap(), testUser(7L))
        );

        // Assertions
        assertSame(exception, thrown);
    }

    @Test
    void submitCommunityPost_tooManyImages_returnsFormWithErrors() throws Exception {
        // Arrange
        when(communityService.getCommunityBySlug("classics")).thenReturn(Optional.of(community()));
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();
        final MockMultipartFile imageOne = new MockMultipartFile("files", "one.png", "image/png", new byte[]{1});
        final MockMultipartFile imageTwo = new MockMultipartFile("files", "two.png", "image/png", new byte[]{2});
        final MockMultipartFile imageThree = new MockMultipartFile("files", "three.png", "image/png", new byte[]{3});
        final MockMultipartFile imageFour = new MockMultipartFile("files", "four.png", "image/png", new byte[]{4});

        // Exercise
        final ResultActions resultActions = mockMvc.perform(
                multipart("/communities/classics/posts")
                        .file(imageOne)
                        .file(imageTwo)
                        .file(imageThree)
                        .file(imageFour)
                        .param("title", "First post")
                        .param("body", "This is the first real community post.")
        );

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("community-post-form.jsp"))
                .andExpect(model().attributeHasFieldErrors("communityPostForm", "files"));
        clearSecurityContext();
    }

    @Test
    void getCommunityPostImage_existingImage_returnsBytes() throws Exception {
        // Arrange
        final CommunityPostDetailData detailData = communityPostDetailData();
        final CommunityPostImage image = new CommunityPostImage();
        image.setImageId(15L);
        image.setContentType("image/png");
        image.setImageData(new byte[]{1, 2, 3});
        image.setUpdatedAt(LocalDateTime.of(2026, 6, 1, 12, 0));
        when(communityService.getCommunityPostDetail("classics", "falcon-60", null))
                .thenReturn(Optional.of(detailData));
        when(communityService.getPostImageById(1L, 15L)).thenReturn(Optional.of(image));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(get("/communities/classics/posts/falcon-60/images/15"));

        // Assertions
        resultActions
                .andExpect(status().isOk());
    }

    @Test
    void togglePostHelpful_authenticatedUser_redirectsToPostDetail() throws Exception {
        // Arrange
        when(communityService.togglePostHelpfulReaction("classics", "falcon-60", 7L)).thenReturn(Optional.of(true));
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/communities/classics/posts/falcon-60/helpful")
        );

        // Assertions
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/communities/classics/posts/falcon-60"));
        clearSecurityContext();
    }

    @Test
    void toggleCommentHelpful_authenticatedUser_redirectsToPostDetail() throws Exception {
        // Arrange
        when(communityService.toggleCommentHelpfulReaction("classics", "falcon-60", 10L, 7L))
                .thenReturn(Optional.of(true));
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                        "/communities/classics/posts/falcon-60/comments/10/helpful"
                )
        );

        // Assertions
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/communities/classics/posts/falcon-60"));
        clearSecurityContext();
    }

    @Test
    void createCommunityPostComment_validBody_redirectsToPostDetail() throws Exception {
        // Arrange
        when(communityService.getCommunityPostDetail("classics", "falcon-60", 7L))
                .thenReturn(Optional.of(communityPostDetailData()));
        when(communityService.createCommunityPostComment("classics", "falcon-60", 7L, "This deserves more photos."))
                .thenReturn(Optional.of(comment()));
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/communities/classics/posts/falcon-60/comments")
                        .param("body", "This deserves more photos.")
        );

        // Assertions
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/communities/classics/posts/falcon-60"));
        clearSecurityContext();
    }

    @Test
    void createCommunityPostComment_blankBody_returnsPostDetailWithError() throws Exception {
        // Arrange
        when(communityService.getCommunityPostDetail("classics", "falcon-60", 7L))
                .thenReturn(Optional.of(communityPostDetailData()));
        when(relativeTimeFormatter.format(any(LocalDateTime.class))).thenReturn("2 hours ago");
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/communities/classics/posts/falcon-60/comments")
                        .param("body", "   ")
        );

        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("community-post-detail.jsp"))
                .andExpect(model().attributeHasFieldErrors("communityPostCommentForm", "body"));
        clearSecurityContext();
    }

    @Test
    void createCommunityPostComment_nonMember_redirectsToPostDetail() throws Exception {
        // Arrange
        when(communityService.getCommunityPostDetail("classics", "falcon-60", 7L))
                .thenReturn(Optional.of(communityPostDetailData()));
        when(communityService.createCommunityPostComment("classics", "falcon-60", 7L, "This deserves more photos."))
                .thenThrow(new CommunityMembershipRequiredException("classics"));
        bindPrincipal(testUser(7L));
        final MockMvc mockMvc = communityMockMvc();

        // Exercise
        final ResultActions resultActions = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/communities/classics/posts/falcon-60/comments")
                        .param("body", "This deserves more photos.")
        );

        // Assertions
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/communities/classics/posts/falcon-60"));
        clearSecurityContext();
    }

    private static CommunityHubEntry communityHubEntry() {
        return new CommunityHubEntry(
                community(),
                List.of(topic((short) 1, "classics")),
                12L,
                3L,
                true
        );
    }

    private static CommunityDetailData communityDetailData() {
        return new CommunityDetailData(
                community(),
                List.of(topic((short) 1, "classics")),
                List.of(new CommunityPostSummary(post(), 4L, 2L)),
                12L,
                3L,
                true,
                "member",
                "recent",
                false
        );
    }

    private static CommunityPostDetailData communityPostDetailData() {
        return new CommunityPostDetailData(
                community(),
                post(),
                List.of(comment()),
                4L,
                true,
                1L,
                java.util.Map.of(10L, 2L),
                java.util.Map.of(10L, true),
                "member",
                7L
        );
    }

    private static CommunityMembersData communityMembersData() {
        return new CommunityMembersData(
                community(),
                List.of(
                        new CommunityMembershipEntry(7L, "mateo.classics", "moderator", LocalDateTime.now(), false),
                        new CommunityMembershipEntry(8L, "lu.driver", "member", LocalDateTime.now(), false)
                ),
                "moderator",
                false
        );
    }

    private static Community community() {
        final Community community = new Community();
        community.setId(1L);
        community.setSlug("classics");
        community.setName("Classics");
        community.setDescription("Pre-1990 cars and honest restoration projects.");
        community.setCreatedAt(LocalDateTime.now().minusDays(10));
        return community;
    }

    private static CommunityTopic topic(final short id, final String code) {
        final CommunityTopic topic = new CommunityTopic();
        topic.setId(id);
        topic.setCode(code);
        topic.setCreatedAt(LocalDateTime.now().minusDays(10));
        return topic;
    }

    private static CommunityPost post() {
        final CommunityPost post = new CommunityPost();
        post.setId(1L);
        post.setCommunity(community());
        post.setAuthor(author(7L, "mateo.classics"));
        post.setSlug("falcon-60");
        post.setTitle("My grandfather's Falcon turned 60 today");
        post.setBody("Still runs beautifully and keeps every original detail intact.");
        post.setCreatedAt(LocalDateTime.now().minusHours(2));
        post.setUpdatedAt(LocalDateTime.now().minusHours(2));
        return post;
    }

    private static CommunityPostComment comment() {
        final CommunityPostComment comment = new CommunityPostComment();
        comment.setId(10L);
        comment.setPost(post());
        comment.setUser(author(8L, "lu.driver"));
        comment.setBody("That paint looks original.");
        comment.setCreatedAt(LocalDateTime.now().minusHours(1));
        comment.setUpdatedAt(LocalDateTime.now().minusHours(1));
        return comment;
    }

    private static User author(final long id, final String username) {
        final User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("secret");
        user.setRole("user");
        user.setPreferredLocale("es");
        user.setCreatedAt(LocalDateTime.now().minusDays(100));
        return user;
    }

    private static CommunityForm communityForm() {
        final CommunityForm form = new CommunityForm();
        form.setName("Classics");
        form.setDescription("Pre-1990 cars and honest restoration projects.");
        form.setSelectedTopicIds(Set.of((short) 1));
        return form;
    }

    private static CommunityPostForm communityPostForm() {
        final CommunityPostForm form = new CommunityPostForm();
        form.setTitle("First post");
        form.setBody("This is the first real community post.");
        return form;
    }

    private static AuthenticatedUser testUser(final long id) {
        return new AuthenticatedUser(
                id,
                "user" + id,
                "user" + id + "@example.com",
                "secret",
                "es",
                List.of()
        );
    }
}
