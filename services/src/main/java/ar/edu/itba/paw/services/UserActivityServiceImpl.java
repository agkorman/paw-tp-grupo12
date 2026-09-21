package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.ProfileActivityItem;
import ar.edu.itba.paw.persistence.UserActivityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserActivityServiceImpl implements UserActivityService {

    private final UserActivityDao userActivityDao;

    @Autowired
    public UserActivityServiceImpl(final UserActivityDao userActivityDao) {
        this.userActivityDao = userActivityDao;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfileActivityItem> getAuthoredActivity(final long userId, final int page) {
        return userActivityDao.findAuthoredActivity(userId, page);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAuthoredActivity(final long userId) {
        return userActivityDao.countAuthoredActivity(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfileActivityItem> getLikedActivity(final long userId, final int page) {
        return userActivityDao.findLikedActivity(userId, page);
    }

    @Override
    @Transactional(readOnly = true)
    public long countLikedActivity(final long userId) {
        return userActivityDao.countLikedActivity(userId);
    }
}
