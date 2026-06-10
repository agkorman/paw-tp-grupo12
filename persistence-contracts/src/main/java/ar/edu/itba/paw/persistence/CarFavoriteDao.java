package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Page;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CarFavoriteDao {
    boolean favorite(long userId, long carId);

    boolean unfavorite(long userId, long carId);

    boolean isFavorited(long userId, long carId);

    Page<Car> findFavoriteCars(long userId, int page);

    long countFavoriteCars(long userId);

    Set<Long> findFavoritedCarIds(long userId, Collection<Long> carIds);

    Map<Long, List<Long>> findAllFavoriteCarIdsByUser();
}
