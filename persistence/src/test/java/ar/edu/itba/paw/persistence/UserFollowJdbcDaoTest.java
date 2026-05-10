package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserFollowJdbcDaoTest extends AbstractPersistenceTest {

    @Test
    public void shouldPaginateFollowersWithEightPerPage() {
        // Arrange
        final User profile = createUser("paged-followers-target");
        for (int i = 0; i < 9; i++) {
            final User follower = createUser("paged-follower-" + i);
            jdbcTemplate.update(
                    "INSERT INTO user_follows (follower_id, followed_id) VALUES (?, ?)",
                    follower.getId(), profile.getId()
            );
        }

        // Exercise
        final Page<User> page2 = userFollowDao.findFollowers(profile.getId(), 2);

        // Assertions
        assertEquals(9L, page2.getTotalItems());
        assertEquals(2, page2.getPageNumber());
        assertEquals(8, page2.getPageSize());
        assertEquals(1, page2.getItems().size());
    }

    @Test
    public void shouldReturnFirstPageOfFollowersOrderedByMostRecentFirst() {
        // Arrange
        final User profile = createUser("ordered-followers-target");
        final User olderFollower = createUser("ordered-follower-older");
        final User newerFollower = createUser("ordered-follower-newer");
        jdbcTemplate.update(
                "INSERT INTO user_follows (follower_id, followed_id, created_at) VALUES (?, ?, TIMESTAMP '2026-01-01 00:00:00')",
                olderFollower.getId(), profile.getId()
        );
        jdbcTemplate.update(
                "INSERT INTO user_follows (follower_id, followed_id, created_at) VALUES (?, ?, TIMESTAMP '2026-02-01 00:00:00')",
                newerFollower.getId(), profile.getId()
        );

        // Exercise
        final Page<User> page1 = userFollowDao.findFollowers(profile.getId(), 1);

        // Assertions
        assertEquals(2L, page1.getTotalItems());
        assertEquals(2, page1.getItems().size());
        assertEquals(newerFollower.getId(), page1.getItems().get(0).getId());
        assertEquals(olderFollower.getId(), page1.getItems().get(1).getId());
    }

    @Test
    public void shouldReturnEmptyPageWhenUserHasNoFollowers() {
        // Arrange
        final User profile = createUser("no-followers");

        // Exercise
        final Page<User> page1 = userFollowDao.findFollowers(profile.getId(), 1);

        // Assertions
        assertEquals(0L, page1.getTotalItems());
        assertTrue(page1.getItems().isEmpty());
    }

    @Test
    public void shouldPaginateFollowingWithEightPerPage() {
        // Arrange
        final User follower = createUser("paged-following-source");
        for (int i = 0; i < 9; i++) {
            final User followed = createUser("paged-followed-" + i);
            jdbcTemplate.update(
                    "INSERT INTO user_follows (follower_id, followed_id) VALUES (?, ?)",
                    follower.getId(), followed.getId()
            );
        }

        // Exercise
        final Page<User> page2 = userFollowDao.findFollowing(follower.getId(), 2);

        // Assertions
        assertEquals(9L, page2.getTotalItems());
        assertEquals(2, page2.getPageNumber());
        assertEquals(8, page2.getPageSize());
        assertEquals(1, page2.getItems().size());
    }

    @Test
    public void shouldClampOutOfRangePageToLastPage() {
        // Arrange
        final User profile = createUser("clamp-followers");
        for (int i = 0; i < 3; i++) {
            final User follower = createUser("clamp-follower-" + i);
            jdbcTemplate.update(
                    "INSERT INTO user_follows (follower_id, followed_id) VALUES (?, ?)",
                    follower.getId(), profile.getId()
            );
        }

        // Exercise
        final Page<User> result = userFollowDao.findFollowers(profile.getId(), 99);

        // Assertions
        assertEquals(3L, result.getTotalItems());
        assertEquals(1, result.getPageNumber());
        assertEquals(3, result.getItems().size());
    }
}
