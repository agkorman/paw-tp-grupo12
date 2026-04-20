package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class UserFollowJdbcDao implements UserFollowDao {

    private static final String USER_SELECT =
            "SELECT u.user_id, u.username, u.email, u.password, u.role, u.created_at FROM users u ";

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("user_id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("role"),
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
            return jdbcTemplate.update(
                    "INSERT INTO user_follows (follower_id, followed_id) VALUES (?, ?)",
                    followerId,
                    followedId
            ) > 0;
        } catch (final DuplicateKeyException ignored) {
            return false;
        }
    }

    @Override
    public boolean unfollow(final long followerId, final long followedId) {
        return jdbcTemplate.update(
                "DELETE FROM user_follows WHERE follower_id = ? AND followed_id = ?",
                followerId,
                followedId
        ) > 0;
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
    public List<User> findFollowers(final long userId) {
        return jdbcTemplate.query(
                USER_SELECT
                        + "JOIN user_follows f ON f.follower_id = u.user_id "
                        + "WHERE f.followed_id = ? ORDER BY f.created_at DESC, u.username ASC",
                USER_ROW_MAPPER,
                userId
        );
    }

    @Override
    public List<User> findFollowing(final long userId) {
        return jdbcTemplate.query(
                USER_SELECT
                        + "JOIN user_follows f ON f.followed_id = u.user_id "
                        + "WHERE f.follower_id = ? ORDER BY f.created_at DESC, u.username ASC",
                USER_ROW_MAPPER,
                userId
        );
    }
}
