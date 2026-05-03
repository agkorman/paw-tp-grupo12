package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertTrue(carFavoriteDao.isFavorited(user.getId(), car.getId()));
        assertEquals(1, carFavoriteDao.countFavoriteCars(user.getId()));
        assertEquals(car.getId(), carFavoriteDao.findFavoriteCars(user.getId()).get(0).getId());
    }

    @Test
    public void shouldNotFavoriteAgainWhenAlreadyFavorited() {
        // Arrange
        final User user = createUser("favorite-again");
        final Car car = createCar("favorite-again");
        carFavoriteDao.favorite(user.getId(), car.getId());

        // Exercise
        final boolean result = carFavoriteDao.favorite(user.getId(), car.getId());

        // Assertions
        assertFalse(result);
        assertEquals(1, carFavoriteDao.countFavoriteCars(user.getId()));
    }

    @Test
    public void shouldUnfavoriteWhenUserAndCarExist() {
        // Arrange
        final User user = createUser("unfavorite");
        final Car car = createCar("unfavorite");
        carFavoriteDao.favorite(user.getId(), car.getId());

        // Exercise
        final boolean result = carFavoriteDao.unfavorite(user.getId(), car.getId());

        // Assertions
        assertTrue(result);
        assertFalse(carFavoriteDao.isFavorited(user.getId(), car.getId()));
        assertEquals(0, carFavoriteDao.countFavoriteCars(user.getId()));
    }

    @Test
    public void shouldReturnFavoritedCarIdsForGivenSet() {
        // Arrange
        final User user = createUser("favorite-ids");
        final Car favorited = createCar("favorite-ids-yes");
        final Car notFavorited = createCar("favorite-ids-no");
        carFavoriteDao.favorite(user.getId(), favorited.getId());

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
        assertTrue(userFollowDao.isFollowing(follower.getId(), followed.getId()));
        assertEquals(1, userFollowDao.countFollowing(follower.getId()));
        assertEquals(1, userFollowDao.countFollowers(followed.getId()));
        assertEquals(follower.getId(), userFollowDao.findFollowers(followed.getId()).get(0).getId());
        assertEquals(followed.getId(), userFollowDao.findFollowing(follower.getId()).get(0).getId());
    }

    @Test
    public void shouldNotFollowAgainWhenAlreadyFollowing() {
        // Arrange
        final User follower = createUser("follow-again-follower");
        final User followed = createUser("follow-again-followed");
        userFollowDao.follow(follower.getId(), followed.getId());

        // Exercise
        final boolean result = userFollowDao.follow(follower.getId(), followed.getId());

        // Assertions
        assertFalse(result);
        assertEquals(1, userFollowDao.countFollowing(follower.getId()));
    }

    @Test
    public void shouldUnfollowWhenBothUsersExist() {
        // Arrange
        final User follower = createUser("unfollow-follower");
        final User followed = createUser("unfollow-followed");
        userFollowDao.follow(follower.getId(), followed.getId());

        // Exercise
        final boolean result = userFollowDao.unfollow(follower.getId(), followed.getId());

        // Assertions
        assertTrue(result);
        assertFalse(userFollowDao.isFollowing(follower.getId(), followed.getId()));
        assertEquals(0, userFollowDao.countFollowing(follower.getId()));
        assertEquals(0, userFollowDao.countFollowers(followed.getId()));
    }
}
