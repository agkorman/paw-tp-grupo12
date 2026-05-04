package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RelationJdbcDaoTest extends AbstractPersistenceTest {

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
    public void shouldReturnFalseWhenUnfavoritingNonFavoritedCar() {
        // Arrange
        final User user = createUser("unfavorite-missing");
        final Car car = createCar("unfavorite-missing");

        // Exercise
        final boolean result = carFavoriteDao.unfavorite(user.getId(), car.getId());

        // Assertions
        assertFalse(result);
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM car_favorites WHERE user_id = ? AND car_id = ?",
                user.getId(), car.getId()
        ));
    }

    @Test
    public void shouldReturnFalseWhenUnfollowingNonFollowedUser() {
        // Arrange
        final User follower = createUser("unfollow-missing-follower");
        final User followed = createUser("unfollow-missing-followed");

        // Exercise
        final boolean result = userFollowDao.unfollow(follower.getId(), followed.getId());

        // Assertions
        assertFalse(result);
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM user_follows WHERE follower_id = ? AND followed_id = ?",
                follower.getId(), followed.getId()
        ));
    }

    @Test
    public void shouldRejectSelfFollow() {
        // Arrange
        final User user = createUser("self-follow");

        // Exercise
        assertThrows(DataIntegrityViolationException.class,
                () -> userFollowDao.follow(user.getId(), user.getId()));

        // Assertions
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM user_follows WHERE follower_id = ? AND followed_id = ?",
                user.getId(), user.getId()
        ));
    }
}
