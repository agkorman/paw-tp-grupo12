package ar.edu.itba.paw.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class ReviewLikeJdbcDao implements ReviewLikeDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public ReviewLikeJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public boolean likeReview(final long reviewId, final long userId) {
        try {
            return jdbcTemplate.update(
                    "INSERT INTO review_likes (review_id, user_id) VALUES (?, ?)",
                    reviewId,
                    userId
            ) > 0;
        } catch (final DuplicateKeyException ignored) {
            return false;
        }
    }

    @Override
    public boolean unlikeReview(final long reviewId, final long userId) {
        return jdbcTemplate.update(
                "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?",
                reviewId,
                userId
        ) > 0;
    }

    @Override
    public boolean isReviewLikedByUser(final long reviewId, final long userId) {
        return exists("review_likes", "review_id", reviewId, userId);
    }

    @Override
    public long countReviewLikes(final long reviewId) {
        return count("review_likes", "review_id", reviewId);
    }

    @Override
    public Map<Long, Long> countReviewLikesByReviewIds(final Collection<Long> reviewIds) {
        return countByIds("review_likes", "review_id", "reviewIds", reviewIds);
    }

    @Override
    public Set<Long> findLikedReviewIds(final Collection<Long> reviewIds, final long userId) {
        return findLikedIds("review_likes", "review_id", "reviewIds", reviewIds, userId);
    }

    @Override
    public List<Long> findLikedReviewIdsByUserId(final long userId) {
        return jdbcTemplate.queryForList(
                "SELECT review_id FROM review_likes WHERE user_id = ? ORDER BY created_at DESC, review_id DESC",
                Long.class,
                userId
        );
    }

    @Override
    public boolean likeReply(final long replyId, final long userId) {
        try {
            return jdbcTemplate.update(
                    "INSERT INTO review_reply_likes (reply_id, user_id) VALUES (?, ?)",
                    replyId,
                    userId
            ) > 0;
        } catch (final DuplicateKeyException ignored) {
            return false;
        }
    }

    @Override
    public boolean unlikeReply(final long replyId, final long userId) {
        return jdbcTemplate.update(
                "DELETE FROM review_reply_likes WHERE reply_id = ? AND user_id = ?",
                replyId,
                userId
        ) > 0;
    }

    @Override
    public boolean isReplyLikedByUser(final long replyId, final long userId) {
        return exists("review_reply_likes", "reply_id", replyId, userId);
    }

    @Override
    public long countReplyLikes(final long replyId) {
        return count("review_reply_likes", "reply_id", replyId);
    }

    @Override
    public Map<Long, Long> countReplyLikesByReplyIds(final Collection<Long> replyIds) {
        return countByIds("review_reply_likes", "reply_id", "replyIds", replyIds);
    }

    @Override
    public Set<Long> findLikedReplyIds(final Collection<Long> replyIds, final long userId) {
        return findLikedIds("review_reply_likes", "reply_id", "replyIds", replyIds, userId);
    }

    @Override
    public List<Long> findLikedReplyIdsByUserId(final long userId) {
        return jdbcTemplate.queryForList(
                "SELECT reply_id FROM review_reply_likes WHERE user_id = ? ORDER BY created_at DESC, reply_id DESC",
                Long.class,
                userId
        );
    }

    private boolean exists(final String tableName, final String idColumn, final long id, final long userId) {
        final Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM " + tableName + " WHERE " + idColumn + " = ? AND user_id = ?)",
                Boolean.class,
                id,
                userId
        );
        return Boolean.TRUE.equals(exists);
    }

    private long count(final String tableName, final String idColumn, final long id) {
        final Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE " + idColumn + " = ?",
                Long.class,
                id
        );
        return count == null ? 0 : count;
    }

    private Map<Long, Long> countByIds(final String tableName, final String idColumn, final String paramName,
                                       final Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        final Map<Long, Long> counts = new HashMap<>();
        namedParameterJdbcTemplate.query(
                "SELECT " + idColumn + " AS liked_id, COUNT(*) AS like_count "
                        + "FROM " + tableName + " WHERE " + idColumn + " IN (:" + paramName + ") "
                        + "GROUP BY " + idColumn,
                new MapSqlParameterSource(paramName, ids),
                rs -> {
                    counts.put(rs.getLong("liked_id"), rs.getLong("like_count"));
                }
        );
        return counts;
    }

    private Set<Long> findLikedIds(final String tableName, final String idColumn, final String paramName,
                                   final Collection<Long> ids, final long userId) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }

        final MapSqlParameterSource params = new MapSqlParameterSource(paramName, ids)
                .addValue("userId", userId);
        return new HashSet<>(namedParameterJdbcTemplate.queryForList(
                "SELECT " + idColumn + " FROM " + tableName
                        + " WHERE user_id = :userId AND " + idColumn + " IN (:" + paramName + ")",
                params,
                Long.class
        ));
    }
}
