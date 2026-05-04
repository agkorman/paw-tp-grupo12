package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.BodyType;
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

@Repository
public class BodyTypeJdbcDao implements BodyTypeDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(BodyTypeJdbcDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static final RowMapper<BodyType> ROW_MAPPER = (rs, rowNum) -> new BodyType(
            rs.getLong("body_type_id"),
            rs.getString("name"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    @Autowired
    public BodyTypeJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("body_types")
                .usingGeneratedKeyColumns("body_type_id")
                .usingColumns("name");
    }

    @Override
    public List<BodyType> findAll() {
        return jdbcTemplate.query(
                "SELECT body_type_id, name, created_at FROM body_types ORDER BY name",
                ROW_MAPPER
        );
    }

    @Override
    public Optional<BodyType> findById(final long id) {
        return jdbcTemplate.query(
                "SELECT body_type_id, name, created_at FROM body_types WHERE body_type_id = ?",
                ROW_MAPPER, id
        ).stream().findFirst();
    }

    @Override
    public Optional<BodyType> findByName(final String name) {
        return jdbcTemplate.query(
                "SELECT body_type_id, name, created_at FROM body_types WHERE LOWER(name) = LOWER(?)",
                ROW_MAPPER, name
        ).stream().findFirst();
    }

    @Override
    public BodyType insertAndFetch(String name) {
        final Map<String, Object> params = new HashMap<>();
        params.put("name", name);

        final long id = jdbcInsert.executeAndReturnKey(params).longValue();
        LOGGER.info("created body type id={} name={}", id, name);
        return findById(id).orElseThrow();
    }

    @Override
    public Optional<BodyType> update(final long id, final String name) {
        final int updated = jdbcTemplate.update(
                "UPDATE body_types SET name = ? WHERE body_type_id = ?",
                name, id
        );
        if (updated == 0) {
            LOGGER.warn("body type update affected 0 rows id={}", id);
            return Optional.empty();
        }
        LOGGER.info("updated body type id={} name={}", id, name);
        return findById(id);
    }

    @Override
    public boolean delete(final long id) {
        final boolean deleted = jdbcTemplate.update("DELETE FROM body_types WHERE body_type_id = ?", id) > 0;
        if (deleted) {
            LOGGER.info("deleted body type id={}", id);
        } else {
            LOGGER.warn("body type delete affected 0 rows id={}", id);
        }
        return deleted;
    }
}
