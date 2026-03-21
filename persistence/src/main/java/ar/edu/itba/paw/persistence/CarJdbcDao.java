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

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static final RowMapper<Car> ROW_MAPPER = (rs, rowNum) -> new Car(
            rs.getLong("car_id"),
            rs.getLong("brand_id"),
            rs.getString("model"),
            rs.getString("generation"),
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
                "SELECT car_id, brand_id, model, generation, body_type, description, image_url, created_at FROM cars ORDER BY car_id",
                ROW_MAPPER
        );
    }

    @Override
    public Optional<Car> findById(long id) {
        return jdbcTemplate.query(
                "SELECT car_id, brand_id, model, generation, body_type, description, image_url, created_at FROM cars WHERE car_id = ?",
                ROW_MAPPER, id
        ).stream().findFirst();
    }

    @Override
    public List<Car> findByBrandId(long brandId) {
        return jdbcTemplate.query(
                "SELECT car_id, brand_id, model, generation, body_type, description, image_url, created_at FROM cars WHERE brand_id = ? ORDER BY model",
                ROW_MAPPER, brandId
        );
    }

    @Override
    public Car create(long brandId, String model, String generation, String bodyType, String description, String imageUrl) {
        Map<String, Object> params = new HashMap<>();
        params.put("brand_id", brandId);
        params.put("model", model);
        params.put("generation", generation);
        params.put("body_type", bodyType);
        params.put("description", description);
        params.put("image_url", imageUrl);

        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return findById(id).orElseThrow();
    }
}
