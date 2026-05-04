package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.User;
import java.util.List;

public interface UserFollowService {
    boolean followUser(long followerId, long followedId);
    boolean unfollowUser(long followerId, long followedId);
    boolean isFollowing(long followerId, long followedId);
    long countFollowers(long userId);
    long countFollowing(long userId);
    List<User> getFollowers(long userId);
    List<User> getFollowing(long userId);
}
