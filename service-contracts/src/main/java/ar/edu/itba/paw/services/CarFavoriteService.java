package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Page;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CarFavoriteService {
    Map<Long, List<Long>> findAllFavoriteCarIdsByUser();

    void setFavorite(long userId, long carId, boolean favorite);

    boolean isFavorited(long userId, long carId);

    Page<Car> getFavoriteCars(long userId, int page);

    long countFavoriteCars(long userId);

    Set<Long> getFavoritedCarIds(long userId, Collection<Long> carIds);
}
