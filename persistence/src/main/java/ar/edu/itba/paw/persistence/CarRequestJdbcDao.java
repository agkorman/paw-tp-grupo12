package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequestImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CarRequestJdbcDao implements CarRequestDao {

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
            rs.getString("model"),
            rs.getString("description"),
            rs.getString("image_content_type"),
            rs.getBytes("image_data"),
            rs.getString("status"),
            rs.getTimestamp("created_at").toLocalDateTime()
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
                .usingColumns("submitted_by_user_id", "submitter_email", "brand_id", "body_type_id", "model",
                        "description", "image_content_type", "image_data", "status");
        this.imageJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("car_request_images")
                .usingGeneratedKeyColumns("image_id")
                .usingColumns("car_request_id", "display_order", "content_type", "image_data");
    }

    @Override
    public Optional<CarRequest> findById(final long id) {
        return jdbcTemplate.query(
                "SELECT car_request_id, submitted_by_user_id, submitter_email, brand_id, body_type_id, model, "
                        + "description, image_content_type, image_data, status, created_at "
                        + "FROM car_requests WHERE car_request_id = ?",
                ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    @Override
    public List<CarRequest> findAll() {
        return jdbcTemplate.query(
                "SELECT car_request_id, submitted_by_user_id, submitter_email, brand_id, body_type_id, model, "
                        + "description, image_content_type, image_data, status, created_at "
                        + "FROM car_requests ORDER BY created_at DESC, car_request_id DESC",
                ROW_MAPPER
        );
    }

    @Override
    public List<CarRequest> findByStatus(final String status) {
        return jdbcTemplate.query(
                "SELECT car_request_id, submitted_by_user_id, submitter_email, brand_id, body_type_id, model, "
                        + "description, image_content_type, image_data, status, created_at "
                        + "FROM car_requests WHERE status = ? ORDER BY created_at DESC, car_request_id DESC",
                ROW_MAPPER,
                status
        );
    }

    @Override
    public CarRequest create(final long submittedByUserId, final long brandId, final long bodyTypeId,
                             final String model, final String description,
                             final String imageContentType, final byte[] imageData, final String status) {
        final Map<String, Object> params = new HashMap<>();
        params.put("submitted_by_user_id", submittedByUserId);
        params.put("submitter_email", null);
        params.put("brand_id", brandId);
        params.put("body_type_id", bodyTypeId);
        params.put("model", model);
        params.put("description", description);
        params.put("image_content_type", imageContentType);
        params.put("image_data", imageData);
        params.put("status", status);

        final long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return findById(id).orElseThrow();
    }

    @Override
    public List<CarRequestImage> findImagesByRequestId(final long requestId) {
        try {
            return jdbcTemplate.query(
                    "SELECT image_id, car_request_id, display_order, content_type, NULL::bytea AS image_data, updated_at "
                            + "FROM car_request_images WHERE car_request_id = ? "
                            + "ORDER BY display_order ASC, image_id ASC",
                    IMAGE_ROW_MAPPER,
                    requestId
            );
        } catch (final DataAccessException e) {
            throw new IllegalStateException("Failed to fetch image metadata for car request " + requestId + ".", e);
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
            throw new IllegalStateException("Failed to fetch image " + imageId
                    + " for car request " + requestId + ".", e);
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
        } catch (final DataAccessException e) {
            throw new IllegalStateException("Failed to replace image gallery for car request " + requestId + ".", e);
        }
    }

    @Override
    public boolean updateStatus(final long id, final String expectedStatus, final String newStatus) {
        return jdbcTemplate.update(
                "UPDATE car_requests SET status = ? WHERE car_request_id = ? AND status = ?",
                newStatus,
                id,
                expectedStatus
        ) > 0;
    }

    @Override
    public int bindRequestsToUserByEmail(final long userId, final String email) {
        return jdbcTemplate.update(
                "UPDATE car_requests SET submitted_by_user_id = ? "
                        + "WHERE submitted_by_user_id IS NULL "
                        + "AND submitter_email IS NOT NULL AND LOWER(BTRIM(submitter_email)) = LOWER(?)",
                userId,
                email
        );
    }
}
