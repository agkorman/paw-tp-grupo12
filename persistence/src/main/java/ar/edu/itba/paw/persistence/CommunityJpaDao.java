package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.CommunityPostComment;
import ar.edu.itba.paw.model.CommunityTopic;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
                "SELECT t FROM CommunityTopic t ORDER BY t.id ASC",
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

    private List<Long> normalizeIds(final Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
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
