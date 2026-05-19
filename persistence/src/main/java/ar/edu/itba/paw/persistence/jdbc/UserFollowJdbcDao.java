package ar.edu.itba.paw.persistence.jdbc;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.UserFollowDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserFollowJdbcDao implements UserFollowDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserFollowJdbcDao.class);

    private static final String USER_SELECT =
        "SELECT u.user_id, u.username, u.email, u.password, u.role, u.preferred_locale, u.created_at FROM users u ";

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) ->
        new User(
            rs.getLong("user_id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("role"),
            rs.getString("preferred_locale"),
            rs.getTimestamp("created_at").toLocalDateTime()
        );

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserFollowJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean follow(final long followerId, final long followedId) {
        try {
            final boolean inserted =
                jdbcTemplate.update(
                    "INSERT INTO user_follows (follower_id, followed_id) VALUES (?, ?)",
                    followerId,
                    followedId
                ) > 0;
            if (inserted) {
                LOGGER.info("user id={} followed user id={}", followerId, followedId);
            }
            return inserted;
        } catch (final DuplicateKeyException ignored) {
            LOGGER.debug("user id={} already follows user id={}", followerId, followedId);
            return false;
        }
    }

    @Override
    public boolean unfollow(final long followerId, final long followedId) {
        final boolean removed =
            jdbcTemplate.update(
                "DELETE FROM user_follows WHERE follower_id = ? AND followed_id = ?",
                followerId,
                followedId
            ) > 0;
        if (removed) {
            LOGGER.info("user id={} unfollowed user id={}", followerId, followedId);
        }
        return removed;
    }

    @Override
    public boolean isFollowing(final long followerId, final long followedId) {
        final Boolean exists = jdbcTemplate.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM user_follows WHERE follower_id = ? AND followed_id = ?)",
            Boolean.class,
            followerId,
            followedId
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public long countFollowers(final long userId) {
        final Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_follows WHERE followed_id = ?",
            Long.class,
            userId
        );
        return count == null ? 0 : count;
    }

    @Override
    public long countFollowing(final long userId) {
        final Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_follows WHERE follower_id = ?",
            Long.class,
            userId
        );
        return count == null ? 0 : count;
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
        final List<User> items = jdbcTemplate.query(
                USER_SELECT
                        + "JOIN user_follows f ON f.follower_id = u.user_id "
                        + "WHERE f.followed_id = ? ORDER BY f.created_at DESC, u.username ASC "
                        + "LIMIT ? OFFSET ?",
                USER_ROW_MAPPER,
                userId,
                pageSize,
                offset
        );
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
        final List<User> items = jdbcTemplate.query(
                USER_SELECT
                        + "JOIN user_follows f ON f.followed_id = u.user_id "
                        + "WHERE f.follower_id = ? ORDER BY f.created_at DESC, u.username ASC "
                        + "LIMIT ? OFFSET ?",
                USER_ROW_MAPPER,
                userId,
                pageSize,
                offset
        );
        return new Page<>(items, effectivePage, pageSize, total);
    }

    @Override
    public Set<Long> getFollowedIds(final long followerId, final Collection<Long> targetIds) {
        if (targetIds.isEmpty()) {
            return Set.of();
        }
        final String placeholders = String.join(",", targetIds.stream().map(id -> "?").toArray(String[]::new));
        final List<Long> followedIds = jdbcTemplate.queryForList(
            "SELECT followed_id FROM user_follows WHERE follower_id = ? AND followed_id IN (" + placeholders + ")",
            Long.class,
            buildFollowedIdsParams(followerId, targetIds)
        );
        return new HashSet<>(followedIds);
    }

    private Object[] buildFollowedIdsParams(final long followerId, final Collection<Long> targetIds) {
        final Object[] params = new Object[targetIds.size() + 1];
        params[0] = followerId;
        int i = 1;
        for (final Long id : targetIds) {
            params[i++] = id;
        }
        return params;
    }
}
