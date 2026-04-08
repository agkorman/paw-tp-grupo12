package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
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
                .usingGeneratedKeyColumns("brand_id");
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
    public Brand create(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);

        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return findById(id).orElseThrow();
    }
}
