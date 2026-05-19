package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class CarJpaDao implements CarDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarJpaDao.class);

    private static final String REVIEW_STATS_JOIN =
            "LEFT JOIN (" +
            "SELECT car_id, COUNT(review_id) AS review_count, AVG(rating) AS average_rating " +
            "FROM reviews GROUP BY car_id" +
            ") rs ON rs.car_id = c.car_id ";

    @PersistenceContext
    private EntityManager em;

    private List<Car> loadCarsByIds(final List<Long> ids) {
        return em.createQuery(
                "SELECT c FROM Car c JOIN FETCH c.brand JOIN FETCH c.bodyTypeEntity WHERE c.id IN :ids",
                Car.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    private void populateHasImage(final List<Car> cars) {
        if (cars.isEmpty()) {
            return;
        }
        final List<Long> carIds = cars.stream().map(Car::getId).collect(Collectors.toList());
        final Set<Long> withImages = em.createQuery(
                "SELECT DISTINCT i.car.id FROM CarImage i WHERE i.car.id IN :carIds", Long.class)
                .setParameter("carIds", carIds)
                .getResultStream()
                .collect(Collectors.toSet());
        cars.forEach(c -> c.setHasImage(withImages.contains(c.getId())));
    }

    private List<Car> sortByIds(final List<Car> cars, final List<Long> orderedIds) {
        final Map<Long, Car> byId = cars.stream().collect(Collectors.toMap(Car::getId, Function.identity()));
        return orderedIds.stream().map(byId::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<Car> loadAndDecorate(final List<?> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Long> longIds = ids.stream().map(r -> ((Number) r).longValue()).collect(Collectors.toList());
        final List<Car> cars = loadCarsByIds(longIds);
        populateHasImage(cars);
        return sortByIds(cars, longIds);
    }

    @Override
    public List<Car> findAll() {
        final List<Car> cars = em.createQuery(
                "SELECT c FROM Car c JOIN FETCH c.brand JOIN FETCH c.bodyTypeEntity ORDER BY c.id",
                Car.class)
                .getResultList();
        populateHasImage(cars);
        return cars;
    }

    @Override
    public Optional<Car> findById(final long id) {
        final List<Car> results = em.createQuery(
                "SELECT c FROM Car c JOIN FETCH c.brand JOIN FETCH c.bodyTypeEntity WHERE c.id = :id",
                Car.class)
                .setParameter("id", id)
                .getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        final Car car = results.get(0);
        populateHasImage(List.of(car));
        return Optional.of(car);
    }

    @Override
    public List<Car> findByIds(final Collection<Long> ids) {
        final List<Long> normalizedIds = ids == null
                ? List.of()
                : ids.stream().filter(Objects::nonNull).distinct().sorted().collect(Collectors.toList());
        if (normalizedIds.isEmpty()) {
            return List.of();
        }
        final List<Car> cars = loadCarsByIds(normalizedIds);
        populateHasImage(cars);
        return sortByIds(cars, normalizedIds);
    }

    @Override
    public List<Car> findByBrandIdAndBodyTypeId(final long brandId, final long bodyTypeId) {
        final List<Car> cars = em.createQuery(
                "SELECT c FROM Car c JOIN FETCH c.brand JOIN FETCH c.bodyTypeEntity " +
                "WHERE c.brand.id = :brandId AND c.bodyTypeEntity.id = :bodyTypeId ORDER BY c.model",
                Car.class)
                .setParameter("brandId", brandId)
                .setParameter("bodyTypeId", bodyTypeId)
                .getResultList();
        populateHasImage(cars);
        return cars;
    }

    @Override
    public Page<Car> findByCriteria(final CarSearchCriteria criteria) {
        final List<Object> params = new ArrayList<>();
        final String whereClause = buildWhereClause(criteria, params);
        final boolean needsReviewStats = criteria.getSortBy() == null;
        final String orderClause = buildOrderClause(criteria);
        final int pageSize = Pagination.CARS_PAGE_SIZE;

        final String fromJoin = "FROM cars c " +
                "JOIN brands b ON c.brand_id = b.brand_id " +
                "JOIN body_types bt ON c.body_type_id = bt.body_type_id " +
                (needsReviewStats ? REVIEW_STATS_JOIN : "");

        // count
        final javax.persistence.Query countQuery = em.createNativeQuery("SELECT COUNT(*) " + fromJoin + whereClause);
        for (int i = 0; i < params.size(); i++) {
            countQuery.setParameter(i + 1, params.get(i));
        }
        final Number total = (Number) countQuery.getSingleResult();
        final long totalItems = total == null ? 0L : total.longValue();

        if (totalItems == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }

        final int page = Pagination.clampPage(Pagination.normalizePage(criteria.getPage()), totalItems, pageSize);
        final long offset = Pagination.offsetFor(page, pageSize);

        // IDs
        final javax.persistence.Query idsQuery = em.createNativeQuery(
                "SELECT c.car_id " + fromJoin + whereClause + orderClause + " LIMIT ? OFFSET ?");
        for (int i = 0; i < params.size(); i++) {
            idsQuery.setParameter(i + 1, params.get(i));
        }
        idsQuery.setParameter(params.size() + 1, pageSize);
        idsQuery.setParameter(params.size() + 2, offset);

        final List<?> ids = idsQuery.getResultList();
        if (ids.isEmpty()) {
            return Page.empty(page, pageSize);
        }

        final List<Long> longIds = ids.stream().map(r -> ((Number) r).longValue()).collect(Collectors.toList());
        final List<Car> cars = loadCarsByIds(longIds);
        populateHasImage(cars);
        return new Page<>(sortByIds(cars, longIds), page, pageSize, totalItems);
    }

    @Override
    public Car create(final long brandId, final String model, final long bodyTypeId, final Integer year,
                      final String description, final String fuelType, final Integer horsepower,
                      final Integer airbagCount, final String transmission, final BigDecimal fuelConsumption,
                      final Integer maxSpeedKmh, final BigDecimal priceUsd) {
        final Car car = new Car();
        car.setBrand(em.getReference(Brand.class, brandId));
        car.setModel(model);
        car.setBodyTypeEntity(em.getReference(BodyType.class, bodyTypeId));
        car.setYear(year);
        car.setDescription(description);
        car.setFuelType(fuelType);
        car.setHorsepower(horsepower);
        car.setAirbagCount(airbagCount);
        car.setTransmission(transmission);
        car.setFuelConsumption(fuelConsumption);
        car.setMaxSpeedKmh(maxSpeedKmh);
        car.setPriceUsd(priceUsd);
        em.persist(car);
        LOGGER.info("created car id={} brandId={} model={} bodyTypeId={}", car.getId(), brandId, model, bodyTypeId);
        return car;
    }

    @Override
    public Optional<Car> update(final long id, final long brandId, final String model, final long bodyTypeId,
                                final Integer year, final String description, final String fuelType,
                                final Integer horsepower, final Integer airbagCount, final String transmission,
                                final BigDecimal fuelConsumption, final Integer maxSpeedKmh, final BigDecimal priceUsd) {
        final Car car = em.find(Car.class, id);
        if (car == null) {
            LOGGER.warn("car update affected 0 rows id={}", id);
            return Optional.empty();
        }
        car.setBrand(em.getReference(Brand.class, brandId));
        car.setModel(model);
        car.setBodyTypeEntity(em.getReference(BodyType.class, bodyTypeId));
        car.setYear(year);
        car.setDescription(description);
        car.setFuelType(fuelType);
        car.setHorsepower(horsepower);
        car.setAirbagCount(airbagCount);
        car.setTransmission(transmission);
        car.setFuelConsumption(fuelConsumption);
        car.setMaxSpeedKmh(maxSpeedKmh);
        car.setPriceUsd(priceUsd);
        LOGGER.info("updated car id={} brandId={} model={}", id, brandId, model);
        return Optional.of(car);
    }

    @Override
    public boolean delete(final long id) {
        final Car car = em.find(Car.class, id);
        if (car == null) {
            LOGGER.warn("car delete affected 0 rows id={}", id);
            return false;
        }
        em.remove(car);
        LOGGER.info("deleted car id={}", id);
        return true;
    }

    @Override
    public long countByBrandId(final long brandId) {
        return em.createQuery(
                "SELECT COUNT(c) FROM Car c WHERE c.brand.id = :brandId", Long.class)
                .setParameter("brandId", brandId)
                .getSingleResult();
    }

    @Override
    public long countByBodyTypeId(final long bodyTypeId) {
        return em.createQuery(
                "SELECT COUNT(c) FROM Car c WHERE c.bodyTypeEntity.id = :bodyTypeId", Long.class)
                .setParameter("bodyTypeId", bodyTypeId)
                .getSingleResult();
    }

    @Override
    public List<Car> findTopRated(final int limit) {
        final List<Long> ids = em.createQuery(
                "SELECT r.car.id FROM Review r GROUP BY r.car.id " +
                "ORDER BY AVG(r.rating) DESC, COUNT(r.id) DESC",
                Long.class)
                .setMaxResults(limit)
                .getResultList();
        return loadAndDecorate(ids);
    }

    @Override
    public List<Car> findRecentlyAdded(final int limit, final Collection<Long> excludedIds) {
        final List<Long> excluded = excludedIds == null
                ? List.of()
                : excludedIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());

        final List<?> ids;
        if (excluded.isEmpty()) {
            ids = em.createQuery(
                    "SELECT c.id FROM Car c ORDER BY c.createdAt DESC, c.id ASC", Long.class)
                    .setMaxResults(limit)
                    .getResultList();
        } else {
            ids = em.createQuery(
                    "SELECT c.id FROM Car c WHERE c.id NOT IN :excluded " +
                    "ORDER BY c.createdAt DESC, c.id ASC", Long.class)
                    .setParameter("excluded", excluded)
                    .setMaxResults(limit)
                    .getResultList();
        }
        return loadAndDecorate(ids);
    }

    private String buildWhereClause(final CarSearchCriteria criteria, final List<Object> params) {
        final StringBuilder sql = new StringBuilder();
        boolean hasWhere = false;

        if (criteria.getQ() != null) {
            final String escaped = criteria.getQ().toLowerCase()
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            final String likeQ = "%" + escaped + "%";
            final String tsQ = criteria.getQ().replaceAll("[%_\\\\]", " ").trim();
            final boolean useTsQuery = tsQ.matches(".*[a-zA-Z0-9]{2,}.*");
            if (!useTsQuery) {
                sql.append("WHERE (lower(b.name) LIKE ? ESCAPE '\\' " +
                        "OR lower(c.model) LIKE ? ESCAPE '\\' " +
                        "OR lower(COALESCE(c.description, '')) LIKE ? ESCAPE '\\') ");
                params.add(likeQ);
                params.add(likeQ);
                params.add(likeQ);
            } else {
                sql.append("WHERE (c.search_vector @@ websearch_to_tsquery('simple', ?) " +
                        "OR lower(b.name) LIKE ? ESCAPE '\\' " +
                        "OR lower(c.model) LIKE ? ESCAPE '\\' " +
                        "OR lower(COALESCE(c.description, '')) LIKE ? ESCAPE '\\') ");
                params.add(tsQ);
                params.add(likeQ);
                params.add(likeQ);
                params.add(likeQ);
            }
            hasWhere = true;
        }

        if (criteria.getBrand() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("b.name = ? ");
            params.add(criteria.getBrand());
            hasWhere = true;
        }
        if (criteria.getBodyType() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("bt.name = ? ");
            params.add(criteria.getBodyType());
            hasWhere = true;
        }
        if (!criteria.getFuelTypes().isEmpty()) {
            final String fp = criteria.getFuelTypes().stream().map(ft -> "?").collect(Collectors.joining(","));
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.fuel_type IN (").append(fp).append(") ");
            params.addAll(criteria.getFuelTypes());
            hasWhere = true;
        }
        if (criteria.getYearMin() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.year >= ? ");
            params.add(criteria.getYearMin());
            hasWhere = true;
        }
        if (criteria.getYearMax() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.year <= ? ");
            params.add(criteria.getYearMax());
            hasWhere = true;
        }
        if (criteria.getHorsepowerMin() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.horsepower >= ? ");
            params.add(criteria.getHorsepowerMin());
            hasWhere = true;
        }
        if (criteria.getHorsepowerMax() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.horsepower <= ? ");
            params.add(criteria.getHorsepowerMax());
            hasWhere = true;
        }
        if (criteria.getAirbagMin() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.airbag_count >= ? ");
            params.add(criteria.getAirbagMin());
            hasWhere = true;
        }
        if (criteria.getTransmission() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.transmission = ? ");
            params.add(criteria.getTransmission());
            hasWhere = true;
        }
        if (criteria.getFuelConsumptionMax() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.fuel_consumption <= ? ");
            params.add(criteria.getFuelConsumptionMax());
            hasWhere = true;
        }
        if (criteria.getMaxSpeedMin() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.max_speed_kmh >= ? ");
            params.add(criteria.getMaxSpeedMin());
            hasWhere = true;
        }
        if (criteria.getPriceMin() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.price_usd >= ? ");
            params.add(criteria.getPriceMin());
            hasWhere = true;
        }
        if (criteria.getPriceMax() != null) {
            sql.append(hasWhere ? "AND " : "WHERE ").append("c.price_usd <= ? ");
            params.add(criteria.getPriceMax());
        }
        return sql.toString();
    }

    private String buildOrderClause(final CarSearchCriteria criteria) {
        final String sortBy = criteria.getSortBy();
        if (sortBy != null) {
            switch (sortBy) {
                case CarSearchCriteria.SORT_NAME_ASC:
                    return "ORDER BY b.name ASC, c.model ASC";
                case CarSearchCriteria.SORT_HP_DESC:
                    return "ORDER BY c.horsepower DESC NULLS LAST, c.car_id ASC";
                case CarSearchCriteria.SORT_HP_ASC:
                    return "ORDER BY c.horsepower ASC NULLS LAST, c.car_id ASC";
                case CarSearchCriteria.SORT_SPEED_DESC:
                    return "ORDER BY c.max_speed_kmh DESC NULLS LAST, c.car_id ASC";
                case CarSearchCriteria.SORT_CONSUMPTION_ASC:
                    return "ORDER BY c.fuel_consumption ASC NULLS LAST, c.car_id ASC";
                case CarSearchCriteria.SORT_PRICE_ASC:
                    return "ORDER BY c.price_usd ASC NULLS LAST, c.car_id ASC";
                case CarSearchCriteria.SORT_PRICE_DESC:
                    return "ORDER BY c.price_usd DESC NULLS LAST, c.car_id ASC";
                default:
                    return "ORDER BY c.car_id ASC";
            }
        }
        return "ORDER BY COALESCE(rs.average_rating, 0) DESC, COALESCE(rs.review_count, 0) DESC, c.car_id ASC";
    }
}
