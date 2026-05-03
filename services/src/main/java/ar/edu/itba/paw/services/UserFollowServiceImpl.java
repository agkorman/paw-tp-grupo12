package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.persistence.UserFollowDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserFollowServiceImpl implements UserFollowService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserFollowServiceImpl.class);

    private final UserFollowDao userFollowDao;
    private final UserDao userDao;

    @Autowired
    public UserFollowServiceImpl(final UserFollowDao userFollowDao, final UserDao userDao) {
        this.userFollowDao = userFollowDao;
        this.userDao = userDao;
    }

    @Override
    @Transactional
    public boolean followUser(final long followerId, final long followedId) {
        validateFollow(followerId, followedId);
        return userFollowDao.follow(followerId, followedId);
    }

    @Override
    @Transactional
    public boolean unfollowUser(final long followerId, final long followedId) {
        validateFollow(followerId, followedId);
        return userFollowDao.unfollow(followerId, followedId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(final long followerId, final long followedId) {
        if (followerId == followedId) {
            return false;
        }
        return userFollowDao.isFollowing(followerId, followedId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFollowers(final long userId) {
        return userFollowDao.countFollowers(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFollowing(final long userId) {
        return userFollowDao.countFollowing(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getFollowers(final long userId) {
        return userFollowDao.findFollowers(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getFollowing(final long userId) {
        return userFollowDao.findFollowing(userId);
    }

    private void validateFollow(final long followerId, final long followedId) {
        if (followerId == followedId) {
            LOGGER.warn("follow rejected: self-follow attempt userId={}", followerId);
            throw new IllegalArgumentException("Users cannot follow themselves.");
        }
        if (userDao.findById(followerId).isEmpty()) {
            LOGGER.warn("follow rejected: follower not found id={}", followerId);
            throw new IllegalArgumentException("Follower user not found.");
        }
        if (userDao.findById(followedId).isEmpty()) {
            LOGGER.warn("follow rejected: followed not found id={}", followedId);
            throw new IllegalArgumentException("Followed user not found.");
        }
    }
}
