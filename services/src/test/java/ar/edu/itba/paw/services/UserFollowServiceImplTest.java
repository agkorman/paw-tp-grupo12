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
        final long sameUserId = FOLLOWER_ID;

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userFollowService.followUser(sameUserId, sameUserId));

        // Assertions
        assertEquals("User cannot follow themselves: 1", ex.getMessage());
    }

    @Test
    public void shouldRejectFollowWhenFollowerDoesNotExist() {
        // Arrange
        when(userDao.findById(FOLLOWER_ID)).thenReturn(Optional.empty());

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userFollowService.followUser(FOLLOWER_ID, FOLLOWED_ID));

        // Assertions
        assertEquals("User not found: 1", ex.getMessage());
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
        assertEquals("User not found: 2", ex.getMessage());
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
        final long sameUserId = FOLLOWER_ID;

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userFollowService.unfollowUser(sameUserId, sameUserId));

        // Assertions
        assertEquals("User cannot follow themselves: 1", ex.getMessage());
    }

    @Test
    public void shouldReturnFalseWhenIsFollowingSelf() {
        // Arrange
        final long sameUserId = FOLLOWER_ID;

        // Exercise
        final boolean result = userFollowService.isFollowing(sameUserId, sameUserId);

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
    public void shouldToggleFollowFromFalseToTrue() {
        // Arrange
        when(userDao.findById(FOLLOWER_ID)).thenReturn(Optional.of(userWithId(FOLLOWER_ID)));
        when(userDao.findById(FOLLOWED_ID)).thenReturn(Optional.of(userWithId(FOLLOWED_ID)));
        when(userFollowDao.isFollowing(FOLLOWER_ID, FOLLOWED_ID)).thenReturn(false);
        when(userFollowDao.follow(FOLLOWER_ID, FOLLOWED_ID)).thenReturn(true);

        // Exercise
        final boolean result = userFollowService.toggleFollow(FOLLOWER_ID, FOLLOWED_ID);

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldToggleFollowFromTrueToFalse() {
        // Arrange
        when(userDao.findById(FOLLOWER_ID)).thenReturn(Optional.of(userWithId(FOLLOWER_ID)));
        when(userDao.findById(FOLLOWED_ID)).thenReturn(Optional.of(userWithId(FOLLOWED_ID)));
        when(userFollowDao.isFollowing(FOLLOWER_ID, FOLLOWED_ID)).thenReturn(true);
        when(userFollowDao.unfollow(FOLLOWER_ID, FOLLOWED_ID)).thenReturn(true);

        // Exercise
        final boolean result = userFollowService.toggleFollow(FOLLOWER_ID, FOLLOWED_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldRejectToggleFollowSelf() {
        // Arrange
        final long sameUserId = FOLLOWER_ID;

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userFollowService.toggleFollow(sameUserId, sameUserId));

        // Assertions
        assertEquals("User cannot follow themselves: 1", ex.getMessage());
    }

    @Test
    public void shouldReturnEmptySetWhenBatchLookupEmpty() {
        // Arrange
        final java.util.Collection<Long> targetIds = java.util.List.of();

        // Exercise
        final java.util.Set<Long> result = userFollowService.getFollowedIds(FOLLOWER_ID, targetIds);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnFollowedIdsFromDaoBatch() {
        // Arrange
        final java.util.Collection<Long> targetIds = java.util.List.of(2L, 3L, 4L);
        when(userFollowDao.getFollowedIds(FOLLOWER_ID, targetIds))
                .thenReturn(java.util.Set.of(2L, 4L));

        // Exercise
        final java.util.Set<Long> result = userFollowService.getFollowedIds(FOLLOWER_ID, targetIds);

        // Assertions
        assertEquals(2, result.size());
        assertTrue(result.contains(2L));
        assertTrue(result.contains(4L));
        assertFalse(result.contains(3L));
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
