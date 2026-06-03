package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityMembershipEntry;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.CommunitySearchCriteria;
import ar.edu.itba.paw.model.CommunityTopic;
import ar.edu.itba.paw.model.User;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommunityDaoTest extends AbstractPersistenceTest {

    @Autowired
    private CommunityDao communityDao;

    @Test
    void shouldFindCommunityBySlugIgnoringCase() {
        // Arrange
        final User creator = insertUser("classics-owner", "classics-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("classics", "Classics", "Pre-1990 cars only.", creator.getId());

        // Exercise
        final Optional<Community> result = communityDao.findBySlug("CLASSICS");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(communityId, result.get().getId());
        assertEquals("Classics", result.get().getName());
        assertEquals("Pre-1990 cars only.", result.get().getDescription());
    }

    @Test
    void shouldFindTopicsByCommunityIds() {
        // Arrange
        final User creator = insertUser("topics-owner", "topics-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("off-road", "Off-road", "Mud and gravel.", creator.getId());
        final short topicId = insertTopic("offroad");
        assignTopic(communityId, topicId);

        // Exercise
        final Map<Long, List<CommunityTopic>> result = communityDao.findTopicsByCommunityIds(List.of(communityId));

        // Assertions
        assertEquals(1, result.size());
        assertEquals(1, result.get(communityId).size());
        assertEquals("offroad", result.get(communityId).get(0).getCode());
    }

    @Test
    void shouldFindCommunitiesPaginated() {
        // Arrange
        final User creator = insertUser("paged-communities-owner", "paged-communities-owner@example.com", "secret", "user");
        for (int i = 1; i <= 13; i++) {
            insertCommunity("paged-community-" + i, "Community " + i, "Description " + i, creator.getId());
        }

        // Exercise
        final Page<Community> result = communityDao.findAll(2);

        // Assertions
        assertEquals(2, result.getPageNumber());
        assertEquals(2, result.getTotalPages());
        assertEquals(1, result.getItems().size());
        assertEquals("paged-community-13", result.getItems().get(0).getSlug());
    }

    @Test
    void shouldFindCommunitiesBySearchCriteria() {
        // Arrange
        final User creator = insertUser("criteria-owner", "criteria-owner@example.com", "secret", "user");
        final User member = insertUser("criteria-member", "criteria-member@example.com", "secret", "user");
        final long matchingCommunityId = insertCommunity("x-track", "X Track", "Track day setup.", creator.getId());
        final long otherCommunityId = insertCommunity("daily", "Daily Drivers", "Commute setups.", creator.getId());
        final short motorsportTopicId = insertTopic("motorsport");
        final short classicsTopicId = insertTopic("classics");
        assignTopic(matchingCommunityId, motorsportTopicId);
        assignTopic(otherCommunityId, classicsTopicId);
        insertCommunityMembership(matchingCommunityId, member.getId(), "member");
        final CommunitySearchCriteria criteria = new CommunitySearchCriteria();
        criteria.setQ("x");
        criteria.setTopic("motorsport");
        criteria.setJoinedOnly(true);
        criteria.setSortBy("name_asc");
        criteria.setPage(1);

        // Exercise
        final Page<Community> result = communityDao.findByCriteria(criteria, member.getId());

        // Assertions
        assertEquals(1, result.getItems().size());
        assertEquals(matchingCommunityId, result.getItems().get(0).getId());
        assertEquals(1L, result.getTotalItems());
    }

    @Test
    void shouldCreateCommunity() {
        // Arrange
        final User creator = insertUser("creator-owner", "creator-owner@example.com", "secret", "user");

        // Exercise
        final Community result = communityDao.create(
                creator.getId(),
                "classics-2",
                "Classics",
                "Pre-1990 cars only."
        );

        // Assertions
        assertEquals("classics-2", result.getSlug());
        assertEquals("Classics", result.getName());
        assertEquals(
                1,
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM communities WHERE community_id = ? AND slug = ? AND created_by_user_id = ?",
                        Integer.class,
                        result.getId(),
                        "classics-2",
                        creator.getId()
                )
        );
    }

    @Test
    void shouldReplaceTopicAssignments() {
        // Arrange
        final User creator = insertUser("assignment-owner", "assignment-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("classics", "Classics", "Pre-1990 cars only.", creator.getId());
        final short firstTopicId = insertTopic("classics");
        final short secondTopicId = insertTopic("repairs");

        // Exercise
        communityDao.replaceTopicAssignments(communityId, List.of(firstTopicId, secondTopicId));

        // Assertions
        assertEquals(
                2,
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM community_topic_assignments WHERE community_id = ?",
                        Integer.class,
                        communityId
                )
        );
        assertEquals(
                1,
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM community_topic_assignments WHERE community_id = ? AND topic_id = ?",
                        Integer.class,
                        communityId,
                        firstTopicId
                )
        );
        assertEquals(
                1,
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM community_topic_assignments WHERE community_id = ? AND topic_id = ?",
                        Integer.class,
                        communityId,
                        secondTopicId
                )
        );
    }

    @Test
    void shouldCreateCommunityMembership() {
        // Arrange
        final User creator = insertUser("membership-owner", "membership-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("classics", "Classics", "Pre-1990 cars only.", creator.getId());

        // Exercise
        communityDao.createMembership(communityId, creator.getId(), "moderator");

        // Assertions
        assertEquals(
                1,
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM community_memberships WHERE community_id = ? AND user_id = ? AND role = ?",
                        Integer.class,
                        communityId,
                        creator.getId(),
                        "moderator"
                )
        );
    }

    @Test
    void shouldCreateCommunityPost() {
        // Arrange
        final User creator = insertUser("post-create-owner", "post-create-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("classics", "Classics", "Pre-1990 cars only.", creator.getId());

        // Exercise
        final CommunityPost result = communityDao.createPost(
                communityId,
                creator.getId(),
                "first-post",
                "First post",
                "This is the first real community post."
        );

        // Assertions
        assertEquals("first-post", result.getSlug());
        assertEquals(
                1,
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM community_posts WHERE post_id = ? AND community_id = ? AND author_user_id = ? AND slug = ?",
                        Integer.class,
                        result.getId(),
                        communityId,
                        creator.getId(),
                        "first-post"
                )
        );
    }

    @Test
    void shouldCreateCommunityComment() {
        // Arrange
        final User creator = insertUser("comment-create-owner", "comment-create-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("classics", "Classics", "Pre-1990 cars only.", creator.getId());
        final long postId = insertCommunityPost(
                communityId,
                creator.getId(),
                "first-post",
                "First post",
                "This is the first real community post.",
                LocalDateTime.now().minusHours(2)
        );

        // Exercise
        final ar.edu.itba.paw.model.CommunityPostComment result = communityDao.createComment(
                postId,
                creator.getId(),
                "This deserves more photos."
        );

        // Assertions
        assertEquals("This deserves more photos.", result.getBody());
        assertEquals(
                1,
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM community_post_comments WHERE comment_id = ? AND post_id = ? AND user_id = ?",
                        Integer.class,
                        result.getId(),
                        postId,
                        creator.getId()
                )
        );
    }

    @Test
    void shouldUpdateCommunityPost() {
        // Arrange
        final User creator = insertUser("post-update-owner", "post-update-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("post-update-c", "Post update", "Desc", creator.getId());
        final long postId = insertCommunityPost(
                communityId,
                creator.getId(),
                "first-post",
                "Old title",
                "Old body",
                LocalDateTime.now().minusHours(2)
        );

        // Exercise
        communityDao.updatePost(postId, "New title", "New body");

        // Assertions
        flushAndClear();
        final Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT title, body FROM community_posts WHERE post_id = ?",
                postId
        );
        assertEquals("New title", row.get("title"));
        assertEquals("New body", row.get("body"));
    }

    @Test
    void shouldUpdateCommunityComment() {
        // Arrange
        final User creator = insertUser("comment-update-owner", "comment-update-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("comment-update-c", "Comment update", "Desc", creator.getId());
        final long postId = insertCommunityPost(
                communityId,
                creator.getId(),
                "first-post",
                "First post",
                "Post body",
                LocalDateTime.now().minusHours(2)
        );
        final long commentId = insertCommunityComment(
                postId,
                creator.getId(),
                "Old comment",
                LocalDateTime.now().minusHours(1)
        );

        // Exercise
        communityDao.updateComment(commentId, "New comment");

        // Assertions
        flushAndClear();
        assertEquals(
                "New comment",
                jdbcTemplate.queryForObject(
                        "SELECT body FROM community_post_comments WHERE comment_id = ?",
                        String.class,
                        commentId
                )
        );
    }

    @Test
    void shouldDeleteCommunityMembership() {
        // Arrange
        final User creator = insertUser("membership-remove-owner", "membership-remove-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("classics", "Classics", "Pre-1990 cars only.", creator.getId());
        insertCommunityMembership(communityId, creator.getId(), "member");

        // Exercise
        communityDao.deleteMembership(communityId, creator.getId());

        // Assertions
        assertEquals(
                0,
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM community_memberships WHERE community_id = ? AND user_id = ?",
                        Integer.class,
                        communityId,
                        creator.getId()
                )
        );
    }

    @Test
    void shouldFindCommunityPostWithAuthor() {
        // Arrange
        final User creator = insertUser("post-owner", "post-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("classics", "Classics", "Pre-1990 cars only.", creator.getId());
        final long postId = insertCommunityPost(
                communityId,
                creator.getId(),
                "falcon-60",
                "My grandfather's Falcon turned 60 today",
                "Still runs beautifully.",
                LocalDateTime.now().minusHours(2)
        );

        // Exercise
        final Optional<CommunityPost> result = communityDao.findPostByCommunityIdAndSlug(communityId, "FALCON-60");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(postId, result.get().getId());
        assertEquals("post-owner", result.get().getAuthorUsername());
        assertEquals("My grandfather's Falcon turned 60 today", result.get().getTitle());
    }

    @Test
    void shouldFindCommunityPostsByIdsWithAuthorAndCommunity() {
        // Arrange
        final User creator = insertUser("post-ids-owner", "post-ids-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("jdm-heads", "JDM Heads", "Boost noises.", creator.getId());
        final long firstPostId = insertCommunityPost(
                communityId,
                creator.getId(),
                "first-post",
                "First post",
                "Body 1",
                LocalDateTime.now().minusHours(2)
        );
        final long secondPostId = insertCommunityPost(
                communityId,
                creator.getId(),
                "second-post",
                "Second post",
                "Body 2",
                LocalDateTime.now().minusHours(1)
        );

        // Exercise
        final List<CommunityPost> result = communityDao.findPostsByIds(List.of(firstPostId, secondPostId));

        // Assertions
        assertEquals(2, result.size());
        assertEquals("post-ids-owner", result.get(0).getAuthorUsername());
        assertEquals("jdm-heads", result.get(0).getCommunity().getSlug());
        assertEquals("JDM Heads", result.get(0).getCommunity().getName());
    }

    @Test
    void shouldFindVisibleCommunityPostsPaginatedByRecent() {
        // Arrange
        final User creator = insertUser("paged-posts-owner", "paged-posts-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("paged-posts", "Paged posts", "Posts by page.", creator.getId());
        final LocalDateTime baseTime = LocalDateTime.now();
        for (int i = 1; i <= 7; i++) {
            insertCommunityPost(
                    communityId,
                    creator.getId(),
                    "post-" + i,
                    "Post " + i,
                    "Body " + i,
                    baseTime.minusMinutes(i)
            );
        }
        final long hiddenPostId = insertCommunityPost(
                communityId,
                creator.getId(),
                "hidden-post",
                "Hidden post",
                "Hidden body",
                baseTime.plusMinutes(1)
        );
        jdbcTemplate.update("UPDATE community_posts SET hidden = TRUE WHERE post_id = ?", hiddenPostId);

        // Exercise
        final Page<CommunityPost> result = communityDao.findVisiblePostsByCommunityId(communityId, "recent", 2);

        // Assertions
        assertEquals(2, result.getPageNumber());
        assertEquals(2, result.getTotalPages());
        assertEquals(1, result.getItems().size());
        assertEquals("post-7", result.getItems().get(0).getSlug());
    }

    @Test
    void shouldFindVisibleCommunityPostsOrderedByHelpful() {
        // Arrange
        final User creator = insertUser("helpful-page-owner", "helpful-page-owner@example.com", "secret", "user");
        final User helper = insertUser("helpful-page-user", "helpful-page-user@example.com", "secret", "user");
        final long communityId = insertCommunity("helpful-page", "Helpful page", "Helpful posts.", creator.getId());
        final long lowPostId = insertCommunityPost(
                communityId,
                creator.getId(),
                "low-helpful",
                "Low helpful",
                "Body",
                LocalDateTime.now().minusHours(2)
        );
        final long highPostId = insertCommunityPost(
                communityId,
                creator.getId(),
                "high-helpful",
                "High helpful",
                "Body",
                LocalDateTime.now().minusHours(3)
        );
        insertHelpfulReaction(highPostId, helper.getId());

        // Exercise
        final Page<CommunityPost> result = communityDao.findVisiblePostsByCommunityId(communityId, "helpful", 1);

        // Assertions
        assertEquals(1, result.getPageNumber());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getItems().size());
        assertEquals(highPostId, result.getItems().get(0).getId());
        assertEquals(lowPostId, result.getItems().get(1).getId());
    }

    @Test
    void shouldCountCommentsByPostIds() {
        // Arrange
        final User creator = insertUser("comments-owner", "comments-owner@example.com", "secret", "user");
        final User commenter = insertUser("comments-user", "comments-user@example.com", "secret", "user");
        final long communityId = insertCommunity("classics", "Classics", "Pre-1990 cars only.", creator.getId());
        final long postId = insertCommunityPost(
                communityId,
                creator.getId(),
                "falcon-60",
                "My grandfather's Falcon turned 60 today",
                "Still runs beautifully.",
                LocalDateTime.now().minusHours(2)
        );
        insertCommunityComment(postId, commenter.getId(), "That paint looks original.", LocalDateTime.now().minusHours(1));

        // Exercise
        final Map<Long, Long> result = communityDao.countCommentsByPostIds(List.of(postId));

        // Assertions
        assertEquals(1, result.size());
        assertEquals(1L, result.get(postId));
    }

    @Test
    void shouldReturnOnlyVisibleCommentsByPostId() {
        // Arrange
        final User creator = insertUser("visible-comments-owner", "visible-comments-owner@example.com", "secret", "user");
        final User commenter = insertUser("visible-comments-user", "visible-comments-user@example.com", "secret", "user");
        final long communityId = insertCommunity("visible-comments", "Visible Comments", "Desc", creator.getId());
        final long postId = insertCommunityPost(
                communityId,
                creator.getId(),
                "visible-post",
                "Visible post",
                "Visible body",
                LocalDateTime.now().minusHours(2)
        );
        final long visibleCommentId = insertCommunityComment(
                postId,
                commenter.getId(),
                "Shown comment",
                LocalDateTime.now().minusHours(1)
        );
        final long hiddenCommentId = insertCommunityComment(
                postId,
                commenter.getId(),
                "Hidden comment",
                LocalDateTime.now().minusMinutes(30)
        );
        jdbcTemplate.update("UPDATE community_post_comments SET hidden = TRUE WHERE comment_id = ?", hiddenCommentId);

        // Exercise
        final List<ar.edu.itba.paw.model.CommunityPostComment> result = communityDao.findCommentsByPostId(postId);

        // Assertions
        assertEquals(1, result.size());
        assertEquals(visibleCommentId, result.get(0).getId());
    }

    @Test
    void shouldCountHelpfulReactionsByPostIds() {
        // Arrange
        final User creator = insertUser("helpful-owner", "helpful-owner@example.com", "secret", "user");
        final User helper = insertUser("helpful-user", "helpful-user@example.com", "secret", "user");
        final long communityId = insertCommunity("classics", "Classics", "Pre-1990 cars only.", creator.getId());
        final long postId = insertCommunityPost(
                communityId,
                creator.getId(),
                "falcon-60",
                "My grandfather's Falcon turned 60 today",
                "Still runs beautifully.",
                LocalDateTime.now().minusHours(2)
        );
        insertHelpfulReaction(postId, helper.getId());

        // Exercise
        final Map<Long, Long> result = communityDao.countHelpfulReactionsByPostIds(List.of(postId));

        // Assertions
        assertEquals(1, result.size());
        assertEquals(1L, result.get(postId));
    }

    @Test
    void shouldAddAndRemoveHelpfulReaction() {
        // Arrange
        final User creator = insertUser("helpful-toggle-owner", "helpful-toggle-owner@example.com", "secret", "user");
        final User helper = insertUser("helpful-toggle-user", "helpful-toggle-user@example.com", "secret", "user");
        final long communityId = insertCommunity("classics", "Classics", "Pre-1990 cars only.", creator.getId());
        final long postId = insertCommunityPost(
                communityId,
                creator.getId(),
                "falcon-60",
                "My grandfather's Falcon turned 60 today",
                "Still runs beautifully.",
                LocalDateTime.now().minusHours(2)
        );

        // Exercise
        final boolean added = communityDao.addHelpfulReaction(postId, helper.getId());

        // Assertions
        assertTrue(added);
        assertEquals(
                1,
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM community_post_helpful_reactions WHERE post_id = ? AND user_id = ?",
                        Integer.class,
                        postId,
                        helper.getId()
                )
        );
        assertTrue(communityDao.isHelpfulReactionAddedByUser(postId, helper.getId()));

        // Arrange
        final boolean removed = communityDao.removeHelpfulReaction(postId, helper.getId());

        // Assertions
        assertTrue(removed);
        assertEquals(
                0,
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM community_post_helpful_reactions WHERE post_id = ? AND user_id = ?",
                        Integer.class,
                        postId,
                        helper.getId()
                )
        );
    }

    @Test
    void shouldCountHelpfulReactionsByCommentIds() {
        // Arrange
        final User creator = insertUser("comment-helpful-owner", "comment-helpful-owner@example.com", "secret", "user");
        final User helper = insertUser("comment-helpful-user", "comment-helpful-user@example.com", "secret", "user");
        final long communityId = insertCommunity("comment-helpful", "Comment helpful", "Comment helpful posts.", creator.getId());
        final long postId = insertCommunityPost(
                communityId,
                creator.getId(),
                "falcon-60",
                "My grandfather's Falcon turned 60 today",
                "Still runs beautifully.",
                LocalDateTime.now().minusHours(2)
        );
        final long commentId = insertCommunityComment(
                postId,
                creator.getId(),
                "That paint looks original.",
                LocalDateTime.now().minusHours(1)
        );
        insertCommentHelpfulReaction(commentId, helper.getId());

        // Exercise
        final Map<Long, Long> result = communityDao.countHelpfulReactionsByCommentIds(List.of(commentId));

        // Assertions
        assertEquals(1, result.size());
        assertEquals(1L, result.get(commentId));
    }

    @Test
    void shouldAddAndRemoveCommentHelpfulReaction() {
        // Arrange
        final User creator = insertUser("comment-toggle-owner", "comment-toggle-owner@example.com", "secret", "user");
        final User helper = insertUser("comment-toggle-user", "comment-toggle-user@example.com", "secret", "user");
        final long communityId = insertCommunity("comment-toggle", "Comment toggle", "Comment toggle posts.", creator.getId());
        final long postId = insertCommunityPost(
                communityId,
                creator.getId(),
                "falcon-60",
                "My grandfather's Falcon turned 60 today",
                "Still runs beautifully.",
                LocalDateTime.now().minusHours(2)
        );
        final long commentId = insertCommunityComment(
                postId,
                creator.getId(),
                "That paint looks original.",
                LocalDateTime.now().minusHours(1)
        );

        // Exercise
        final boolean added = communityDao.addCommentHelpfulReaction(commentId, helper.getId());

        // Assertions
        assertTrue(added);
        assertEquals(
                1,
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM community_post_comment_helpful_reactions WHERE comment_id = ? AND user_id = ?",
                        Integer.class,
                        commentId,
                        helper.getId()
                )
        );
        assertTrue(communityDao.isCommentHelpfulReactionAddedByUser(commentId, helper.getId()));

        // Arrange
        final boolean removed = communityDao.removeCommentHelpfulReaction(commentId, helper.getId());

        // Assertions
        assertTrue(removed);
        assertEquals(
                0,
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM community_post_comment_helpful_reactions WHERE comment_id = ? AND user_id = ?",
                        Integer.class,
                        commentId,
                        helper.getId()
                )
        );
    }

    @Test
    void shouldFindCommentHelpfulReactionsByUser() {
        // Arrange
        final User creator = insertUser("comment-reaction-owner", "comment-reaction-owner@example.com", "secret", "user");
        final User helper = insertUser("comment-reaction-user", "comment-reaction-user@example.com", "secret", "user");
        final User other = insertUser("comment-reaction-other", "comment-reaction-other@example.com", "secret", "user");
        final long communityId = insertCommunity("comment-reactions", "Comment reactions", "Comment reaction posts.", creator.getId());
        final long postId = insertCommunityPost(
                communityId,
                creator.getId(),
                "falcon-60",
                "My grandfather's Falcon turned 60 today",
                "Still runs beautifully.",
                LocalDateTime.now().minusHours(2)
        );
        final long firstCommentId = insertCommunityComment(
                postId,
                creator.getId(),
                "That paint looks original.",
                LocalDateTime.now().minusHours(1)
        );
        final long secondCommentId = insertCommunityComment(
                postId,
                other.getId(),
                "The interior is perfect.",
                LocalDateTime.now().minusMinutes(30)
        );
        insertCommentHelpfulReaction(firstCommentId, helper.getId());
        insertCommentHelpfulReaction(secondCommentId, other.getId());

        // Exercise
        final Set<Long> result = communityDao.findCommentHelpfulReactionsByUser(
                List.of(firstCommentId, secondCommentId),
                helper.getId()
        );

        // Assertions
        assertEquals(1, result.size());
        assertTrue(result.contains(firstCommentId));
    }

    @Test
    void shouldFindMembershipRole() {
        // Arrange
        final User creator = insertUser("role-owner", "role-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("role-c", "Role", "Desc", creator.getId());
        insertCommunityMembership(communityId, creator.getId(), "moderator");

        // Exercise
        final Optional<String> result = communityDao.findMembershipRole(communityId, creator.getId());

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("moderator", result.get());
    }

    @Test
    void shouldUpdateMembershipRole() {
        // Arrange
        final User creator = insertUser("promote-owner", "promote-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("promote-c", "Promote", "Desc", creator.getId());
        insertCommunityMembership(communityId, creator.getId(), "member");

        // Exercise
        communityDao.updateMembershipRole(communityId, creator.getId(), "moderator");

        // Assertions
        assertEquals(
                "moderator",
                jdbcTemplate.queryForObject(
                        "SELECT role FROM community_memberships WHERE community_id = ? AND user_id = ?",
                        String.class,
                        communityId,
                        creator.getId()
                )
        );
    }

    @Test
    void shouldListMembersWithRoleAndUsername() {
        // Arrange
        final User creator = insertUser("list-owner", "list-owner@example.com", "secret", "user");
        final User other = insertUser("list-other", "list-other@example.com", "secret", "user");
        final long communityId = insertCommunity("list-c", "List", "Desc", creator.getId());
        insertCommunityMembership(communityId, creator.getId(), "moderator");
        insertCommunityMembership(communityId, other.getId(), "member");

        // Exercise
        final List<CommunityMembershipEntry> result = communityDao.listMembers(communityId);

        // Assertions
        assertEquals(2, result.size());
        assertEquals("moderator", result.get(0).getRole());
        assertEquals("list-owner", result.get(0).getUsername());
        assertEquals("member", result.get(1).getRole());
        assertEquals("list-other", result.get(1).getUsername());
    }

    @Test
    void shouldSetPostHidden() {
        // Arrange
        final User creator = insertUser("hide-post-owner", "hide-post-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("hide-c", "Hide", "Desc", creator.getId());
        final long postId = insertCommunityPost(
                communityId,
                creator.getId(),
                "p1",
                "Title",
                "Body",
                LocalDateTime.now().minusHours(1)
        );

        // Exercise
        communityDao.setPostHidden(postId, true);

        // Assertions
        flushAndClear();
        assertEquals(
                Boolean.TRUE,
                jdbcTemplate.queryForObject(
                        "SELECT hidden FROM community_posts WHERE post_id = ?",
                        Boolean.class,
                        postId
                )
        );
    }

    @Test
    void shouldSetCommentHidden() {
        // Arrange
        final User creator = insertUser("hide-comment-owner", "hide-comment-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("hide-c-c", "HideC", "Desc", creator.getId());
        final long postId = insertCommunityPost(
                communityId,
                creator.getId(),
                "p1",
                "Title",
                "Body",
                LocalDateTime.now().minusHours(1)
        );
        final long commentId = insertCommunityComment(postId, creator.getId(), "body", LocalDateTime.now().minusMinutes(30));

        // Exercise
        communityDao.setCommentHidden(commentId, true);

        // Assertions
        flushAndClear();
        assertEquals(
                Boolean.TRUE,
                jdbcTemplate.queryForObject(
                        "SELECT hidden FROM community_post_comments WHERE comment_id = ?",
                        Boolean.class,
                        commentId
                )
        );
    }

    @Test
    void shouldUpdateCommunityDetails() {
        // Arrange
        final User creator = insertUser("edit-owner", "edit-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("edit-c", "Old name", "Old description.", creator.getId());

        // Exercise
        communityDao.updateDetails(communityId, "New name", "New description.");

        // Assertions
        flushAndClear();
        final Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT name, description FROM communities WHERE community_id = ?",
                communityId
        );
        assertEquals("New name", row.get("name"));
        assertEquals("New description.", row.get("description"));
    }

    @Test
    void shouldDeleteCommunityAndCascadeChildren() {
        // Arrange
        final User creator = insertUser("delete-owner", "delete-owner@example.com", "secret", "user");
        final long communityId = insertCommunity("delete-c", "Doomed", "Soon gone.", creator.getId());
        insertCommunityMembership(communityId, creator.getId(), "moderator");
        final long postId = insertCommunityPost(
                communityId, creator.getId(), "p1", "Title", "Body", LocalDateTime.now().minusHours(1));
        insertCommunityComment(postId, creator.getId(), "a comment", LocalDateTime.now().minusMinutes(30));

        // Exercise
        final boolean deleted = communityDao.delete(communityId);

        // Assertions
        flushAndClear();
        assertTrue(deleted);
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM communities WHERE community_id = ?", Integer.class, communityId));
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM community_memberships WHERE community_id = ?", Integer.class, communityId));
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM community_posts WHERE community_id = ?", Integer.class, communityId));
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM community_post_comments WHERE post_id = ?", Integer.class, postId));
    }

    private long insertCommunity(final String slug, final String name, final String description, final long createdByUserId) {
        jdbcTemplate.update(
                "INSERT INTO communities (slug, name, description, created_by_user_id) VALUES (?, ?, ?, ?)",
                slug, name, description, createdByUserId
        );
        return jdbcTemplate.queryForObject(
                "SELECT community_id FROM communities WHERE slug = ?",
                Long.class,
                slug
        );
    }

    private short insertTopic(final String code) {
        jdbcTemplate.update("INSERT INTO community_topics (code) VALUES (?)", code);
        return jdbcTemplate.queryForObject(
                "SELECT topic_id FROM community_topics WHERE code = ?",
                Short.class,
                code
        );
    }

    private void assignTopic(final long communityId, final short topicId) {
        jdbcTemplate.update(
                "INSERT INTO community_topic_assignments (community_id, topic_id) VALUES (?, ?)",
                communityId,
                topicId
        );
    }

    private void insertCommunityMembership(final long communityId, final long userId, final String role) {
        jdbcTemplate.update(
                "INSERT INTO community_memberships (community_id, user_id, role) VALUES (?, ?, ?)",
                communityId,
                userId,
                role
        );
    }

    private long insertCommunityPost(final long communityId, final long authorUserId, final String slug,
                                     final String title, final String body,
                                     final LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO community_posts (community_id, author_user_id, slug, title, body, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                communityId,
                authorUserId,
                slug,
                title,
                body,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt)
        );
        return jdbcTemplate.queryForObject(
                "SELECT post_id FROM community_posts WHERE community_id = ? AND slug = ?",
                Long.class,
                communityId,
                slug
        );
    }

    private long insertCommunityComment(final long postId, final long userId, final String body, final LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO community_post_comments (post_id, user_id, body, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                postId,
                userId,
                body,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt)
        );
        return jdbcTemplate.queryForObject(
                "SELECT comment_id FROM community_post_comments WHERE post_id = ? AND user_id = ? AND body = ?",
                Long.class,
                postId,
                userId,
                body
        );
    }

    private void insertHelpfulReaction(final long postId, final long userId) {
        jdbcTemplate.update(
                "INSERT INTO community_post_helpful_reactions (post_id, user_id) VALUES (?, ?)",
                postId,
                userId
        );
    }

    private void insertCommentHelpfulReaction(final long commentId, final long userId) {
        jdbcTemplate.update(
                "INSERT INTO community_post_comment_helpful_reactions (comment_id, user_id) VALUES (?, ?)",
                commentId,
                userId
        );
    }
}
