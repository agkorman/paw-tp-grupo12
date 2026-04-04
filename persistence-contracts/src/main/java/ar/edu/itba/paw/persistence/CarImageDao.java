package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.CarImage;

import java.util.Optional;

public interface CarImageDao {

    Optional<CarImage> findByCarId(long carId);

    void saveOrReplace(long carId, String contentType, byte[] imageData);
}
