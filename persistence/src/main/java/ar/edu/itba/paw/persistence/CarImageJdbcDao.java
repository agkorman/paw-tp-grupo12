package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.CarImage;
//import ar.edu.itba.paw.persistence.CarImageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Optional;

@Repository
public class CarImageJdbcDao implements CarImageDao {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<CarImage> ROW_MAPPER = (rs, rowNum) -> new CarImage(
            rs.getLong("car_id"),
            rs.getString("content_type"),
            rs.getBytes("image_data"),
            rs.getTimestamp("updated_at").toLocalDateTime()
    );

    @Autowired
    public CarImageJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<CarImage> findByCarId(final long carId) {
        return jdbcTemplate.query(
                "SELECT car_id, content_type, image_data, updated_at FROM car_images WHERE car_id = ?",
                ROW_MAPPER,
                carId
        ).stream().findFirst();
    }

    @Override
    public void saveOrReplace(final long carId, final String contentType, final byte[] imageData) {
        jdbcTemplate.update(
                "INSERT INTO car_images (car_id, content_type, image_data) VALUES (?, ?, ?) "
                        + "ON CONFLICT (car_id) DO UPDATE SET content_type = EXCLUDED.content_type, "
                        + "image_data = EXCLUDED.image_data, updated_at = CURRENT_TIMESTAMP",
                carId, contentType, imageData
        );
    }
}
