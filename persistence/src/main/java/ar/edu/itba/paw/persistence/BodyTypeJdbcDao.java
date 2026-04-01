package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.BodyType;
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
                .usingGeneratedKeyColumns("body_type_id");
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
                "SELECT body_type_id, name, created_at FROM body_types WHERE name = ?",
                ROW_MAPPER, name
        ).stream().findFirst();
    }

    @Override
    public BodyType create(final String name) {
        final Map<String, Object> params = new HashMap<>();
        params.put("name", name);

        final long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return findById(id).orElseThrow();
    }
}
