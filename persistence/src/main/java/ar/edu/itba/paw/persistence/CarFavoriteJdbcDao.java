package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class CarFavoriteJdbcDao implements CarFavoriteDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarFavoriteJdbcDao.class);

    private static final String SELECT_COLUMNS =
            "SELECT c.car_id, c.brand_id, b.name AS brand_name, c.model, c.body_type_id, bt.name AS body_type, "
                    + "c.year, c.description, c.created_at, "
                    + "EXISTS (SELECT 1 FROM car_images ci WHERE ci.car_id = c.car_id) AS has_image, "
                    + "c.fuel_type, c.horsepower, c.airbag_count, c.transmission, c.fuel_consumption, c.max_speed_kmh, c.price_usd ";

    private static final String FROM_JOIN =
            "FROM cars c "
                    + "JOIN brands b ON c.brand_id = b.brand_id "
                    + "JOIN body_types bt ON c.body_type_id = bt.body_type_id ";

    private static final RowMapper<Car> CAR_ROW_MAPPER = (rs, rowNum) -> new Car(
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

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CarFavoriteJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean favorite(final long userId, final long carId) {
        try {
            final boolean inserted = jdbcTemplate.update(
                    "INSERT INTO car_favorites (user_id, car_id) VALUES (?, ?)",
                    userId,
                    carId
            ) > 0;
            if (inserted) {
                LOGGER.info("user id={} favorited car id={}", userId, carId);
            }
            return inserted;
        } catch (final DuplicateKeyException ignored) {
            LOGGER.debug("user id={} already favorited car id={}", userId, carId);
            return false;
        }
    }

    @Override
    public boolean unfavorite(final long userId, final long carId) {
        final boolean removed = jdbcTemplate.update(
                "DELETE FROM car_favorites WHERE user_id = ? AND car_id = ?",
                userId,
                carId
        ) > 0;
        if (removed) {
            LOGGER.info("user id={} unfavorited car id={}", userId, carId);
        }
        return removed;
    }

    @Override
    public boolean isFavorited(final long userId, final long carId) {
        final Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM car_favorites WHERE user_id = ? AND car_id = ?)",
                Boolean.class,
                userId,
                carId
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public List<Car> findFavoriteCars(final long userId) {
        return jdbcTemplate.query(
                SELECT_COLUMNS + FROM_JOIN
                        + "JOIN car_favorites cf ON cf.car_id = c.car_id "
                        + "WHERE cf.user_id = ? ORDER BY cf.created_at DESC, c.car_id DESC",
                CAR_ROW_MAPPER,
                userId
        );
    }

    @Override
    public Page<Car> findFavoriteCars(final long userId, final int page) {
        final int normalizedPage = Pagination.normalizePage(page);
        final int pageSize = Pagination.CARS_PAGE_SIZE;
        final long total = countFavoriteCars(userId);
        if (total == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }
        final int effectivePage = Pagination.clampPage(normalizedPage, total, pageSize);
        final long offset = Pagination.offsetFor(effectivePage, pageSize);
        final List<Car> items = jdbcTemplate.query(
                SELECT_COLUMNS + FROM_JOIN
                        + "JOIN car_favorites cf ON cf.car_id = c.car_id "
                        + "WHERE cf.user_id = ? ORDER BY cf.created_at DESC, c.car_id DESC LIMIT ? OFFSET ?",
                CAR_ROW_MAPPER,
                userId,
                pageSize,
                offset
        );
        return new Page<>(items, effectivePage, pageSize, total);
    }

    @Override
    public long countFavoriteCars(final long userId) {
        final Long count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM car_favorites WHERE user_id = ?", Long.class, userId);
        return count == null ? 0L : count;
    }

    @Override
    public Set<Long> findFavoritedCarIds(final long userId, final Collection<Long> carIds) {
        final List<Long> normalizedCarIds = carIds == null
                ? List.of()
                : carIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (normalizedCarIds.isEmpty()) {
            return Collections.emptySet();
        }

        final String placeholders = String.join(", ", Collections.nCopies(normalizedCarIds.size(), "?"));
        final Object[] params = new Object[normalizedCarIds.size() + 1];
        params[0] = userId;
        for (int i = 0; i < normalizedCarIds.size(); i++) {
            params[i + 1] = normalizedCarIds.get(i);
        }

        return jdbcTemplate.queryForList(
                        "SELECT car_id FROM car_favorites WHERE user_id = ? AND car_id IN (" + placeholders + ")",
                        Long.class,
                        params
                )
                .stream()
                .collect(Collectors.toSet());
    }
}
