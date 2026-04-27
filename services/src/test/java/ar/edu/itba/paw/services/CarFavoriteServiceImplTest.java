package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarFavoriteDao;
import ar.edu.itba.paw.persistence.UserDao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarFavoriteServiceImplTest {

    @Test
    void setFavoriteAddsFavoriteWhenUserAndCarExist() {
        final FakeCarFavoriteDao favoriteDao = new FakeCarFavoriteDao();
        final CarFavoriteService service = service(favoriteDao, true, true);

        final boolean changed = service.setFavorite(7L, 10L, true);

        assertTrue(changed);
        assertTrue(favoriteDao.isFavorited(7L, 10L));
    }

    @Test
    void setFavoriteIsIdempotentWhenAlreadyFavorited() {
        final FakeCarFavoriteDao favoriteDao = new FakeCarFavoriteDao();
        favoriteDao.favorite(7L, 10L);
        final CarFavoriteService service = service(favoriteDao, true, true);

        final boolean changed = service.setFavorite(7L, 10L, true);

        assertFalse(changed);
        assertTrue(favoriteDao.isFavorited(7L, 10L));
    }

    @Test
    void setFavoriteRemovesFavorite() {
        final FakeCarFavoriteDao favoriteDao = new FakeCarFavoriteDao();
        favoriteDao.favorite(7L, 10L);
        final CarFavoriteService service = service(favoriteDao, true, true);

        final boolean changed = service.setFavorite(7L, 10L, false);

        assertTrue(changed);
        assertFalse(favoriteDao.isFavorited(7L, 10L));
    }

    @Test
    void setFavoriteRejectsMissingUser() {
        final CarFavoriteService service = service(new FakeCarFavoriteDao(), false, true);

        assertThrows(IllegalArgumentException.class, () -> service.setFavorite(7L, 10L, true));
    }

    @Test
    void setFavoriteRejectsMissingCar() {
        final CarFavoriteService service = service(new FakeCarFavoriteDao(), true, false);

        assertThrows(IllegalArgumentException.class, () -> service.setFavorite(7L, 10L, true));
    }

    @Test
    void getFavoritedCarIdsReturnsOnlyRequestedFavorites() {
        final FakeCarFavoriteDao favoriteDao = new FakeCarFavoriteDao();
        favoriteDao.favorite(7L, 10L);
        favoriteDao.favorite(7L, 11L);
        favoriteDao.favorite(8L, 12L);
        final CarFavoriteService service = service(favoriteDao, true, true);

        final Set<Long> ids = service.getFavoritedCarIds(7L, List.of(10L, 12L));

        assertEquals(Set.of(10L), ids);
    }

    private CarFavoriteService service(final FakeCarFavoriteDao favoriteDao, final boolean userExists,
                                       final boolean carExists) {
        return new CarFavoriteServiceImpl(favoriteDao, new FakeUserDao(userExists), new FakeCarDao(carExists));
    }

    private static final class FakeCarFavoriteDao implements CarFavoriteDao {
        private final Set<String> favorites = new LinkedHashSet<>();

        @Override
        public boolean favorite(final long userId, final long carId) {
            return favorites.add(key(userId, carId));
        }

        @Override
        public boolean unfavorite(final long userId, final long carId) {
            return favorites.remove(key(userId, carId));
        }

        @Override
        public boolean isFavorited(final long userId, final long carId) {
            return favorites.contains(key(userId, carId));
        }

        @Override
        public List<Car> findFavoriteCars(final long userId) {
            final List<Car> cars = new ArrayList<>();
            for (final String favorite : favorites) {
                final String[] parts = favorite.split(":");
                if (Long.parseLong(parts[0]) == userId) {
                    final long carId = Long.parseLong(parts[1]);
                    cars.add(car(carId));
                }
            }
            return cars;
        }

        @Override
        public Set<Long> findFavoritedCarIds(final long userId, final Collection<Long> carIds) {
            final Set<Long> result = new LinkedHashSet<>();
            for (final Long carId : carIds) {
                if (carId != null && isFavorited(userId, carId)) {
                    result.add(carId);
                }
            }
            return result;
        }

        private String key(final long userId, final long carId) {
            return userId + ":" + carId;
        }
    }

    private static final class FakeUserDao implements UserDao {
        private final boolean userExists;

        private FakeUserDao(final boolean userExists) {
            this.userExists = userExists;
        }

        @Override
        public Optional<User> findById(final long id) {
            return userExists
                    ? Optional.of(new User(id, "driver", "driver@example.com", "password", "user", LocalDateTime.now()))
                    : Optional.empty();
        }

        @Override
        public Optional<User> findByEmail(final String email) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByUsername(final String username) {
            return Optional.empty();
        }

        @Override
        public User create(final String username, final String email, final String password, final String role) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> findEmailsByRoles(final Collection<String> roles) {
            return Collections.emptyList();
        }

        @Override
        public List<User> findAll() {
            return Collections.emptyList();
        }
    }

    private static final class FakeCarDao implements CarDao {
        private final boolean carExists;

        private FakeCarDao(final boolean carExists) {
            this.carExists = carExists;
        }

        @Override
        public List<Car> findAll() {
            return Collections.emptyList();
        }

        @Override
        public Optional<Car> findById(final long id) {
            return carExists ? Optional.of(car(id)) : Optional.empty();
        }

        @Override
        public List<Car> findByIds(final Collection<Long> ids) {
            return Collections.emptyList();
        }

        @Override
        public List<Car> findByBrandIdAndBodyTypeId(final long brandId, final long bodyTypeId) {
            return Collections.emptyList();
        }

        @Override
        public ar.edu.itba.paw.model.Page<Car> findByCriteria(final CarSearchCriteria criteria) {
            return ar.edu.itba.paw.model.Page.empty(1, 0);
        }

        @Override
        public Car create(final long brandId, final String model, final long bodyTypeId, final Integer year,
                          final String description,
                          final String fuelType, final Integer horsepower, final Integer airbagCount,
                          final String transmission, final BigDecimal fuelConsumption, final Integer maxSpeedKmh,
                          final BigDecimal priceUsd) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Car> update(final long id, final long brandId, final String model, final long bodyTypeId,
                                    final Integer year, final String description, final String fuelType, final Integer horsepower,
                                    final Integer airbagCount, final String transmission,
                                    final BigDecimal fuelConsumption, final Integer maxSpeedKmh,
                                    final BigDecimal priceUsd) {
            return Optional.empty();
        }

        @Override
        public boolean delete(final long id) {
            return false;
        }
    }

    private static Car car(final long carId) {
        return new Car(carId, 1L, "Toyota", "Supra", 1L, "Coupe", "Desc", LocalDateTime.now());
    }
}
