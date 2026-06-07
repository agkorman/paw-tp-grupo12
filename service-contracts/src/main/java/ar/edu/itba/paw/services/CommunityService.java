package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityDetailData;
import ar.edu.itba.paw.model.CommunityEditData;
import ar.edu.itba.paw.model.CommunityHubEntry;
import ar.edu.itba.paw.model.CommunityMembersData;
import ar.edu.itba.paw.model.CommunityMembershipEntry;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.CommunityPostComment;
import ar.edu.itba.paw.model.CommunityPostDetailData;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.CommunitySearchCriteria;
import ar.edu.itba.paw.model.CommunityTopic;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.StoredImagePayload;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface CommunityService {
    List<CommunityHubEntry> getCommunityHub(Long currentUserId);
    Page<CommunityHubEntry> getCommunityHub(Long currentUserId, int page);
    Page<CommunityHubEntry> getCommunityHub(CommunitySearchCriteria criteria, Long currentUserId);
    List<CommunityTopic> getAvailableTopics();
    Community createCommunity(long userId, String name, String description, Collection<Short> topicIds);
    Optional<CommunityEditData> getCommunityForEdit(String communitySlug, long callerUserId);
    Optional<Community> editCommunity(String communitySlug, long callerUserId, String name, String description, Collection<Short> topicIds);
    Optional<Boolean> deleteCommunity(String communitySlug, long callerUserId);
    Optional<CommunityPost> createCommunityPost(String communitySlug, long userId, String title, String body,
                                                List<ImagePayload> images);
    Optional<CommunityPost> createCommunityPost(String communitySlug, long userId, String title, String body,
                                                List<ImagePayload> images, Long linkedReviewId);
    Optional<CommunityPost> getCommunityPostForEdit(String communitySlug, String postSlug, long callerUserId);
    Optional<CommunityPost> updateCommunityPost(String communitySlug, String postSlug, long callerUserId, String title,
                                                String body, List<ImagePayload> images);
    Optional<CommunityPostComment> createCommunityPostComment(String communitySlug, String postSlug, long userId, String body);
    Optional<CommunityPostComment> updateCommunityPostComment(String communitySlug, long commentId, long callerUserId, String body);
    Optional<Boolean> toggleMembership(String slug, long userId);
    Set<Long> getHelpfulPostIds(Collection<Long> postIds, long userId);
    Optional<Boolean> togglePostHelpfulReaction(String communitySlug, String postSlug, long userId);
    Optional<Boolean> toggleCommentHelpfulReaction(String communitySlug, String postSlug, long commentId, long userId);
    Optional<CommunityDetailData> getCommunityDetail(String slug, Long currentUserId, String sort);
    Optional<CommunityDetailData> getCommunityDetail(String slug, Long currentUserId, String sort, int page);
    Optional<Community> getCommunityBySlug(String slug);
    Optional<CommunityPostDetailData> getCommunityPostDetail(String communitySlug, String postSlug, Long currentUserId);
    Optional<CommunityPostDetailData> getCommunityPostDetail(String communitySlug, String postSlug, Long currentUserId,
                                                             boolean viewerAdmin);
    List<ImageMetadata> getPostImagesByPostId(long postId);
    Map<Long, List<ImageMetadata>> getImagesByPostIds(Collection<Long> postIds);
    Optional<StoredImagePayload> getPostImageById(long postId, long imageId);
    Optional<ImageMetadata> getPostImageMetadataById(long postId, long imageId);
    List<ImagePayload> collectRetainedPostImagePayloads(long postId, List<Long> retainedImageIds);
    Optional<String> getViewerRole(String communitySlug, Long userId);
    Set<Long> getHideablePostIds(Collection<CommunityPost> posts, Long viewerUserId, boolean viewerAdmin);
    Optional<List<CommunityMembershipEntry>> listMembers(String communitySlug, long callerUserId);
    Optional<CommunityMembersData> getCommunityMembers(String communitySlug, long callerUserId);
    Optional<Boolean> hidePost(String communitySlug, String postSlug, long callerUserId, String reason);
    Optional<Boolean> hidePost(String communitySlug, String postSlug, long callerUserId, String reason, boolean callerAdmin);
    Optional<Boolean> hideComment(String communitySlug, long commentId, long callerUserId, String reason);
    Optional<Boolean> hideComment(String communitySlug, long commentId, long callerUserId, String reason, boolean callerAdmin);
    Optional<Boolean> deletePost(String communitySlug, String postSlug, long callerUserId);
    Optional<Boolean> deleteComment(String communitySlug, long commentId, long callerUserId);
    Optional<Boolean> kickMember(String communitySlug, long targetUserId, long callerUserId);
    Optional<Boolean> promoteToModerator(String communitySlug, long targetUserId, long callerUserId);
    Optional<Boolean> transferOwnership(String communitySlug, long newOwnerUserId, long callerUserId);
    List<Community> getJoinedCommunities(long userId);
    List<CommunityPost> getPostsByIds(Collection<Long> postIds);
    Map<Long, Long> countHelpfulReactionsByPostIds(Collection<Long> postIds);
    Set<Long> findPostHelpfulReactionsByUser(Collection<Long> postIds, long userId);
    Map<Long, Long> countCommentsByPostIds(Collection<Long> postIds);
}
