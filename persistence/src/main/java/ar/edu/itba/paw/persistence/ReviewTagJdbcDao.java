package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ReviewTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class ReviewTagJdbcDao implements ReviewTagDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewTagJdbcDao.class);

    private static final String TAG_SELECT =
            "SELECT tag_id, code, label_es, sentiment, dimension, created_at FROM review_tags ";

    private static final RowMapper<ReviewTag> ROW_MAPPER = (rs, rowNum) -> new ReviewTag(
            rs.getShort("tag_id"),
            rs.getString("code"),
            rs.getString("label_es"),
            rs.getString("sentiment"),
            rs.getString("dimension"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public ReviewTagJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public List<ReviewTag> findAll() {
        return jdbcTemplate.query(TAG_SELECT + "ORDER BY dimension, sentiment, label_es", ROW_MAPPER);
    }

    @Override
    public Optional<ReviewTag> findById(final short tagId) {
        return jdbcTemplate.query(TAG_SELECT + "WHERE tag_id = ?", ROW_MAPPER, tagId)
                .stream().findFirst();
    }

    @Override
    public List<ReviewTag> findByIds(final Collection<Short> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        return namedParameterJdbcTemplate.query(
                TAG_SELECT + "WHERE tag_id IN (:ids) ORDER BY dimension, sentiment, label_es",
                new MapSqlParameterSource("ids", tagIds),
                ROW_MAPPER
        );
    }

    @Override
    public void replaceAssignments(final long reviewId, final Collection<Short> tagIds) {
        jdbcTemplate.update("DELETE FROM review_tag_assignments WHERE review_id = ?", reviewId);
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        final Set<Short> uniqueIds = new LinkedHashSet<>(tagIds);
        final List<Object[]> batchArgs = new ArrayList<>(uniqueIds.size());
        for (final Short tagId : uniqueIds) {
            if (tagId != null) {
                batchArgs.add(new Object[]{reviewId, tagId});
            }
        }
        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "INSERT INTO review_tag_assignments (review_id, tag_id) VALUES (?, ?)",
                    batchArgs
            );
            LOGGER.info("replaced tag assignments for review id={} tagCount={}", reviewId, batchArgs.size());
        } else {
            LOGGER.info("cleared tag assignments for review id={}", reviewId);
        }
    }

    @Override
    public Map<Long, Map<Short, Integer>> getTagCountsForCars(final Collection<Long> carIds) {
        if (carIds == null || carIds.isEmpty()) {
            return Map.of();
        }
        return namedParameterJdbcTemplate.query(
                "SELECT r.car_id, rta.tag_id, COUNT(*) AS mentions "
                        + "FROM review_tag_assignments rta "
                        + "JOIN reviews r ON r.review_id = rta.review_id "
                        + "WHERE r.car_id IN (:carIds) "
                        + "GROUP BY r.car_id, rta.tag_id",
                new MapSqlParameterSource("carIds", carIds),
                rs -> {
                    final Map<Long, Map<Short, Integer>> result = new HashMap<>();
                    while (rs.next()) {
                        final long carId = rs.getLong("car_id");
                        final short tagId = rs.getShort("tag_id");
                        final int mentions = rs.getInt("mentions");
                        result.computeIfAbsent(carId, k -> new HashMap<>()).put(tagId, mentions);
                    }
                    return result;
                }
        );
    }

    @Override
    public Map<Long, List<ReviewTag>> findByReviewIds(final Collection<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Map.of();
        }
        return namedParameterJdbcTemplate.query(
                "SELECT rta.review_id, rt.tag_id, rt.code, rt.label_es, rt.sentiment, rt.dimension, rt.created_at "
                        + "FROM review_tag_assignments rta "
                        + "JOIN review_tags rt ON rt.tag_id = rta.tag_id "
                        + "WHERE rta.review_id IN (:ids) "
                        + "ORDER BY rta.review_id, rt.sentiment, rt.dimension, rt.label_es",
                new MapSqlParameterSource("ids", reviewIds),
                rs -> {
                    final Map<Long, List<ReviewTag>> result = new HashMap<>();
                    while (rs.next()) {
                        final long reviewId = rs.getLong("review_id");
                        final ReviewTag tag = new ReviewTag(
                                rs.getShort("tag_id"),
                                rs.getString("code"),
                                rs.getString("label_es"),
                                rs.getString("sentiment"),
                                rs.getString("dimension"),
                                rs.getTimestamp("created_at").toLocalDateTime()
                        );
                        result.computeIfAbsent(reviewId, k -> new ArrayList<>()).add(tag);
                    }
                    return result;
                }
        );
    }
}
