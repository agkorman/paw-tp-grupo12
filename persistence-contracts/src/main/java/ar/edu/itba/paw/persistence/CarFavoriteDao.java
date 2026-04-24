package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CarFavoriteDao {
    boolean favorite(long userId, long carId);

    boolean unfavorite(long userId, long carId);

    boolean isFavorited(long userId, long carId);

    List<Car> findFavoriteCars(long userId);

    Set<Long> findFavoritedCarIds(long userId, Collection<Long> carIds);
}
