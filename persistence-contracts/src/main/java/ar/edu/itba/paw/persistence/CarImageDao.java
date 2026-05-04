package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;

import java.util.List;
import java.util.Optional;

public interface CarImageDao {

    Optional<CarImage> findByCarId(long carId);

    List<CarImage> findAllByCarId(long carId);

    List<CarImage> findAllByCarIdWithData(long carId);

    Optional<CarImage> findByCarIdAndImageId(long carId, long imageId);

    void saveOrReplace(long carId, String contentType, byte[] imageData);

    void replaceAll(long carId, List<CarImagePayload> images);
}
