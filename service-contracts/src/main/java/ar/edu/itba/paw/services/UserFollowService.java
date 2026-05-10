package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserFollowService {
    boolean followUser(long followerId, long followedId);
    boolean unfollowUser(long followerId, long followedId);
    boolean toggleFollow(long followerId, long followedId);
    boolean isFollowing(long followerId, long followedId);
    Set<Long> getFollowedIds(long followerId, Collection<Long> targetIds);
    long countFollowers(long userId);
    long countFollowing(long userId);
    List<User> getFollowers(long userId);
    List<User> getFollowing(long userId);
    Page<User> getFollowers(long userId, int page);
    Page<User> getFollowing(long userId, int page);
}
