package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ReviewJdbcDao implements ReviewDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static final RowMapper<Review> ROW_MAPPER = (rs, rowNum) -> new Review(
            rs.getLong("review_id"),
            rs.getLong("user_id"),
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

    @Autowired
    public ReviewJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reviews")
                .usingColumns("user_id", "car_id", "rating", "title", "body",
                        "ownership_status", "model_year", "mileage_km", "would_recommend")
                .usingGeneratedKeyColumns("review_id");
    }

    @Override
    public Optional<Review> findById(long id) {
        return jdbcTemplate.query(
                "SELECT review_id, user_id, car_id, rating, title, body, ownership_status, model_year, mileage_km, would_recommend, created_at, updated_at FROM reviews WHERE review_id = ?",
                ROW_MAPPER, id
        ).stream().findFirst();
    }

    @Override
    public List<Review> findAll() {
        return jdbcTemplate.query(
                "SELECT review_id, user_id, car_id, rating, title, body, ownership_status, model_year, mileage_km, would_recommend, created_at, updated_at FROM reviews ORDER BY created_at DESC",
                ROW_MAPPER
        );
    }

    @Override
    public List<Review> findByCarId(long carId) {
        return jdbcTemplate.query(
                "SELECT review_id, user_id, car_id, rating, title, body, ownership_status, model_year, mileage_km, would_recommend, created_at, updated_at FROM reviews WHERE car_id = ? ORDER BY created_at DESC",
                ROW_MAPPER, carId
        );
    }

    @Override
    public List<Review> findByUserId(long userId) {
        return jdbcTemplate.query(
                "SELECT review_id, user_id, car_id, rating, title, body, ownership_status, model_year, mileage_km, would_recommend, created_at, updated_at FROM reviews WHERE user_id = ? ORDER BY created_at DESC",
                ROW_MAPPER, userId
        );
    }

    @Override
    public Review create(long userId, long carId, BigDecimal rating, String title, String body,
                         String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
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
    public boolean delete(long id) {
        return jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", id) > 0;
    }
}
