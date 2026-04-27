package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
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

    private static final String REVIEW_STATS_JOIN =
            "LEFT JOIN ("
                    + "SELECT car_id, COUNT(review_id) AS review_count, AVG(rating) AS average_rating "
                    + "FROM reviews GROUP BY car_id"
                    + ") rs ON rs.car_id = c.car_id ";

    private static final String SELECT_COLUMNS =
            "SELECT c.car_id, c.brand_id, b.name AS brand_name, c.model, c.body_type_id, bt.name AS body_type, "
                    + "c.year, c.description, c.created_at, "
                    + "EXISTS (SELECT 1 FROM car_images ci WHERE ci.car_id = c.car_id) AS has_image, "
                    + "c.fuel_type, c.horsepower, c.airbag_count, c.transmission, c.fuel_consumption, c.max_speed_kmh, c.price_usd ";

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static final RowMapper<Car> ROW_MAPPER = (rs, rowNum) -> new Car(
            rs.getLong("car_id"),
            rs.getLong("brand_id"),
            rs.getString("brand_name"),
            rs.getString("model"),
            rs.getLong("body_type_id"),
            rs.getObject("year", Integer.class),
            rs.getString("body_type"),
            rs.getString("description"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getBoolean("has_image"),
            rs.getString("fuel_type"),
            rs.getObject("horsepower", Integer.class),
            rs.getObject("airbag_count", Integer.class),
            rs.getString("transmission"),
            rs.getBigDecimal("fuel_consumption"),
            rs.getObject("max_speed_kmh", Integer.class),
            rs.getBigDecimal("price_usd")
    );

    @Autowired
    public CarJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("cars")
                .usingGeneratedKeyColumns("car_id")
                .usingColumns("brand_id", "model", "body_type_id", "year", "description",
                        "fuel_type", "horsepower", "airbag_count", "transmission",
                        "fuel_consumption", "max_speed_kmh", "price_usd");
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
    public Car create(final long brandId, final String model, final long bodyTypeId, final Integer year,
                      final String description,
                      final String fuelType, final Integer horsepower, final Integer airbagCount,
                      final String transmission, final BigDecimal fuelConsumption, final Integer maxSpeedKmh,
                      final BigDecimal priceUsd) {
        final Map<String, Object> params = new HashMap<>();
        params.put("brand_id", brandId);
        params.put("model", model);
        params.put("body_type_id", bodyTypeId);
        params.put("year", year);
        params.put("description", description);
        params.put("fuel_type", fuelType);
        params.put("horsepower", horsepower);
        params.put("airbag_count", airbagCount);
        params.put("transmission", transmission);
        params.put("fuel_consumption", fuelConsumption);
        params.put("max_speed_kmh", maxSpeedKmh);
        params.put("price_usd", priceUsd);

        final long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return findById(id).orElseThrow();
    }

    @Override
    public Page<Car> findByCriteria(final CarSearchCriteria criteria) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        final String whereClause = buildWhereClause(criteria, params);
        final boolean requiresReviewStats = requiresReviewStatsJoin(criteria);
        final String orderClause = buildOrderClause(criteria);

        final int pageSize = Pagination.CARS_PAGE_SIZE;

        final Long total = namedJdbcTemplate.queryForObject(
                "SELECT count(*) " + FROM_JOIN + whereClause,
                params, Long.class);
        final long totalItems = total == null ? 0L : total;

        if (totalItems == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }

        final int page = Pagination.clampPage(Pagination.normalizePage(criteria.getPage()), totalItems, pageSize);
        final long offset = Pagination.offsetFor(page, pageSize);

        final MapSqlParameterSource pagedParams = new MapSqlParameterSource(params.getValues());
        pagedParams.addValue("limit", pageSize);
        pagedParams.addValue("offset", offset);

        final String fromClause = FROM_JOIN + (requiresReviewStats ? REVIEW_STATS_JOIN : "");
        final List<Car> items = namedJdbcTemplate.query(
                SELECT_COLUMNS + fromClause + whereClause + orderClause + " LIMIT :limit OFFSET :offset",
                pagedParams, ROW_MAPPER);

        return new Page<>(items, page, pageSize, totalItems);
    }

    private boolean requiresReviewStatsJoin(final CarSearchCriteria criteria) {
        return criteria.getSortBy() == null;
    }

    private String buildWhereClause(final CarSearchCriteria criteria, final MapSqlParameterSource params) {
        final StringBuilder sql = new StringBuilder();
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
        if (!criteria.getFuelTypes().isEmpty()) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.fuel_type IN (:fuelTypes) ");
            params.addValue("fuelTypes", criteria.getFuelTypes());
            hasWhere = true;
        }
        if (criteria.getYear() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.year = :year ");
            params.addValue("year", criteria.getYear());
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
            hasWhere = true;
        }
        if (criteria.getPriceMin() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.price_usd >= :priceMin ");
            params.addValue("priceMin", criteria.getPriceMin());
            hasWhere = true;
        }
        if (criteria.getPriceMax() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.price_usd <= :priceMax ");
            params.addValue("priceMax", criteria.getPriceMax());
        }
        return sql.toString();
    }

    private String buildOrderClause(final CarSearchCriteria criteria) {
        final String sortBy = criteria.getSortBy();
        if (sortBy != null) {
            switch (sortBy) {
                case "name_asc":
                    return "ORDER BY b.name ASC, c.model ASC";
                case "hp_desc":
                    return "ORDER BY c.horsepower DESC NULLS LAST, c.car_id ASC";
                case "hp_asc":
                    return "ORDER BY c.horsepower ASC NULLS LAST, c.car_id ASC";
                case "speed_desc":
                    return "ORDER BY c.max_speed_kmh DESC NULLS LAST, c.car_id ASC";
                case "consumption_asc":
                    return "ORDER BY c.fuel_consumption ASC NULLS LAST, c.car_id ASC";
                case "price_asc":
                    return "ORDER BY c.price_usd ASC NULLS LAST, c.car_id ASC";
                case "price_desc":
                    return "ORDER BY c.price_usd DESC NULLS LAST, c.car_id ASC";
                default:
                    return "ORDER BY c.car_id ASC";
            }
        }
        return "ORDER BY COALESCE(rs.average_rating, 0) DESC, COALESCE(rs.review_count, 0) DESC, c.car_id ASC";
    }

    @Override
    public Optional<Car> update(final long id, final long brandId, final String model,
                                final long bodyTypeId, final Integer year, final String description,
                                final String fuelType, final Integer horsepower, final Integer airbagCount,
                                final String transmission, final BigDecimal fuelConsumption,
                                final Integer maxSpeedKmh, final BigDecimal priceUsd) {
        final int updated = jdbcTemplate.update(
                "UPDATE cars SET brand_id = ?, model = ?, body_type_id = ?, year = ?, description = ?, "
                        + "fuel_type = ?, horsepower = ?, airbag_count = ?, transmission = ?, "
                        + "fuel_consumption = ?, max_speed_kmh = ?, price_usd = ? WHERE car_id = ?",
                brandId, model, bodyTypeId, year, description, fuelType, horsepower, airbagCount, transmission,
                fuelConsumption, maxSpeedKmh, priceUsd, id
        );
        return updated > 0 ? findById(id) : Optional.empty();
    }

    @Override
    public boolean delete(final long id) {
        return jdbcTemplate.update("DELETE FROM cars WHERE car_id = ?", id) > 0;
    }

    @Override
    public List<Car> findByBrandIdAndBodyTypeId(final long brandId, final long bodyTypeId) {
        return jdbcTemplate.query(
                SELECT_COLUMNS + FROM_JOIN + "WHERE c.brand_id = ? AND c.body_type_id = ? ORDER BY c.model",
                ROW_MAPPER, brandId, bodyTypeId
        );
    }

}
