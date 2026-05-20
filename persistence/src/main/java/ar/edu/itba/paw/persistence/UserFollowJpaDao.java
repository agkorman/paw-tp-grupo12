package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserFollowJpaDao implements UserFollowDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserFollowJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean follow(final long followerId, final long followedId) {
        final int rows = em.createNativeQuery(
                        "INSERT INTO user_follows (follower_id, followed_id) " +
                        "SELECT :followerId, :followedId FROM (SELECT 1) AS d " +
                        "WHERE NOT EXISTS (SELECT 1 FROM user_follows WHERE follower_id = :followerId AND followed_id = :followedId)")
                .setParameter("followerId", followerId)
                .setParameter("followedId", followedId)
                .executeUpdate();
        if (rows == 0) {
            LOGGER.debug("user id={} already follows user id={}", followerId, followedId);
            return false;
        }
        LOGGER.info("user id={} followed user id={}", followerId, followedId);
        return true;
    }

    @Override
    public boolean unfollow(final long followerId, final long followedId) {
        final int rows = em.createNativeQuery(
                        "DELETE FROM user_follows WHERE follower_id = :followerId AND followed_id = :followedId")
                .setParameter("followerId", followerId)
                .setParameter("followedId", followedId)
                .executeUpdate();
        final boolean removed = rows > 0;
        if (removed) {
            LOGGER.info("user id={} unfollowed user id={}", followerId, followedId);
        }
        return removed;
    }

    @Override
    public boolean isFollowing(final long followerId, final long followedId) {
        final Number count = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM user_follows WHERE follower_id = :followerId AND followed_id = :followedId")
                .setParameter("followerId", followerId)
                .setParameter("followedId", followedId)
                .getSingleResult();
        return count.intValue() > 0;
    }

    @Override
    public long countFollowers(final long userId) {
        final Number count = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM user_follows WHERE followed_id = :userId")
                .setParameter("userId", userId)
                .getSingleResult();
        return count.longValue();
    }

    @Override
    public long countFollowing(final long userId) {
        final Number count = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM user_follows WHERE follower_id = :userId")
                .setParameter("userId", userId)
                .getSingleResult();
        return count.longValue();
    }

    @Override
    public Page<User> findFollowers(final long userId, final int page) {
        final int pageSize = Pagination.CONNECTIONS_PAGE_SIZE;
        final long total = countFollowers(userId);
        if (total == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }
        final int effectivePage = Pagination.clampPage(page, total, pageSize);
        final long offset = Pagination.offsetFor(effectivePage, pageSize);
        final List<?> ids = em.createNativeQuery(
                "SELECT u.user_id FROM users u JOIN user_follows f ON f.follower_id = u.user_id " +
                "WHERE f.followed_id = ? ORDER BY f.created_at DESC, u.username ASC LIMIT ? OFFSET ?")
                .setParameter(1, userId)
                .setParameter(2, pageSize)
                .setParameter(3, offset)
                .getResultList();
        if (ids.isEmpty()) {
            return Page.empty(effectivePage, pageSize);
        }
        final List<Long> longIds = ids.stream().map(r -> ((Number) r).longValue()).collect(Collectors.toList());
        final List<User> items = sortUsersByIds(loadUsersByIds(longIds), longIds);
        return new Page<>(items, effectivePage, pageSize, total);
    }

    @Override
    public Page<User> findFollowing(final long userId, final int page) {
        final int pageSize = Pagination.CONNECTIONS_PAGE_SIZE;
        final long total = countFollowing(userId);
        if (total == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }
        final int effectivePage = Pagination.clampPage(page, total, pageSize);
        final long offset = Pagination.offsetFor(effectivePage, pageSize);
        final List<?> ids = em.createNativeQuery(
                "SELECT u.user_id FROM users u JOIN user_follows f ON f.followed_id = u.user_id " +
                "WHERE f.follower_id = ? ORDER BY f.created_at DESC, u.username ASC LIMIT ? OFFSET ?")
                .setParameter(1, userId)
                .setParameter(2, pageSize)
                .setParameter(3, offset)
                .getResultList();
        if (ids.isEmpty()) {
            return Page.empty(effectivePage, pageSize);
        }
        final List<Long> longIds = ids.stream().map(r -> ((Number) r).longValue()).collect(Collectors.toList());
        final List<User> items = sortUsersByIds(loadUsersByIds(longIds), longIds);
        return new Page<>(items, effectivePage, pageSize, total);
    }

    private List<User> loadUsersByIds(final List<Long> ids) {
        return em.createQuery(
                "SELECT u FROM User u WHERE u.id IN :ids",
                User.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    private List<User> sortUsersByIds(final List<User> users, final List<Long> orderedIds) {
        final Map<Long, User> byId = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        return orderedIds.stream().map(byId::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public Set<Long> getFollowedIds(final long followerId, final Collection<Long> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return Set.of();
        }
        final List<?> ids = em.createNativeQuery(
                        "SELECT followed_id FROM user_follows WHERE follower_id = :followerId AND followed_id IN (:targetIds)")
                .setParameter("followerId", followerId)
                .setParameter("targetIds", targetIds)
                .getResultList();
        final Set<Long> result = new HashSet<>();
        for (final Object id : ids) {
            result.add(((Number) id).longValue());
        }
        return result;
    }
}
