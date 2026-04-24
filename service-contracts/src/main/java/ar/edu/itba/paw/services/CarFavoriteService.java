package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CarFavoriteService {
    List<Long> findFavoriteCarIdsByUser(long userId);

    boolean setFavorite(long userId, long carId, boolean favorite);

    boolean isFavorited(long userId, long carId);

    List<Car> getFavoriteCars(long userId);

    Set<Long> getFavoritedCarIds(long userId, Collection<Long> carIds);
}
