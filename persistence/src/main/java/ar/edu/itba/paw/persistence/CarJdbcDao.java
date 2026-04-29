package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class CarJdbcDao implements CarDao {

    private static final String FROM_JOIN =
            "FROM cars c "
                    + "JOIN brands b ON c.brand_id = b.brand_id "
                    + "JOIN body_types bt ON c.body_type_id = bt.body_type_id ";

    private static final String SELECT_COLUMNS =
            "SELECT c.car_id, c.brand_id, b.name AS brand_name, c.model, c.body_type_id, bt.name AS body_type, "
                    + "c.description, c.created_at, "
                    + "EXISTS (SELECT 1 FROM car_images ci WHERE ci.car_id = c.car_id) AS has_image, "
                    + "c.fuel_type, c.horsepower, c.airbag_count, c.transmission, c.fuel_consumption, c.max_speed_kmh ";

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static final RowMapper<Car> ROW_MAPPER = (rs, rowNum) -> new Car(
            rs.getLong("car_id"),
            rs.getLong("brand_id"),
            rs.getString("brand_name"),
            rs.getString("model"),
            rs.getLong("body_type_id"),
            rs.getString("body_type"),
            rs.getString("description"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getBoolean("has_image"),
            rs.getString("fuel_type"),
            rs.getObject("horsepower", Integer.class),
            rs.getObject("airbag_count", Integer.class),
            rs.getString("transmission"),
            rs.getBigDecimal("fuel_consumption"),
            rs.getObject("max_speed_kmh", Integer.class)
    );

    @Autowired
    public CarJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("cars")
                .usingGeneratedKeyColumns("car_id")
                .usingColumns("brand_id", "model", "body_type_id", "description",
                        "fuel_type", "horsepower", "airbag_count", "transmission",
                        "fuel_consumption", "max_speed_kmh");
    }

    @Override
    public List<Car> findAll() {
        return jdbcTemplate.query(
                SELECT_COLUMNS + FROM_JOIN + "ORDER BY c.car_id",
                ROW_MAPPER
        );
    }

    @Override
    public Optional<Car> findById(final long id) {
        return jdbcTemplate.query(
                SELECT_COLUMNS + FROM_JOIN + "WHERE c.car_id = ?",
                ROW_MAPPER, id
        ).stream().findFirst();
    }

    @Override
    public List<Car> findByIds(final Collection<Long> ids) {
        final List<Long> normalizedIds = ids == null
                ? List.of()
                : ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (normalizedIds.isEmpty()) {
            return List.of();
        }

        final String placeholders = String.join(", ", java.util.Collections.nCopies(normalizedIds.size(), "?"));
        return jdbcTemplate.query(
                SELECT_COLUMNS + FROM_JOIN + "WHERE c.car_id IN (" + placeholders + ") ORDER BY c.car_id",
                ROW_MAPPER,
                normalizedIds.toArray()
        );
    }

    @Override
    public List<Car> findByBrandId(final long brandId) {
        return jdbcTemplate.query(
                SELECT_COLUMNS + FROM_JOIN + "WHERE c.brand_id = ? ORDER BY c.model",
                ROW_MAPPER, brandId
        );
    }

    @Override
    public Car create(final long brandId, final String model, final long bodyTypeId, final String description,
                      final String fuelType, final Integer horsepower, final Integer airbagCount,
                      final String transmission, final BigDecimal fuelConsumption, final Integer maxSpeedKmh) {
        final Map<String, Object> params = new HashMap<>();
        params.put("brand_id", brandId);
        params.put("model", model);
        params.put("body_type_id", bodyTypeId);
        params.put("description", description);
        params.put("fuel_type", fuelType);
        params.put("horsepower", horsepower);
        params.put("airbag_count", airbagCount);
        params.put("transmission", transmission);
        params.put("fuel_consumption", fuelConsumption);
        params.put("max_speed_kmh", maxSpeedKmh);

        final long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return findById(id).orElseThrow();
    }

    @Override
    public List<Car> findByCriteria(final CarSearchCriteria criteria) {
        final StringBuilder sql = new StringBuilder(SELECT_COLUMNS).append(FROM_JOIN);
        final MapSqlParameterSource params = new MapSqlParameterSource();
        boolean hasWhere = false;

        if (criteria.getQ() != null) {
            final String escaped = criteria.getQ().toLowerCase().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            final String likeQ = "%" + escaped + "%";
            final String tsQ = criteria.getQ().replaceAll("[%_\\\\]", " ").trim();
            final boolean useTsQuery = tsQ.matches(".*[a-zA-Z0-9]{2,}.*");
            if (!useTsQuery) {
                sql.append("WHERE (lower(b.name) LIKE :likeQ ESCAPE '\\' "
                        + "OR lower(c.model) LIKE :likeQ ESCAPE '\\' "
                        + "OR lower(COALESCE(c.description, '')) LIKE :likeQ ESCAPE '\\') ");
            } else {
                sql.append("WHERE (c.search_vector @@ websearch_to_tsquery('simple', :q) "
                        + "OR lower(b.name) LIKE :likeQ ESCAPE '\\' "
                        + "OR lower(c.model) LIKE :likeQ ESCAPE '\\' "
                        + "OR lower(COALESCE(c.description, '')) LIKE :likeQ ESCAPE '\\') ");
                params.addValue("q", tsQ);
            }
            params.addValue("likeQ", likeQ);
            hasWhere = true;
        }

        if (criteria.getBrand() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("b.name = :brand ");
            params.addValue("brand", criteria.getBrand());
            hasWhere = true;
        }
        if (criteria.getBodyType() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("bt.name = :bodyType ");
            params.addValue("bodyType", criteria.getBodyType());
            hasWhere = true;
        }
        if (criteria.getFuelType() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.fuel_type = :fuelType ");
            params.addValue("fuelType", criteria.getFuelType());
            hasWhere = true;
        }
        if (criteria.getHorsepowerMin() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.horsepower >= :horsepowerMin ");
            params.addValue("horsepowerMin", criteria.getHorsepowerMin());
            hasWhere = true;
        }
        if (criteria.getHorsepowerMax() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.horsepower <= :horsepowerMax ");
            params.addValue("horsepowerMax", criteria.getHorsepowerMax());
            hasWhere = true;
        }
        if (criteria.getAirbagMin() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.airbag_count >= :airbagMin ");
            params.addValue("airbagMin", criteria.getAirbagMin());
            hasWhere = true;
        }
        if (criteria.getTransmission() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.transmission = :transmission ");
            params.addValue("transmission", criteria.getTransmission());
            hasWhere = true;
        }
        if (criteria.getFuelConsumptionMax() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.fuel_consumption <= :fuelConsumptionMax ");
            params.addValue("fuelConsumptionMax", criteria.getFuelConsumptionMax());
            hasWhere = true;
        }
        if (criteria.getMaxSpeedMin() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.max_speed_kmh >= :maxSpeedMin ");
            params.addValue("maxSpeedMin", criteria.getMaxSpeedMin());
        }

        final String sortBy = criteria.getSortBy();
        if (sortBy != null) {
            switch (sortBy) {
                case "name_asc":
                    sql.append("ORDER BY b.name ASC, c.model ASC");
                    break;
                case "name_desc":
                    sql.append("ORDER BY b.name DESC, c.model DESC");
                    break;
                case "hp_desc":
                    sql.append("ORDER BY c.horsepower DESC NULLS LAST, c.car_id ASC");
                    break;
                case "hp_asc":
                    sql.append("ORDER BY c.horsepower ASC NULLS LAST, c.car_id ASC");
                    break;
                case "speed_desc":
                    sql.append("ORDER BY c.max_speed_kmh DESC NULLS LAST, c.car_id ASC");
                    break;
                case "consumption_asc":
                    sql.append("ORDER BY c.fuel_consumption ASC NULLS LAST, c.car_id ASC");
                    break;
                default:
                    sql.append("ORDER BY c.car_id ASC");
            }
        } else if (criteria.getQ() != null) {
            final String tsQ = criteria.getQ().replaceAll("[%_\\\\]", " ").trim();
            final StringBuilder order = new StringBuilder("ORDER BY ");
            if (tsQ.matches(".*[a-zA-Z0-9]{2,}.*")) {
                order.append("ts_rank(c.search_vector, websearch_to_tsquery('simple', :q)) DESC, ");
            }
            order.append("CASE WHEN lower(c.model) LIKE :likeQ ESCAPE '\\' THEN 0 "
                    + "     WHEN lower(b.name) LIKE :likeQ ESCAPE '\\' THEN 1 "
                    + "     WHEN lower(COALESCE(c.description, '')) LIKE :likeQ ESCAPE '\\' THEN 2 "
                    + "     ELSE 3 END, c.car_id ASC");
            sql.append(order);
        } else {
            sql.append("ORDER BY c.car_id ASC");
        }

        return namedJdbcTemplate.query(sql.toString(), params, ROW_MAPPER);
    }

    @Override
    public Optional<Car> update(final long id, final long brandId, final String model,
                                final long bodyTypeId, final String description,
                                final String fuelType, final Integer horsepower, final Integer airbagCount,
                                final String transmission, final BigDecimal fuelConsumption,
                                final Integer maxSpeedKmh) {
        final int updated = jdbcTemplate.update(
                "UPDATE cars SET brand_id = ?, model = ?, body_type_id = ?, description = ?, "
                        + "fuel_type = ?, horsepower = ?, airbag_count = ?, transmission = ?, "
                        + "fuel_consumption = ?, max_speed_kmh = ? WHERE car_id = ?",
                brandId, model, bodyTypeId, description, fuelType, horsepower, airbagCount, transmission,
                fuelConsumption, maxSpeedKmh, id
        );
        return updated > 0 ? findById(id) : Optional.empty();
    }

    @Override
    public boolean delete(final long id) {
        return jdbcTemplate.update("DELETE FROM cars WHERE car_id = ?", id) > 0;
    }

    @Override
    public List<Car> findByBodyTypeId(final long bodyTypeId) {
        return jdbcTemplate.query(
                SELECT_COLUMNS + FROM_JOIN + "WHERE c.body_type_id = ? ORDER BY c.model",
                ROW_MAPPER, bodyTypeId
        );
    }

    @Override
    public List<Car> findByBrandIdAndBodyTypeId(final long brandId, final long bodyTypeId) {
        return jdbcTemplate.query(
                SELECT_COLUMNS + FROM_JOIN + "WHERE c.brand_id = ? AND c.body_type_id = ? ORDER BY c.model",
                ROW_MAPPER, brandId, bodyTypeId
        );
    }

    @Override
    public List<Car> search(final String query, final Long brandId, final Long bodyTypeId) {
        final String trimmed = query.trim();
        final String escaped = trimmed.toLowerCase().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        final String likeQuery = "%" + escaped + "%";
        final String tsQuery = trimmed.replaceAll("[%_\\\\]", " ").trim();
        final StringBuilder sql = new StringBuilder(SELECT_COLUMNS).append(FROM_JOIN);
        final boolean useTsQuery = tsQuery.matches(".*[a-zA-Z0-9]{2,}.*");
        final List<Object> params = new ArrayList<>();
        if (!useTsQuery) {
            sql.append("WHERE (lower(b.name) LIKE ? ESCAPE '\\' ");
            sql.append("   OR lower(c.model) LIKE ? ESCAPE '\\' ");
            sql.append("   OR lower(COALESCE(c.description, '')) LIKE ? ESCAPE '\\') ");
        } else {
            sql.append("WHERE (c.search_vector @@ websearch_to_tsquery('simple', ?) ");
            sql.append("   OR lower(b.name) LIKE ? ESCAPE '\\' ");
            sql.append("   OR lower(c.model) LIKE ? ESCAPE '\\' ");
            sql.append("   OR lower(COALESCE(c.description, '')) LIKE ? ESCAPE '\\') ");
            params.add(tsQuery);
        }
        params.add(likeQuery);
        params.add(likeQuery);
        params.add(likeQuery);

        if (brandId != null) {
            sql.append("AND c.brand_id = ? ");
            params.add(brandId);
        }
        if (bodyTypeId != null) {
            sql.append("AND c.body_type_id = ? ");
            params.add(bodyTypeId);
        }

        if (useTsQuery) {
            sql.append("ORDER BY ts_rank(c.search_vector, websearch_to_tsquery('simple', ?)) DESC, ");
            params.add(tsQuery);
        } else {
            sql.append("ORDER BY ");
        }
        sql.append("CASE WHEN lower(c.model) LIKE ? ESCAPE '\\' THEN 0 ");
        sql.append("     WHEN lower(b.name) LIKE ? ESCAPE '\\' THEN 1 ");
        sql.append("     WHEN lower(COALESCE(c.description, '')) LIKE ? ESCAPE '\\' THEN 2 ");
        sql.append("     ELSE 3 END, ");
        sql.append("c.car_id ASC");
        params.add(likeQuery);
        params.add(likeQuery);
        params.add(likeQuery);

        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, params.toArray());
    }
}
