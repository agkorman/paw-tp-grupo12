package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityDetailData;
import ar.edu.itba.paw.model.CommunityHubEntry;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.CommunityPostComment;
import ar.edu.itba.paw.model.CommunityPostDetailData;
import ar.edu.itba.paw.model.CommunityTopic;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommunityService {
    List<CommunityHubEntry> getCommunityHub(Long currentUserId);
    List<CommunityTopic> getAvailableTopics();
    Community createCommunity(long userId, String name, String description, Collection<Short> topicIds);
    Optional<CommunityPost> createCommunityPost(String communitySlug, long userId, String title, String body);
    Optional<CommunityPostComment> createCommunityPostComment(String communitySlug, String postSlug, long userId, String body);
    Optional<Boolean> toggleMembership(String slug, long userId);
    Optional<Boolean> togglePostHelpfulReaction(String communitySlug, String postSlug, long userId);
    Optional<CommunityDetailData> getCommunityDetail(String slug, Long currentUserId);
    Optional<Community> getCommunityBySlug(String slug);
    Optional<CommunityPostDetailData> getCommunityPostDetail(String communitySlug, String postSlug, Long currentUserId);
}
