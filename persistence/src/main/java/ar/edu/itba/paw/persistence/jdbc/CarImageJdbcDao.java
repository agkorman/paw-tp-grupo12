package ar.edu.itba.paw.persistence.jdbc;

import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.persistence.CarImageDao;
import ar.edu.itba.paw.persistence.exception.PersistenceOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class CarImageJdbcDao implements CarImageDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarImageJdbcDao.class);

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<CarImage> ROW_MAPPER = (rs, rowNum) -> new CarImage(
            rs.getLong("image_id"),
            rs.getLong("car_id"),
            rs.getInt("display_order"),
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
        try {
            return jdbcTemplate.query(
                    "SELECT image_id, car_id, display_order, content_type, image_data, updated_at "
                            + "FROM car_images WHERE car_id = ? "
                            + "ORDER BY display_order ASC, image_id ASC LIMIT 1",
                    ROW_MAPPER,
                    carId
            ).stream().findFirst();
        } catch (final DataAccessException e) {
            LOGGER.error("failed to fetch cover image for car id={}", carId, e);
            throw new PersistenceOperationException("fetch cover image for car " + carId, e);
        }
    }

    @Override
    public List<CarImage> findAllByCarId(final long carId) {
        try {
            return jdbcTemplate.query(
                    "SELECT image_id, car_id, display_order, content_type, CAST(NULL AS bytea) AS image_data, updated_at "
                            + "FROM car_images WHERE car_id = ? ORDER BY display_order ASC, image_id ASC",
                    ROW_MAPPER,
                    carId
            );
        } catch (final DataAccessException e) {
            LOGGER.error("failed to fetch image metadata for car id={}", carId, e);
            throw new PersistenceOperationException("fetch image metadata for car " + carId, e);
        }
    }

    @Override
    public List<CarImage> findAllByCarIdWithData(final long carId) {
        try {
            return jdbcTemplate.query(
                    "SELECT image_id, car_id, display_order, content_type, image_data, updated_at "
                            + "FROM car_images WHERE car_id = ? ORDER BY display_order ASC, image_id ASC",
                    ROW_MAPPER,
                    carId
            );
        } catch (final DataAccessException e) {
            LOGGER.error("failed to fetch images with data for car id={}", carId, e);
            throw new PersistenceOperationException("fetch images with data for car " + carId, e);
        }
    }

    @Override
    public Optional<CarImage> findByCarIdAndImageId(final long carId, final long imageId) {
        try {
            return jdbcTemplate.query(
                    "SELECT image_id, car_id, display_order, content_type, image_data, updated_at "
                            + "FROM car_images WHERE car_id = ? AND image_id = ?",
                    ROW_MAPPER,
                    carId,
                    imageId
            ).stream().findFirst();
        } catch (final DataAccessException e) {
            LOGGER.error("failed to fetch image id={} for car id={}", imageId, carId, e);
            throw new PersistenceOperationException("fetch image " + imageId + " for car " + carId, e);
        }
    }

    @Override
    public void saveOrReplace(final long carId, final String contentType, final byte[] imageData) {
        replaceAll(carId, List.of(new CarImagePayload(contentType, imageData)));
    }

    @Override
    public void replaceAll(final long carId, final List<CarImagePayload> images) {
        try {
            jdbcTemplate.update("DELETE FROM car_images WHERE car_id = ?", carId);
            for (int i = 0; i < images.size(); i++) {
                final CarImagePayload image = images.get(i);
                jdbcTemplate.update(
                        "INSERT INTO car_images (car_id, display_order, content_type, image_data) "
                                + "VALUES (?, ?, ?, ?)",
                        carId,
                        i,
                        image.getContentType(),
                        image.getImageData()
                );
            }
            LOGGER.info("replaced image gallery for car id={} imageCount={}", carId, images.size());
        } catch (final DataAccessException e) {
            LOGGER.error("failed to replace image gallery for car id={}", carId, e);
            throw new PersistenceOperationException("replace image gallery for car " + carId, e);
        }
    }
}
