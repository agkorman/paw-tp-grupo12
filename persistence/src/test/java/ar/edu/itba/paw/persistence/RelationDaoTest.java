package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.beans.factory.annotation.Autowired;

public class RelationDaoTest extends AbstractPersistenceTest {

    @Autowired
    private CarFavoriteDao carFavoriteDao;

    @Autowired
    private UserFollowDao userFollowDao;

    @Test
    public void shouldFavoriteWhenUserAndCarExist() {
        // Arrange
        final User user = createUser("favorite");
        final Car car = createCar("favorite");

        // Exercise
        final boolean result = carFavoriteDao.favorite(user.getId(), car.getId());

        // Assertions
        assertTrue(result);
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM car_favorites WHERE user_id = ? AND car_id = ?",
                user.getId(), car.getId()
        ));
    }

    @Test
    public void shouldNotFavoriteAgainWhenAlreadyFavorited() {
        // Arrange
        final User user = createUser("favorite-again");
        final Car car = createCar("favorite-again");
        jdbcTemplate.update("INSERT INTO car_favorites (user_id, car_id) VALUES (?, ?)", user.getId(), car.getId());

        // Exercise
        final boolean result = carFavoriteDao.favorite(user.getId(), car.getId());

        // Assertions
        assertFalse(result);
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM car_favorites WHERE user_id = ? AND car_id = ?",
                user.getId(), car.getId()
        ));
    }

    @Test
    public void shouldUnfavoriteWhenUserAndCarExist() {
        // Arrange
        final User user = createUser("unfavorite");
        final Car car = createCar("unfavorite");
        jdbcTemplate.update("INSERT INTO car_favorites (user_id, car_id) VALUES (?, ?)", user.getId(), car.getId());

        // Exercise
        final boolean result = carFavoriteDao.unfavorite(user.getId(), car.getId());

        // Assertions
        assertTrue(result);
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM car_favorites WHERE user_id = ? AND car_id = ?",
                user.getId(), car.getId()
        ));
    }

    @Test
    public void shouldReturnFavoritedCarIdsForGivenSet() {
        // Arrange
        final User user = createUser("favorite-ids");
        final Car favorited = createCar("favorite-ids-yes");
        final Car notFavorited = createCar("favorite-ids-no");
        jdbcTemplate.update("INSERT INTO car_favorites (user_id, car_id) VALUES (?, ?)", user.getId(), favorited.getId());

        // Exercise
        final Set<Long> result = carFavoriteDao.findFavoritedCarIds(user.getId(), List.of(favorited.getId(), notFavorited.getId()));

        // Assertions
        assertEquals(Set.of(favorited.getId()), result);
    }

    @Test
    public void shouldFollowWhenBothUsersExist() {
        // Arrange
        final User follower = createUser("follower");
        final User followed = createUser("followed");

        // Exercise
        final boolean result = userFollowDao.follow(follower.getId(), followed.getId());

        // Assertions
        assertTrue(result);
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM user_follows WHERE follower_id = ? AND followed_id = ?",
                follower.getId(), followed.getId()
        ));
    }

    @Test
    public void shouldNotFollowAgainWhenAlreadyFollowing() {
        // Arrange
        final User follower = createUser("follow-again-follower");
        final User followed = createUser("follow-again-followed");
        jdbcTemplate.update(
                "INSERT INTO user_follows (follower_id, followed_id) VALUES (?, ?)",
                follower.getId(), followed.getId()
        );

        // Exercise
        final boolean result = userFollowDao.follow(follower.getId(), followed.getId());

        // Assertions
        assertFalse(result);
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM user_follows WHERE follower_id = ? AND followed_id = ?",
                follower.getId(), followed.getId()
        ));
    }

    @Test
    public void shouldUnfollowWhenBothUsersExist() {
        // Arrange
        final User follower = createUser("unfollow-follower");
        final User followed = createUser("unfollow-followed");
        jdbcTemplate.update(
                "INSERT INTO user_follows (follower_id, followed_id) VALUES (?, ?)",
                follower.getId(), followed.getId()
        );

        // Exercise
        final boolean result = userFollowDao.unfollow(follower.getId(), followed.getId());

        // Assertions
        assertTrue(result);
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM user_follows WHERE follower_id = ? AND followed_id = ?",
                follower.getId(), followed.getId()
        ));
    }

    @Test
    public void shouldPaginateFavoriteCarsAcrossMultiplePages() {
        // Arrange
        final User user = createUser("favorite-paged");
        // Insert 17 cars (Pagination.CARS_PAGE_SIZE + 1)
        for (int i = 0; i < 17; i++) {
            Car car = createCar("fav-paged-" + i);
            jdbcTemplate.update("INSERT INTO car_favorites (user_id, car_id) VALUES (?, ?)", user.getId(), car.getId());
        }

        // Exercise
        final Page<Car> result = carFavoriteDao.findFavoriteCars(user.getId(), 2);

        // Assertions
        assertEquals(17L, result.getTotalItems());
        assertEquals(2, result.getPageNumber());
        assertEquals(1, result.getItems().size());
    }

    @Test
    public void shouldGroupAllFavoriteCarIdsByUser() {
        // Arrange
        final User firstUser = createUser("fav-all-first");
        final User secondUser = createUser("fav-all-second");
        final Car carA = createCar("fav-all-a");
        final Car carB = createCar("fav-all-b");
        final Car carC = createCar("fav-all-c");
        jdbcTemplate.update("INSERT INTO car_favorites (user_id, car_id) VALUES (?, ?)", firstUser.getId(), carA.getId());
        jdbcTemplate.update("INSERT INTO car_favorites (user_id, car_id) VALUES (?, ?)", firstUser.getId(), carB.getId());
        jdbcTemplate.update("INSERT INTO car_favorites (user_id, car_id) VALUES (?, ?)", secondUser.getId(), carC.getId());

        // Exercise
        final Map<Long, List<Long>> result = carFavoriteDao.findAllFavoriteCarIdsByUser();

        // Assertions
        assertEquals(List.of(carA.getId(), carB.getId()), result.get(firstUser.getId()));
        assertEquals(List.of(carC.getId()), result.get(secondUser.getId()));
    }
}
