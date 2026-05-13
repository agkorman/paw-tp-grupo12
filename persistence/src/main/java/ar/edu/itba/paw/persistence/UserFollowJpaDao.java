package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class UserFollowJpaDao implements UserFollowDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserFollowJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean follow(final long followerId, final long followedId) {
        final Number count = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM user_follows WHERE follower_id = :followerId AND followed_id = :followedId")
                .setParameter("followerId", followerId)
                .setParameter("followedId", followedId)
                .getSingleResult();
        if (count.intValue() > 0) {
            LOGGER.debug("user id={} already follows user id={}", followerId, followedId);
            return false;
        }
        em.createNativeQuery("INSERT INTO user_follows (follower_id, followed_id) VALUES (:followerId, :followedId)")
                .setParameter("followerId", followerId)
                .setParameter("followedId", followedId)
                .executeUpdate();
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
    @SuppressWarnings("unchecked")
    public List<User> findFollowers(final long userId) {
        return (List<User>) em.createNativeQuery(
                        "SELECT u.user_id, u.username, u.email, u.password, u.role, u.preferred_locale, u.created_at "
                        + "FROM users u JOIN user_follows f ON f.follower_id = u.user_id "
                        + "WHERE f.followed_id = :userId ORDER BY f.created_at DESC, u.username ASC",
                        User.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<User> findFollowing(final long userId) {
        return (List<User>) em.createNativeQuery(
                        "SELECT u.user_id, u.username, u.email, u.password, u.role, u.preferred_locale, u.created_at "
                        + "FROM users u JOIN user_follows f ON f.followed_id = u.user_id "
                        + "WHERE f.follower_id = :userId ORDER BY f.created_at DESC, u.username ASC",
                        User.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Long> getFollowedIds(final long followerId, final Collection<Long> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return Set.of();
        }
        final List<Number> ids = em.createNativeQuery(
                        "SELECT followed_id FROM user_follows WHERE follower_id = :followerId AND followed_id IN (:targetIds)")
                .setParameter("followerId", followerId)
                .setParameter("targetIds", targetIds)
                .getResultList();
        final Set<Long> result = new HashSet<>();
        for (final Number id : ids) {
            result.add(id.longValue());
        }
        return result;
    }
}
