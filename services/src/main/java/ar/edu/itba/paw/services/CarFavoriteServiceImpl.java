package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarFavoriteDao;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.services.exception.CarNotFoundException;
import ar.edu.itba.paw.services.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CarFavoriteServiceImpl implements CarFavoriteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarFavoriteServiceImpl.class);

    private final CarFavoriteDao carFavoriteDao;
    private final UserDao userDao;
    private final CarDao carDao;

    @Autowired
    public CarFavoriteServiceImpl(final CarFavoriteDao carFavoriteDao, final UserDao userDao, final CarDao carDao) {
        this.carFavoriteDao = carFavoriteDao;
        this.userDao = userDao;
        this.carDao = carDao;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<Long>> findAllFavoriteCarIdsByUser() {
        return carFavoriteDao.findAllFavoriteCarIdsByUser();
    }

    @Override
    @Transactional
    public void setFavorite(final long userId, final long carId, final boolean favorite) {
        validateFavorite(userId, carId);
        if (favorite) {
            carFavoriteDao.favorite(userId, carId);
        } else {
            carFavoriteDao.unfavorite(userId, carId);
        }
        LOGGER.info("user id={} set favorite carId={} favorited={}", userId, carId, favorite);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorited(final long userId, final long carId) {
        return carFavoriteDao.isFavorited(userId, carId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Car> getFavoriteCars(final long userId, final int page) {
        return carFavoriteDao.findFavoriteCars(userId, page);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFavoriteCars(final long userId) {
        return carFavoriteDao.countFavoriteCars(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getFavoritedCarIds(final long userId, final Collection<Long> carIds) {
        return carFavoriteDao.findFavoritedCarIds(userId, carIds);
    }

    private void validateFavorite(final long userId, final long carId) {
        if (userDao.findById(userId).isEmpty()) {
            LOGGER.warn("favorite rejected: user not found id={}", userId);
            throw new UserNotFoundException(userId);
        }
        if (carDao.findById(carId).isEmpty()) {
            LOGGER.warn("favorite rejected: car not found id={}", carId);
            throw new CarNotFoundException(carId);
        }
    }
}
