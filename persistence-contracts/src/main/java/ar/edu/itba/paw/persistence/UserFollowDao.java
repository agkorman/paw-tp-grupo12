package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.User;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserFollowDao {
    boolean follow(long followerId, long followedId);
    boolean unfollow(long followerId, long followedId);
    boolean isFollowing(long followerId, long followedId);
    long countFollowers(long userId);
    long countFollowing(long userId);
    List<User> findFollowers(long userId);
    List<User> findFollowing(long userId);
    Set<Long> getFollowedIds(long followerId, Collection<Long> targetIds);
}
