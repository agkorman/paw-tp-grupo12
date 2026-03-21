package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;

import java.util.List;
import java.util.Optional;

public interface CarDao {
    List<Car> findAll();
    Optional<Car> findById(long id);
    List<Car> findByBrandId(long brandId);
    Car create(long brandId, String model, String generation, String bodyType, String description, String imageUrl);
}
