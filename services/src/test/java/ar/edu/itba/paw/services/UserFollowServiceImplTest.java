package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.persistence.UserFollowDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserFollowServiceImplTest {

    private static final long FOLLOWER_ID = 1L;
    private static final long FOLLOWED_ID = 2L;

    @Mock
    private UserFollowDao userFollowDao;
    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserFollowServiceImpl userFollowService;

    private User userWithId(final long id) {
        return new User(id, "u" + id, id + "@x.com", "p", "user", LocalDateTime.now());
    }

    @Test
    public void shouldFollowWhenBothUsersExist() {
        // Arrange
        when(userDao.findById(FOLLOWER_ID)).thenReturn(Optional.of(userWithId(FOLLOWER_ID)));
        when(userDao.findById(FOLLOWED_ID)).thenReturn(Optional.of(userWithId(FOLLOWED_ID)));
        when(userFollowDao.follow(FOLLOWER_ID, FOLLOWED_ID)).thenReturn(true);

        // Exercise
        final boolean result = userFollowService.followUser(FOLLOWER_ID, FOLLOWED_ID);

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldRejectFollowingSelf() {
        // Arrange

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userFollowService.followUser(FOLLOWER_ID, FOLLOWER_ID));

        // Assertions
        assertEquals("Users cannot follow themselves.", ex.getMessage());
    }

    @Test
    public void shouldRejectFollowWhenFollowerDoesNotExist() {
        // Arrange
        when(userDao.findById(FOLLOWER_ID)).thenReturn(Optional.empty());

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userFollowService.followUser(FOLLOWER_ID, FOLLOWED_ID));

        // Assertions
        assertEquals("Follower user not found.", ex.getMessage());
    }

    @Test
    public void shouldRejectFollowWhenFollowedDoesNotExist() {
        // Arrange
        when(userDao.findById(FOLLOWER_ID)).thenReturn(Optional.of(userWithId(FOLLOWER_ID)));
        when(userDao.findById(FOLLOWED_ID)).thenReturn(Optional.empty());

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userFollowService.followUser(FOLLOWER_ID, FOLLOWED_ID));

        // Assertions
        assertEquals("Followed user not found.", ex.getMessage());
    }

    @Test
    public void shouldUnfollowWhenBothUsersExist() {
        // Arrange
        when(userDao.findById(FOLLOWER_ID)).thenReturn(Optional.of(userWithId(FOLLOWER_ID)));
        when(userDao.findById(FOLLOWED_ID)).thenReturn(Optional.of(userWithId(FOLLOWED_ID)));
        when(userFollowDao.unfollow(FOLLOWER_ID, FOLLOWED_ID)).thenReturn(true);

        // Exercise
        final boolean result = userFollowService.unfollowUser(FOLLOWER_ID, FOLLOWED_ID);

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldRejectUnfollowingSelf() {
        // Arrange

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userFollowService.unfollowUser(FOLLOWER_ID, FOLLOWER_ID));

        // Assertions
        assertEquals("Users cannot follow themselves.", ex.getMessage());
    }

    @Test
    public void shouldReturnFalseWhenIsFollowingSelf() {
        // Arrange

        // Exercise
        final boolean result = userFollowService.isFollowing(FOLLOWER_ID, FOLLOWER_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldDelegateIsFollowingToDaoForDifferentUsers() {
        // Arrange
        when(userFollowDao.isFollowing(FOLLOWER_ID, FOLLOWED_ID)).thenReturn(true);

        // Exercise
        final boolean result = userFollowService.isFollowing(FOLLOWER_ID, FOLLOWED_ID);

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldReturnFollowersFromDao() {
        // Arrange
        final List<User> followers = List.of(userWithId(10L), userWithId(11L));
        when(userFollowDao.findFollowers(FOLLOWED_ID)).thenReturn(followers);

        // Exercise
        final List<User> result = userFollowService.getFollowers(FOLLOWED_ID);

        // Assertions
        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals(11L, result.get(1).getId());
    }

    @Test
    public void shouldReturnFollowingFromDao() {
        // Arrange
        final List<User> following = List.of(userWithId(20L));
        when(userFollowDao.findFollowing(FOLLOWER_ID)).thenReturn(following);

        // Exercise
        final List<User> result = userFollowService.getFollowing(FOLLOWER_ID);

        // Assertions
        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getId());
    }

    @Test
    public void shouldReturnFollowerCountFromDao() {
        // Arrange
        when(userFollowDao.countFollowers(FOLLOWED_ID)).thenReturn(7L);

        // Exercise
        final long result = userFollowService.countFollowers(FOLLOWED_ID);

        // Assertions
        assertEquals(7L, result);
    }

    @Test
    public void shouldReturnFollowingCountFromDao() {
        // Arrange
        when(userFollowDao.countFollowing(FOLLOWER_ID)).thenReturn(3L);

        // Exercise
        final long result = userFollowService.countFollowing(FOLLOWER_ID);

        // Assertions
        assertEquals(3L, result);
    }
}
