package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.CarRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public CarRequestJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("car_requests")
                .usingGeneratedKeyColumns("car_request_id")
                .usingColumns("submitted_by_user_id", "submitter_email", "brand_id", "body_type_id", "model",
                        "description", "image_content_type", "image_data", "status");
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
    public CarRequest create(final Long submittedByUserId, final String submitterEmail, final long brandId,
                             final long bodyTypeId, final String model, final String description,
                             final String imageContentType, final byte[] imageData, final String status) {
        final Map<String, Object> params = new HashMap<>();
        params.put("submitted_by_user_id", submittedByUserId);
        params.put("submitter_email", submitterEmail);
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
}
