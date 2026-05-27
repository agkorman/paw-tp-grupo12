package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityDetailData;
import ar.edu.itba.paw.model.CommunityHubEntry;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.CommunityPostComment;
import ar.edu.itba.paw.model.CommunityPostDetailData;
import ar.edu.itba.paw.model.CommunityTopic;
import ar.edu.itba.paw.model.EmailRecipient;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.CommunityDao;
import ar.edu.itba.paw.services.exception.CannotModerateCreatorException;
import ar.edu.itba.paw.services.exception.CommunityContentOwnershipException;
import ar.edu.itba.paw.services.exception.CommunityCreatorCannotLeaveException;
import ar.edu.itba.paw.services.exception.CommunityMembershipRequiredException;
import ar.edu.itba.paw.services.exception.CommunityModeratorRequiredException;
import ar.edu.itba.paw.services.exception.CommunityOwnerRequiredException;
import ar.edu.itba.paw.services.exception.InvalidCommunityTopicSelectionException;
import ar.edu.itba.paw.services.exception.InvalidServiceInputException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CommunityServiceImpl communityService;

    @Test
    void getCommunityHub_returnsAggregatedEntries() {
        // Arrange
        final Community community = community();
        when(communityDao.findByCriteria(any(), anyLong())).thenReturn(new Page<>(List.of(community), 1, 12, 1L));
        when(communityDao.findTopicsByCommunityIds(anyCollection()))
                .thenReturn(Map.of(community.getId(), List.of(topic((short) 1, "classics"))));
        when(communityDao.countMembersByCommunityIds(anyCollection()))
                .thenReturn(Map.of(community.getId(), 12L));
        when(communityDao.countWeeklyPostsByCommunityIds(anyCollection(), any()))
                .thenReturn(Map.of(community.getId(), 3L));
        when(communityDao.findJoinedCommunityIds(anyLong(), anyCollection()))
                .thenReturn(Set.of(community.getId()));

        // Exercise
        final Page<CommunityHubEntry> result = communityService.getCommunityHub(7L, 1);

        // Assertions
        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getTotalPages());
        assertEquals("classics", result.getItems().get(0).getCommunity().getSlug());
        assertEquals(12L, result.getItems().get(0).getMemberCount());
        assertEquals(3L, result.getItems().get(0).getWeeklyPostCount());
        assertTrue(result.getItems().get(0).isJoined());
        assertEquals(1, result.getItems().get(0).getTopics().size());
    }

    @Test
    void getCommunityDetail_returnsAggregatedCommunityData() {
        // Arrange
        final Community community = community();
        final CommunityPost post = post(community, author(7L, "mateo.classics"));
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findVisiblePostsByCommunityId(community.getId(), "recent", 1))
                .thenReturn(new Page<>(List.of(post), 1, 6, 1L));
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
        final Optional<CommunityDetailData> result = communityService.getCommunityDetail("classics", 7L, null);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("Classics", result.get().getCommunity().getName());
        assertEquals(1, result.get().getTopics().size());
        assertEquals(1, result.get().getPosts().size());
        assertEquals(1, result.get().getPostsPage().getTotalPages());
        assertEquals(4L, result.get().getPosts().get(0).getHelpfulCount());
        assertEquals(2L, result.get().getPosts().get(0).getCommentCount());
        assertTrue(result.get().isJoined());
        assertEquals("recent", result.get().getCurrentSort());
    }

    @Test
    void getCommunityDetail_sortByHelpful_ordersByHelpfulCountDescending() {
        // Arrange
        final Community community = community();
        final CommunityPost lowVotes = post(community, author(7L, "mateo.classics"));
        lowVotes.setId(10L);
        lowVotes.setSlug("low");
        final CommunityPost highVotes = post(community, author(7L, "mateo.classics"));
        highVotes.setId(11L);
        highVotes.setSlug("high");
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findVisiblePostsByCommunityId(community.getId(), "helpful", 1))
                .thenReturn(new Page<>(List.of(highVotes, lowVotes), 1, 6, 2L));
        when(communityDao.countCommentsByPostIds(anyCollection())).thenReturn(Map.of());
        when(communityDao.countHelpfulReactionsByPostIds(anyCollection()))
                .thenReturn(Map.of(lowVotes.getId(), 1L, highVotes.getId(), 9L));
        when(communityDao.findTopicsByCommunityIds(anyCollection())).thenReturn(Map.of());
        when(communityDao.countMembersByCommunityIds(anyCollection())).thenReturn(Map.of());
        when(communityDao.countWeeklyPostsByCommunityIds(anyCollection(), any())).thenReturn(Map.of());

        // Exercise
        final Optional<CommunityDetailData> result = communityService.getCommunityDetail("classics", null, "helpful");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("helpful", result.get().getCurrentSort());
        assertEquals(11L, result.get().getPosts().get(0).getPost().getId());
        assertEquals(10L, result.get().getPosts().get(1).getPost().getId());
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
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("member"));
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
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("member"));
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
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("member"));
        when(communityDao.findPostByCommunityIdAndSlug(community.getId(), "missing")).thenReturn(Optional.empty());

        // Exercise
        final Optional<CommunityPostComment> result =
                communityService.createCommunityPostComment("classics", "missing", USER_ID, "This deserves more photos.");

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void updateCommunityPost_byAuthor_updatesAndReturnsPost() {
        // Arrange
        final Community community = community();
        final CommunityPost post = post(community, author(USER_ID, "mateo.classics"));
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findPostByCommunityIdAndSlug(community.getId(), "falcon-60")).thenReturn(Optional.of(post));

        // Exercise
        final Optional<CommunityPost> result =
                communityService.updateCommunityPost("classics", "falcon-60", USER_ID, "Updated title", "Updated body");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("Updated title", result.get().getTitle());
        assertEquals("Updated body", result.get().getBody());
    }

    @Test
    void updateCommunityPost_byNonAuthor_throws() {
        // Arrange
        final Community community = community();
        final CommunityPost post = post(community, author(42L, "someone"));
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findPostByCommunityIdAndSlug(community.getId(), "falcon-60")).thenReturn(Optional.of(post));

        // Exercise
        final CommunityContentOwnershipException exception = assertThrows(
                CommunityContentOwnershipException.class,
                () -> communityService.updateCommunityPost("classics", "falcon-60", USER_ID, "Updated title", "Updated body")
        );

        // Assertions
        assertTrue(exception.getMessage().contains("author"));
    }

    @Test
    void updateCommunityPostComment_byAuthor_updatesAndReturnsComment() {
        // Arrange
        final Community community = community();
        final CommunityPost post = post(community, author(99L, "op"));
        final CommunityPostComment comment = comment(post, author(USER_ID, "me"));
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findCommentById(comment.getId())).thenReturn(Optional.of(comment));

        // Exercise
        final Optional<CommunityPostComment> result =
                communityService.updateCommunityPostComment("classics", comment.getId(), USER_ID, "Updated comment");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("Updated comment", result.get().getBody());
    }

    @Test
    void updateCommunityPostComment_byNonAuthor_throws() {
        // Arrange
        final Community community = community();
        final CommunityPost post = post(community, author(99L, "op"));
        final CommunityPostComment comment = comment(post, author(42L, "someone"));
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findCommentById(comment.getId())).thenReturn(Optional.of(comment));

        // Exercise
        final CommunityContentOwnershipException exception = assertThrows(
                CommunityContentOwnershipException.class,
                () -> communityService.updateCommunityPostComment("classics", comment.getId(), USER_ID, "Updated comment")
        );

        // Assertions
        assertTrue(exception.getMessage().contains("author"));
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
    void toggleMembership_creatorCannotLeave_throws() {
        // Arrange
        final long creatorId = 7L;
        final Community community = communityWithCreator(creatorId);
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findJoinedCommunityIds(creatorId, List.of(community.getId())))
                .thenReturn(Set.of(community.getId()));

        // Exercise
        final CommunityCreatorCannotLeaveException exception = assertThrows(
                CommunityCreatorCannotLeaveException.class,
                () -> communityService.toggleMembership("classics", creatorId)
        );

        // Assertions
        assertEquals("classics", exception.getCommunitySlug());
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

    @Test
    void createCommunityPost_whenNotMember_throws() {
        // Arrange
        final Community community = community();
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.empty());

        // Exercise
        final CommunityMembershipRequiredException exception = assertThrows(
                CommunityMembershipRequiredException.class,
                () -> communityService.createCommunityPost("classics", USER_ID, "Title", "Body")
        );

        // Assertions
        assertEquals("classics", exception.getCommunitySlug());
    }

    @Test
    void createCommunityPostComment_whenNotMember_throws() {
        // Arrange
        final Community community = community();
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.empty());

        // Exercise
        final CommunityMembershipRequiredException exception = assertThrows(
                CommunityMembershipRequiredException.class,
                () -> communityService.createCommunityPostComment("classics", "falcon-60", USER_ID, "body")
        );

        // Assertions
        assertEquals("classics", exception.getCommunitySlug());
    }

    @Test
    void kickMember_byNonModerator_throws() {
        // Arrange
        final Community community = communityWithCreator(99L);
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("member"));

        // Exercise
        final CommunityModeratorRequiredException exception = assertThrows(
                CommunityModeratorRequiredException.class,
                () -> communityService.kickMember("classics", 42L, USER_ID)
        );

        // Assertions
        assertEquals("classics", exception.getCommunitySlug());
    }

    @Test
    void kickMember_targetIsCreator_throws() {
        // Arrange
        final long creatorId = 99L;
        final Community community = communityWithCreator(creatorId);
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("moderator"));

        // Exercise
        final CannotModerateCreatorException exception = assertThrows(
                CannotModerateCreatorException.class,
                () -> communityService.kickMember("classics", creatorId, USER_ID)
        );

        // Assertions
        assertEquals("classics", exception.getCommunitySlug());
    }

    @Test
    void kickMember_byModerator_kicksTarget() {
        // Arrange
        final Community community = communityWithCreator(99L);
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("moderator"));
        when(communityDao.findMembershipRole(community.getId(), 42L)).thenReturn(Optional.of("member"));

        // Exercise
        final Optional<Boolean> result = communityService.kickMember("classics", 42L, USER_ID);

        // Assertions
        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    void kickMember_byModerator_sendsEmailToTarget() {
        // Arrange
        final RecordingEmailService recordingEmailService = new RecordingEmailService();
        final CommunityServiceImpl service = new CommunityServiceImpl(communityDao, userService, recordingEmailService);
        final Community community = communityWithCreator(99L);
        final User target = author(42L, "target");
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("moderator"));
        when(communityDao.findMembershipRole(community.getId(), target.getId())).thenReturn(Optional.of("member"));
        when(userService.getUserById(target.getId())).thenReturn(Optional.of(target));

        // Exercise
        final Optional<Boolean> result = service.kickMember("classics", target.getId(), USER_ID);

        // Assertions
        assertTrue(result.isPresent());
        assertTrue(result.get());
        assertEquals(1, recordingEmailService.communityKickedEmails.size());
        assertEquals(target.getEmail(), recordingEmailService.communityKickedEmails.get(0));
    }

    @Test
    void promoteToModerator_byModerator_promotesMember() {
        // Arrange
        final Community community = communityWithCreator(99L);
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("moderator"));
        when(communityDao.findMembershipRole(community.getId(), 42L)).thenReturn(Optional.of("member"));

        // Exercise
        final Optional<Boolean> result = communityService.promoteToModerator("classics", 42L, USER_ID);

        // Assertions
        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    void promoteToModerator_byModerator_sendsEmailToTarget() {
        // Arrange
        final RecordingEmailService recordingEmailService = new RecordingEmailService();
        final CommunityServiceImpl service = new CommunityServiceImpl(communityDao, userService, recordingEmailService);
        final Community community = communityWithCreator(99L);
        final User target = author(42L, "target");
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("moderator"));
        when(communityDao.findMembershipRole(community.getId(), target.getId())).thenReturn(Optional.of("member"));
        when(userService.getUserById(target.getId())).thenReturn(Optional.of(target));

        // Exercise
        final Optional<Boolean> result = service.promoteToModerator("classics", target.getId(), USER_ID);

        // Assertions
        assertTrue(result.isPresent());
        assertTrue(result.get());
        assertEquals(1, recordingEmailService.communityPromotedEmails.size());
        assertEquals(target.getEmail(), recordingEmailService.communityPromotedEmails.get(0));
    }

    @Test
    void hidePost_byNonModerator_throws() {
        // Arrange
        final Community community = communityWithCreator(99L);
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("member"));

        // Exercise
        final CommunityModeratorRequiredException exception = assertThrows(
                CommunityModeratorRequiredException.class,
                () -> communityService.hidePost("classics", "falcon-60", USER_ID, "Duplicated content.")
        );

        // Assertions
        assertEquals("classics", exception.getCommunitySlug());
    }

    @Test
    void hidePost_byModerator_setsHiddenAndReturnsState() {
        // Arrange
        final Community community = communityWithCreator(99L);
        final CommunityPost post = post(community, author(8L, "lu.driver"));
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("moderator"));
        when(communityDao.findPostByCommunityIdAndSlug(community.getId(), "falcon-60")).thenReturn(Optional.of(post));

        // Exercise
        final Optional<Boolean> result = communityService.hidePost("classics", "falcon-60", USER_ID, "Duplicated content.");

        // Assertions
        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    void hidePost_byModerator_sendsEmailToPostAuthor() {
        // Arrange
        final RecordingEmailService recordingEmailService = new RecordingEmailService();
        final CommunityServiceImpl service = new CommunityServiceImpl(communityDao, userService, recordingEmailService);
        final Community community = communityWithCreator(99L);
        final User postAuthor = author(8L, "lu.driver");
        final CommunityPost post = post(community, postAuthor);
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("moderator"));
        when(communityDao.findPostByCommunityIdAndSlug(community.getId(), "falcon-60")).thenReturn(Optional.of(post));
        when(userService.getUserById(postAuthor.getId())).thenReturn(Optional.of(postAuthor));

        // Exercise
        final Optional<Boolean> result = service.hidePost("classics", "falcon-60", USER_ID, "Duplicated content.");

        // Assertions
        assertTrue(result.isPresent());
        assertTrue(result.get());
        assertEquals(1, recordingEmailService.communityPostHiddenEmails.size());
        assertEquals(postAuthor.getEmail(), recordingEmailService.communityPostHiddenEmails.get(0));
    }

    @Test
    void hideComment_byModerator_sendsEmailToCommentAuthor() {
        // Arrange
        final RecordingEmailService recordingEmailService = new RecordingEmailService();
        final CommunityServiceImpl service = new CommunityServiceImpl(communityDao, userService, recordingEmailService);
        final Community community = communityWithCreator(99L);
        final CommunityPost post = post(community, author(8L, "lu.driver"));
        final User commentAuthor = author(10L, "commenter");
        final CommunityPostComment comment = comment(post, commentAuthor);
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("moderator"));
        when(communityDao.findCommentById(comment.getId())).thenReturn(Optional.of(comment));
        when(userService.getUserById(commentAuthor.getId())).thenReturn(Optional.of(commentAuthor));

        // Exercise
        final Optional<Boolean> result = service.hideComment("classics", comment.getId(), USER_ID, "Off-topic comment.");

        // Assertions
        assertTrue(result.isPresent());
        assertTrue(result.get());
        assertEquals(1, recordingEmailService.communityCommentHiddenEmails.size());
        assertEquals(commentAuthor.getEmail(), recordingEmailService.communityCommentHiddenEmails.get(0));
    }

    @Test
    void deleteComment_byAuthor_deletes() {
        // Arrange
        final Community community = community();
        final CommunityPost post = post(community, author(99L, "op"));
        final CommunityPostComment comment = comment(post, author(USER_ID, "me"));
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findCommentById(comment.getId())).thenReturn(Optional.of(comment));
        when(communityDao.deleteComment(comment.getId())).thenReturn(true);

        // Exercise
        final Optional<Boolean> result = communityService.deleteComment("classics", comment.getId(), USER_ID);

        // Assertions
        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    void deleteComment_byNonAuthor_throws() {
        // Arrange
        final Community community = community();
        final CommunityPost post = post(community, author(99L, "op"));
        final CommunityPostComment comment = comment(post, author(42L, "someone"));
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findCommentById(comment.getId())).thenReturn(Optional.of(comment));

        // Exercise
        final CommunityContentOwnershipException exception = assertThrows(
                CommunityContentOwnershipException.class,
                () -> communityService.deleteComment("classics", comment.getId(), USER_ID)
        );

        // Assertions
        assertTrue(exception.getMessage().contains("author"));
    }

    @Test
    void deletePost_byNonAuthor_throws() {
        // Arrange
        final Community community = community();
        final CommunityPost post = post(community, author(42L, "someone"));
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findPostByCommunityIdAndSlug(community.getId(), "falcon-60")).thenReturn(Optional.of(post));

        // Exercise
        final CommunityContentOwnershipException exception = assertThrows(
                CommunityContentOwnershipException.class,
                () -> communityService.deletePost("classics", "falcon-60", USER_ID)
        );

        // Assertions
        assertTrue(exception.getMessage().contains("author"));
    }

    @Test
    void deletePost_byAuthor_deletes() {
        // Arrange
        final Community community = community();
        final CommunityPost post = post(community, author(USER_ID, "me"));
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findPostByCommunityIdAndSlug(community.getId(), "falcon-60")).thenReturn(Optional.of(post));
        when(communityDao.deletePost(post.getId())).thenReturn(true);

        // Exercise
        final Optional<Boolean> result = communityService.deletePost("classics", "falcon-60", USER_ID);

        // Assertions
        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    void editCommunity_byNonModerator_throws() {
        // Arrange
        final Community community = community();
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("member"));

        // Exercise
        final CommunityModeratorRequiredException exception = assertThrows(
                CommunityModeratorRequiredException.class,
                () -> communityService.editCommunity("classics", USER_ID, "New name", "New desc", List.of((short) 1))
        );

        // Assertions
        assertEquals("classics", exception.getCommunitySlug());
    }

    @Test
    void editCommunity_byModerator_updatesAndReturnsCommunity() {
        // Arrange
        final Community community = community();
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("moderator"));
        when(communityDao.findTopicsByIds(anyCollection())).thenReturn(List.of(topic((short) 1, "classics")));

        // Exercise
        final Optional<Community> result =
                communityService.editCommunity("classics", USER_ID, "Updated", "Updated desc", List.of((short) 1));

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(community.getId(), result.get().getId());
    }

    @Test
    void deleteCommunity_byNonModerator_throws() {
        // Arrange
        final Community community = community();
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("member"));

        // Exercise
        final CommunityModeratorRequiredException exception = assertThrows(
                CommunityModeratorRequiredException.class,
                () -> communityService.deleteCommunity("classics", USER_ID)
        );

        // Assertions
        assertEquals("classics", exception.getCommunitySlug());
    }

    @Test
    void deleteCommunity_byNonOwnerModerator_throws() {
        // Arrange
        final Community community = communityWithCreator(99L);
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("moderator"));

        // Exercise
        final CommunityOwnerRequiredException exception = assertThrows(
                CommunityOwnerRequiredException.class,
                () -> communityService.deleteCommunity("classics", USER_ID)
        );

        // Assertions
        assertEquals("classics", exception.getCommunitySlug());
    }

    @Test
    void deleteCommunity_byOwner_deletes() {
        // Arrange
        final Community community = communityWithCreator(USER_ID);
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), USER_ID)).thenReturn(Optional.of("moderator"));
        when(communityDao.delete(community.getId())).thenReturn(true);

        // Exercise
        final Optional<Boolean> result = communityService.deleteCommunity("classics", USER_ID);

        // Assertions
        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    void transferOwnership_byNonOwner_throws() {
        // Arrange
        final Community community = communityWithCreator(99L);
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));

        // Exercise
        final CommunityOwnerRequiredException exception = assertThrows(
                CommunityOwnerRequiredException.class,
                () -> communityService.transferOwnership("classics", 42L, USER_ID)
        );

        // Assertions
        assertEquals("classics", exception.getCommunitySlug());
    }

    @Test
    void transferOwnership_toNonModerator_throws() {
        // Arrange
        final long ownerId = 7L;
        final Community community = communityWithCreator(ownerId);
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), 42L)).thenReturn(Optional.of("member"));

        // Exercise
        final InvalidServiceInputException exception = assertThrows(
                InvalidServiceInputException.class,
                () -> communityService.transferOwnership("classics", 42L, ownerId)
        );

        // Assertions
        assertTrue(exception.getMessage().contains("moderator"));
    }

    @Test
    void transferOwnership_toModerator_succeeds() {
        // Arrange
        final long ownerId = 7L;
        final Community community = communityWithCreator(ownerId);
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), 42L)).thenReturn(Optional.of("moderator"));

        // Exercise
        final Optional<Boolean> result = communityService.transferOwnership("classics", 42L, ownerId);

        // Assertions
        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    void transferOwnership_toModerator_sendsEmailToNewOwner() {
        // Arrange
        final RecordingEmailService recordingEmailService = new RecordingEmailService();
        final CommunityServiceImpl service = new CommunityServiceImpl(communityDao, userService, recordingEmailService);
        final long ownerId = 7L;
        final User newOwner = author(42L, "new.owner");
        final Community community = communityWithCreator(ownerId);
        when(communityDao.findBySlug("classics")).thenReturn(Optional.of(community));
        when(communityDao.findMembershipRole(community.getId(), newOwner.getId())).thenReturn(Optional.of("moderator"));
        when(userService.getUserById(newOwner.getId())).thenReturn(Optional.of(newOwner));

        // Exercise
        final Optional<Boolean> result = service.transferOwnership("classics", newOwner.getId(), ownerId);

        // Assertions
        assertTrue(result.isPresent());
        assertTrue(result.get());
        assertEquals(1, recordingEmailService.communityOwnershipEmails.size());
        assertEquals(newOwner.getEmail(), recordingEmailService.communityOwnershipEmails.get(0));
    }

    private static Community communityWithCreator(final long creatorId) {
        final Community community = community();
        community.setCreatedBy(author(creatorId, "creator"));
        return community;
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

    private static final class RecordingEmailService implements EmailService {

        private final List<String> communityPostHiddenEmails = new ArrayList<>();
        private final List<String> communityCommentHiddenEmails = new ArrayList<>();
        private final List<String> communityKickedEmails = new ArrayList<>();
        private final List<String> communityPromotedEmails = new ArrayList<>();
        private final List<String> communityOwnershipEmails = new ArrayList<>();

        @Override
        public void sendNewCarRequestNotification(final ar.edu.itba.paw.model.CarRequest request,
                                                  final String brandName, final String bodyTypeName) {
        }

        @Override
        public void sendCarApprovedNotification(final String recipientEmail, final String brandName,
                                                final String model, final long carId) {
        }

        @Override
        public void sendCarRejectedNotification(final String recipientEmail, final String brandName,
                                                final String model) {
        }

        @Override
        public void sendCatalogRequestApprovedNotification(final String recipientEmail, final String requestType,
                                                           final String requestedName) {
        }

        @Override
        public void sendCatalogRequestRejectedNotification(final String recipientEmail, final String requestType,
                                                           final String requestedName) {
        }

        @Override
        public void sendAdminRequestApprovedNotification(final String recipientEmail) {
        }

        @Override
        public void sendAdminRequestRejectedNotification(final String recipientEmail) {
        }

        @Override
        public void sendReviewHiddenNotification(final String recipientEmail, final String reviewTitle,
                                                 final String carName, final String moderatorReason) {
        }

        @Override
        public void sendCommunityPostHiddenNotification(final String recipientEmail, final String communityName,
                                                        final String postTitle, final String moderatorReason,
                                                        final String postUrl) {
            communityPostHiddenEmails.add(recipientEmail);
        }

        @Override
        public void sendCommunityCommentHiddenNotification(final String recipientEmail, final String communityName,
                                                           final String postTitle, final String commentBody,
                                                           final String moderatorReason, final String postUrl) {
            communityCommentHiddenEmails.add(recipientEmail);
        }

        @Override
        public void sendCommunityMemberKickedNotification(final String recipientEmail, final String communityName,
                                                          final String communityUrl) {
            communityKickedEmails.add(recipientEmail);
        }

        @Override
        public void sendCommunityModeratorPromotedNotification(final String recipientEmail, final String communityName,
                                                              final String communityMembersUrl) {
            communityPromotedEmails.add(recipientEmail);
        }

        @Override
        public void sendCommunityOwnershipTransferredNotification(final String recipientEmail,
                                                                 final String communityName,
                                                                 final String communityMembersUrl) {
            communityOwnershipEmails.add(recipientEmail);
        }

        @Override
        public void sendWeeklyModeratorDigest(final List<EmailRecipient> moderatorRecipients,
                                              final int pendingRequestCount) {
        }

        @Override
        public void sendWeeklyUserDigest(final String recipientEmail, final String username,
                                         final List<ReviewActivityItem> reviewActivity,
                                         final List<FavoriteActivityItem> favoriteActivity) {
        }
    }
}
