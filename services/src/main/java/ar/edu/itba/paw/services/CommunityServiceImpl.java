package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityDetailData;
import ar.edu.itba.paw.model.CommunityHubEntry;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.CommunityPostComment;
import ar.edu.itba.paw.model.CommunityPostDetailData;
import ar.edu.itba.paw.model.CommunityPostSummary;
import ar.edu.itba.paw.model.CommunityTopic;
import ar.edu.itba.paw.persistence.CommunityDao;
import ar.edu.itba.paw.services.exception.InvalidCommunityTopicSelectionException;
import ar.edu.itba.paw.services.exception.InvalidServiceInputException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommunityServiceImpl implements CommunityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityServiceImpl.class);

    private static final int WEEKLY_WINDOW_DAYS = 7;
    private static final int MAX_TOPICS_PER_COMMUNITY = 4;
    private static final String CREATOR_ROLE = "moderator";
    private static final String MEMBER_ROLE = "member";
    private static final int MAX_COMMUNITY_SLUG_LENGTH = 60;
    private static final String DEFAULT_COMMUNITY_SLUG = "community";
    private static final int MAX_POST_SLUG_LENGTH = 80;
    private static final String DEFAULT_POST_SLUG = "post";
    private static final int MAX_POST_COMMENT_BODY_LENGTH = 1000;

    private final CommunityDao communityDao;

    @Autowired
    public CommunityServiceImpl(final CommunityDao communityDao) {
        this.communityDao = communityDao;
    }

    @Override
    public List<CommunityHubEntry> getCommunityHub(final Long currentUserId) {
        try {
            final List<Community> communities = communityDao.findAll();
            if (communities.isEmpty()) {
                return Collections.emptyList();
            }

            final List<Long> communityIds = communities.stream()
                    .map(Community::getId)
                    .collect(Collectors.toList());
            final Map<Long, List<CommunityTopic>> topicsByCommunity =
                    communityDao.findTopicsByCommunityIds(communityIds);
            final Map<Long, Long> memberCounts = communityDao.countMembersByCommunityIds(communityIds);
            final Map<Long, Long> weeklyPostCounts = communityDao.countWeeklyPostsByCommunityIds(
                    communityIds,
                    LocalDateTime.now().minusDays(WEEKLY_WINDOW_DAYS)
            );
            final Set<Long> joinedCommunityIds = currentUserId == null
                    ? Collections.emptySet()
                    : communityDao.findJoinedCommunityIds(currentUserId, communityIds);

            final List<CommunityHubEntry> entries = new ArrayList<>();
            for (final Community community : communities) {
                entries.add(new CommunityHubEntry(
                        community,
                        topicsByCommunity.getOrDefault(community.getId(), Collections.emptyList()),
                        memberCounts.getOrDefault(community.getId(), 0L),
                        weeklyPostCounts.getOrDefault(community.getId(), 0L),
                        joinedCommunityIds.contains(community.getId())
                ));
            }
            return entries;
        } catch (final DataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<CommunityTopic> getAvailableTopics() {
        try {
            return communityDao.findAllTopics();
        } catch (final DataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public Community createCommunity(final long userId,
                                     final String name,
                                     final String description,
                                     final Collection<Short> topicIds) {
        if (userId <= 0) {
            throw new InvalidServiceInputException("Community creator is required.");
        }

        final String normalizedName = StringUtils.normalizeRequired(name, "Community name is required.");
        final String normalizedDescription = StringUtils.normalizeRequired(
                description,
                "Community description is required."
        );
        final List<CommunityTopic> selectedTopics = validateTopicSelection(topicIds);
        final String slug = nextAvailableSlug(normalizedName);

        final Community community = communityDao.create(userId, slug, normalizedName, normalizedDescription);
        communityDao.replaceTopicAssignments(
                community.getId(),
                selectedTopics.stream().map(CommunityTopic::getId).collect(Collectors.toList())
        );
        communityDao.createMembership(community.getId(), userId, CREATOR_ROLE);
        LOGGER.info("created community id={} slug={} userId={} topicCount={}",
                community.getId(), slug, userId, selectedTopics.size());
        return community;
    }

    @Override
    @Transactional
    public Optional<CommunityPost> createCommunityPost(final String communitySlug,
                                                       final long userId,
                                                       final String title,
                                                       final String body) {
        if (userId <= 0) {
            throw new InvalidServiceInputException("Community post author is required.");
        }

        final String normalizedCommunitySlug = StringUtils.normalizeRequired(
                communitySlug,
                "Community slug is required."
        );
        final String normalizedTitle = StringUtils.normalizeRequired(title, "Community post title is required.");
        final String normalizedBody = StringUtils.normalizeRequired(body, "Community post body is required.");

        final Optional<Community> communityOptional = communityDao.findBySlug(normalizedCommunitySlug);
        if (communityOptional.isEmpty()) {
            return Optional.empty();
        }

        final Community community = communityOptional.get();
        final String slug = nextAvailablePostSlug(community.getId(), normalizedTitle);
        final CommunityPost post = communityDao.createPost(
                community.getId(),
                userId,
                slug,
                normalizedTitle,
                normalizedBody
        );
        LOGGER.info("created community post id={} communityId={} userId={} slug={}",
                post.getId(), community.getId(), userId, slug);
        return Optional.of(post);
    }

    @Override
    @Transactional
    public Optional<CommunityPostComment> createCommunityPostComment(final String communitySlug,
                                                                     final String postSlug,
                                                                     final long userId,
                                                                     final String body) {
        if (userId <= 0) {
            throw new InvalidServiceInputException("Community post comment author is required.");
        }

        final String normalizedCommunitySlug = StringUtils.normalizeRequired(
                communitySlug,
                "Community slug is required."
        );
        final String normalizedPostSlug = StringUtils.normalizeRequired(
                postSlug,
                "Community post slug is required."
        );
        final String normalizedBody = StringUtils.normalizeRequired(body, "Community post comment body is required.");
        if (normalizedBody.length() > MAX_POST_COMMENT_BODY_LENGTH) {
            throw new InvalidServiceInputException("Community post comment body is too long.");
        }

        final Optional<Community> communityOptional = communityDao.findBySlug(normalizedCommunitySlug);
        if (communityOptional.isEmpty()) {
            return Optional.empty();
        }

        final Community community = communityOptional.get();
        final Optional<CommunityPost> postOptional =
                communityDao.findPostByCommunityIdAndSlug(community.getId(), normalizedPostSlug);
        if (postOptional.isEmpty()) {
            return Optional.empty();
        }

        final CommunityPostComment comment = communityDao.createComment(postOptional.get().getId(), userId, normalizedBody);
        LOGGER.info("created community post comment id={} communityId={} postId={} userId={}",
                comment.getId(), community.getId(), postOptional.get().getId(), userId);
        return Optional.of(comment);
    }

    @Override
    @Transactional
    public Optional<Boolean> toggleMembership(final String slug, final long userId) {
        if (userId <= 0) {
            throw new InvalidServiceInputException("Community member is required.");
        }

        final String normalizedSlug = StringUtils.normalizeRequired(slug, "Community slug is required.");
        final Optional<Community> communityOptional = communityDao.findBySlug(normalizedSlug);
        if (communityOptional.isEmpty()) {
            return Optional.empty();
        }

        final Community community = communityOptional.get();
        final long communityId = community.getId();
        final boolean wasJoined = communityDao.findJoinedCommunityIds(userId, List.of(communityId))
                .contains(communityId);
        if (wasJoined) {
            communityDao.deleteMembership(communityId, userId);
            LOGGER.info("left community id={} slug={} userId={}", communityId, community.getSlug(), userId);
            return Optional.of(false);
        }

        if (!wasJoined) {
            communityDao.createMembership(communityId, userId, MEMBER_ROLE);
            LOGGER.info("joined community id={} slug={} userId={}", communityId, community.getSlug(), userId);
        }
        return Optional.of(true);
    }

    @Override
    @Transactional
    public Optional<Boolean> togglePostHelpfulReaction(final String communitySlug,
                                                       final String postSlug,
                                                       final long userId) {
        if (userId <= 0) {
            throw new InvalidServiceInputException("Community post helpful reaction user is required.");
        }

        final String normalizedCommunitySlug = StringUtils.normalizeRequired(
                communitySlug,
                "Community slug is required."
        );
        final String normalizedPostSlug = StringUtils.normalizeRequired(
                postSlug,
                "Community post slug is required."
        );
        final Optional<Community> communityOptional = communityDao.findBySlug(normalizedCommunitySlug);
        if (communityOptional.isEmpty()) {
            return Optional.empty();
        }

        final Community community = communityOptional.get();
        final Optional<CommunityPost> postOptional =
                communityDao.findPostByCommunityIdAndSlug(community.getId(), normalizedPostSlug);
        if (postOptional.isEmpty()) {
            return Optional.empty();
        }

        final CommunityPost post = postOptional.get();
        if (communityDao.isHelpfulReactionAddedByUser(post.getId(), userId)) {
            communityDao.removeHelpfulReaction(post.getId(), userId);
            LOGGER.info("removed helpful reaction communityPostId={} communitySlug={} userId={}",
                    post.getId(), community.getSlug(), userId);
            return Optional.of(false);
        }

        communityDao.addHelpfulReaction(post.getId(), userId);
        LOGGER.info("added helpful reaction communityPostId={} communitySlug={} userId={}",
                post.getId(), community.getSlug(), userId);
        return Optional.of(true);
    }

    @Override
    public Optional<CommunityDetailData> getCommunityDetail(final String slug, final Long currentUserId) {
        try {
            final Optional<Community> communityOptional = communityDao.findBySlug(slug);
            if (communityOptional.isEmpty()) {
                return Optional.empty();
            }

            final Community community = communityOptional.get();
            final long communityId = community.getId();
            final List<CommunityPost> posts = communityDao.findPostsByCommunityId(communityId);
            final List<Long> postIds = posts.stream().map(CommunityPost::getId).collect(Collectors.toList());
            final Map<Long, Long> commentCounts = communityDao.countCommentsByPostIds(postIds);
            final Map<Long, Long> helpfulCounts = communityDao.countHelpfulReactionsByPostIds(postIds);
            final List<CommunityPostSummary> postSummaries = posts.stream()
                    .map(post -> new CommunityPostSummary(
                            post,
                            helpfulCounts.getOrDefault(post.getId(), 0L),
                            commentCounts.getOrDefault(post.getId(), 0L)
                    ))
                    .collect(Collectors.toList());
            final List<CommunityTopic> topics = communityDao.findTopicsByCommunityIds(List.of(communityId))
                    .getOrDefault(communityId, Collections.emptyList());
            final long memberCount = communityDao.countMembersByCommunityIds(List.of(communityId))
                    .getOrDefault(communityId, 0L);
            final long weeklyPostCount = communityDao.countWeeklyPostsByCommunityIds(
                    List.of(communityId),
                    LocalDateTime.now().minusDays(WEEKLY_WINDOW_DAYS)
            ).getOrDefault(communityId, 0L);
            final boolean joined = currentUserId != null
                    && communityDao.findJoinedCommunityIds(currentUserId, List.of(communityId)).contains(communityId);

            return Optional.of(new CommunityDetailData(
                    community,
                    topics,
                    postSummaries,
                    memberCount,
                    weeklyPostCount,
                    joined
            ));
        } catch (final DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Community> getCommunityBySlug(final String slug) {
        try {
            return communityDao.findBySlug(slug);
        } catch (final DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<CommunityPostDetailData> getCommunityPostDetail(final String communitySlug,
                                                                    final String postSlug,
                                                                    final Long currentUserId) {
        try {
            final Optional<Community> communityOptional = communityDao.findBySlug(communitySlug);
            if (communityOptional.isEmpty()) {
                return Optional.empty();
            }

            final Community community = communityOptional.get();
            final Optional<CommunityPost> postOptional =
                    communityDao.findPostByCommunityIdAndSlug(community.getId(), postSlug);
            if (postOptional.isEmpty()) {
                return Optional.empty();
            }

            final CommunityPost post = postOptional.get();
            final List<CommunityPostComment> comments = communityDao.findCommentsByPostId(post.getId());
            final long helpfulCount = communityDao.countHelpfulReactionsByPostIds(List.of(post.getId()))
                    .getOrDefault(post.getId(), 0L);
            final boolean helpfulByCurrentUser = currentUserId != null
                    && communityDao.isHelpfulReactionAddedByUser(post.getId(), currentUserId);
            final long commentCount = communityDao.countCommentsByPostIds(List.of(post.getId()))
                    .getOrDefault(post.getId(), 0L);
            return Optional.of(new CommunityPostDetailData(
                    community,
                    post,
                    comments,
                    helpfulCount,
                    helpfulByCurrentUser,
                    commentCount
            ));
        } catch (final DataAccessException e) {
            return Optional.empty();
        }
    }

    private List<CommunityTopic> validateTopicSelection(final Collection<Short> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            throw new InvalidCommunityTopicSelectionException(
                    InvalidCommunityTopicSelectionException.Reason.REQUIRED,
                    "Elegí al menos un tema."
            );
        }

        final Set<Short> uniqueTopicIds = topicIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (uniqueTopicIds.isEmpty()) {
            throw new InvalidCommunityTopicSelectionException(
                    InvalidCommunityTopicSelectionException.Reason.REQUIRED,
                    "Elegí al menos un tema."
            );
        }
        if (uniqueTopicIds.size() > MAX_TOPICS_PER_COMMUNITY) {
            throw new InvalidCommunityTopicSelectionException(
                    InvalidCommunityTopicSelectionException.Reason.TOO_MANY,
                    "Podés elegir hasta " + MAX_TOPICS_PER_COMMUNITY + " temas."
            );
        }

        final List<CommunityTopic> resolvedTopics = communityDao.findTopicsByIds(uniqueTopicIds);
        if (resolvedTopics.size() != uniqueTopicIds.size()) {
            throw new InvalidCommunityTopicSelectionException(
                    InvalidCommunityTopicSelectionException.Reason.UNKNOWN_TOPIC,
                    "Uno de los temas seleccionados no es válido."
            );
        }
        return resolvedTopics;
    }

    private String nextAvailableSlug(final String name) {
        final String baseSlug = slugBase(name);
        String candidate = baseSlug;
        int suffix = 2;
        while (communityDao.findBySlug(candidate).isPresent()) {
            candidate = appendSlugSuffix(baseSlug, suffix++);
        }
        return candidate;
    }

    private String nextAvailablePostSlug(final long communityId, final String title) {
        final String baseSlug = postSlugBase(title);
        String candidate = baseSlug;
        int suffix = 2;
        while (communityDao.findPostByCommunityIdAndSlug(communityId, candidate).isPresent()) {
            candidate = appendPostSlugSuffix(baseSlug, suffix++);
        }
        return candidate;
    }

    private String slugBase(final String name) {
        final String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "")
                .replaceAll("-{2,}", "-");
        if (normalized.isEmpty()) {
            return DEFAULT_COMMUNITY_SLUG;
        }
        return truncateSlug(normalized, MAX_COMMUNITY_SLUG_LENGTH);
    }

    private String postSlugBase(final String title) {
        final String normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "")
                .replaceAll("-{2,}", "-");
        if (normalized.isEmpty()) {
            return DEFAULT_POST_SLUG;
        }
        return truncateSlug(normalized, MAX_POST_SLUG_LENGTH);
    }

    private String appendSlugSuffix(final String baseSlug, final int suffix) {
        final String suffixText = "-" + suffix;
        final String truncatedBase = truncateSlug(baseSlug, MAX_COMMUNITY_SLUG_LENGTH - suffixText.length())
                .replaceAll("-+$", "");
        final String candidateBase = truncatedBase.isEmpty() ? DEFAULT_COMMUNITY_SLUG : truncatedBase;
        return candidateBase + suffixText;
    }

    private String appendPostSlugSuffix(final String baseSlug, final int suffix) {
        final String suffixText = "-" + suffix;
        final String truncatedBase = truncateSlug(baseSlug, MAX_POST_SLUG_LENGTH - suffixText.length())
                .replaceAll("-+$", "");
        final String candidateBase = truncatedBase.isEmpty() ? DEFAULT_POST_SLUG : truncatedBase;
        return candidateBase + suffixText;
    }

    private String truncateSlug(final String slug, final int maxLength) {
        if (slug.length() <= maxLength) {
            return slug;
        }
        return slug.substring(0, maxLength);
    }
}
