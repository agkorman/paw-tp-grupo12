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
import ar.edu.itba.paw.model.CommunitySearchCriteria;
import ar.edu.itba.paw.model.CommunityTopic;
import ar.edu.itba.paw.model.Page;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommunityService {
    List<CommunityHubEntry> getCommunityHub(Long currentUserId);
    Page<CommunityHubEntry> getCommunityHub(Long currentUserId, int page);
    Page<CommunityHubEntry> getCommunityHub(CommunitySearchCriteria criteria, Long currentUserId);
    List<CommunityTopic> getAvailableTopics();
    Community createCommunity(long userId, String name, String description, Collection<Short> topicIds);
    Optional<CommunityEditData> getCommunityForEdit(String communitySlug, long callerUserId);
    Optional<Community> editCommunity(String communitySlug, long callerUserId, String name, String description, Collection<Short> topicIds);
    Optional<Boolean> deleteCommunity(String communitySlug, long callerUserId);
    Optional<CommunityPost> createCommunityPost(String communitySlug, long userId, String title, String body);
    Optional<CommunityPostComment> createCommunityPostComment(String communitySlug, String postSlug, long userId, String body);
    Optional<Boolean> toggleMembership(String slug, long userId);
    Optional<Boolean> togglePostHelpfulReaction(String communitySlug, String postSlug, long userId);
    Optional<Boolean> toggleCommentHelpfulReaction(String communitySlug, String postSlug, long commentId, long userId);
    Optional<CommunityDetailData> getCommunityDetail(String slug, Long currentUserId, String sort);
    Optional<CommunityDetailData> getCommunityDetail(String slug, Long currentUserId, String sort, int page);
    Optional<Community> getCommunityBySlug(String slug);
    Optional<CommunityPostDetailData> getCommunityPostDetail(String communitySlug, String postSlug, Long currentUserId);
    Optional<String> getViewerRole(String communitySlug, Long userId);
    Optional<List<CommunityMembershipEntry>> listMembers(String communitySlug, long callerUserId);
    Optional<CommunityMembersData> getCommunityMembers(String communitySlug, long callerUserId);
    Optional<Boolean> hidePost(String communitySlug, String postSlug, long callerUserId, String reason);
    Optional<Boolean> hideComment(String communitySlug, long commentId, long callerUserId, String reason);
    Optional<Boolean> deletePost(String communitySlug, String postSlug, long callerUserId);
    Optional<Boolean> deleteComment(String communitySlug, long commentId, long callerUserId);
    Optional<Boolean> kickMember(String communitySlug, long targetUserId, long callerUserId);
    Optional<Boolean> promoteToModerator(String communitySlug, long targetUserId, long callerUserId);
    Optional<Boolean> transferOwnership(String communitySlug, long newOwnerUserId, long callerUserId);
}
