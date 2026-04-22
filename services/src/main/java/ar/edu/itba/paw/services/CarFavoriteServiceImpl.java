package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarFavoriteDao;
import ar.edu.itba.paw.persistence.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class CarFavoriteServiceImpl implements CarFavoriteService {

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
    public List<Long> findFavoriteCarIdsByUser(final long userId) {
        return carFavoriteDao.findFavoriteCars(userId).stream()
                .map(Car::getId)
                .toList();
    }

    @Override
    @Transactional
    public boolean setFavorite(final long userId, final long carId, final boolean favorite) {
        validateFavorite(userId, carId);
        return favorite
                ? carFavoriteDao.favorite(userId, carId)
                : carFavoriteDao.unfavorite(userId, carId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorited(final long userId, final long carId) {
        return carFavoriteDao.isFavorited(userId, carId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Car> getFavoriteCars(final long userId) {
        return carFavoriteDao.findFavoriteCars(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getFavoritedCarIds(final long userId, final Collection<Long> carIds) {
        return carFavoriteDao.findFavoritedCarIds(userId, carIds);
    }

    private void validateFavorite(final long userId, final long carId) {
        if (userDao.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        if (carDao.findById(carId).isEmpty()) {
            throw new IllegalArgumentException("Car not found.");
        }
    }
}
