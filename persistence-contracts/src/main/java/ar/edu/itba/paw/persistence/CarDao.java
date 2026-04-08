package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;

import java.util.List;
import java.util.Optional;

public interface CarDao {
    List<Car> findAll();

    Optional<Car> findById(long id);

    List<Car> findByBrandId(long brandId);

    List<Car> findByBodyTypeId(long bodyTypeId);

    List<Car> findByBrandIdAndBodyTypeId(long brandId, long bodyTypeId);

    List<Car> search(String query, Long brandId, Long bodyTypeId);

    Car create(long brandId, String model, long bodyTypeId, String description);
}
