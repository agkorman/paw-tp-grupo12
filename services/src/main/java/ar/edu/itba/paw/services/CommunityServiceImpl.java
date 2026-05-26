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
import ar.edu.itba.paw.model.CommunityPostSummary;
import ar.edu.itba.paw.model.CommunitySearchCriteria;
import ar.edu.itba.paw.model.CommunityTopic;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
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
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
    static final String POST_SORT_RECENT = "recent";
    static final String POST_SORT_HELPFUL = "helpful";
    static final String POST_SORT_COMMENTED = "commented";
    private static final int MAX_COMMUNITY_SLUG_LENGTH = 60;
    private static final String DEFAULT_COMMUNITY_SLUG = "community";
    private static final int MAX_POST_SLUG_LENGTH = 80;
    private static final String DEFAULT_POST_SLUG = "post";
    private final CommunityDao communityDao;
    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public CommunityServiceImpl(final CommunityDao communityDao, final UserService userService,
                                final EmailService emailService) {
        this.communityDao = communityDao;
        this.userService = userService;
        this.emailService = emailService;
    }

    private CommunitySearchCriteria emptyCommunityCriteria(final int page) {
        final CommunitySearchCriteria criteria = new CommunitySearchCriteria();
        criteria.setPage(page);
        return criteria;
    }

    @Override
    public List<CommunityHubEntry> getCommunityHub(final Long currentUserId) {
        return getCommunityHub(emptyCommunityCriteria(Pagination.DEFAULT_PAGE), currentUserId).getItems();
    }

    @Override
    public Page<CommunityHubEntry> getCommunityHub(final Long currentUserId, final int page) {
        return getCommunityHub(emptyCommunityCriteria(page), currentUserId);
    }

    @Override
    public Page<CommunityHubEntry> getCommunityHub(final CommunitySearchCriteria criteria, final Long currentUserId) {
        final CommunitySearchCriteria safeCriteria = criteria == null ? emptyCommunityCriteria(Pagination.DEFAULT_PAGE) : criteria;
        try {
            if (!safeCriteria.isValid()) {
                return Page.empty(Pagination.DEFAULT_PAGE, Pagination.COMMUNITIES_PAGE_SIZE);
            }
            final Page<Community> communitiesPage = communityDao.findByCriteria(safeCriteria, currentUserId);
            final List<Community> communities = communitiesPage.getItems();
            if (communities.isEmpty()) {
                return Page.empty(communitiesPage.getPageNumber(), communitiesPage.getPageSize());
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
            return new Page<>(
                    entries,
                    communitiesPage.getPageNumber(),
                    communitiesPage.getPageSize(),
                    communitiesPage.getTotalItems()
            );
        } catch (final DataAccessException e) {
            LOGGER.warn("failed to load community hub for userId={}", currentUserId, e);
            return Page.empty(Pagination.normalizePage(safeCriteria.getPage()), Pagination.COMMUNITIES_PAGE_SIZE);
        }
    }

    @Override
    public List<CommunityTopic> getAvailableTopics() {
        try {
            return communityDao.findAllTopics();
        } catch (final DataAccessException e) {
            LOGGER.warn("failed to load available community topics", e);
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
    public Optional<CommunityEditData> getCommunityForEdit(final String communitySlug, final long callerUserId) {
        final Community community = requireModerator(communitySlug, callerUserId);
        if (community == null) {
            return Optional.empty();
        }
        final List<CommunityTopic> topics = communityDao.findTopicsByCommunityIds(List.of(community.getId()))
                .getOrDefault(community.getId(), Collections.emptyList());
        return Optional.of(new CommunityEditData(community, topics, isCreator(community, callerUserId)));
    }

    @Override
    @Transactional
    public Optional<Community> editCommunity(final String communitySlug,
                                             final long callerUserId,
                                             final String name,
                                             final String description,
                                             final Collection<Short> topicIds) {
        final Community community = requireModerator(communitySlug, callerUserId);
        if (community == null) {
            return Optional.empty();
        }
        final String normalizedName = StringUtils.normalizeRequired(name, "Community name is required.");
        final String normalizedDescription = StringUtils.normalizeRequired(
                description,
                "Community description is required."
        );
        final List<CommunityTopic> selectedTopics = validateTopicSelection(topicIds);

        communityDao.updateDetails(community.getId(), normalizedName, normalizedDescription);
        communityDao.replaceTopicAssignments(
                community.getId(),
                selectedTopics.stream().map(CommunityTopic::getId).collect(Collectors.toList())
        );
        LOGGER.info("edited community id={} callerUserId={} topicCount={}",
                community.getId(), callerUserId, selectedTopics.size());
        return Optional.of(community);
    }

    @Override
    @Transactional
    public Optional<Boolean> deleteCommunity(final String communitySlug, final long callerUserId) {
        final Community community = requireModerator(communitySlug, callerUserId);
        if (community == null) {
            return Optional.empty();
        }
        if (!isCreator(community, callerUserId)) {
            LOGGER.warn("community delete denied: caller userId={} is not owner of communityId={}",
                    callerUserId, community.getId());
            throw new CommunityOwnerRequiredException(community.getSlug());
        }
        communityDao.delete(community.getId());
        LOGGER.info("deleted community id={} slug={} callerUserId={}",
                community.getId(), community.getSlug(), callerUserId);
        return Optional.of(true);
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
        if (communityDao.findMembershipRole(community.getId(), userId).isEmpty()) {
            LOGGER.warn("post denied: not a member communitySlug={} userId={}", normalizedCommunitySlug, userId);
            throw new CommunityMembershipRequiredException(normalizedCommunitySlug);
        }
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

        final Optional<Community> communityOptional = communityDao.findBySlug(normalizedCommunitySlug);
        if (communityOptional.isEmpty()) {
            return Optional.empty();
        }

        final Community community = communityOptional.get();
        if (communityDao.findMembershipRole(community.getId(), userId).isEmpty()) {
            LOGGER.warn("comment denied: not a member communitySlug={} userId={}", normalizedCommunitySlug, userId);
            throw new CommunityMembershipRequiredException(normalizedCommunitySlug);
        }
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
            if (isCreator(community, userId)) {
                LOGGER.warn("leave denied: creator cannot leave communityId={} userId={}", communityId, userId);
                throw new CommunityCreatorCannotLeaveException(normalizedSlug);
            }
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
    public Optional<CommunityDetailData> getCommunityDetail(final String slug, final Long currentUserId,
                                                            final String sort) {
        return getCommunityDetail(slug, currentUserId, sort, Pagination.DEFAULT_PAGE);
    }

    @Override
    public Optional<CommunityDetailData> getCommunityDetail(final String slug, final Long currentUserId,
                                                            final String sort, final int page) {
        try {
            final Optional<Community> communityOptional = communityDao.findBySlug(slug);
            if (communityOptional.isEmpty()) {
                return Optional.empty();
            }

            final Community community = communityOptional.get();
            final long communityId = community.getId();
            final String normalizedSort = normalizePostSort(sort);
            final String viewerRole = currentUserId == null
                    ? null
                    : communityDao.findMembershipRole(communityId, currentUserId).orElse(null);
            final Page<CommunityPost> posts = communityDao.findVisiblePostsByCommunityId(
                    communityId,
                    normalizedSort,
                    page
            );
            final List<Long> postIds = posts.getItems().stream().map(CommunityPost::getId).collect(Collectors.toList());
            final Map<Long, Long> commentCounts = communityDao.countCommentsByPostIds(postIds);
            final Map<Long, Long> helpfulCounts = communityDao.countHelpfulReactionsByPostIds(postIds);
            final List<CommunityPostSummary> postSummaries = posts.getItems().stream()
                    .map(post -> new CommunityPostSummary(
                            post,
                            helpfulCounts.getOrDefault(post.getId(), 0L),
                            commentCounts.getOrDefault(post.getId(), 0L)
                    ))
                    .collect(Collectors.toList());
            final Page<CommunityPostSummary> postSummariesPage = new Page<>(
                    postSummaries,
                    posts.getPageNumber(),
                    posts.getPageSize(),
                    posts.getTotalItems()
            );
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
            final boolean viewerCreator = currentUserId != null && isCreator(community, currentUserId);

            return Optional.of(new CommunityDetailData(
                    community,
                    topics,
                    postSummariesPage,
                    memberCount,
                    weeklyPostCount,
                    joined,
                    viewerRole,
                    normalizedSort,
                    viewerCreator
            ));
        } catch (final DataAccessException e) {
            LOGGER.warn("failed to load community detail for slug={}", slug, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Community> getCommunityBySlug(final String slug) {
        try {
            return communityDao.findBySlug(slug);
        } catch (final DataAccessException e) {
            LOGGER.warn("failed to load community for slug={}", slug, e);
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
            if (post.isHidden()) {
                return Optional.empty();
            }
            final String viewerRole = currentUserId == null
                    ? null
                    : communityDao.findMembershipRole(community.getId(), currentUserId).orElse(null);
            final List<CommunityPostComment> comments = communityDao.findCommentsByPostId(post.getId()).stream()
                    .filter(c -> !c.isHidden())
                    .collect(Collectors.toList());
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
                    commentCount,
                    viewerRole,
                    currentUserId
            ));
        } catch (final DataAccessException e) {
            LOGGER.warn("failed to load post detail for communitySlug={} postSlug={}", communitySlug, postSlug, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getViewerRole(final String communitySlug, final Long userId) {
        if (userId == null || userId <= 0) {
            return Optional.empty();
        }
        try {
            final Optional<Community> communityOptional = communityDao.findBySlug(communitySlug);
            if (communityOptional.isEmpty()) {
                return Optional.empty();
            }
            return communityDao.findMembershipRole(communityOptional.get().getId(), userId);
        } catch (final DataAccessException e) {
            LOGGER.warn("failed to resolve viewer role communitySlug={} userId={}", communitySlug, userId, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<CommunityMembershipEntry>> listMembers(final String communitySlug, final long callerUserId) {
        if (callerUserId <= 0) {
            throw new InvalidServiceInputException("Caller is required.");
        }
        final String normalizedSlug = StringUtils.normalizeRequired(communitySlug, "Community slug is required.");
        final Optional<Community> communityOptional = communityDao.findBySlug(normalizedSlug);
        if (communityOptional.isEmpty()) {
            return Optional.empty();
        }
        final Community community = communityOptional.get();
        if (communityDao.findMembershipRole(community.getId(), callerUserId).isEmpty()) {
            throw new CommunityMembershipRequiredException(normalizedSlug);
        }
        final Long creatorUserId = community.getCreatedByUserId();
        final List<CommunityMembershipEntry> raw = communityDao.listMembers(community.getId());
        final List<CommunityMembershipEntry> entries = new ArrayList<>(raw.size());
        for (final CommunityMembershipEntry entry : raw) {
            final boolean isCreator = creatorUserId != null && creatorUserId == entry.getUserId();
            entries.add(new CommunityMembershipEntry(
                    entry.getUserId(),
                    entry.getUsername(),
                    entry.getRole(),
                    entry.getJoinedAt(),
                    isCreator
            ));
        }
        return Optional.of(entries);
    }

    @Override
    public Optional<CommunityMembersData> getCommunityMembers(final String communitySlug, final long callerUserId) {
        if (callerUserId <= 0) {
            throw new InvalidServiceInputException("Caller is required.");
        }
        final String normalizedSlug = StringUtils.normalizeRequired(communitySlug, "Community slug is required.");
        final Optional<Community> communityOptional = communityDao.findBySlug(normalizedSlug);
        if (communityOptional.isEmpty()) {
            return Optional.empty();
        }
        final Community community = communityOptional.get();
        final Optional<String> callerRole = communityDao.findMembershipRole(community.getId(), callerUserId);
        if (callerRole.isEmpty()) {
            throw new CommunityMembershipRequiredException(normalizedSlug);
        }
        final Long creatorUserId = community.getCreatedByUserId();
        final List<CommunityMembershipEntry> raw = communityDao.listMembers(community.getId());
        final List<CommunityMembershipEntry> entries = new ArrayList<>(raw.size());
        for (final CommunityMembershipEntry entry : raw) {
            final boolean isCreator = creatorUserId != null && creatorUserId == entry.getUserId();
            entries.add(new CommunityMembershipEntry(
                    entry.getUserId(),
                    entry.getUsername(),
                    entry.getRole(),
                    entry.getJoinedAt(),
                    isCreator
            ));
        }
        return Optional.of(new CommunityMembersData(community, entries, callerRole.get(),
                isCreator(community, callerUserId)));
    }

    @Override
    @Transactional
    public Optional<Boolean> hidePost(final String communitySlug, final String postSlug,
                                      final long callerUserId, final String reason) {
        final Community community = requireModerator(communitySlug, callerUserId);
        if (community == null) {
            return Optional.empty();
        }
        final Optional<CommunityPost> postOptional =
                communityDao.findPostByCommunityIdAndSlug(community.getId(), postSlug);
        if (postOptional.isEmpty()) {
            return Optional.empty();
        }
        final CommunityPost post = postOptional.get();
        final String recipientEmail = resolveUserEmail(post.getAuthorUserId());
        communityDao.setPostHidden(post.getId(), true);
        LOGGER.info("moderator userId={} hid communityId={} postId={}",
                callerUserId, community.getId(), post.getId());
        if (recipientEmail != null) {
            emailService.sendCommunityPostHiddenNotification(
                    recipientEmail,
                    community.getName(),
                    post.getTitle(),
                    reason,
                    communityPostPath(community, post)
            );
        }
        return Optional.of(true);
    }

    @Override
    @Transactional
    public Optional<Boolean> hideComment(final String communitySlug, final long commentId,
                                         final long callerUserId, final String reason) {
        final Community community = requireModerator(communitySlug, callerUserId);
        if (community == null) {
            return Optional.empty();
        }
        final Optional<CommunityPostComment> commentOptional = communityDao.findCommentById(commentId);
        if (commentOptional.isEmpty()
                || commentOptional.get().getPost().getCommunity().getId() != community.getId()) {
            return Optional.empty();
        }
        final CommunityPostComment comment = commentOptional.get();
        final CommunityPost post = comment.getPost();
        final String recipientEmail = resolveUserEmail(comment.getUserId());
        communityDao.setCommentHidden(commentId, true);
        LOGGER.info("moderator userId={} hid communityId={} commentId={}",
                callerUserId, community.getId(), commentId);
        if (recipientEmail != null) {
            emailService.sendCommunityCommentHiddenNotification(
                    recipientEmail,
                    community.getName(),
                    post == null ? null : post.getTitle(),
                    comment.getBody(),
                    reason,
                    post == null ? communityPath(community) : communityPostPath(community, post)
            );
        }
        return Optional.of(true);
    }

    @Override
    @Transactional
    public Optional<Boolean> deletePost(final String communitySlug, final String postSlug,
                                        final long callerUserId) {
        if (callerUserId <= 0) {
            throw new InvalidServiceInputException("Caller is required.");
        }
        final String normalizedSlug = StringUtils.normalizeRequired(communitySlug, "Community slug is required.");
        final Optional<Community> communityOptional = communityDao.findBySlug(normalizedSlug);
        if (communityOptional.isEmpty()) {
            return Optional.empty();
        }
        final Optional<CommunityPost> postOptional =
                communityDao.findPostByCommunityIdAndSlug(communityOptional.get().getId(), postSlug);
        if (postOptional.isEmpty()) {
            return Optional.empty();
        }
        final CommunityPost post = postOptional.get();
        if (post.getAuthorUserId() != callerUserId) {
            LOGGER.warn("post delete denied: caller userId={} is not author of postId={}", callerUserId, post.getId());
            throw new CommunityContentOwnershipException("Only the author can delete this post.");
        }
        communityDao.deletePost(post.getId());
        LOGGER.info("author userId={} deleted communityId={} postId={}",
                callerUserId, communityOptional.get().getId(), post.getId());
        return Optional.of(true);
    }

    @Override
    @Transactional
    public Optional<Boolean> deleteComment(final String communitySlug, final long commentId,
                                           final long callerUserId) {
        if (callerUserId <= 0) {
            throw new InvalidServiceInputException("Caller is required.");
        }
        final String normalizedSlug = StringUtils.normalizeRequired(communitySlug, "Community slug is required.");
        final Optional<Community> communityOptional = communityDao.findBySlug(normalizedSlug);
        if (communityOptional.isEmpty()) {
            return Optional.empty();
        }
        final Optional<CommunityPostComment> commentOptional = communityDao.findCommentById(commentId);
        if (commentOptional.isEmpty()
                || commentOptional.get().getPost().getCommunity().getId() != communityOptional.get().getId()) {
            return Optional.empty();
        }
        final CommunityPostComment comment = commentOptional.get();
        if (comment.getUserId() != callerUserId) {
            LOGGER.warn("comment delete denied: caller userId={} is not author of commentId={}", callerUserId, commentId);
            throw new CommunityContentOwnershipException("Only the author can delete this comment.");
        }
        communityDao.deleteComment(commentId);
        LOGGER.info("author userId={} deleted communityId={} commentId={}",
                callerUserId, communityOptional.get().getId(), commentId);
        return Optional.of(true);
    }

    @Override
    @Transactional
    public Optional<Boolean> kickMember(final String communitySlug, final long targetUserId,
                                        final long callerUserId) {
        final Community community = requireModerator(communitySlug, callerUserId);
        if (community == null) {
            return Optional.empty();
        }
        if (targetUserId <= 0 || targetUserId == callerUserId) {
            throw new InvalidServiceInputException("Target user is required and cannot be self.");
        }
        if (isCreator(community, targetUserId)) {
            LOGGER.warn("kick denied: target is creator communitySlug={} targetUserId={}",
                    communitySlug, targetUserId);
            throw new CannotModerateCreatorException(communitySlug);
        }
        if (communityDao.findMembershipRole(community.getId(), targetUserId).isEmpty()) {
            return Optional.of(false);
        }
        final String recipientEmail = resolveUserEmail(targetUserId);
        communityDao.deleteMembership(community.getId(), targetUserId);
        LOGGER.info("moderator userId={} kicked userId={} from communityId={}",
                callerUserId, targetUserId, community.getId());
        if (recipientEmail != null) {
            emailService.sendCommunityMemberKickedNotification(
                    recipientEmail,
                    community.getName(),
                    communityPath(community)
            );
        }
        return Optional.of(true);
    }

    @Override
    @Transactional
    public Optional<Boolean> promoteToModerator(final String communitySlug, final long targetUserId,
                                                final long callerUserId) {
        final Community community = requireModerator(communitySlug, callerUserId);
        if (community == null) {
            return Optional.empty();
        }
        if (targetUserId <= 0) {
            throw new InvalidServiceInputException("Target user is required.");
        }
        final Optional<String> currentRole = communityDao.findMembershipRole(community.getId(), targetUserId);
        if (currentRole.isEmpty()) {
            return Optional.of(false);
        }
        if (CREATOR_ROLE.equals(currentRole.get())) {
            return Optional.of(false);
        }
        final String recipientEmail = resolveUserEmail(targetUserId);
        communityDao.updateMembershipRole(community.getId(), targetUserId, CREATOR_ROLE);
        LOGGER.info("moderator userId={} promoted userId={} in communityId={}",
                callerUserId, targetUserId, community.getId());
        if (recipientEmail != null) {
            emailService.sendCommunityModeratorPromotedNotification(
                    recipientEmail,
                    community.getName(),
                    communityMembersPath(community)
            );
        }
        return Optional.of(true);
    }

    @Override
    @Transactional
    public Optional<Boolean> transferOwnership(final String communitySlug, final long newOwnerUserId,
                                               final long callerUserId) {
        if (callerUserId <= 0) {
            throw new InvalidServiceInputException("Caller is required.");
        }
        final String normalizedSlug = StringUtils.normalizeRequired(communitySlug, "Community slug is required.");
        final Optional<Community> communityOptional = communityDao.findBySlug(normalizedSlug);
        if (communityOptional.isEmpty()) {
            return Optional.empty();
        }
        final Community community = communityOptional.get();
        if (!isCreator(community, callerUserId)) {
            LOGGER.warn("ownership transfer denied: caller userId={} is not owner of communityId={}",
                    callerUserId, community.getId());
            throw new CommunityOwnerRequiredException(normalizedSlug);
        }
        if (newOwnerUserId <= 0 || newOwnerUserId == callerUserId) {
            throw new InvalidServiceInputException("New owner is required and must be a different user.");
        }
        final Optional<String> targetRole = communityDao.findMembershipRole(community.getId(), newOwnerUserId);
        if (targetRole.isEmpty() || !CREATOR_ROLE.equals(targetRole.get())) {
            throw new InvalidServiceInputException("Ownership can only be transferred to a moderator.");
        }
        final String recipientEmail = resolveUserEmail(newOwnerUserId);
        communityDao.updateCreatedBy(community.getId(), newOwnerUserId);
        LOGGER.info("owner userId={} transferred community id={} ownership to userId={}",
                callerUserId, community.getId(), newOwnerUserId);
        if (recipientEmail != null) {
            emailService.sendCommunityOwnershipTransferredNotification(
                    recipientEmail,
                    community.getName(),
                    communityMembersPath(community)
            );
        }
        return Optional.of(true);
    }

    private String resolveUserEmail(final long userId) {
        if (userId <= 0) {
            return null;
        }
        try {
            return userService.getUserById(userId)
                    .map(User::getEmail)
                    .filter(email -> !email.isBlank())
                    .orElse(null);
        } catch (final RuntimeException e) {
            LOGGER.warn("failed to resolve community notification recipient userId={}", userId, e);
            return null;
        }
    }

    private String communityPath(final Community community) {
        return "/communities/" + community.getSlug();
    }

    private String communityMembersPath(final Community community) {
        return communityPath(community) + "/members";
    }

    private String communityPostPath(final Community community, final CommunityPost post) {
        return communityPath(community) + "/posts/" + post.getSlug();
    }

    private Community requireModerator(final String communitySlug, final long callerUserId) {
        if (callerUserId <= 0) {
            throw new InvalidServiceInputException("Caller is required.");
        }
        final String normalizedSlug = StringUtils.normalizeRequired(communitySlug, "Community slug is required.");
        final Optional<Community> communityOptional = communityDao.findBySlug(normalizedSlug);
        if (communityOptional.isEmpty()) {
            return null;
        }
        final Community community = communityOptional.get();
        final Optional<String> role = communityDao.findMembershipRole(community.getId(), callerUserId);
        if (role.isEmpty() || !CREATOR_ROLE.equals(role.get())) {
            throw new CommunityModeratorRequiredException(normalizedSlug);
        }
        return community;
    }

    private boolean isCreator(final Community community, final long userId) {
        return community.getCreatedBy() != null && community.getCreatedBy().getId() == userId;
    }

    private String normalizePostSort(final String sort) {
        if (POST_SORT_HELPFUL.equals(sort) || POST_SORT_COMMENTED.equals(sort)) {
            return sort;
        }
        return POST_SORT_RECENT;
    }

    private Comparator<CommunityPostSummary> postSummaryComparator(final String normalizedSort) {
        final Comparator<CommunityPostSummary> byRecency = Comparator
                .comparing((CommunityPostSummary summary) -> summary.getPost().getCreatedAt(),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed()
                .thenComparing(summary -> summary.getPost().getId(), Comparator.reverseOrder());
        switch (normalizedSort) {
            case POST_SORT_HELPFUL:
                return Comparator.comparingLong(CommunityPostSummary::getHelpfulCount).reversed()
                        .thenComparing(byRecency);
            case POST_SORT_COMMENTED:
                return Comparator.comparingLong(CommunityPostSummary::getCommentCount).reversed()
                        .thenComparing(byRecency);
            default:
                return byRecency;
        }
    }

    private List<CommunityTopic> validateTopicSelection(final Collection<Short> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            throw new InvalidCommunityTopicSelectionException(
                    InvalidCommunityTopicSelectionException.Reason.REQUIRED,
                    "At least one topic is required."
            );
        }

        final Set<Short> uniqueTopicIds = topicIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (uniqueTopicIds.isEmpty()) {
            throw new InvalidCommunityTopicSelectionException(
                    InvalidCommunityTopicSelectionException.Reason.REQUIRED,
                    "At least one topic is required."
            );
        }
        if (uniqueTopicIds.size() > MAX_TOPICS_PER_COMMUNITY) {
            throw new InvalidCommunityTopicSelectionException(
                    InvalidCommunityTopicSelectionException.Reason.TOO_MANY,
                    "Too many topics selected: max=" + MAX_TOPICS_PER_COMMUNITY
            );
        }

        final List<CommunityTopic> resolvedTopics = communityDao.findTopicsByIds(uniqueTopicIds);
        if (resolvedTopics.size() != uniqueTopicIds.size()) {
            throw new InvalidCommunityTopicSelectionException(
                    InvalidCommunityTopicSelectionException.Reason.UNKNOWN_TOPIC,
                    "One or more selected topics are unknown."
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
