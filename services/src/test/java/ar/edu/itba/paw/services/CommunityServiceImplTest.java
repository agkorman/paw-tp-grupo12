package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityDetailData;
import ar.edu.itba.paw.model.CommunityHubEntry;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.CommunityPostComment;
import ar.edu.itba.paw.model.CommunityPostDetailData;
import ar.edu.itba.paw.model.CommunityTopic;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.CommunityDao;
import ar.edu.itba.paw.services.exception.InvalidCommunityTopicSelectionException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityServiceImplTest {

    @Mock
    private CommunityDao communityDao;

    @InjectMocks
    private CommunityServiceImpl communityService;

    @Test
    void getCommunityHub_returnsAggregatedEntries() {
        // Arrange
        final Community community = community();
        when(communityDao.findAll()).thenReturn(List.of(community));
        when(communityDao.findTopicsByCommunityIds(anyCollection()))
                .thenReturn(Map.of(community.getId(), List.of(topic((short) 1, "classics"))));
        when(communityDao.countMembersByCommunityIds(anyCollection()))
                .thenReturn(Map.of(community.getId(), 12L));
        when(communityDao.countWeeklyPostsByCommunityIds(anyCollection(), any()))
                .thenReturn(Map.of(community.getId(), 3L));
        when(communityDao.findJoinedCommunityIds(anyLong(), anyCollection()))
                .thenReturn(Set.of(community.getId()));

        // Exercise
        final List<CommunityHubEntry> result = communityService.getCommunityHub(7L);

        // Assertions
        assertEquals(1, result.size());
        assertEquals("classics", result.get(0).getCommunity().getSlug());
        assertEquals(12L, result.get(0).getMemberCount());
        assertEquals(3L, result.get(0).getWeeklyPostCount());
        assertTrue(result.get(0).isJoined());
        assertEquals(1, result.get(0).getTopics().size());
    }

    @Test
    void getCommunityDetail_returnsAggregatedCommunityData() {
        // Arrange
        final Community community = community();
        final CommunityPost post = post(community, author(7L, "mateo.classics"));
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findPostsByCommunityId(community.getId())).thenReturn(List.of(post));
        when(communityDao.countCommentsByPostIds(anyCollection())).thenReturn(Map.of(post.getId(), 2L));
        when(communityDao.countHelpfulReactionsByPostIds(anyCollection())).thenReturn(Map.of(post.getId(), 4L));
        when(communityDao.findTopicsByCommunityIds(anyCollection()))
                .thenReturn(Map.of(community.getId(), List.of(topic((short) 1, "classics"))));
        when(communityDao.countMembersByCommunityIds(anyCollection()))
                .thenReturn(Map.of(community.getId(), 12L));
        when(communityDao.countWeeklyPostsByCommunityIds(anyCollection(), any()))
                .thenReturn(Map.of(community.getId(), 3L));
        when(communityDao.findJoinedCommunityIds(anyLong(), anyCollection()))
                .thenReturn(Set.of(community.getId()));

        // Exercise
        final Optional<CommunityDetailData> result = communityService.getCommunityDetail("classics", 7L);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("Classics", result.get().getCommunity().getName());
        assertEquals(1, result.get().getTopics().size());
        assertEquals(1, result.get().getPosts().size());
        assertEquals(4L, result.get().getPosts().get(0).getHelpfulCount());
        assertEquals(2L, result.get().getPosts().get(0).getCommentCount());
        assertTrue(result.get().isJoined());
    }

    @Test
    void getCommunityPostDetail_returnsPostAndTopLevelComments() {
        // Arrange
        final Community community = community();
        final User postAuthor = author(7L, "mateo.classics");
        final CommunityPost post = post(community, postAuthor);
        final CommunityPostComment comment = comment(post, author(8L, "lu.driver"));
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findPostByCommunityIdAndSlug(community.getId(), "falcon-60"))
                .thenReturn(Optional.of(post));
        when(communityDao.findCommentsByPostId(post.getId())).thenReturn(List.of(comment));
        when(communityDao.countHelpfulReactionsByPostIds(anyCollection())).thenReturn(Map.of(post.getId(), 4L));
        when(communityDao.isHelpfulReactionAddedByUser(post.getId(), USER_ID)).thenReturn(true);
        when(communityDao.countCommentsByPostIds(anyCollection())).thenReturn(Map.of(post.getId(), 1L));

        // Exercise
        final Optional<CommunityPostDetailData> result =
                communityService.getCommunityPostDetail("classics", "falcon-60", USER_ID);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("falcon-60", result.get().getPost().getSlug());
        assertEquals(4L, result.get().getHelpfulCount());
        assertTrue(result.get().isHelpfulByCurrentUser());
        assertEquals(1L, result.get().getCommentCount());
        assertEquals(1, result.get().getComments().size());
        assertEquals("That paint looks original.", result.get().getComments().get(0).getBody());
    }

    @Test
    void getCommunityBySlug_missingCommunity_returnsEmpty() {
        // Arrange
        when(communityDao.findBySlug(anyString())).thenReturn(Optional.empty());

        // Exercise
        final Optional<Community> result = communityService.getCommunityBySlug("missing");

        // Assertions
        assertFalse(result.isPresent());
    }

    @Test
    void createCommunity_generatesNextAvailableSlug() {
        // Arrange
        final Community existingCommunity = community();
        final CommunityTopic selectedTopic = topic((short) 1, "classics");
        final Community createdCommunity = community();
        createdCommunity.setId(9L);
        createdCommunity.setSlug("classics-2");
        when(communityDao.findTopicsByIds(anyCollection())).thenReturn(List.of(selectedTopic));
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(existingCommunity));
        when(communityDao.findBySlug("classics-2")).thenReturn(Optional.empty());
        when(communityDao.create(USER_ID, "classics-2", "Classics", "Pre-1990 cars and honest restoration projects."))
                .thenReturn(createdCommunity);

        // Exercise
        final Community result = communityService.createCommunity(
                USER_ID,
                "Classics",
                "Pre-1990 cars and honest restoration projects.",
                List.of((short) 1)
        );

        // Assertions
        assertEquals(9L, result.getId());
        assertEquals("classics-2", result.getSlug());
        assertEquals("Classics", result.getName());
    }

    @Test
    void createCommunity_withoutTopics_rejectsSelection() {
        // Arrange
        final Collection<Short> topicIds = List.of();

        // Exercise
        final InvalidCommunityTopicSelectionException exception = assertThrows(
                InvalidCommunityTopicSelectionException.class,
                () -> communityService.createCommunity(USER_ID, "Classics", "Desc", topicIds)
        );

        // Assertions
        assertEquals(InvalidCommunityTopicSelectionException.Reason.REQUIRED, exception.getReason());
    }

    @Test
    void createCommunity_withUnknownTopic_rejectsSelection() {
        // Arrange
        when(communityDao.findTopicsByIds(anyCollection())).thenReturn(List.of());

        // Exercise
        final InvalidCommunityTopicSelectionException exception = assertThrows(
                InvalidCommunityTopicSelectionException.class,
                () -> communityService.createCommunity(USER_ID, "Classics", "Desc", List.of((short) 99))
        );

        // Assertions
        assertEquals(InvalidCommunityTopicSelectionException.Reason.UNKNOWN_TOPIC, exception.getReason());
    }

    @Test
    void createCommunityPost_generatesNextAvailableSlug() {
        // Arrange
        final Community community = community();
        final CommunityPost existingPost = post(community, author(USER_ID, "mateo.classics"));
        final CommunityPost createdPost = post(community, author(USER_ID, "mateo.classics"));
        createdPost.setId(8L);
        createdPost.setSlug("first-post-2");
        createdPost.setTitle("First post");
        createdPost.setBody("Body");
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findPostByCommunityIdAndSlug(community.getId(), "first-post")).thenReturn(Optional.of(existingPost));
        when(communityDao.findPostByCommunityIdAndSlug(community.getId(), "first-post-2")).thenReturn(Optional.empty());
        when(communityDao.createPost(community.getId(), USER_ID, "first-post-2", "First post", "Body"))
                .thenReturn(createdPost);

        // Exercise
        final Optional<CommunityPost> result = communityService.createCommunityPost("classics", USER_ID, "First post", "Body");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("first-post-2", result.get().getSlug());
    }

    @Test
    void createCommunityPost_missingCommunity_returnsEmpty() {
        // Arrange
        when(communityDao.findBySlug("missing")).thenReturn(Optional.empty());

        // Exercise
        final Optional<CommunityPost> result = communityService.createCommunityPost("missing", USER_ID, "First post", "Body");

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void createCommunityPostComment_existingPost_returnsCreatedComment() {
        // Arrange
        final Community community = community();
        final User postAuthor = author(USER_ID, "mateo.classics");
        final CommunityPost post = post(community, postAuthor);
        final CommunityPostComment createdComment = comment(post, author(8L, "lu.driver"));
        createdComment.setBody("This deserves more photos.");
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findPostByCommunityIdAndSlug(community.getId(), "falcon-60")).thenReturn(Optional.of(post));
        when(communityDao.createComment(post.getId(), USER_ID, "This deserves more photos.")).thenReturn(createdComment);

        // Exercise
        final Optional<CommunityPostComment> result =
                communityService.createCommunityPostComment("classics", "falcon-60", USER_ID, "This deserves more photos.");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("This deserves more photos.", result.get().getBody());
    }

    @Test
    void createCommunityPostComment_missingPost_returnsEmpty() {
        // Arrange
        final Community community = community();
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findPostByCommunityIdAndSlug(community.getId(), "missing")).thenReturn(Optional.empty());

        // Exercise
        final Optional<CommunityPostComment> result =
                communityService.createCommunityPostComment("classics", "missing", USER_ID, "This deserves more photos.");

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void toggleMembership_missingCommunity_returnsEmpty() {
        // Arrange
        when(communityDao.findBySlug("missing")).thenReturn(Optional.empty());

        // Exercise
        final Optional<Boolean> result = communityService.toggleMembership("missing", USER_ID);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void toggleMembership_whenNotJoined_returnsJoinedState() {
        // Arrange
        final Community community = community();
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findJoinedCommunityIds(USER_ID, List.of(community.getId()))).thenReturn(Set.of());

        // Exercise
        final Optional<Boolean> result = communityService.toggleMembership("classics", USER_ID);

        // Assertions
        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    void toggleMembership_whenJoined_returnsLeftState() {
        // Arrange
        final Community community = community();
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findJoinedCommunityIds(USER_ID, List.of(community.getId()))).thenReturn(Set.of(community.getId()));

        // Exercise
        final Optional<Boolean> result = communityService.toggleMembership("classics", USER_ID);

        // Assertions
        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    void togglePostHelpfulReaction_whenAlreadyHelpful_returnsRemovedState() {
        // Arrange
        final Community community = community();
        final CommunityPost post = post(community, author(USER_ID, "mateo.classics"));
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findPostByCommunityIdAndSlug(community.getId(), "falcon-60")).thenReturn(Optional.of(post));
        when(communityDao.isHelpfulReactionAddedByUser(post.getId(), USER_ID)).thenReturn(true);

        // Exercise
        final Optional<Boolean> result = communityService.togglePostHelpfulReaction("classics", "falcon-60", USER_ID);

        // Assertions
        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    void togglePostHelpfulReaction_whenNotHelpful_returnsAddedState() {
        // Arrange
        final Community community = community();
        final CommunityPost post = post(community, author(USER_ID, "mateo.classics"));
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findPostByCommunityIdAndSlug(community.getId(), "falcon-60")).thenReturn(Optional.of(post));
        when(communityDao.isHelpfulReactionAddedByUser(post.getId(), USER_ID)).thenReturn(false);

        // Exercise
        final Optional<Boolean> result = communityService.togglePostHelpfulReaction("classics", "falcon-60", USER_ID);

        // Assertions
        assertTrue(result.isPresent());
        assertTrue(result.get());
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

    private static final long USER_ID = 7L;

    private static CommunityTopic topic(final short id, final String code) {
        final CommunityTopic topic = new CommunityTopic();
        topic.setId(id);
        topic.setCode(code);
        topic.setCreatedAt(LocalDateTime.now().minusDays(20));
        return topic;
    }

    private static CommunityPost post(final Community community, final User author) {
        final CommunityPost post = new CommunityPost();
        post.setId(3L);
        post.setCommunity(community);
        post.setAuthor(author);
        post.setSlug("falcon-60");
        post.setTitle("My grandfather's Falcon turned 60 today");
        post.setBody("Still runs beautifully.");
        post.setCreatedAt(LocalDateTime.now().minusHours(2));
        post.setUpdatedAt(LocalDateTime.now().minusHours(2));
        return post;
    }

    private static CommunityPostComment comment(final CommunityPost post, final User author) {
        final CommunityPostComment comment = new CommunityPostComment();
        comment.setId(5L);
        comment.setPost(post);
        comment.setUser(author);
        comment.setBody("That paint looks original.");
        comment.setCreatedAt(LocalDateTime.now().minusHours(1));
        comment.setUpdatedAt(LocalDateTime.now().minusHours(1));
        return comment;
    }

    private static User author(final long id, final String username) {
        return TestModels.user(
                id,
                username,
                username + "@example.com",
                "secret",
                "user",
                LocalDateTime.now().minusDays(100)
        );
    }
}
