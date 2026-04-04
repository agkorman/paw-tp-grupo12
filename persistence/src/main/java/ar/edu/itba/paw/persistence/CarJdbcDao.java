package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CarJdbcDao implements CarDao {

    private static final String FROM_JOIN =
            "FROM cars c "
                    + "JOIN brands b ON c.brand_id = b.brand_id "
                    + "JOIN body_types bt ON c.body_type_id = bt.body_type_id ";

    private static final String SELECT_COLUMNS =
            "SELECT c.car_id, c.brand_id, b.name AS brand_name, c.model, c.body_type_id, bt.name AS body_type, "
                    + "c.description, c.image_url, c.created_at ";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static final RowMapper<Car> ROW_MAPPER = (rs, rowNum) -> new Car(
            rs.getLong("car_id"),
            rs.getLong("brand_id"),
            rs.getString("brand_name"),
            rs.getString("model"),
            rs.getLong("body_type_id"),
            rs.getString("body_type"),
            rs.getString("description"),
            rs.getString("image_url"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    @Autowired
    public CarJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("cars")
                .usingGeneratedKeyColumns("car_id");
    }

    @Override
    public List<Car> findAll() {
        return jdbcTemplate.query(
                SELECT_COLUMNS + FROM_JOIN + "ORDER BY c.car_id",
                ROW_MAPPER
        );
    }

    @Override
    public Optional<Car> findById(final long id) {
        return jdbcTemplate.query(
                SELECT_COLUMNS + FROM_JOIN + "WHERE c.car_id = ?",
                ROW_MAPPER, id
        ).stream().findFirst();
    }

    @Override
    public List<Car> findByBrandId(final long brandId) {
        return jdbcTemplate.query(
                SELECT_COLUMNS + FROM_JOIN + "WHERE c.brand_id = ? ORDER BY c.model",
                ROW_MAPPER, brandId
        );
    }

    @Override
    public Car create(final long brandId, final String model, final long bodyTypeId,
                      final String description, final String imageUrl) {
        final Map<String, Object> params = new HashMap<>();
        params.put("brand_id", brandId);
        params.put("model", model);
        params.put("body_type_id", bodyTypeId);
        params.put("description", description);
        params.put("image_url", imageUrl);

        final long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return findById(id).orElseThrow();
    }

    @Override
    public List<Car> findByBodyTypeId(final long bodyTypeId) {
        return jdbcTemplate.query(
                SELECT_COLUMNS + FROM_JOIN + "WHERE c.body_type_id = ? ORDER BY c.model",
                ROW_MAPPER, bodyTypeId
        );
    }

    @Override
    public List<Car> findByBrandIdAndBodyTypeId(final long brandId, final long bodyTypeId) {
        return jdbcTemplate.query(
                SELECT_COLUMNS + FROM_JOIN + "WHERE c.brand_id = ? AND c.body_type_id = ? ORDER BY c.model",
                ROW_MAPPER, brandId, bodyTypeId
        );
    }

    @Override
    public List<Car> search(final String query) {
        final String normalizedQuery = "%" + query.trim().toLowerCase() + "%";
        return jdbcTemplate.query(
                SELECT_COLUMNS + FROM_JOIN
                        + "WHERE LOWER(b.name) LIKE ? "
                        + "OR LOWER(c.model) LIKE ? "
                        + "OR LOWER(bt.name) LIKE ? "
                        + "OR LOWER(COALESCE(c.description, '')) LIKE ? "
                        + "ORDER BY c.car_id",
                ROW_MAPPER,
                normalizedQuery, normalizedQuery, normalizedQuery, normalizedQuery
        );
    }
}
