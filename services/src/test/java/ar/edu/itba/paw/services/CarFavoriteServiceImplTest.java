package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarFavoriteDao;
import ar.edu.itba.paw.persistence.UserDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CarFavoriteServiceImplTest {

    private static final long USER_ID = 1L;
    private static final long CAR_ID = 10L;

    @Mock
    private CarFavoriteDao carFavoriteDao;
    @Mock
    private UserDao userDao;
    @Mock
    private CarDao carDao;

    @InjectMocks
    private CarFavoriteServiceImpl carFavoriteService;

    private static User user() {
        return TestModels.user(USER_ID, "joaco", "joaco@example.com", "p", "user", LocalDateTime.now());
    }

    private static Car car(final long id) {
        return TestModels.car(id, 1L, "Toyota", "Corolla", 2L, "sedan", "desc", LocalDateTime.now());
    }

    @Test
    public void shouldRejectSetFavoriteWhenUserDoesNotExist() {
        // Arrange
        when(userDao.findById(USER_ID)).thenReturn(Optional.empty());

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carFavoriteService.setFavorite(USER_ID, CAR_ID, true));

        // Assertions
        assertEquals("User not found: 1", ex.getMessage());
    }

    @Test
    public void shouldRejectSetFavoriteWhenCarDoesNotExist() {
        // Arrange
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(carDao.findById(CAR_ID)).thenReturn(Optional.empty());

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carFavoriteService.setFavorite(USER_ID, CAR_ID, true));

        // Assertions
        assertEquals("Car not found: 10", ex.getMessage());
    }

    @Test
    public void shouldReturnFavoriteCarIdsMappedFromCars() {
        // Arrange
        when(carFavoriteDao.findFavoriteCars(USER_ID)).thenReturn(List.of(car(7L), car(8L)));

        // Exercise
        final List<Long> result = carFavoriteService.findFavoriteCarIdsByUser(USER_ID);

        // Assertions
        assertEquals(List.of(7L, 8L), result);
    }

    @Test
    public void shouldReturnEmptyFavoriteCarIdsWhenDaoReturnsEmpty() {
        // Arrange
        when(carFavoriteDao.findFavoriteCars(USER_ID)).thenReturn(List.of());

        // Exercise
        final List<Long> result = carFavoriteService.findFavoriteCarIdsByUser(USER_ID);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldDelegateIsFavoritedToDao() {
        // Arrange
        when(carFavoriteDao.isFavorited(USER_ID, CAR_ID)).thenReturn(true);

        // Exercise
        final boolean result = carFavoriteService.isFavorited(USER_ID, CAR_ID);

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenIsFavoritedDelegatesFalse() {
        // Arrange
        when(carFavoriteDao.isFavorited(USER_ID, CAR_ID)).thenReturn(false);

        // Exercise
        final boolean result = carFavoriteService.isFavorited(USER_ID, CAR_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldReturnFavoriteCarsFromDao() {
        // Arrange
        final List<Car> cars = List.of(car(7L), car(8L));
        when(carFavoriteDao.findFavoriteCars(USER_ID)).thenReturn(cars);

        // Exercise
        final List<Car> result = carFavoriteService.getFavoriteCars(USER_ID);

        // Assertions
        assertEquals(2, result.size());
        assertEquals(7L, result.get(0).getId());
    }

    @Test
    public void shouldReturnPagedFavoriteCarsFromDao() {
        // Arrange
        final Page<Car> page = new Page<>(List.of(car(7L)), 1, 10, 1L);
        when(carFavoriteDao.findFavoriteCars(USER_ID, 1)).thenReturn(page);

        // Exercise
        final Page<Car> result = carFavoriteService.getFavoriteCars(USER_ID, 1);

        // Assertions
        assertEquals(1, result.getItems().size());
        assertEquals(1L, result.getTotalItems());
    }

    @Test
    public void shouldReturnFavoriteCarCountFromDao() {
        // Arrange
        when(carFavoriteDao.countFavoriteCars(USER_ID)).thenReturn(5L);

        // Exercise
        final long result = carFavoriteService.countFavoriteCars(USER_ID);

        // Assertions
        assertEquals(5L, result);
    }

    @Test
    public void shouldReturnFavoritedCarIdsForGivenSet() {
        // Arrange
        final Set<Long> ids = Set.of(7L, 8L);
        when(carFavoriteDao.findFavoritedCarIds(USER_ID, ids)).thenReturn(Set.of(7L));

        // Exercise
        final Set<Long> result = carFavoriteService.getFavoritedCarIds(USER_ID, ids);

        // Assertions
        assertEquals(Set.of(7L), result);
    }
}
