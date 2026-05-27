package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityMembershipEntry;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.CommunityPostComment;
import ar.edu.itba.paw.model.CommunitySearchCriteria;
import ar.edu.itba.paw.model.CommunityTopic;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class CommunityJpaDao implements CommunityDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Community> findAll() {
        return em.createQuery(
                "SELECT c FROM Community c ORDER BY c.id ASC",
                Community.class
        ).getResultList();
    }

    @Override
    public Page<Community> findAll(final int page) {
        final int pageSize = Pagination.COMMUNITIES_PAGE_SIZE;
        final Number count = (Number) em.createNativeQuery("SELECT COUNT(*) FROM communities").getSingleResult();
        final long totalItems = count == null ? 0L : count.longValue();
        if (totalItems <= 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }

        final int effectivePage = Pagination.clampPage(Pagination.normalizePage(page), totalItems, pageSize);
        final Query idsQuery = em.createNativeQuery(
                "SELECT community_id FROM communities ORDER BY community_id ASC LIMIT ? OFFSET ?"
        );
        idsQuery.setParameter(1, pageSize);
        idsQuery.setParameter(2, Pagination.offsetFor(effectivePage, pageSize));

        final List<Long> ids = toLongIds(idsQuery.getResultList());
        if (ids.isEmpty()) {
            return Page.empty(effectivePage, pageSize);
        }

        final List<Community> communities = em.createQuery(
                "SELECT c FROM Community c WHERE c.id IN :ids",
                Community.class
        )
                .setParameter("ids", ids)
                .getResultList();
        return new Page<>(sortByIds(communities, ids, Community::getId), effectivePage, pageSize, totalItems);
    }

    @Override
    public Page<Community> findByCriteria(final CommunitySearchCriteria criteria, final Long currentUserId) {
        final CommunitySearchCriteria safeCriteria = criteria == null ? new CommunitySearchCriteria() : criteria;
        final int pageSize = Pagination.COMMUNITIES_PAGE_SIZE;
        final List<Object> params = new ArrayList<>();
        final String fromJoin = communitySearchFromJoin(safeCriteria);
        final String whereClause = buildCommunityWhereClause(safeCriteria, currentUserId, params);
        final String orderClause = buildCommunityOrderClause(safeCriteria);

        final Query countQuery = em.createNativeQuery("SELECT COUNT(*) " + fromJoin + whereClause);
        for (int i = 0; i < params.size(); i++) {
            countQuery.setParameter(i + 1, params.get(i));
        }
        final Number count = (Number) countQuery.getSingleResult();
        final long totalItems = count == null ? 0L : count.longValue();
        if (totalItems <= 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }

        final int effectivePage = Pagination.clampPage(Pagination.normalizePage(safeCriteria.getPage()), totalItems, pageSize);
        final Query idsQuery = em.createNativeQuery(
                "SELECT c.community_id " + fromJoin + whereClause + orderClause + " LIMIT ? OFFSET ?"
        );
        for (int i = 0; i < params.size(); i++) {
            idsQuery.setParameter(i + 1, params.get(i));
        }
        idsQuery.setParameter(params.size() + 1, pageSize);
        idsQuery.setParameter(params.size() + 2, Pagination.offsetFor(effectivePage, pageSize));

        final List<Long> ids = toLongIds(idsQuery.getResultList());
        if (ids.isEmpty()) {
            return Page.empty(effectivePage, pageSize);
        }

        final List<Community> communities = em.createQuery(
                "SELECT c FROM Community c WHERE c.id IN :ids",
                Community.class
        )
                .setParameter("ids", ids)
                .getResultList();
        return new Page<>(sortByIds(communities, ids, Community::getId), effectivePage, pageSize, totalItems);
    }

    @Override
    public Optional<Community> findBySlug(final String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return Optional.empty();
        }
        final List<Community> results = em.createQuery(
                "SELECT c FROM Community c WHERE LOWER(c.slug) = :slug",
                Community.class
        )
                .setParameter("slug", slug.trim().toLowerCase(Locale.ROOT))
                .getResultList();
        return results.stream().findFirst();
    }

    @Override
    public List<CommunityTopic> findAllTopics() {
        return em.createQuery(
                "SELECT t FROM CommunityTopic t WHERE t.active = TRUE ORDER BY t.id ASC",
                CommunityTopic.class
        ).getResultList();
    }

    @Override
    public List<CommunityTopic> findTopicsByIds(final Collection<Short> topicIds) {
        final List<Short> normalizedIds = normalizeTopicIds(topicIds);
        if (normalizedIds.isEmpty()) {
            return Collections.emptyList();
        }

        return em.createQuery(
                "SELECT t FROM CommunityTopic t WHERE t.id IN :ids ORDER BY t.id ASC",
                CommunityTopic.class
        )
                .setParameter("ids", normalizedIds)
                .getResultList();
    }

    @Override
    public Community create(final long createdByUserId,
                            final String slug,
                            final String name,
                            final String description) {
        final Community community = new Community();
        community.setCreatedBy(em.getReference(ar.edu.itba.paw.model.User.class, createdByUserId));
        community.setSlug(slug);
        community.setName(name);
        community.setDescription(description);
        em.persist(community);
        LOGGER.info("created community id={} slug={} createdByUserId={}",
                community.getId(), slug, createdByUserId);
        return community;
    }

    @Override
    public void updateDetails(final long communityId, final String name, final String description) {
        final Community community = em.find(Community.class, communityId);
        if (community == null) {
            return;
        }
        community.setName(name);
        community.setDescription(description);
        LOGGER.info("updated community details id={}", communityId);
    }

    @Override
    public void updateCreatedBy(final long communityId, final long newOwnerUserId) {
        final Community community = em.find(Community.class, communityId);
        if (community == null) {
            return;
        }
        community.setCreatedBy(em.getReference(ar.edu.itba.paw.model.User.class, newOwnerUserId));
        LOGGER.info("transferred community ownership id={} newOwnerUserId={}", communityId, newOwnerUserId);
    }

    @Override
    public boolean delete(final long communityId) {
        final Community community = em.find(Community.class, communityId);
        if (community == null) {
            return false;
        }
        em.remove(community);
        LOGGER.info("deleted community id={}", communityId);
        return true;
    }

    @Override
    public CommunityPost createPost(final long communityId,
                                    final long authorUserId,
                                    final String slug,
                                    final String title,
                                    final String body) {
        final CommunityPost post = new CommunityPost();
        post.setCommunity(em.getReference(Community.class, communityId));
        post.setAuthor(em.getReference(ar.edu.itba.paw.model.User.class, authorUserId));
        post.setSlug(slug);
        post.setTitle(title);
        post.setBody(body);
        em.persist(post);
        LOGGER.info("created community post id={} communityId={} authorUserId={} slug={}",
                post.getId(), communityId, authorUserId, slug);
        return post;
    }

    @Override
    public CommunityPostComment createComment(final long postId, final long userId, final String body) {
        final CommunityPostComment comment = new CommunityPostComment();
        comment.setPost(em.getReference(CommunityPost.class, postId));
        comment.setUser(em.getReference(ar.edu.itba.paw.model.User.class, userId));
        comment.setBody(body);
        em.persist(comment);
        LOGGER.info("created community post comment id={} postId={} userId={}",
                comment.getId(), postId, userId);
        return comment;
    }

    @Override
    public void replaceTopicAssignments(final long communityId, final Collection<Short> topicIds) {
        em.createNativeQuery("DELETE FROM community_topic_assignments WHERE community_id = :communityId")
                .setParameter("communityId", communityId)
                .executeUpdate();

        final List<Short> normalizedTopicIds = normalizeTopicIds(topicIds);
        if (normalizedTopicIds.isEmpty()) {
            LOGGER.info("cleared community topic assignments communityId={}", communityId);
            return;
        }

        final StringBuilder sql = new StringBuilder(
                "INSERT INTO community_topic_assignments (community_id, topic_id) VALUES "
        );
        for (int i = 0; i < normalizedTopicIds.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(?, ?)");
        }
        final Query query = em.createNativeQuery(sql.toString());
        for (int i = 0; i < normalizedTopicIds.size(); i++) {
            query.setParameter(2 * i + 1, communityId);
            query.setParameter(2 * i + 2, normalizedTopicIds.get(i));
        }
        query.executeUpdate();
        LOGGER.info("replaced community topic assignments communityId={} topicCount={}",
                communityId, normalizedTopicIds.size());
    }

    @Override
    public void createMembership(final long communityId, final long userId, final String role) {
        em.createNativeQuery(
                "INSERT INTO community_memberships (community_id, user_id, role) VALUES (:communityId, :userId, :role)"
        )
                .setParameter("communityId", communityId)
                .setParameter("userId", userId)
                .setParameter("role", role)
                .executeUpdate();
        LOGGER.info("created community membership communityId={} userId={} role={}", communityId, userId, role);
    }

    @Override
    public void deleteMembership(final long communityId, final long userId) {
        em.createNativeQuery(
                "DELETE FROM community_memberships WHERE community_id = :communityId AND user_id = :userId"
        )
                .setParameter("communityId", communityId)
                .setParameter("userId", userId)
                .executeUpdate();
        LOGGER.info("deleted community membership communityId={} userId={}", communityId, userId);
    }

    @Override
    public Optional<String> findMembershipRole(final long communityId, final long userId) {
        final List<?> rows = em.createNativeQuery(
                "SELECT role FROM community_memberships WHERE community_id = :communityId AND user_id = :userId"
        )
                .setParameter("communityId", communityId)
                .setParameter("userId", userId)
                .getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable((String) rows.get(0));
    }

    @Override
    public void updateMembershipRole(final long communityId, final long userId, final String newRole) {
        em.createNativeQuery(
                "UPDATE community_memberships SET role = :role WHERE community_id = :communityId AND user_id = :userId"
        )
                .setParameter("role", newRole)
                .setParameter("communityId", communityId)
                .setParameter("userId", userId)
                .executeUpdate();
        LOGGER.info("updated community membership role communityId={} userId={} role={}",
                communityId, userId, newRole);
    }

    @Override
    public List<CommunityMembershipEntry> listMembers(final long communityId) {
        final List<?> rows = em.createNativeQuery(
                "SELECT m.user_id, u.username, m.role, m.joined_at " +
                "FROM community_memberships m " +
                "JOIN users u ON u.user_id = m.user_id " +
                "WHERE m.community_id = :communityId " +
                "ORDER BY (CASE WHEN m.role = 'moderator' THEN 0 ELSE 1 END) ASC, m.joined_at ASC"
        )
                .setParameter("communityId", communityId)
                .getResultList();
        final List<CommunityMembershipEntry> entries = new ArrayList<>(rows.size());
        for (final Object rowObject : rows) {
            final Object[] row = (Object[]) rowObject;
            final long userId = ((Number) row[0]).longValue();
            final String username = (String) row[1];
            final String role = (String) row[2];
            final LocalDateTime joinedAt = row[3] == null ? null : toLocalDateTime(row[3]);
            entries.add(new CommunityMembershipEntry(userId, username, role, joinedAt));
        }
        return entries;
    }

    @Override
    public void setPostHidden(final long postId, final boolean hidden) {
        em.createQuery(
                "UPDATE CommunityPost p SET p.hidden = :hidden WHERE p.id = :postId"
        )
                .setParameter("hidden", hidden)
                .setParameter("postId", postId)
                .executeUpdate();
        LOGGER.info("set community post hidden flag postId={} hidden={}", postId, hidden);
    }

    @Override
    public void setCommentHidden(final long commentId, final boolean hidden) {
        em.createQuery(
                "UPDATE CommunityPostComment c SET c.hidden = :hidden WHERE c.id = :commentId"
        )
                .setParameter("hidden", hidden)
                .setParameter("commentId", commentId)
                .executeUpdate();
        LOGGER.info("set community comment hidden flag commentId={} hidden={}", commentId, hidden);
    }

    private LocalDateTime toLocalDateTime(final Object value) {
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        return null;
    }

    @Override
    public boolean addHelpfulReaction(final long postId, final long userId) {
        final int rows = em.createNativeQuery(
                "INSERT INTO community_post_helpful_reactions (post_id, user_id) " +
                "SELECT :postId, :userId FROM (SELECT 1) AS d " +
                "WHERE NOT EXISTS (" +
                "SELECT 1 FROM community_post_helpful_reactions WHERE post_id = :postId AND user_id = :userId" +
                ")"
        )
                .setParameter("postId", postId)
                .setParameter("userId", userId)
                .executeUpdate();
        if (rows == 0) {
            LOGGER.debug("user id={} already marked community post id={} as helpful", userId, postId);
            return false;
        }
        LOGGER.info("user id={} marked community post id={} as helpful", userId, postId);
        return true;
    }

    @Override
    public boolean removeHelpfulReaction(final long postId, final long userId) {
        final int rows = em.createNativeQuery(
                "DELETE FROM community_post_helpful_reactions WHERE post_id = :postId AND user_id = :userId"
        )
                .setParameter("postId", postId)
                .setParameter("userId", userId)
                .executeUpdate();
        if (rows > 0) {
            LOGGER.info("user id={} removed helpful reaction from community post id={}", userId, postId);
        }
        return rows > 0;
    }

    @Override
    public boolean isHelpfulReactionAddedByUser(final long postId, final long userId) {
        final Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM community_post_helpful_reactions WHERE post_id = :postId AND user_id = :userId"
        )
                .setParameter("postId", postId)
                .setParameter("userId", userId)
                .getSingleResult();
        return count != null && count.longValue() > 0;
    }

    @Override
    public boolean addCommentHelpfulReaction(final long commentId, final long userId) {
        final int rows = em.createNativeQuery(
                "INSERT INTO community_post_comment_helpful_reactions (comment_id, user_id) " +
                "SELECT :commentId, :userId FROM (SELECT 1) AS d " +
                "WHERE NOT EXISTS (" +
                "SELECT 1 FROM community_post_comment_helpful_reactions " +
                "WHERE comment_id = :commentId AND user_id = :userId" +
                ")"
        )
                .setParameter("commentId", commentId)
                .setParameter("userId", userId)
                .executeUpdate();
        if (rows == 0) {
            LOGGER.debug("user id={} already marked community comment id={} as helpful", userId, commentId);
            return false;
        }
        LOGGER.info("user id={} marked community comment id={} as helpful", userId, commentId);
        return true;
    }

    @Override
    public boolean removeCommentHelpfulReaction(final long commentId, final long userId) {
        final int rows = em.createNativeQuery(
                "DELETE FROM community_post_comment_helpful_reactions " +
                "WHERE comment_id = :commentId AND user_id = :userId"
        )
                .setParameter("commentId", commentId)
                .setParameter("userId", userId)
                .executeUpdate();
        if (rows > 0) {
            LOGGER.info("user id={} removed helpful reaction from community comment id={}", userId, commentId);
        }
        return rows > 0;
    }

    @Override
    public boolean isCommentHelpfulReactionAddedByUser(final long commentId, final long userId) {
        final Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM community_post_comment_helpful_reactions " +
                "WHERE comment_id = :commentId AND user_id = :userId"
        )
                .setParameter("commentId", commentId)
                .setParameter("userId", userId)
                .getSingleResult();
        return count != null && count.longValue() > 0;
    }

    @Override
    public Map<Long, List<CommunityTopic>> findTopicsByCommunityIds(final Collection<Long> communityIds) {
        final List<Long> normalizedIds = normalizeIds(communityIds);
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        final Query pairsQuery = em.createNativeQuery(
                "SELECT community_id, topic_id " +
                "FROM community_topic_assignments " +
                "WHERE community_id IN (" + placeholders(normalizedIds.size()) + ") " +
                "ORDER BY community_id ASC, topic_id ASC"
        );
        applyPositionalParameters(pairsQuery, normalizedIds);

        final List<?> pairRows = pairsQuery.getResultList();
        if (pairRows.isEmpty()) {
            return Collections.emptyMap();
        }

        final Set<Short> topicIds = new LinkedHashSet<>();
        final Map<Long, List<Short>> topicIdsByCommunity = new LinkedHashMap<>();
        for (final Object pairRow : pairRows) {
            final Object[] row = (Object[]) pairRow;
            final long communityId = ((Number) row[0]).longValue();
            final short topicId = ((Number) row[1]).shortValue();
            topicIds.add(topicId);
            topicIdsByCommunity.computeIfAbsent(communityId, ignored -> new ArrayList<>()).add(topicId);
        }

        final List<CommunityTopic> topics = em.createQuery(
                "SELECT t FROM CommunityTopic t WHERE t.id IN :ids ORDER BY t.id ASC",
                CommunityTopic.class
        )
                .setParameter("ids", topicIds)
                .getResultList();
        final Map<Short, CommunityTopic> topicsById = topics.stream().collect(Collectors.toMap(
                CommunityTopic::getId,
                topic -> topic
        ));

        final Map<Long, List<CommunityTopic>> topicsByCommunity = new LinkedHashMap<>();
        for (final Map.Entry<Long, List<Short>> entry : topicIdsByCommunity.entrySet()) {
            final List<CommunityTopic> communityTopics = entry.getValue().stream()
                    .map(topicsById::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            topicsByCommunity.put(entry.getKey(), communityTopics);
        }
        return topicsByCommunity;
    }

    @Override
    public List<CommunityPost> findPostsByCommunityId(final long communityId) {
        return em.createQuery(
                "SELECT p FROM CommunityPost p " +
                "LEFT JOIN FETCH p.author " +
                "WHERE p.community.id = :communityId " +
                "ORDER BY p.createdAt DESC, p.id DESC",
                CommunityPost.class
        )
                .setParameter("communityId", communityId)
                .getResultList();
    }

    @Override
    public Page<CommunityPost> findVisiblePostsByCommunityId(final long communityId,
                                                             final String sort,
                                                             final int page) {
        final int pageSize = Pagination.COMMUNITY_POSTS_PAGE_SIZE;
        final Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM community_posts WHERE community_id = ? AND hidden = false"
        )
                .setParameter(1, communityId)
                .getSingleResult();
        final long totalItems = count == null ? 0L : count.longValue();
        if (totalItems <= 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }

        final int effectivePage = Pagination.clampPage(Pagination.normalizePage(page), totalItems, pageSize);
        final Query idsQuery = em.createNativeQuery(visiblePostIdsSql(sort));
        idsQuery.setParameter(1, communityId);
        idsQuery.setParameter(2, pageSize);
        idsQuery.setParameter(3, Pagination.offsetFor(effectivePage, pageSize));

        final List<Long> ids = toLongIds(idsQuery.getResultList());
        if (ids.isEmpty()) {
            return Page.empty(effectivePage, pageSize);
        }

        final List<CommunityPost> posts = em.createQuery(
                "SELECT p FROM CommunityPost p " +
                "LEFT JOIN FETCH p.author " +
                "WHERE p.id IN :ids",
                CommunityPost.class
        )
                .setParameter("ids", ids)
                .getResultList();
        return new Page<>(sortByIds(posts, ids, CommunityPost::getId), effectivePage, pageSize, totalItems);
    }

    @Override
    public Optional<CommunityPost> findPostByCommunityIdAndSlug(final long communityId, final String postSlug) {
        if (postSlug == null || postSlug.trim().isEmpty()) {
            return Optional.empty();
        }
        final List<CommunityPost> results = em.createQuery(
                "SELECT p FROM CommunityPost p " +
                "LEFT JOIN FETCH p.author " +
                "WHERE p.community.id = :communityId AND LOWER(p.slug) = :slug",
                CommunityPost.class
        )
                .setParameter("communityId", communityId)
                .setParameter("slug", postSlug.trim().toLowerCase(Locale.ROOT))
                .getResultList();
        return results.stream().findFirst();
    }

    @Override
    public Optional<CommunityPostComment> findCommentById(final long commentId) {
        return Optional.ofNullable(em.find(CommunityPostComment.class, commentId));
    }

    @Override
    public boolean deletePost(final long postId) {
        final CommunityPost post = em.find(CommunityPost.class, postId);
        if (post == null) {
            return false;
        }
        em.remove(post);
        LOGGER.info("deleted community post id={}", postId);
        return true;
    }

    @Override
    public boolean deleteComment(final long commentId) {
        final CommunityPostComment comment = em.find(CommunityPostComment.class, commentId);
        if (comment == null) {
            return false;
        }
        em.remove(comment);
        LOGGER.info("deleted community post comment id={}", commentId);
        return true;
    }

    @Override
    public List<CommunityPostComment> findCommentsByPostId(final long postId) {
        return em.createQuery(
                "SELECT c FROM CommunityPostComment c " +
                "LEFT JOIN FETCH c.user " +
                "WHERE c.post.id = :postId " +
                "ORDER BY c.createdAt ASC, c.id ASC",
                CommunityPostComment.class
        )
                .setParameter("postId", postId)
                .getResultList();
    }

    public Map<Long, Long> countMembersByCommunityIds(final Collection<Long> communityIds) {
        final List<Long> normalizedIds = normalizeIds(communityIds);
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        final Query countQuery = em.createNativeQuery(
                "SELECT community_id, COUNT(*) " +
                "FROM community_memberships " +
                "WHERE community_id IN (" + placeholders(normalizedIds.size()) + ") " +
                "GROUP BY community_id"
        );
        applyPositionalParameters(countQuery, normalizedIds);
        return numberMap(countQuery.getResultList());
    }

    @Override
    public Set<Long> findJoinedCommunityIds(final long userId, final Collection<Long> communityIds) {
        final List<Long> normalizedIds = normalizeIds(communityIds);
        if (normalizedIds.isEmpty()) {
            return Collections.emptySet();
        }

        final Query membershipQuery = em.createNativeQuery(
                "SELECT community_id " +
                "FROM community_memberships " +
                "WHERE user_id = ? AND community_id IN (" + placeholders(normalizedIds.size()) + ")"
        );
        membershipQuery.setParameter(1, userId);
        for (int i = 0; i < normalizedIds.size(); i++) {
            membershipQuery.setParameter(i + 2, normalizedIds.get(i));
        }

        final List<?> rawIds = membershipQuery.getResultList();
        return rawIds.stream()
                .map(value -> ((Number) value).longValue())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Map<Long, Long> countWeeklyPostsByCommunityIds(final Collection<Long> communityIds,
                                                          final LocalDateTime since) {
        final List<Long> normalizedIds = normalizeIds(communityIds);
        if (normalizedIds.isEmpty() || since == null) {
            return Collections.emptyMap();
        }

        final List<?> rows = em.createQuery(
                "SELECT p.community.id, COUNT(p) " +
                "FROM CommunityPost p " +
                "WHERE p.community.id IN :communityIds AND p.createdAt >= :since " +
                "GROUP BY p.community.id"
        )
                .setParameter("communityIds", normalizedIds)
                .setParameter("since", since)
                .getResultList();
        return numberMap(rows);
    }

    @Override
    public Map<Long, Long> countCommentsByPostIds(final Collection<Long> postIds) {
        final List<Long> normalizedIds = normalizeIds(postIds);
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        final String placeholders = placeholders(normalizedIds.size());
        final Query countQuery = em.createNativeQuery(
                "SELECT c.post_id, COUNT(*) " +
                "FROM community_post_comments c " +
                "WHERE c.post_id IN (" + placeholders + ") " +
                "GROUP BY c.post_id"
        );
        applyPositionalParameters(countQuery, normalizedIds);
        return numberMap(countQuery.getResultList());
    }

    @Override
    public Map<Long, Long> countHelpfulReactionsByPostIds(final Collection<Long> postIds) {
        final List<Long> normalizedIds = normalizeIds(postIds);
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        final Query countQuery = em.createNativeQuery(
                "SELECT post_id, COUNT(*) " +
                "FROM community_post_helpful_reactions " +
                "WHERE post_id IN (" + placeholders(normalizedIds.size()) + ") " +
                "GROUP BY post_id"
        );
        applyPositionalParameters(countQuery, normalizedIds);
        return numberMap(countQuery.getResultList());
    }

    @Override
    public Map<Long, Long> countHelpfulReactionsByCommentIds(final Collection<Long> commentIds) {
        final List<Long> normalizedIds = normalizeIds(commentIds);
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        final Query countQuery = em.createNativeQuery(
                "SELECT comment_id, COUNT(*) " +
                "FROM community_post_comment_helpful_reactions " +
                "WHERE comment_id IN (" + placeholders(normalizedIds.size()) + ") " +
                "GROUP BY comment_id"
        );
        applyPositionalParameters(countQuery, normalizedIds);
        return numberMap(countQuery.getResultList());
    }

    private String communitySearchFromJoin(final CommunitySearchCriteria criteria) {
        final boolean needsMemberCounts = CommunitySearchCriteria.SORT_MEMBERS.equals(criteria.getSortBy());
        final boolean needsPostCounts = criteria.getSortBy() == null
                || CommunitySearchCriteria.SORT_ACTIVE.equals(criteria.getSortBy());
        final StringBuilder sql = new StringBuilder("FROM communities c ");
        if (needsMemberCounts) {
            sql.append("LEFT JOIN (")
                    .append("SELECT community_id, COUNT(*) AS member_count ")
                    .append("FROM community_memberships GROUP BY community_id")
                    .append(") mc ON mc.community_id = c.community_id ");
        }
        if (needsPostCounts) {
            sql.append("LEFT JOIN (")
                    .append("SELECT community_id, COUNT(*) AS post_count ")
                    .append("FROM community_posts ")
                    .append("WHERE hidden = false ")
                    .append("GROUP BY community_id")
                    .append(") pc ON pc.community_id = c.community_id ");
        }
        return sql.toString();
    }

    private String buildCommunityWhereClause(final CommunitySearchCriteria criteria,
                                             final Long currentUserId,
                                             final List<Object> params) {
        final StringBuilder sql = new StringBuilder();
        boolean hasWhere = false;

        if (criteria.getQ() != null) {
            final String escaped = criteria.getQ().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            final String likeQ = "%" + escaped + "%";
            final String tsQ = criteria.getQ().replaceAll("[%_\\\\]", " ").trim();
            final boolean useTsQuery = tsQ.matches(".*[a-zA-Z0-9]{2,}.*");
            if (useTsQuery) {
                sql.append("WHERE (c.search_vector @@ websearch_to_tsquery('simple', ?) ")
                        .append("OR lower(c.name) LIKE ? ESCAPE '\\' ")
                        .append("OR lower(c.slug) LIKE ? ESCAPE '\\' ")
                        .append("OR lower(COALESCE(c.description, '')) LIKE ? ESCAPE '\\' ")
                        .append("OR EXISTS (")
                        .append("SELECT 1 FROM community_topic_assignments cta ")
                        .append("JOIN community_topics ct ON ct.topic_id = cta.topic_id ")
                        .append("WHERE cta.community_id = c.community_id AND lower(ct.code) LIKE ? ESCAPE '\\'")
                        .append(")) ");
                params.add(tsQ);
            } else {
                sql.append("WHERE (lower(c.name) LIKE ? ESCAPE '\\' ")
                        .append("OR lower(c.slug) LIKE ? ESCAPE '\\' ")
                        .append("OR lower(COALESCE(c.description, '')) LIKE ? ESCAPE '\\' ")
                        .append("OR EXISTS (")
                        .append("SELECT 1 FROM community_topic_assignments cta ")
                        .append("JOIN community_topics ct ON ct.topic_id = cta.topic_id ")
                        .append("WHERE cta.community_id = c.community_id AND lower(ct.code) LIKE ? ESCAPE '\\'")
                        .append(")) ");
            }
            params.add(likeQ);
            params.add(likeQ);
            params.add(likeQ);
            params.add(likeQ);
            hasWhere = true;
        }

        if (criteria.getTopic() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ")
                    .append("EXISTS (")
                    .append("SELECT 1 FROM community_topic_assignments cta ")
                    .append("JOIN community_topics ct ON ct.topic_id = cta.topic_id ")
                    .append("WHERE cta.community_id = c.community_id AND lower(ct.code) = ?")
                    .append(") ");
            params.add(criteria.getTopic());
            hasWhere = true;
        }

        if (criteria.isJoinedOnly()) {
            if (currentUserId == null || currentUserId <= 0L) {
                sql.append(hasWhere ? "AND " : "WHERE ").append("1 = 0 ");
            } else {
                sql.append(hasWhere ? "AND " : "WHERE ")
                        .append("EXISTS (")
                        .append("SELECT 1 FROM community_memberships cm ")
                        .append("WHERE cm.community_id = c.community_id AND cm.user_id = ?")
                        .append(") ");
                params.add(currentUserId);
            }
        }

        return sql.toString();
    }

    private String buildCommunityOrderClause(final CommunitySearchCriteria criteria) {
        final String sortBy = criteria.getSortBy();
        if (CommunitySearchCriteria.SORT_MEMBERS.equals(sortBy)) {
            return "ORDER BY COALESCE(mc.member_count, 0) DESC, lower(c.name) ASC, c.community_id ASC";
        }
        if (CommunitySearchCriteria.SORT_NAME_ASC.equals(sortBy)) {
            return "ORDER BY lower(c.name) ASC, c.community_id ASC";
        }
        if (CommunitySearchCriteria.SORT_NEWEST.equals(sortBy)) {
            return "ORDER BY c.created_at DESC, c.community_id DESC";
        }
        return "ORDER BY COALESCE(pc.post_count, 0) DESC, c.created_at DESC, c.community_id DESC";
    }

    private List<Long> normalizeIds(final Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Long> toLongIds(final List<?> rawIds) {
        if (rawIds == null || rawIds.isEmpty()) {
            return Collections.emptyList();
        }
        return rawIds.stream()
                .map(value -> ((Number) value).longValue())
                .collect(Collectors.toList());
    }

    private String visiblePostIdsSql(final String sort) {
        if ("helpful".equals(sort)) {
            return "SELECT p.post_id " +
                    "FROM community_posts p " +
                    "LEFT JOIN community_post_helpful_reactions r ON r.post_id = p.post_id " +
                    "WHERE p.community_id = ? AND p.hidden = false " +
                    "GROUP BY p.post_id, p.created_at " +
                    "ORDER BY COUNT(r.user_id) DESC, p.created_at DESC, p.post_id DESC LIMIT ? OFFSET ?";
        }
        if ("commented".equals(sort)) {
            return "SELECT p.post_id " +
                    "FROM community_posts p " +
                    "LEFT JOIN community_post_comments c ON c.post_id = p.post_id " +
                    "WHERE p.community_id = ? AND p.hidden = false " +
                    "GROUP BY p.post_id, p.created_at " +
                    "ORDER BY COUNT(c.comment_id) DESC, p.created_at DESC, p.post_id DESC LIMIT ? OFFSET ?";
        }
        return "SELECT p.post_id " +
                "FROM community_posts p " +
                "WHERE p.community_id = ? AND p.hidden = false " +
                "ORDER BY p.created_at DESC, p.post_id DESC LIMIT ? OFFSET ?";
    }

    private <T> List<T> sortByIds(final List<T> items, final List<Long> ids,
                                  final java.util.function.ToLongFunction<T> idExtractor) {
        final Map<Long, Integer> positionById = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            positionById.put(ids.get(i), i);
        }
        return items.stream()
                .sorted(Comparator.comparingInt(item ->
                        positionById.getOrDefault(idExtractor.applyAsLong(item), Integer.MAX_VALUE)))
                .collect(Collectors.toList());
    }

    private List<Short> normalizeTopicIds(final Collection<Short> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private String placeholders(final int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(ignored -> "?")
                .collect(Collectors.joining(", "));
    }

    private void applyPositionalParameters(final Query query, final List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            query.setParameter(i + 1, ids.get(i));
        }
    }

    private Map<Long, Long> numberMap(final List<?> rows) {
        final Map<Long, Long> result = new HashMap<>();
        for (final Object rowObject : rows) {
            final Object[] row = (Object[]) rowObject;
            result.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return result;
    }
}
