package ar.edu.itba.paw.services;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CarFavoriteServiceImpl implements CarFavoriteService {

    @Override
    public List<Long> findFavoriteCarIdsByUser(final long userId) {
        return Collections.emptyList();
    }
}
