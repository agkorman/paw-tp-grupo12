package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.CommunityPostComment;
import ar.edu.itba.paw.model.CommunityTopic;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;

public interface CommunityDao {
    List<Community> findAll();
    Optional<Community> findBySlug(String slug);
    List<CommunityTopic> findAllTopics();
    List<CommunityTopic> findTopicsByIds(Collection<Short> topicIds);
    Community create(long createdByUserId, String slug, String name, String description);
    CommunityPost createPost(long communityId, long authorUserId, String slug, String title, String body);
    CommunityPostComment createComment(long postId, long userId, String body);
    void replaceTopicAssignments(long communityId, Collection<Short> topicIds);
    void createMembership(long communityId, long userId, String role);
    void deleteMembership(long communityId, long userId);
    boolean addHelpfulReaction(long postId, long userId);
    boolean removeHelpfulReaction(long postId, long userId);
    boolean isHelpfulReactionAddedByUser(long postId, long userId);
    Map<Long, List<CommunityTopic>> findTopicsByCommunityIds(Collection<Long> communityIds);
    List<CommunityPost> findPostsByCommunityId(long communityId);
    Optional<CommunityPost> findPostByCommunityIdAndSlug(long communityId, String postSlug);
    List<CommunityPostComment> findCommentsByPostId(long postId);
    Map<Long, Long> countMembersByCommunityIds(Collection<Long> communityIds);
    Set<Long> findJoinedCommunityIds(long userId, Collection<Long> communityIds);
    Map<Long, Long> countWeeklyPostsByCommunityIds(Collection<Long> communityIds, LocalDateTime since);
    Map<Long, Long> countCommentsByPostIds(Collection<Long> postIds);
    Map<Long, Long> countHelpfulReactionsByPostIds(Collection<Long> postIds);
}
