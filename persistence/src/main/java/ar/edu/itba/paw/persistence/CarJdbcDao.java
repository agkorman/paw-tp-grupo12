package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class CarJdbcDao implements CarDao {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Car> ROW_MAPPER = (rs, rowNum) -> new Car(
            rs.getLong("car_id"),
            rs.getString("brand"),
            rs.getString("model"),
            rs.getString("generation"),
            rs.getString("description"),
            rs.getString("image_url")
    );

    @Autowired
    public CarJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Car> findAll() {
        return jdbcTemplate.query(
                "SELECT car_id, brand, model, generation, description, image_url FROM cars ORDER BY brand, model",
                ROW_MAPPER
        );
    }
}
