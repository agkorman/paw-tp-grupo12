package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.ProfileActivityItem;
import ar.edu.itba.paw.persistence.UserActivityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserActivityServiceImpl implements UserActivityService {

    private final UserActivityDao userActivityDao;

    @Autowired
    public UserActivityServiceImpl(final UserActivityDao userActivityDao) {
        this.userActivityDao = userActivityDao;
    }

    @Override
    public Page<ProfileActivityItem> getAuthoredActivity(final long userId, final int page) {
        return userActivityDao.findAuthoredActivity(userId, page);
    }

    @Override
    public long countAuthoredActivity(final long userId) {
        return userActivityDao.countAuthoredActivity(userId);
    }

    @Override
    public Page<ProfileActivityItem> getLikedActivity(final long userId, final int page) {
        return userActivityDao.findLikedActivity(userId, page);
    }

    @Override
    public long countLikedActivity(final long userId) {
        return userActivityDao.countLikedActivity(userId);
    }
}
