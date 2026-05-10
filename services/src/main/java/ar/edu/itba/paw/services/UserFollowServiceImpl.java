package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.persistence.UserFollowDao;
import ar.edu.itba.paw.services.exception.SelfFollowException;
import ar.edu.itba.paw.services.exception.UserNotFoundException;
import java.util.Collection;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserFollowServiceImpl implements UserFollowService {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        UserFollowServiceImpl.class
    );

    private final UserFollowDao userFollowDao;
    private final UserDao userDao;

    @Autowired
    public UserFollowServiceImpl(
        final UserFollowDao userFollowDao,
        final UserDao userDao
    ) {
        this.userFollowDao = userFollowDao;
        this.userDao = userDao;
    }

    @Override
    @Transactional
    public boolean toggleFollow(final long followerId, final long followedId) {
        validateFollow(followerId, followedId);
        final boolean wasFollowing = userFollowDao.isFollowing(followerId, followedId);
        if (wasFollowing) {
            userFollowDao.unfollow(followerId, followedId);
        } else {
            userFollowDao.follow(followerId, followedId);
        }
        final boolean following = !wasFollowing;
        LOGGER.info("user id={} toggled follow targetUserId={} following={}", followerId, followedId, following);
        return following;
    }

    @Override
    @Transactional
    public boolean followUser(final long followerId, final long followedId) {
        validateFollow(followerId, followedId);
        final boolean result = userFollowDao.follow(followerId, followedId);
        LOGGER.info("user id={} followed user id={}", followerId, followedId);
        return result;
    }

    @Override
    @Transactional
    public boolean unfollowUser(final long followerId, final long followedId) {
        validateFollow(followerId, followedId);
        final boolean result = userFollowDao.unfollow(followerId, followedId);
        LOGGER.info("user id={} unfollowed user id={}", followerId, followedId);
        return result;
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
    public Set<Long> getFollowedIds(final long followerId, final Collection<Long> targetIds) {
        if (targetIds.isEmpty()) {
            return Set.of();
        }
        return userFollowDao.getFollowedIds(followerId, targetIds);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFollowing(final long userId) {
        return userFollowDao.countFollowing(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getFollowers(final long userId, final int page) {
        return userFollowDao.findFollowers(userId, page);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getFollowing(final long userId, final int page) {
        return userFollowDao.findFollowing(userId, page);
    }

    private void validateFollow(final long followerId, final long followedId) {
        if (followerId == followedId) {
            LOGGER.warn(
                "follow rejected: self-follow attempt userId={}",
                followerId
            );
            throw new SelfFollowException(followerId);
        }
        if (userDao.findById(followerId).isEmpty()) {
            LOGGER.warn(
                "follow rejected: follower not found id={}",
                followerId
            );
            throw new UserNotFoundException(followerId);
        }
        if (userDao.findById(followedId).isEmpty()) {
            LOGGER.warn(
                "follow rejected: followed not found id={}",
                followedId
            );
            throw new UserNotFoundException(followedId);
        }
    }
}
