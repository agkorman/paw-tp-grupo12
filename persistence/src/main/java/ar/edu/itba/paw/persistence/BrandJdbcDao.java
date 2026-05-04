package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
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
public class BrandJdbcDao implements BrandDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrandJdbcDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static final RowMapper<Brand> ROW_MAPPER = (rs, rowNum) -> new Brand(
            rs.getLong("brand_id"),
            rs.getString("name"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    @Autowired
    public BrandJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("brands")
                .usingGeneratedKeyColumns("brand_id")
                .usingColumns("name");
    }

    @Override
    public List<Brand> findAll() {
        return jdbcTemplate.query(
                "SELECT brand_id, name, created_at FROM brands ORDER BY name",
                ROW_MAPPER
        );
    }

    @Override
    public Optional<Brand> findById(long id) {
        return jdbcTemplate.query(
                "SELECT brand_id, name, created_at FROM brands WHERE brand_id = ?",
                ROW_MAPPER, id
        ).stream().findFirst();
    }

    @Override
    public Optional<Brand> findByName(String name) {
        return jdbcTemplate.query(
                "SELECT brand_id, name, created_at FROM brands WHERE LOWER(name) = LOWER(?)",
                ROW_MAPPER, name
        ).stream().findFirst();
    }

    @Override
    public Brand insertAndFetch(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);

        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        LOGGER.info("created brand id={} name={}", id, name);
        return findById(id).orElseThrow();
    }

    @Override
    public Optional<Brand> update(final long id, final String name) {
        final int updated = jdbcTemplate.update(
                "UPDATE brands SET name = ? WHERE brand_id = ?",
                name, id
        );
        if (updated == 0) {
            LOGGER.warn("brand update affected 0 rows id={}", id);
            return Optional.empty();
        }
        LOGGER.info("updated brand id={} name={}", id, name);
        return findById(id);
    }

    @Override
    public boolean delete(final long id) {
        final boolean deleted = jdbcTemplate.update("DELETE FROM brands WHERE brand_id = ?", id) > 0;
        if (deleted) {
            LOGGER.info("deleted brand id={}", id);
        } else {
            LOGGER.warn("brand delete affected 0 rows id={}", id);
        }
        return deleted;
    }
}
