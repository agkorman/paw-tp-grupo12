package ar.edu.itba.paw.persistence.jdbc;

import ar.edu.itba.paw.model.AdminRequest;
import ar.edu.itba.paw.persistence.AdminRequestDao;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class AdminRequestJdbcDao implements AdminRequestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminRequestJdbcDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static final RowMapper<AdminRequest> ROW_MAPPER = (rs, rowNum) -> new AdminRequest(
            rs.getLong("admin_request_id"),
            rs.getLong("submitted_by_user_id"),
            rs.getString("submitter_email"),
            rs.getString("motivation"),
            rs.getString("bio"),
            rs.getString("justification"),
            rs.getString("status"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    @Autowired
    public AdminRequestJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("admin_requests")
                .usingGeneratedKeyColumns("admin_request_id")
                .usingColumns("submitted_by_user_id", "submitter_email", "motivation", "bio",
                        "justification", "status");
    }

    @Override
    public Optional<AdminRequest> findById(final long id) {
        return jdbcTemplate.query(
                "SELECT admin_request_id, submitted_by_user_id, submitter_email, motivation, bio, "
                        + "justification, status, created_at "
                        + "FROM admin_requests WHERE admin_request_id = ?",
                ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    @Override
    public List<AdminRequest> findByStatus(final String status) {
        return jdbcTemplate.query(
                "SELECT admin_request_id, submitted_by_user_id, submitter_email, motivation, bio, "
                        + "justification, status, created_at "
                        + "FROM admin_requests WHERE status = ? ORDER BY created_at DESC, admin_request_id DESC",
                ROW_MAPPER,
                status
        );
    }

    @Override
    public Page<AdminRequest> findByStatus(final String status, final int page) {
        final int normalizedPage = Pagination.normalizePage(page);
        final int pageSize = Pagination.REQUESTS_PAGE_SIZE;

        final long totalItems = countByStatus(status);
        if (totalItems == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }

        final int effectivePage = Pagination.clampPage(normalizedPage, totalItems, pageSize);
        final long offset = Pagination.offsetFor(effectivePage, pageSize);

        final List<AdminRequest> items = jdbcTemplate.query(
                "SELECT admin_request_id, submitted_by_user_id, submitter_email, motivation, bio, "
                        + "justification, status, created_at "
                        + "FROM admin_requests WHERE status = ? ORDER BY created_at DESC, admin_request_id DESC "
                        + "LIMIT ? OFFSET ?",
                ROW_MAPPER,
                status, pageSize, offset
        );
        return new Page<>(items, effectivePage, pageSize, totalItems);
    }

    @Override
    public long countByStatus(final String status) {
        final Long count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM admin_requests WHERE status = ?", Long.class, status);
        return count == null ? 0L : count;
    }

    @Override
    public boolean existsPendingByUser(final long userId) {
        final Long count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM admin_requests WHERE submitted_by_user_id = ? AND status = 'pending'",
                Long.class, userId);
        return count != null && count > 0L;
    }

    @Override
    public AdminRequest create(final long submittedByUserId, final String submitterEmail,
                               final String motivation, final String bio, final String justification,
                               final String status) {
        final Map<String, Object> params = new HashMap<>();
        params.put("submitted_by_user_id", submittedByUserId);
        params.put("submitter_email", submitterEmail);
        params.put("motivation", motivation);
        params.put("bio", bio);
        params.put("justification", justification);
        params.put("status", status);

        final long id = jdbcInsert.executeAndReturnKey(params).longValue();
        LOGGER.info("created admin request id={} userId={} status={}", id, submittedByUserId, status);
        return findById(id).orElseThrow();
    }

    @Override
    public boolean updateStatus(final long id, final String expectedStatus, final String newStatus) {
        final boolean updated = jdbcTemplate.update(
                "UPDATE admin_requests SET status = ? WHERE admin_request_id = ? AND status = ?",
                newStatus,
                id,
                expectedStatus
        ) > 0;
        if (updated) {
            LOGGER.info("updated admin request id={} status {}->{}", id, expectedStatus, newStatus);
        } else {
            LOGGER.warn("admin request status update affected 0 rows id={} expectedStatus={} newStatus={}", id, expectedStatus, newStatus);
        }
        return updated;
    }
}
