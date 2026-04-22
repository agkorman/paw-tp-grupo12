package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ReviewJdbcDao implements ReviewDao {

    private static final String REVIEW_SELECT =
            "SELECT r.review_id, r.user_id, r.reviewer_email, u.username AS reviewer_username, "
                    + "r.car_id, r.rating, r.title, r.body, r.ownership_status, r.model_year, r.mileage_km, "
                    + "r.would_recommend, r.created_at, r.updated_at "
                    + "FROM reviews r LEFT JOIN users u ON r.user_id = u.user_id ";

    private static final String REVIEW_STATS_SELECT =
            "SELECT c.car_id, COUNT(r.review_id) AS review_count, ROUND(AVG(r.rating), 1) AS average_rating "
                    + "FROM cars c LEFT JOIN reviews r ON r.car_id = c.car_id ";

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static Long getNullableLong(final java.sql.ResultSet rs, final String columnName) throws java.sql.SQLException {
        final Number value = (Number) rs.getObject(columnName);
        return value == null ? null : value.longValue();
    }

    private static final RowMapper<Review> ROW_MAPPER = (rs, rowNum) -> new Review(
            rs.getLong("review_id"),
            getNullableLong(rs, "user_id"),
            rs.getString("reviewer_email"),
            rs.getString("reviewer_username"),
            rs.getLong("car_id"),
            rs.getBigDecimal("rating"),
            rs.getString("title"),
            rs.getString("body"),
            rs.getString("ownership_status"),
            rs.getObject("model_year", Integer.class),
            rs.getObject("mileage_km", Integer.class),
            rs.getObject("would_recommend") != null ? rs.getBoolean("would_recommend") : null,
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
    );

    private static final RowMapper<ReviewStats> REVIEW_STATS_ROW_MAPPER = (rs, rowNum) -> new ReviewStats(
            rs.getLong("car_id"),
            rs.getLong("review_count"),
            rs.getBigDecimal("average_rating")
    );

    @Autowired
    public ReviewJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reviews")
                .usingColumns("user_id", "reviewer_email", "car_id", "rating", "title", "body",
                        "ownership_status", "model_year", "mileage_km", "would_recommend")
                .usingGeneratedKeyColumns("review_id");
    }

    @Override
    public Optional<Review> findById(long id) {
        return jdbcTemplate.query(
                REVIEW_SELECT + "WHERE r.review_id = ?",
                ROW_MAPPER, id
        ).stream().findFirst();
    }

    @Override
    public List<Review> findByIds(final Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return namedParameterJdbcTemplate.query(
                REVIEW_SELECT + "WHERE r.review_id IN (:reviewIds)",
                new MapSqlParameterSource("reviewIds", ids),
                ROW_MAPPER
        );
    }

    @Override
    public List<Review> findAll() {
        return jdbcTemplate.query(
                REVIEW_SELECT + "ORDER BY r.created_at DESC",
                ROW_MAPPER
        );
    }

    @Override
    public List<Review> findByCarId(long carId) {
        return findByCarIdOrdered(carId, "r.created_at DESC");
    }

    @Override
    public Optional<Review> findLatestByCarId(final long carId) {
        return jdbcTemplate.query(
                REVIEW_SELECT + "WHERE r.car_id = ? ORDER BY r.created_at DESC LIMIT 1",
                ROW_MAPPER, carId
        ).stream().findFirst();
    }

    @Override
    public Optional<Review> findTopRatedLatestByCarId(final long carId) {
        return jdbcTemplate.query(
                REVIEW_SELECT + "WHERE r.car_id = ? ORDER BY r.rating DESC, r.created_at DESC, r.review_id DESC LIMIT 1",
                ROW_MAPPER, carId
        ).stream().findFirst();
    }

    @Override
    public List<Review> findByCarIdOrderByRatingAsc(final long carId) {
        return findByCarIdOrdered(carId, "r.rating ASC, r.created_at DESC");
    }

    @Override
    public List<Review> findByCarIdOrderByRatingDesc(final long carId) {
        return findByCarIdOrdered(carId, "r.rating DESC, r.created_at DESC");
    }

    private List<Review> findByCarIdOrdered(final long carId, final String orderByClause) {
        return jdbcTemplate.query(
                REVIEW_SELECT + "WHERE r.car_id = ? ORDER BY " + orderByClause,
                ROW_MAPPER, carId
        );
    }

    @Override
    public List<Review> findByUserId(long userId) {
        return jdbcTemplate.query(
                REVIEW_SELECT + "WHERE r.user_id = ? ORDER BY r.created_at DESC",
                ROW_MAPPER, userId
        );
    }

    @Override
    public Optional<ReviewStats> findStatsByCarId(final long carId) {
        return jdbcTemplate.query(
                REVIEW_STATS_SELECT + "WHERE c.car_id = ? GROUP BY c.car_id",
                REVIEW_STATS_ROW_MAPPER,
                carId
        ).stream().findFirst();
    }

    @Override
    public List<ReviewStats> findStatsByCarIds(final Collection<Long> carIds) {
        if (carIds == null || carIds.isEmpty()) {
            return List.of();
        }

        return namedParameterJdbcTemplate.query(
                REVIEW_STATS_SELECT + "WHERE c.car_id IN (:carIds) GROUP BY c.car_id ORDER BY c.car_id",
                new MapSqlParameterSource("carIds", carIds),
                REVIEW_STATS_ROW_MAPPER
        );
    }

    @Override
    public Review create(long userId, long carId, BigDecimal rating, String title, String body,
                         String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("reviewer_email", null);
        params.put("car_id", carId);
        params.put("rating", rating);
        params.put("title", title);
        params.put("body", body);
        params.put("ownership_status", ownershipStatus);
        params.put("model_year", modelYear);
        params.put("mileage_km", mileageKm);
        params.put("would_recommend", wouldRecommend);

        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return findById(id).orElseThrow();
    }

    @Override
    public int bindReviewsToUserByEmail(final long userId, final String email) {
        return jdbcTemplate.update(
                "UPDATE reviews SET user_id = ? "
                        + "WHERE user_id IS NULL AND reviewer_email IS NOT NULL "
                        + "AND LOWER(BTRIM(reviewer_email)) = LOWER(?)",
                userId,
                email
        );
    }

    @Override
    public Optional<Review> update(final long id, final long carId,
                                   final BigDecimal rating, final String title, final String body,
                                   final String ownershipStatus, final Integer modelYear,
                                   final Integer mileageKm, final Boolean wouldRecommend) {
        final int updated = jdbcTemplate.update(
                "UPDATE reviews SET car_id = ?, rating = ?, title = ?, body = ?, "
                        + "ownership_status = ?, model_year = ?, mileage_km = ?, would_recommend = ?, "
                        + "updated_at = CURRENT_TIMESTAMP WHERE review_id = ?",
                carId, rating, title, body, ownershipStatus, modelYear, mileageKm, wouldRecommend, id
        );
        return updated > 0 ? findById(id) : Optional.empty();
    }

    @Override
    public boolean delete(long id) {
        return jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", id) > 0;
    }

    @Override
    public int deleteByCarId(final long carId) {
        return jdbcTemplate.update("DELETE FROM reviews WHERE car_id = ?", carId);
    }
}
