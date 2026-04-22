package ar.edu.itba.paw.services;

import java.util.List;

public interface CarFavoriteService {
    /**
     * Returns the IDs of cars marked as favorites by the given user.
     * Returns an empty list until implemented by the favorites PR.
     */
    List<Long> findFavoriteCarIdsByUser(long userId);
}
