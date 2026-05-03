package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.BrandRequest;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class BrandRequestJdbcDao implements BrandRequestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrandRequestJdbcDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static Long getNullableLong(final java.sql.ResultSet rs, final String columnName) throws SQLException {
        final Number value = (Number) rs.getObject(columnName);
        return value == null ? null : value.longValue();
    }

    private static final RowMapper<BrandRequest> ROW_MAPPER = (rs, rowNum) -> new BrandRequest(
            rs.getLong("brand_request_id"),
            getNullableLong(rs, "submitted_by_user_id"),
            rs.getString("submitter_email"),
            rs.getString("name"),
            rs.getString("comments"),
            rs.getString("status"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    @Autowired
    public BrandRequestJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("brand_requests")
                .usingGeneratedKeyColumns("brand_request_id")
                .usingColumns("submitted_by_user_id", "submitter_email", "name", "comments", "status");
    }

    @Override
    public Optional<BrandRequest> findById(final long id) {
        return jdbcTemplate.query(
                "SELECT brand_request_id, submitted_by_user_id, submitter_email, name, comments, status, created_at "
                        + "FROM brand_requests WHERE brand_request_id = ?",
                ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    @Override
    public List<BrandRequest> findByStatus(final String status) {
        return jdbcTemplate.query(
                "SELECT brand_request_id, submitted_by_user_id, submitter_email, name, comments, status, created_at "
                        + "FROM brand_requests WHERE status = ? ORDER BY created_at DESC, brand_request_id DESC",
                ROW_MAPPER,
                status
        );
    }

    @Override
    public Page<BrandRequest> findByStatus(final String status, final int page) {
        final int normalizedPage = Pagination.normalizePage(page);
        final int pageSize = Pagination.REQUESTS_PAGE_SIZE;

        final long totalItems = countByStatus(status);
        if (totalItems == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }

        final int effectivePage = Pagination.clampPage(normalizedPage, totalItems, pageSize);
        final long offset = Pagination.offsetFor(effectivePage, pageSize);

        final List<BrandRequest> items = jdbcTemplate.query(
                "SELECT brand_request_id, submitted_by_user_id, submitter_email, name, comments, status, created_at "
                        + "FROM brand_requests WHERE status = ? ORDER BY created_at DESC, brand_request_id DESC "
                        + "LIMIT ? OFFSET ?",
                ROW_MAPPER,
                status, pageSize, offset
        );
        return new Page<>(items, effectivePage, pageSize, totalItems);
    }

    @Override
    public long countByStatus(final String status) {
        final Long count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM brand_requests WHERE status = ?", Long.class, status);
        return count == null ? 0L : count;
    }

    @Override
    public BrandRequest create(final Long submittedByUserId, final String submitterEmail,
                               final String name, final String comments, final String status) {
        final Map<String, Object> params = new HashMap<>();
        params.put("submitted_by_user_id", submittedByUserId);
        params.put("submitter_email", submitterEmail);
        params.put("name", name);
        params.put("comments", comments);
        params.put("status", status);

        final long id = jdbcInsert.executeAndReturnKey(params).longValue();
        LOGGER.info("created brand request id={} userId={} name={} status={}", id, submittedByUserId, name, status);
        return findById(id).orElseThrow();
    }

    @Override
    public boolean updateStatus(final long id, final String expectedStatus, final String newStatus) {
        final boolean updated = jdbcTemplate.update(
                "UPDATE brand_requests SET status = ? WHERE brand_request_id = ? AND status = ?",
                newStatus,
                id,
                expectedStatus
        ) > 0;
        if (updated) {
            LOGGER.info("updated brand request id={} status {}->{}", id, expectedStatus, newStatus);
        } else {
            LOGGER.warn("brand request status update affected 0 rows id={} expectedStatus={} newStatus={}", id, expectedStatus, newStatus);
        }
        return updated;
    }
}
