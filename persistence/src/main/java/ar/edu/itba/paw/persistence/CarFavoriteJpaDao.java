package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class CarFavoriteJpaDao implements CarFavoriteDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarFavoriteJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean favorite(final long userId, final long carId) {
        final int rows = em.createNativeQuery(
                "INSERT INTO car_favorites (user_id, car_id) " +
                "SELECT :userId, :carId FROM (SELECT 1) AS d " +
                "WHERE NOT EXISTS (SELECT 1 FROM car_favorites WHERE user_id = :userId AND car_id = :carId)")
                .setParameter("userId", userId)
                .setParameter("carId", carId)
                .executeUpdate();
        if (rows == 0) {
            LOGGER.debug("user id={} already favorited car id={}", userId, carId);
            return false;
        }
        LOGGER.info("user id={} favorited car id={}", userId, carId);
        return true;
    }

    @Override
    public boolean unfavorite(final long userId, final long carId) {
        final int rows = em.createNativeQuery(
                "DELETE FROM car_favorites WHERE user_id = ? AND car_id = ?")
                .setParameter(1, userId)
                .setParameter(2, carId)
                .executeUpdate();
        if (rows > 0) {
            LOGGER.info("user id={} unfavorited car id={}", userId, carId);
        }
        return rows > 0;
    }

    @Override
    public boolean isFavorited(final long userId, final long carId) {
        final Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM car_favorites WHERE user_id = ? AND car_id = ?")
                .setParameter(1, userId)
                .setParameter(2, carId)
                .getSingleResult();
        return count != null && count.longValue() > 0;
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

        final List<?> ids = em.createNativeQuery(
                "SELECT c.car_id FROM cars c " +
                "JOIN car_favorites cf ON cf.car_id = c.car_id " +
                "WHERE cf.user_id = ? ORDER BY cf.created_at DESC, c.car_id DESC LIMIT ? OFFSET ?")
                .setParameter(1, userId)
                .setParameter(2, pageSize)
                .setParameter(3, offset)
                .getResultList();

        if (ids.isEmpty()) {
            return Page.empty(effectivePage, pageSize);
        }

        final List<Long> longIds = ids.stream().map(r -> ((Number) r).longValue()).collect(Collectors.toList());
        final List<Car> cars = loadCarsByIds(longIds);
        populateHasImage(cars);
        return new Page<>(sortByIds(cars, longIds), effectivePage, pageSize, total);
    }

    @Override
    public long countFavoriteCars(final long userId) {
        final Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM car_favorites WHERE user_id = ?")
                .setParameter(1, userId)
                .getSingleResult();
        return count == null ? 0L : count.longValue();
    }

    @Override
    public Set<Long> findFavoritedCarIds(final long userId, final Collection<Long> carIds) {
        final List<Long> normalizedCarIds = carIds == null
                ? List.of()
                : carIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (normalizedCarIds.isEmpty()) {
            return Collections.emptySet();
        }
        final javax.persistence.Query query = em.createNativeQuery(
                "SELECT car_id FROM car_favorites WHERE user_id = :userId AND car_id IN (:ids)");
        query.setParameter("userId", userId);
        query.setParameter("ids", normalizedCarIds);
        final List<?> result = query.getResultList();
        return result.stream().map(r -> ((Number) r).longValue()).collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public Map<Long, List<Long>> findAllFavoriteCarIdsByUser() {
        final List<?> rawRows = em.createNativeQuery(
                "SELECT user_id, car_id FROM car_favorites ORDER BY user_id, car_id")
                .getResultList();
        final Map<Long, List<Long>> favoritesByUser = new java.util.LinkedHashMap<>();
        for (final Object element : rawRows) {
            final Object[] row = (Object[]) element;
            final long userId = ((Number) row[0]).longValue();
            final long carId = ((Number) row[1]).longValue();
            favoritesByUser.computeIfAbsent(userId, k -> new java.util.ArrayList<>()).add(carId);
        }
        return favoritesByUser;
    }

    private List<Car> loadCarsByIds(final List<Long> ids) {
        return em.createQuery(
                "SELECT c FROM Car c JOIN FETCH c.spec.brand JOIN FETCH c.spec.bodyType WHERE c.id IN :ids",
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
}
