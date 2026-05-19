package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ReviewReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ReviewReplyJdbcDao implements ReviewReplyDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewReplyJdbcDao.class);

    private static final String REPLY_SELECT =
            "SELECT rr.reply_id, rr.review_id, rr.user_id, u.username AS author_username, "
                    + "rr.body, rr.created_at, rr.updated_at "
                    + "FROM review_replies rr JOIN users u ON rr.user_id = u.user_id ";

    private static final RowMapper<ReviewReply> ROW_MAPPER = (rs, rowNum) -> new ReviewReply(
            rs.getLong("reply_id"),
            rs.getLong("review_id"),
            rs.getLong("user_id"),
            rs.getString("author_username"),
            rs.getString("body"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
    );

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    @Autowired
    public ReviewReplyJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("review_replies")
                .usingColumns("review_id", "user_id", "body")
                .usingGeneratedKeyColumns("reply_id");
    }

    @Override
    public Optional<ReviewReply> findById(final long id) {
        return jdbcTemplate.query(
                REPLY_SELECT + "WHERE rr.reply_id = ?",
                ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    @Override
    public List<ReviewReply> findByIds(final Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return namedParameterJdbcTemplate.query(
                REPLY_SELECT + "WHERE rr.reply_id IN (:replyIds)",
                new MapSqlParameterSource("replyIds", ids),
                ROW_MAPPER
        );
    }

    @Override
    public List<ReviewReply> findByReviewId(final long reviewId) {
        return jdbcTemplate.query(
                REPLY_SELECT + "WHERE rr.review_id = ? ORDER BY rr.created_at ASC, rr.reply_id ASC",
                ROW_MAPPER,
                reviewId
        );
    }

    @Override
    public List<ReviewReply> findByReviewIds(final Collection<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return List.of();
        }
        return namedParameterJdbcTemplate.query(
                REPLY_SELECT + "WHERE rr.review_id IN (:reviewIds) "
                        + "ORDER BY rr.review_id ASC, rr.created_at ASC, rr.reply_id ASC",
                new MapSqlParameterSource("reviewIds", reviewIds),
                ROW_MAPPER
        );
    }

    @Override
    public ReviewReply create(final long reviewId, final long userId, final String body) {
        final Map<String, Object> params = new HashMap<>();
        params.put("review_id", reviewId);
        params.put("user_id", userId);
        params.put("body", body);

        final long id = jdbcInsert.executeAndReturnKey(params).longValue();
        LOGGER.info("created reply id={} reviewId={} userId={}", id, reviewId, userId);
        return findById(id).orElseThrow();
    }

    @Override
    public Map<Long, Long> countNewRepliesPerReview(final long userId, final LocalDateTime since) {
        return jdbcTemplate.query(
                "SELECT r.review_id, COUNT(*) AS reply_count "
                        + "FROM reviews r "
                        + "JOIN review_replies rr ON rr.review_id = r.review_id "
                        + "WHERE r.user_id = ? AND rr.created_at >= ? "
                        + "GROUP BY r.review_id",
                rs -> {
                    final Map<Long, Long> counts = new HashMap<>();
                    while (rs.next()) {
                        counts.put(rs.getLong("review_id"), rs.getLong("reply_count"));
                    }
                    return counts;
                },
                userId,
                Timestamp.valueOf(since)
        );
    }

    @Override
    public boolean update(final long id, final String body) {
        final boolean updated = jdbcTemplate.update(
                "UPDATE review_replies SET body = ?, updated_at = CURRENT_TIMESTAMP WHERE reply_id = ?",
                body, id) > 0;
        if (updated) {
            LOGGER.info("updated reply id={}", id);
        } else {
            LOGGER.warn("reply update affected 0 rows id={}", id);
        }
        return updated;
    }

    @Override
    public boolean delete(final long id) {
        final boolean deleted = jdbcTemplate.update("DELETE FROM review_replies WHERE reply_id = ?", id) > 0;
        if (deleted) {
            LOGGER.info("deleted reply id={}", id);
        } else {
            LOGGER.warn("reply delete affected 0 rows id={}", id);
        }
        return deleted;
    }
}
