package ar.edu.itba.paw.persistence.jdbc;

import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.persistence.CarRequestDao;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequestImage;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.persistence.exception.PersistenceOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class CarRequestJdbcDao implements CarRequestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarRequestJdbcDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final SimpleJdbcInsert imageJdbcInsert;

    private static Long getNullableLong(final java.sql.ResultSet rs, final String columnName) throws SQLException {
        final Number value = (Number) rs.getObject(columnName);
        return value == null ? null : value.longValue();
    }

    private static final RowMapper<CarRequest> ROW_MAPPER = (rs, rowNum) -> new CarRequest(
            rs.getLong("car_request_id"),
            getNullableLong(rs, "submitted_by_user_id"),
            rs.getString("submitter_email"),
            rs.getLong("brand_id"),
            rs.getLong("body_type_id"),
            rs.getObject("year", Integer.class),
            rs.getString("model"),
            rs.getString("description"),
            rs.getString("image_content_type"),
            rs.getBytes("image_data"),
            rs.getString("status"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getString("fuel_type"),
            rs.getObject("horsepower", Integer.class),
            rs.getObject("airbag_count", Integer.class),
            rs.getString("transmission"),
            rs.getBigDecimal("fuel_consumption"),
            rs.getObject("max_speed_kmh", Integer.class),
            rs.getBigDecimal("price_usd")
    );

    private static final RowMapper<CarRequestImage> IMAGE_ROW_MAPPER = (rs, rowNum) -> new CarRequestImage(
            rs.getLong("image_id"),
            rs.getLong("car_request_id"),
            rs.getInt("display_order"),
            rs.getString("content_type"),
            rs.getBytes("image_data"),
            rs.getTimestamp("updated_at").toLocalDateTime()
    );

    @Autowired
    public CarRequestJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("car_requests")
                .usingGeneratedKeyColumns("car_request_id")
                .usingColumns("submitted_by_user_id", "submitter_email", "brand_id", "body_type_id", "year", "model",
                        "description", "image_content_type", "image_data", "status",
                        "fuel_type", "horsepower", "airbag_count", "transmission",
                        "fuel_consumption", "max_speed_kmh", "price_usd");
        this.imageJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("car_request_images")
                .usingGeneratedKeyColumns("image_id")
                .usingColumns("car_request_id", "display_order", "content_type", "image_data");
    }

    @Override
    public Optional<CarRequest> findById(final long id) {
        return jdbcTemplate.query(
                "SELECT car_request_id, submitted_by_user_id, submitter_email, brand_id, body_type_id, year, model, "
                        + "description, image_content_type, image_data, status, created_at, "
                        + "fuel_type, horsepower, airbag_count, transmission, fuel_consumption, max_speed_kmh, price_usd "
                        + "FROM car_requests WHERE car_request_id = ?",
                ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    @Override
    public List<CarRequest> findByStatus(final String status) {
        return jdbcTemplate.query(
                "SELECT car_request_id, submitted_by_user_id, submitter_email, brand_id, body_type_id, year, model, "
                        + "description, image_content_type, image_data, status, created_at, "
                        + "fuel_type, horsepower, airbag_count, transmission, fuel_consumption, max_speed_kmh, price_usd "
                        + "FROM car_requests WHERE status = ? ORDER BY created_at DESC, car_request_id DESC",
                ROW_MAPPER,
                status
        );
    }

    @Override
    public Page<CarRequest> findByStatus(final String status, final int page) {
        final int normalizedPage = Pagination.normalizePage(page);
        final int pageSize = Pagination.REQUESTS_PAGE_SIZE;

        final long totalItems = countByStatus(status);
        if (totalItems == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }

        final int effectivePage = Pagination.clampPage(normalizedPage, totalItems, pageSize);
        final long offset = Pagination.offsetFor(effectivePage, pageSize);

        final List<CarRequest> items = jdbcTemplate.query(
                "SELECT car_request_id, submitted_by_user_id, submitter_email, brand_id, body_type_id, year, model, "
                        + "description, image_content_type, image_data, status, created_at, "
                        + "fuel_type, horsepower, airbag_count, transmission, fuel_consumption, max_speed_kmh, price_usd "
                        + "FROM car_requests WHERE status = ? ORDER BY created_at DESC, car_request_id DESC "
                        + "LIMIT ? OFFSET ?",
                ROW_MAPPER,
                status, pageSize, offset
        );
        return new Page<>(items, effectivePage, pageSize, totalItems);
    }

    @Override
    public long countByStatus(final String status) {
        final Long count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM car_requests WHERE status = ?", Long.class, status);
        return count == null ? 0L : count;
    }

    @Override
    public CarRequest create(final long submittedByUserId, final String submitterEmail, final long brandId,
                             final long bodyTypeId, final Integer year, final String model, final String description,
                             final String imageContentType, final byte[] imageData, final String status,
                             final String fuelType, final Integer horsepower, final Integer airbagCount,
                             final String transmission, final BigDecimal fuelConsumption, final Integer maxSpeedKmh,
                             final BigDecimal priceUsd) {
        final Map<String, Object> params = new HashMap<>();
        params.put("submitted_by_user_id", submittedByUserId);
        params.put("submitter_email", submitterEmail);
        params.put("brand_id", brandId);
        params.put("body_type_id", bodyTypeId);
        params.put("year", year);
        params.put("model", model);
        params.put("description", description);
        params.put("image_content_type", imageContentType);
        params.put("image_data", imageData);
        params.put("status", status);
        params.put("fuel_type", fuelType);
        params.put("horsepower", horsepower);
        params.put("airbag_count", airbagCount);
        params.put("transmission", transmission);
        params.put("fuel_consumption", fuelConsumption);
        params.put("max_speed_kmh", maxSpeedKmh);
        params.put("price_usd", priceUsd);

        final long id = jdbcInsert.executeAndReturnKey(params).longValue();
        LOGGER.info("created car request id={} userId={} model={} status={}", id, submittedByUserId, model, status);
        return findById(id).orElseThrow();
    }

    @Override
    public List<CarRequestImage> findImagesByRequestId(final long requestId) {
        try {
            return jdbcTemplate.query(
                    "SELECT image_id, car_request_id, display_order, content_type, CAST(NULL AS bytea) AS image_data, updated_at "
                            + "FROM car_request_images WHERE car_request_id = ? "
                            + "ORDER BY display_order ASC, image_id ASC",
                    IMAGE_ROW_MAPPER,
                    requestId
            );
        } catch (final DataAccessException e) {
            LOGGER.error("failed to fetch image metadata for car request id={}", requestId, e);
            throw new PersistenceOperationException("fetch image metadata for car request " + requestId, e);
        }
    }

    @Override
    public Optional<CarRequestImage> findImageByRequestIdAndImageId(final long requestId, final long imageId) {
        try {
            return jdbcTemplate.query(
                    "SELECT image_id, car_request_id, display_order, content_type, image_data, updated_at "
                            + "FROM car_request_images WHERE car_request_id = ? AND image_id = ?",
                    IMAGE_ROW_MAPPER,
                    requestId,
                    imageId
            ).stream().findFirst();
        } catch (final DataAccessException e) {
            LOGGER.error("failed to fetch image id={} for car request id={}", imageId, requestId, e);
            throw new PersistenceOperationException("fetch image " + imageId + " for car request " + requestId, e);
        }
    }

    @Override
    public void replaceImages(final long requestId, final List<CarImagePayload> images) {
        try {
            jdbcTemplate.update("DELETE FROM car_request_images WHERE car_request_id = ?", requestId);
            for (int i = 0; i < images.size(); i++) {
                final CarImagePayload image = images.get(i);
                final Map<String, Object> params = new HashMap<>();
                params.put("car_request_id", requestId);
                params.put("display_order", i);
                params.put("content_type", image.getContentType());
                params.put("image_data", image.getImageData());
                imageJdbcInsert.execute(params);
            }
            LOGGER.info("replaced image gallery for car request id={} imageCount={}", requestId, images.size());
        } catch (final DataAccessException e) {
            LOGGER.error("failed to replace image gallery for car request id={}", requestId, e);
            throw new PersistenceOperationException("replace image gallery for car request " + requestId, e);
        }
    }

    @Override
    public boolean updateStatus(final long id, final String expectedStatus, final String newStatus) {
        final boolean updated = jdbcTemplate.update(
                "UPDATE car_requests SET status = ? WHERE car_request_id = ? AND status = ?",
                newStatus,
                id,
                expectedStatus
        ) > 0;
        if (updated) {
            LOGGER.info("updated car request id={} status {}->{}", id, expectedStatus, newStatus);
        } else {
            LOGGER.warn("car request status update affected 0 rows id={} expectedStatus={} newStatus={}", id, expectedStatus, newStatus);
        }
        return updated;
    }

    @Override
    public int bindRequestsToUserByEmail(final long userId, final String email) {
        final int bound = jdbcTemplate.update(
                "UPDATE car_requests SET submitted_by_user_id = ? "
                        + "WHERE submitted_by_user_id IS NULL "
                        + "AND submitter_email IS NOT NULL AND LOWER(BTRIM(submitter_email)) = LOWER(?)",
                userId,
                email
        );
        if (bound > 0) {
            LOGGER.info("bound {} pending car requests to user id={} email={}", bound, userId, email);
        }
        return bound;
    }
}
