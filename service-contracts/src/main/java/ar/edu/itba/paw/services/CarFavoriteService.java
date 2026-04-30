package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CarFavoriteService {
    List<Long> findFavoriteCarIdsByUser(long userId);

    boolean setFavorite(long userId, long carId, boolean favorite);

    boolean isFavorited(long userId, long carId);

    List<Car> getFavoriteCars(long userId);

    default Page<Car> getFavoriteCars(final long userId, final int page) {
        final List<Car> cars = getFavoriteCars(userId);
        final int pageSize = Pagination.CARS_PAGE_SIZE;
        if (cars.isEmpty()) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }
        final int effectivePage = Pagination.clampPage(Pagination.normalizePage(page), cars.size(), pageSize);
        final int fromIndex = (int) Pagination.offsetFor(effectivePage, pageSize);
        final int toIndex = Math.min(fromIndex + pageSize, cars.size());
        return new Page<>(cars.subList(fromIndex, toIndex), effectivePage, pageSize, cars.size());
    }

    default long countFavoriteCars(final long userId) {
        return getFavoriteCars(userId).size();
    }

    Set<Long> getFavoritedCarIds(long userId, Collection<Long> carIds);
}
